package com.ptithcm.portal.service;

import com.ptithcm.portal.repository.LecturerStudentRepository;
import org.springframework.stereotype.Service;

@Service
public class LecturerStudentService {

    private final LecturerStudentRepository lecturerStudentRepository;

    public LecturerStudentService(LecturerStudentRepository lecturerStudentRepository) {
        this.lecturerStudentRepository = lecturerStudentRepository;
    }

    public Integer countStudentsByLecturerAndSemester(Integer lecturerId, Integer semesterId) {
        return lecturerStudentRepository.countStudentsByLecturerAndSemester(lecturerId, semesterId);
    }
} 