package com.ptithcm.portal.service;

import com.ptithcm.portal.dto.DashboardGVDto;
import com.ptithcm.portal.dto.LecturerDto;
import com.ptithcm.portal.dto.NhanVienDto;
import java.util.List;

public interface LecturerService {
    List<LecturerDto> getAllLecturersWithSchedule();
    
    // Thêm phương thức lấy thông tin giảng viên theo ID
    NhanVienDto getLecturerById(Integer id);
    
    // Thêm phương thức cập nhật thông tin giảng viên
    NhanVienDto updateLecturer(Integer id, NhanVienDto nhanVienDto);
    
    // Thêm phương thức lấy thông tin dashboard của giảng viên
    DashboardGVDto getDashBoardInfo(String username);
} 