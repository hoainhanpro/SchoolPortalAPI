package com.ptithcm.portal.controller;

import com.ptithcm.portal.dto.ClassCountDto;
import com.ptithcm.portal.service.LecturerClassService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lecturers")
public class LecturerClassController {

    private final LecturerClassService lecturerClassService;

    public LecturerClassController(LecturerClassService lecturerClassService) {
        this.lecturerClassService = lecturerClassService;
    }

    @GetMapping("/{lecturerId}/classes/count")
    public ResponseEntity<ClassCountDto> countClassesByLecturer(
            @PathVariable Integer lecturerId,
            @RequestParam Integer semesterId) {
        
        Integer classCount = lecturerClassService.countClassesByLecturerAndSemester(lecturerId, semesterId);
        ClassCountDto result = new ClassCountDto(classCount, lecturerId, semesterId);
        
        return ResponseEntity.ok(result);
    }
} 