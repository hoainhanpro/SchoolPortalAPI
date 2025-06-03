package com.ptithcm.portal.controller;

import com.ptithcm.portal.dto.StudentCountDto;
import com.ptithcm.portal.service.LecturerStudentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lecturers")
public class LecturerStudentController {

    private final LecturerStudentService lecturerStudentService;
    
    public LecturerStudentController(LecturerStudentService lecturerStudentService) {
        this.lecturerStudentService = lecturerStudentService;
    }

    @GetMapping("/{lecturerId}/students/count")
    public ResponseEntity<StudentCountDto> countStudentsByLecturer(
            @PathVariable Integer lecturerId,
            @RequestParam Integer semesterId) {
        
        Integer studentCount = lecturerStudentService.countStudentsByLecturerAndSemester(lecturerId, semesterId);
        StudentCountDto result = new StudentCountDto(studentCount, lecturerId, semesterId);
        
        return ResponseEntity.ok(result);
    }
} 