package com.ptithcm.portal.controller;

import com.ptithcm.portal.dto.DotDongHocPhiDto;
import com.ptithcm.portal.entity.NhanVien;
import com.ptithcm.portal.repository.NhanVienRepository;
import com.ptithcm.portal.service.DotDongHocPhiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/dotdonghocphi")
public class DotDongHocPhiController {

    @Autowired
    private DotDongHocPhiService dotDongHocPhiService;

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @GetMapping
    public ResponseEntity<List<DotDongHocPhiDto>> getAllDotDongHocPhi() {
        return ResponseEntity.ok(dotDongHocPhiService.getAllDotDongHocPhi());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DotDongHocPhiDto> getDotDongHocPhiById(@PathVariable Integer id) {
        Optional<DotDongHocPhiDto> dotDongHocPhi = dotDongHocPhiService.getDotDongHocPhiById(id);
        if (dotDongHocPhi.isPresent()) {
            return new ResponseEntity<>(dotDongHocPhi.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    private boolean isNhanVien() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            String email = userDetails.getUsername();
            Optional<NhanVien> nhanVienOpt = nhanVienRepository.findByEmail(email);
            if (nhanVienOpt.isPresent()) {
                NhanVien nhanVien = nhanVienOpt.get();
                return "Nhan vien".equals(nhanVien.getChucVu());
            }
        }
        return false;
    }

    @PostMapping
    public ResponseEntity<?> createDotDongHocPhi(@RequestBody DotDongHocPhiDto dotDongHocPhiDto) {
        if (!isNhanVien()) {
            return new ResponseEntity<>("Bạn không có quyền thực hiện hành động này", HttpStatus.FORBIDDEN);
        }

        try {
            DotDongHocPhiDto createdDot = dotDongHocPhiService.createDotDongHocPhi(dotDongHocPhiDto);
            return new ResponseEntity<>(createdDot, HttpStatus.CREATED);
        } catch (ResponseStatusException e) {
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                return new ResponseEntity<>("Đã tồn tại đợt đóng học phí cho học kỳ này", HttpStatus.CONFLICT);
            } else if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                return new ResponseEntity<>(e.getReason(), HttpStatus.BAD_REQUEST);
            } else {
                return new ResponseEntity<>("Có lỗi xảy ra: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Có lỗi xảy ra khi tạo đợt đóng học phí: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDotDongHocPhi(@PathVariable Integer id,
            @RequestBody DotDongHocPhiDto dotDongHocPhiDto) {
        if (!isNhanVien()) {
            return new ResponseEntity<>("Bạn không có quyền thực hiện hành động này", HttpStatus.FORBIDDEN);
        }

        try {
            DotDongHocPhiDto updatedDot = dotDongHocPhiService.updateDotDongHocPhi(id, dotDongHocPhiDto);
            return new ResponseEntity<>(updatedDot, HttpStatus.OK);
        } catch (ResponseStatusException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return new ResponseEntity<>("Không tìm thấy đợt đóng học phí với ID: " + id, HttpStatus.NOT_FOUND);
            } else if (e.getStatusCode() == HttpStatus.CONFLICT) {
                return new ResponseEntity<>("Đã tồn tại đợt đóng học phí cho học kỳ này", HttpStatus.CONFLICT);
            } else if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                return new ResponseEntity<>(e.getReason(), HttpStatus.BAD_REQUEST);
            } else {
                return new ResponseEntity<>("Có lỗi xảy ra: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Có lỗi xảy ra khi cập nhật đợt đóng học phí: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}