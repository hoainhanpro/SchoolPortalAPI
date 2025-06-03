package com.ptithcm.portal.controller;

import com.ptithcm.portal.dto.LopDTO;
import com.ptithcm.portal.service.CoVanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/covan")
public class CoVanController {

    @Autowired
    private CoVanService coVanService;

    /**
     * API lấy danh sách lớp cố vấn của giảng viên đang đăng nhập
     * 
     * @return Danh sách các lớp mà giảng viên đang cố vấn
     */
    @GetMapping("/lop")
    public ResponseEntity<List<LopDTO>> getDanhSachLopCoVan() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Giả định rằng Principal có lưu ID của nhân viên trong userDetails
        // Thay thế dòng dưới bằng cách lấy ID người dùng từ authentication theo cách triển khai của bạn
        Integer nvId = getUserIdFromAuthentication(authentication);
        
        List<LopDTO> danhSachLop = coVanService.getLopCoVanByNhanVienId(nvId);
        return ResponseEntity.ok(danhSachLop);
    }

    /**
     * API lấy danh sách lớp cố vấn của giảng viên đang đăng nhập trong năm học hiện tại
     * 
     * @return Danh sách các lớp mà giảng viên đang cố vấn trong năm học hiện tại
     */
    @GetMapping("/lop/namhoc-hientai")
    public ResponseEntity<List<LopDTO>> getDanhSachLopCoVanTrongNamHocHienTai() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Integer nvId = getUserIdFromAuthentication(authentication);
        
        List<LopDTO> danhSachLop = coVanService.getLopCoVanByNhanVienIdTrongNamHocHienTai(nvId);
        return ResponseEntity.ok(danhSachLop);
    }

    /**
     * API lấy danh sách lớp cố vấn của giảng viên đang đăng nhập trong một năm học cụ thể
     * 
     * @param namHoc Năm học cần tìm
     * @return Danh sách các lớp mà giảng viên đang cố vấn trong năm học đó
     */
    @GetMapping("/lop/namhoc/{namHoc}")
    public ResponseEntity<List<LopDTO>> getDanhSachLopCoVanTheoNamHoc(@PathVariable Integer namHoc) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Integer nvId = getUserIdFromAuthentication(authentication);
        
        List<LopDTO> danhSachLop = coVanService.getLopCoVanByNhanVienIdAndNamHoc(nvId, namHoc);
        return ResponseEntity.ok(danhSachLop);
    }
    
    /**
     * Phương thức trích xuất ID của nhân viên từ đối tượng Authentication
     * Cần điều chỉnh tùy theo cách triển khai của bạn
     */
    private Integer getUserIdFromAuthentication(Authentication authentication) {
        // Đây là phương thức giả định, cần thay thế bằng mã thực tế lấy ID từ authentication
        // Ví dụ:
        // UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        // return userPrincipal.getId();
        
        // Tạm thời return một giá trị mặc định để code có thể biên dịch
        // Cần thay thế phương thức này với implementation thực tế
        return 1; // Thay thế với cách lấy ID thực tế
    }
} 