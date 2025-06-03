package com.ptithcm.portal.controller;

import com.ptithcm.portal.dto.ThongBaoDTO;
import com.ptithcm.portal.entity.NhanVien;
import com.ptithcm.portal.repository.NhanVienRepository;
import com.ptithcm.portal.service.ThongBaoService;
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
@RequestMapping("/api/thongbao")
public class ThongBaoController {

    @Autowired
    private ThongBaoService thongBaoService;

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @GetMapping
    public ResponseEntity<List<ThongBaoDTO>> getAllThongBao() {
        List<ThongBaoDTO> thongBaos = thongBaoService.getAllThongBao();
        return ResponseEntity.ok(thongBaos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ThongBaoDTO> getThongBaoById(@PathVariable Integer id) {
        Optional<ThongBaoDTO> thongBao = thongBaoService.getThongBaoById(id);
        if (thongBao.isPresent()) {
            return new ResponseEntity<>(thongBao.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    private NhanVien getCurrentNhanVien() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            String email = userDetails.getUsername();
            Optional<NhanVien> nhanVienOpt = nhanVienRepository.findByEmail(email);
            if (nhanVienOpt.isPresent()) {
                return nhanVienOpt.get();
            }
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Không thể xác định người dùng hiện tại");
    }

    private boolean isNhanVien() {
        try {
            NhanVien nhanVien = getCurrentNhanVien();
            return "Nhan vien".equals(nhanVien.getChucVu());
        } catch (Exception e) {
            return false;
        }
    }

    @PostMapping
    public ResponseEntity<?> createThongBao(@RequestBody ThongBaoDTO thongBaoDTO) {
        if (!isNhanVien()) {
            return new ResponseEntity<>("Bạn không có quyền thực hiện hành động này", HttpStatus.FORBIDDEN);
        }

        try {
            // Tự động gắn ID nhân viên hiện tại nếu không được cung cấp
            if (thongBaoDTO.getNvId() == null) {
                thongBaoDTO.setNvId(getCurrentNhanVien().getId());
            }

            ThongBaoDTO createdThongBao = thongBaoService.createThongBao(thongBaoDTO);
            return new ResponseEntity<>(createdThongBao, HttpStatus.CREATED);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getReason(), e.getStatusCode());
        } catch (Exception e) {
            return new ResponseEntity<>("Có lỗi xảy ra: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateThongBao(@PathVariable Integer id, @RequestBody ThongBaoDTO thongBaoDTO) {
        if (!isNhanVien()) {
            return new ResponseEntity<>("Bạn không có quyền thực hiện hành động này", HttpStatus.FORBIDDEN);
        }

        try {
            // Kiểm tra quyền: chỉ được cập nhật thông báo do mình tạo
            Optional<ThongBaoDTO> existingThongBao = thongBaoService.getThongBaoById(id);
            if (existingThongBao.isPresent()) {
                NhanVien currentUser = getCurrentNhanVien();
                if (!existingThongBao.get().getNvId().equals(currentUser.getId())) {
                    return new ResponseEntity<>("Bạn không có quyền chỉnh sửa thông báo này", HttpStatus.FORBIDDEN);
                }
            }

            ThongBaoDTO updatedThongBao = thongBaoService.updateThongBao(id, thongBaoDTO);
            return new ResponseEntity<>(updatedThongBao, HttpStatus.OK);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getReason(), e.getStatusCode());
        } catch (Exception e) {
            return new ResponseEntity<>("Có lỗi xảy ra: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}