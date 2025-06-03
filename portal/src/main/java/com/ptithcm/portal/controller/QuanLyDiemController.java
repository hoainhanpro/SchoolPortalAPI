package com.ptithcm.portal.controller;

import com.ptithcm.portal.dto.LopTinChiDto;
import com.ptithcm.portal.dto.SinhVienDiemDto;
import com.ptithcm.portal.entity.NhanVien;
import com.ptithcm.portal.repository.NhanVienRepository;
import com.ptithcm.portal.service.QuanLyDiemService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/lecturers/grade-management")
public class QuanLyDiemController {

    @Autowired
    private QuanLyDiemService quanLyDiemService;
    
    @Autowired
    private NhanVienRepository nhanVienRepository;
    
    /**
     * API lấy danh sách lớp tín chỉ mà giảng viên đang giảng dạy
     * @param giangVienId ID của giảng viên, nếu null thì lấy từ người dùng đăng nhập
     * @param hocKyId ID của học kỳ, nếu null thì lấy tất cả các học kỳ
     * @return danh sách lớp tín chỉ
     */
    @GetMapping("/classes")
    public ResponseEntity<?> getLopTinChiByGiangVien(
            @RequestParam(required = false) Integer giangVienId,
            @RequestParam(required = false) Integer hocKyId) {
        
        try {
            // Nếu không có giangVienId, lấy từ thông tin đăng nhập
            if (giangVienId == null) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.getPrincipal() instanceof UserDetails) {
                    UserDetails userDetails = (UserDetails) auth.getPrincipal();
                    String email = userDetails.getUsername();
                    
                    Optional<NhanVien> nhanVienOpt = nhanVienRepository.findByEmail(email);
                    if (nhanVienOpt.isPresent()) {
                        NhanVien giangVien = nhanVienOpt.get();
                        // Kiểm tra xem có phải là giảng viên không
                        if ("Giao vien".equals(giangVien.getChucVu())) {
                            giangVienId = giangVien.getId();
                        } else {
                            return ResponseEntity.badRequest().body("Người dùng không phải là giảng viên");
                        }
                    } else {
                        return ResponseEntity.badRequest().body("Không tìm thấy thông tin giảng viên");
                    }
                } else {
                    return ResponseEntity.badRequest().body("Không thể xác định giảng viên đăng nhập");
                }
            }
            
            List<LopTinChiDto> lopTinChis = quanLyDiemService.getLopTinChiByGiangVien(giangVienId, hocKyId);
            return ResponseEntity.ok(lopTinChis);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi khi lấy danh sách lớp tín chỉ: " + e.getMessage());
        }
    }
    
    /**
     * API lấy danh sách sinh viên và điểm của sinh viên trong một lớp tín chỉ
     * @param lopTcId ID của lớp tín chỉ
     * @return danh sách sinh viên và điểm
     */
    @GetMapping("/classes/{lopTcId}/students")
    public ResponseEntity<?> getSinhVienDiemByLopTinChi(@PathVariable Integer lopTcId) {
        try {
            List<SinhVienDiemDto> sinhVienDiems = quanLyDiemService.getSinhVienDiemByLopTinChi(lopTcId);
            return ResponseEntity.ok(sinhVienDiems);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi khi lấy danh sách sinh viên và điểm: " + e.getMessage());
        }
    }
    
    /**
     * API cập nhật điểm cho sinh viên
     * @param dangKyId ID của bản ghi đăng ký
     * @param diemRequest đối tượng chứa thông tin điểm cần cập nhật
     * @return đối tượng SinhVienDiemDto đã cập nhật
     */
    @PutMapping("/grades/{dangKyId}")
    public ResponseEntity<?> updateDiem(
            @PathVariable Integer dangKyId,
            @RequestBody Map<String, Double> diemRequest) {
        
        try {
            Double diemCC = diemRequest.get("diemCC");
            Double diemKT = diemRequest.get("diemKT");
            Double diemThi = diemRequest.get("diemThi");
            
            SinhVienDiemDto updatedDiem = quanLyDiemService.updateDiem(dangKyId, diemCC, diemKT, diemThi);
            return ResponseEntity.ok(updatedDiem);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi khi cập nhật điểm: " + e.getMessage());
        }
    }
} 