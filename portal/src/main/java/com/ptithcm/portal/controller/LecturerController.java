package com.ptithcm.portal.controller;

import com.ptithcm.portal.dto.LecturerDto;
import com.ptithcm.portal.dto.NhanVienDto;
import com.ptithcm.portal.service.LecturerService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lecturers")
public class LecturerController {

    @Autowired
    private LecturerService lecturerService;

    @GetMapping
    public ResponseEntity<List<LecturerDto>> getAllLecturersWithSchedule() {
        try {
            List<LecturerDto> lecturers = lecturerService.getAllLecturersWithSchedule();
            return ResponseEntity.ok(lecturers);
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy danh sách giảng viên: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getLecturerById(@PathVariable Integer id) {
        try {
            NhanVienDto lecturer = lecturerService.getLecturerById(id);
            return ResponseEntity.ok(lecturer);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy thông tin giảng viên: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateLecturer(@PathVariable Integer id, @RequestBody NhanVienDto nhanVienDto) {
        try {
            NhanVienDto updatedLecturer = lecturerService.updateLecturer(id, nhanVienDto);
            return ResponseEntity.ok(updatedLecturer);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("Lỗi khi cập nhật thông tin giảng viên: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
} 