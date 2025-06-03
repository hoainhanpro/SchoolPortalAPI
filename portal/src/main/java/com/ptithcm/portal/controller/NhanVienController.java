package com.ptithcm.portal.controller;

import com.ptithcm.portal.entity.NhanVien;
import com.ptithcm.portal.dto.NhanVienDto;
import com.ptithcm.portal.dto.NhanVienResponseDto;
import com.ptithcm.portal.repository.NhanVienRepository;
import com.ptithcm.portal.service.NhanVienService;

import jakarta.persistence.EntityNotFoundException;

import com.ptithcm.portal.dto.NhanVienDto;
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
@RequestMapping("/api/nhanvien")
public class NhanVienController {

    @Autowired
    private NhanVienService nhanVienService;

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @GetMapping
    public List<NhanVienDto> getAllNhanVien() {
        return nhanVienService.getAllNhanVien();
    }

    @GetMapping("/{id}")
    public ResponseEntity<NhanVienDto> getNhanVienById(@PathVariable Integer id) {
        Optional<NhanVienDto> nhanVienDto = nhanVienService.getNhanVienById(id);
        return nhanVienDto
                .map(dto -> new ResponseEntity<>(dto, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    private boolean isAdmin() {
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
    public ResponseEntity<?> createNhanVien(@RequestBody NhanVienDto nhanVienDTO) {
        if (!isAdmin()) {
            return new ResponseEntity<>("Bạn không có quyền thực hiện hành động này", HttpStatus.FORBIDDEN);
        }
        try {
            NhanVienResponseDto nhanVienResponseDto = nhanVienService.createNhanVien(nhanVienDTO);
            return new ResponseEntity<>(nhanVienResponseDto, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Lỗi hệ thống", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateNhanVien(@PathVariable Integer id, @RequestBody NhanVienDto nhanVienDto) {
        if (!isAdmin()) {
            return new ResponseEntity<>("Bạn không có quyền thực hiện hành động này", HttpStatus.FORBIDDEN);
        }
        try {
            NhanVienDto updatedNhanVienDto = nhanVienService.updateNhanVien(id, nhanVienDto);
            return new ResponseEntity<>(updatedNhanVienDto, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Lỗi hệ thống", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteNhanVien(@PathVariable Integer id) {
        if (!isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Bạn không có quyền thực hiện hành động này"));
        }
        try {
            nhanVienService.deleteNhanVien(id);
            return ResponseEntity.ok().body(Map.of("message", "Nhân viên đã được xóa"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

}