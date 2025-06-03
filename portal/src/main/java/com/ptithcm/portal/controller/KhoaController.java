package com.ptithcm.portal.controller;

import com.ptithcm.portal.dto.KhoaDTO;
import com.ptithcm.portal.entity.Khoa;
import com.ptithcm.portal.entity.NhanVien;
import com.ptithcm.portal.repository.NhanVienRepository;
import com.ptithcm.portal.repository.KhoaRepository;
import com.ptithcm.portal.service.KhoaService;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/khoa")
public class KhoaController {

    private static final Logger log = LoggerFactory.getLogger(KhoaService.class);

    @Autowired
    private KhoaService khoaService;

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Autowired
    private KhoaRepository khoaRepository;

    @GetMapping
    public List<KhoaDTO> getAllKhoa() {
        List<Khoa> khoaList = khoaRepository.findAll();
        return khoaList.stream()
                .map(khoa -> new KhoaDTO(khoa.getId(), khoa.getTen()))
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Khoa> getKhoaById(@PathVariable Integer id) {
        Optional<Khoa> khoa = khoaService.getKhoaById(id);
        if (khoa.isPresent()) {
            return new ResponseEntity<>(khoa.get(), HttpStatus.OK);
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
    public ResponseEntity<?> createKhoa(@RequestBody Khoa khoa) {
        if (!isNhanVien()) {
            return new ResponseEntity<>("Bạn không có quyền thực hiện hành động này", HttpStatus.FORBIDDEN);
        }
        try {
            Khoa createdKhoa = khoaService.createKhoa(khoa);
            return new ResponseEntity<>(createdKhoa, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Map.of("error", "Tên khoa đã tồn tại", "message", e.getMessage()),
                    HttpStatus.CONFLICT);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateKhoa(@PathVariable Integer id, @RequestBody Khoa khoa) {
        if (!isNhanVien()) {
            return new ResponseEntity<>("Bạn không có quyền thực hiện hành động này", HttpStatus.FORBIDDEN);
        }
        try {
            Khoa updatedKhoa = khoaService.updateKhoa(id, khoa);
            if (updatedKhoa != null) {
                return new ResponseEntity<>(updatedKhoa, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Khoa không tồn tại", HttpStatus.NOT_FOUND);
            }
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Map.of("error", "Tên khoa đã tồn tại", "message", e.getMessage()),
                    HttpStatus.CONFLICT);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteKhoa(@PathVariable Integer id) {
        if (!isNhanVien()) {
            return new ResponseEntity<>("Bạn không có quyền thực hiện hành động này", HttpStatus.FORBIDDEN);
        }

        String error = khoaService.deleteKhoa(id);
        if (error != null) {
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}