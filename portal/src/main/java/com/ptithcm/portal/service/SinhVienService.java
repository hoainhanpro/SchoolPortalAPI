package com.ptithcm.portal.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.ptithcm.portal.dto.DashboardSVDto;
import com.ptithcm.portal.dto.SinhVienDTO;
import com.ptithcm.portal.dto.StudentScoreDto;
import com.ptithcm.portal.entity.SinhVien;

public interface SinhVienService {
    public DashboardSVDto getDashBoardInfo(String id);

    public void updateStudent(String mssv, String diaChi, String sdt, LocalDate ngaySinh) throws Exception;

    public List<StudentScoreDto> getStudentScores(String mssv);

    List<SinhVien> getAllStudents();

    Map<String, Object> getStudentDTOById(String id);

    public SinhVienDTO createSinhVienDTO(SinhVienDTO sinhVienDTO);

    public SinhVienDTO updateSinhVienDTO(String mssv, SinhVienDTO sinhVienDTO);

    void deleteStudent(String id);
}
