package com.ptithcm.portal.controller;

import com.ptithcm.portal.dto.DotDangKyDTO;
import com.ptithcm.portal.entity.NhanVien;
import com.ptithcm.portal.repository.NhanVienRepository;
import com.ptithcm.portal.service.DotDangKyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/dotdangky")
public class DotDangKyController {

    @Autowired
    private DotDangKyService dotDangKyService;

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @GetMapping
    public List<DotDangKyDTO> getAllDotDangKy() {
        return dotDangKyService.getAllDotDangKy();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DotDangKyDTO> getDotDangKyById(@PathVariable Integer id) {
        Optional<DotDangKyDTO> dotDangKy = dotDangKyService.getDotDangKyById(id);
        if (dotDangKy.isPresent()) {
            return new ResponseEntity<>(dotDangKy.get(), HttpStatus.OK);
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
    public ResponseEntity<?> createDotDangKy(@RequestBody DotDangKyDTO dotDangKyDTO) {
        if (!isNhanVien()) {
            return new ResponseEntity<>("Bạn không có quyền thực hiện hành động này", HttpStatus.FORBIDDEN);
        }

        try {
            DotDangKyDTO createdDotDangKy = dotDangKyService.createDotDangKy(dotDangKyDTO);
            return new ResponseEntity<>(createdDotDangKy, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            // Xử lý lỗi trùng lặp học kỳ
            return new ResponseEntity<>(new ErrorResponse("Conflict: " + e.getMessage()),
                    HttpStatus.CONFLICT);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDotDangKy(@PathVariable Integer id, @RequestBody DotDangKyDTO dotDangKyDTO) {
        if (!isNhanVien()) {
            return new ResponseEntity<>("Bạn không có quyền thực hiện hành động này", HttpStatus.FORBIDDEN);
        }

        try {
            DotDangKyDTO updatedDotDangKy = dotDangKyService.updateDotDangKy(id, dotDangKyDTO);
            if (updatedDotDangKy != null) {
                return new ResponseEntity<>(updatedDotDangKy, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (IllegalArgumentException e) {
            // Xử lý lỗi trùng lặp học kỳ hoặc không tìm thấy
            if (e.getMessage().contains("Không tìm thấy đợt đăng ký")) {
                return new ResponseEntity<>(new ErrorResponse(e.getMessage()),
                        HttpStatus.NOT_FOUND);
            } else {
                return new ResponseEntity<>(new ErrorResponse("Conflict: " + e.getMessage()),
                        HttpStatus.CONFLICT);
            }
        }
    }

    // Class để trả về thông báo lỗi
    private static class ErrorResponse {
        private String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}