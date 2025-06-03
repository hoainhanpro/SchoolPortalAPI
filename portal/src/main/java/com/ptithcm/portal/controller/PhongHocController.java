package com.ptithcm.portal.controller;

import com.ptithcm.portal.entity.NhanVien;
import com.ptithcm.portal.entity.PhongHoc;
import com.ptithcm.portal.repository.NhanVienRepository;
import com.ptithcm.portal.repository.PhongHocRepository;
import com.ptithcm.portal.service.PhongHocService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Map;

@RestController
@RequestMapping("/api/phong-hoc")
public class PhongHocController {

    private final PhongHocRepository phongHocRepository;

    @Autowired
    private PhongHocService phongHocService;

    @Autowired
    private NhanVienRepository nhanVienRepository;

    public PhongHocController(PhongHocRepository phongHocRepository) {
        this.phongHocRepository = phongHocRepository;
    }

    /**
     * API lấy danh sách tất cả phòng học
     *
     * @return Danh sách phòng học
     */
    @GetMapping
    public ResponseEntity<List<PhongHoc>> getAllPhongHoc() {
        List<PhongHoc> phongHocList = phongHocRepository.findAll();
        return ResponseEntity.ok(phongHocList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PhongHoc> getPhongHocById(@PathVariable Integer id) {
        Optional<PhongHoc> phongHoc = phongHocService.getPhongHocById(id);
        if (phongHoc.isPresent()) {
            return new ResponseEntity<>(phongHoc.get(), HttpStatus.OK);
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
    public ResponseEntity<?> createPhongHoc(@RequestBody PhongHoc phongHoc) {
        if (!isNhanVien()) {
            return new ResponseEntity<>("Bạn không có quyền thực hiện hành động này", HttpStatus.FORBIDDEN);
        }
        try {
            PhongHoc createdPhongHoc = phongHocService.createPhongHoc(phongHoc);
            return new ResponseEntity<>(createdPhongHoc, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePhongHoc(@PathVariable Integer id, @RequestBody PhongHoc phongHoc) {
        if (!isNhanVien()) {
            return new ResponseEntity<>("Bạn không có quyền thực hiện hành động này", HttpStatus.FORBIDDEN);
        }
        try {
            PhongHoc updatedPhongHoc = phongHocService.updatePhongHoc(id, phongHoc);
            if (updatedPhongHoc != null) {
                return new ResponseEntity<>(updatedPhongHoc, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Phong hoc not found", HttpStatus.NOT_FOUND); // More descriptive message
            }
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePhongHoc(@PathVariable Integer id) {
        if (!isNhanVien()) {
            return new ResponseEntity<>("Bạn không có quyền thực hiện hành động này", HttpStatus.FORBIDDEN);
        }

        Map<String, Object> result = phongHocService.deletePhongHoc(id);

        if ((Boolean) result.get("success")) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(result.get("message"), HttpStatus.CONFLICT);
        }
    }
}