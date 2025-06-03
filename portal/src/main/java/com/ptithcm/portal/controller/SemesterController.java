package com.ptithcm.portal.controller;

import com.ptithcm.portal.dto.HocKyDto;
import com.ptithcm.portal.dto.SemesterInfoDto;
import com.ptithcm.portal.entity.HocKy;
import com.ptithcm.portal.entity.NhanVien;
import com.ptithcm.portal.repository.NhanVienRepository;
import com.ptithcm.portal.service.SemesterService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/semesters")
public class SemesterController {

    private final SemesterService semesterService;

    @Autowired
    private NhanVienRepository nhanVienRepository;

    public SemesterController(SemesterService semesterService) {
        this.semesterService = semesterService;
    }

    @GetMapping("/current")
    public ResponseEntity<SemesterInfoDto> getCurrentSemester() {
        SemesterInfoDto currentSemester = semesterService.getCurrentSemester();
        if (currentSemester != null) {
            return ResponseEntity.ok(currentSemester);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<SemesterInfoDto>> getAllSemesters() {
        List<SemesterInfoDto> semesters = semesterService.getAllSemesters();
        System.out.println("Semesters: " + semesters);
        return ResponseEntity.ok(semesters);
    }

    @GetMapping("/allHocKy")
    public ResponseEntity<List<HocKyDto>> getAllHocKy() {
        List<HocKy> hocKyList = semesterService.getAllHocKy();
        List<HocKyDto> dtoList = hocKyList.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HocKyDto> getHocKyById(@PathVariable Integer id) {
        Optional<HocKy> hocKy = semesterService.getHocKyById(id);
        if (hocKy.isPresent()) {
            HocKyDto dto = convertToDto(hocKy.get());
            return ResponseEntity.ok(dto);
        } else {
            return ResponseEntity.notFound().build();
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
    public ResponseEntity<?> createHocKy(@RequestBody HocKyDto hocKyDto) {
        if (!isNhanVien()) {
            return new ResponseEntity<>("Bạn không có quyền thực hiện hành động này", HttpStatus.FORBIDDEN);
        }

        try {
            HocKy hocKy = convertToEntity(hocKyDto);
            HocKy createdHocKy = semesterService.createHocKy(hocKy);
            HocKyDto createdDto = convertToDto(createdHocKy);
            return new ResponseEntity<>(createdDto, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage()); // 409 Conflict
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi hệ thống khi tạo học kỳ: " + e.getMessage()); // 500 Internal Server Error
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateHocKy(@PathVariable Integer id, @RequestBody HocKyDto hocKyDto) {
        if (!isNhanVien()) {
            return new ResponseEntity<>("Bạn không có quyền thực hiện hành động này", HttpStatus.FORBIDDEN);
        }

        try {
            HocKy hocKy = convertToEntity(hocKyDto);
            hocKy.setId(id);

            HocKy updatedHocKy = semesterService.updateHocKy(id, hocKy);
            if (updatedHocKy != null) {
                HocKyDto updatedDto = convertToDto(updatedHocKy);
                return ResponseEntity.ok(updatedDto);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage()); // 409 Conflict
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi hệ thống khi cập nhật học kỳ: " + e.getMessage()); // 500 Internal Server Error
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteHocKy(@PathVariable Integer id) {
        if (!isNhanVien()) {
            return new ResponseEntity<>("Bạn không có quyền thực hiện hành động này", HttpStatus.FORBIDDEN);
        }

        String errorMessage = semesterService.deleteHocKy(id);
        if (errorMessage != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorMessage);
        }

        return ResponseEntity.ok().build();
    }

    // Phương thức chuyển đổi HocKy entity thành HocKyDTO
    private HocKyDto convertToDto(HocKy hocKy) {
        return new HocKyDto(
                hocKy.getId(),
                hocKy.getTen(),
                hocKy.getNamHoc(),
                hocKy.getThuTu(),
                hocKy.getStartDate(),
                hocKy.getEndDate());
    }

    // Phương thức chuyển đổi HocKyDTO thành HocKy entity
    private HocKy convertToEntity(HocKyDto dto) {
        HocKy hocKy = new HocKy();
        if (dto.getId() != null) {
            hocKy.setId(dto.getId());
        }
        hocKy.setTen(dto.getTen());
        hocKy.setNamHoc(dto.getNamHoc());
        hocKy.setThuTu(dto.getThuTu());
        hocKy.setStartDate(dto.getStartDate());
        hocKy.setEndDate(dto.getEndDate());
        return hocKy;
    }
}