package com.ptithcm.portal.service;

public interface LecturerClassService {
    /**
     * Đếm số lượng lớp tín chỉ của giảng viên trong học kỳ
     * 
     * @param lecturerId ID của giảng viên
     * @param semesterId ID của học kỳ
     * @return Số lượng lớp tín chỉ
     */
    Integer countClassesByLecturerAndSemester(Integer lecturerId, Integer semesterId);
} 