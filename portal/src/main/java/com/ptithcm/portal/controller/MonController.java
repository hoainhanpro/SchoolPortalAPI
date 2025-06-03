package com.ptithcm.portal.controller;

import com.ptithcm.portal.dto.MonDTO;
import com.ptithcm.portal.entity.Mon;
import com.ptithcm.portal.entity.NhanVien;
import com.ptithcm.portal.repository.MonRepository;
import com.ptithcm.portal.repository.NhanVienRepository;
import com.ptithcm.portal.service.MonService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;
import java.util.Map;

@RestController
@RequestMapping("/api/mon")
public class MonController {

    private final MonRepository monRepository;

    @Autowired
    private MonService monService;

    @Autowired
    private NhanVienRepository nhanVienRepository;

    public MonController(MonRepository monRepository) {
        this.monRepository = monRepository;
    }

    /**
     * API lấy danh sách tất cả môn học
     * 
     * @return Danh sách môn học
     */
    @GetMapping
    public ResponseEntity<List<Mon>> getAllMon() {
        List<Mon> monList = monRepository.findAll();
        return ResponseEntity.ok(monList);
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

    /**
     * API lấy danh sách tất cả môn học dạng DTO (tránh vòng lặp vô hạn)
     */
    @GetMapping("/all")
    public ResponseEntity<List<MonDTO>> getAllMonDTO() {
        List<MonDTO> monList = monService.getAllMonDTO();
        return ResponseEntity.ok(monList);
    }

    /**
     * API lấy thông tin chi tiết một môn học
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getMonById(@PathVariable Integer id) {
        Optional<MonDTO> mon = monService.getMonDTOById(id);
        if (mon.isPresent()) {
            return ResponseEntity.ok(mon.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createMon(@RequestBody MonDTO monDTO) {
        if (!isNhanVien()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Bạn không có quyền thực hiện hành động này"));
        }

        try {
            MonDTO createdMon = monService.createMon(monDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdMon);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
        } catch (Exception e) { // Catch any other unexpected exceptions
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Lỗi hệ thống"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMon(@PathVariable Integer id, @RequestBody MonDTO monDTO) {
        if (!isNhanVien()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Bạn không có quyền thực hiện hành động này"));
        }

        try {
            Optional<MonDTO> updatedMon = monService.updateMon(id, monDTO);
            if (updatedMon.isPresent()) {
                return ResponseEntity.ok(updatedMon.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
        } catch (Exception e) { // Catch any other unexpected exceptions
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Lỗi hệ thống"));
        }
    }

    /**
     * API xóa môn học
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMon(@PathVariable Integer id) {
        if (!isNhanVien()) {
            return new ResponseEntity<>("Bạn không có quyền thực hiện hành động này", HttpStatus.FORBIDDEN);
        }

        // Kiểm tra xem môn học có đang được sử dụng không
        if (monService.isMonUsedInLopTinChi(id)) {
            long count = monService.countLopTinChiUsingMon(id);
            String errorMessage = "Không thể xóa môn học này vì đang được sử dụng trong " + count + " lớp tín chỉ";
            return new ResponseEntity<>(errorMessage, HttpStatus.CONFLICT); // 409 Conflict
        }

        try {
            monService.deleteMon(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>("Lỗi khi xóa môn học: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}