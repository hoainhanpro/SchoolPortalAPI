package com.ptithcm.portal.service;

import com.ptithcm.portal.repository.ChiTietGiangDayRepository;
import org.springframework.stereotype.Service;

@Service
public class LecturerClassServiceImpl implements LecturerClassService {

    private final ChiTietGiangDayRepository chiTietGiangDayRepository;

    public LecturerClassServiceImpl(ChiTietGiangDayRepository chiTietGiangDayRepository) {
        this.chiTietGiangDayRepository = chiTietGiangDayRepository;
    }

    @Override
    public Integer countClassesByLecturerAndSemester(Integer lecturerId, Integer semesterId) {
        return chiTietGiangDayRepository.countClassesByLecturerAndSemester(lecturerId, semesterId);
    }
} 