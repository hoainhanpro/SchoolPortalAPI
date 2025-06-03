package com.ptithcm.portal.service;

import com.ptithcm.portal.dto.LopTinChiDto;
import com.ptithcm.portal.dto.SinhVienDiemDto;
import java.util.List;

public interface QuanLyDiemService {
    
    /**
     * Lấy danh sách các lớp tín chỉ mà giảng viên đang giảng dạy
     * @param giangVienId ID của giảng viên
     * @param hocKyId ID của học kỳ (có thể null để lấy tất cả)
     * @return danh sách lớp tín chỉ
     */
    List<LopTinChiDto> getLopTinChiByGiangVien(Integer giangVienId, Integer hocKyId);
    
    /**
     * Lấy danh sách sinh viên và điểm của sinh viên trong một lớp tín chỉ
     * @param lopTcId ID của lớp tín chỉ
     * @return danh sách sinh viên và điểm
     */
    List<SinhVienDiemDto> getSinhVienDiemByLopTinChi(Integer lopTcId);
    
    /**
     * Cập nhật điểm cho sinh viên
     * @param dangKyId ID của bản ghi đăng ký
     * @param diemCC Điểm chuyên cần
     * @param diemKT Điểm kiểm tra
     * @param diemThi Điểm thi
     * @return đối tượng SinhVienDiemDto đã cập nhật
     */
    SinhVienDiemDto updateDiem(Integer dangKyId, Double diemCC, Double diemKT, Double diemThi);
} 