package com.ptithcm.portal.controller;

import com.ptithcm.portal.dto.LopInfoDTO;
import com.ptithcm.portal.entity.Lop;
import com.ptithcm.portal.entity.NhanVien;
import com.ptithcm.portal.repository.NhanVienRepository;
import com.ptithcm.portal.service.LopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Map;

@RestController
@RequestMapping("/api/lop")
public class LopController {

    @Autowired
    private LopService lopService;

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @GetMapping
    public List<Lop> getAllLop() {
        return lopService.getAllLop();
    }

    @GetMapping("/all")
    public List<Map<String, Object>> getAllLopWithKhoa() {
        return lopService.getAllLopWithKhoa();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Lop> getLopById(@PathVariable Integer id) {
        Optional<Lop> lop = lopService.getLopById(id);
        if (lop.isPresent()) {
            return new ResponseEntity<>(lop.get(), HttpStatus.OK);
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
    public ResponseEntity<?> createLop(@RequestBody Lop lop) {
        if (!isNhanVien()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Bạn không có quyền thực hiện hành động này"));
        }
        try {
            Lop createdLop = lopService.createLop(lop);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdLop);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateLop(@PathVariable Integer id, @RequestBody Lop lop) {
        if (!isNhanVien()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Bạn không có quyền thực hiện hành động này"));
        }
        try {
            Lop updatedLop = lopService.updateLop(id, lop);
            if (updatedLop != null) {
                return ResponseEntity.ok(updatedLop);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLop(@PathVariable Integer id) {
        try {
            lopService.deleteLop(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            // Trả về thông báo lỗi khi xóa không thành công
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            // Bắt các lỗi khác nếu có
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}