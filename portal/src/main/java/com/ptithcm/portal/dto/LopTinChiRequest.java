package com.ptithcm.portal.dto;

import java.time.LocalDate;

public class LopTinChiRequest {
    private Integer monId;
    private Integer hocKyId;
    private Integer nhom;
    private Integer siSoToiDa;
    private Integer giangVienId;
    private String loaiGiangDay; // Lý thuyết, Thực hành
    
    // Thông tin lịch học cố định theo tuần
    private Integer thuInWeek; // 2 = Thứ 2, 3 = Thứ 3, ..., 8 = Chủ nhật
    private Integer buoiId; // ID buổi học (sáng/chiều)
    private Integer phongHocId; // Phòng học cố định
    private LocalDate ngayBatDau; // Ngày bắt đầu đăng ký (optional, mặc định là ngày hiện tại)
    
    // Getters and Setters
    public Integer getMonId() {
        return monId;
    }
    
    public void setMonId(Integer monId) {
        this.monId = monId;
    }
    
    public Integer getHocKyId() {
        return hocKyId;
    }
    
    public void setHocKyId(Integer hocKyId) {
        this.hocKyId = hocKyId;
    }
    
    public Integer getNhom() {
        return nhom;
    }
    
    public void setNhom(Integer nhom) {
        this.nhom = nhom;
    }
    
    public Integer getSiSoToiDa() {
        return siSoToiDa;
    }
    
    public void setSiSoToiDa(Integer siSoToiDa) {
        this.siSoToiDa = siSoToiDa;
    }
    
    public Integer getGiangVienId() {
        return giangVienId;
    }
    
    public void setGiangVienId(Integer giangVienId) {
        this.giangVienId = giangVienId;
    }
    
    public String getLoaiGiangDay() {
        return loaiGiangDay;
    }
    
    public void setLoaiGiangDay(String loaiGiangDay) {
        this.loaiGiangDay = loaiGiangDay;
    }
    
    public Integer getThuInWeek() {
        return thuInWeek;
    }
    
    public void setThuInWeek(Integer thuInWeek) {
        this.thuInWeek = thuInWeek;
    }
    
    public Integer getBuoiId() {
        return buoiId;
    }
    
    public void setBuoiId(Integer buoiId) {
        this.buoiId = buoiId;
    }
    
    public Integer getPhongHocId() {
        return phongHocId;
    }
    
    public void setPhongHocId(Integer phongHocId) {
        this.phongHocId = phongHocId;
    }
    
    public LocalDate getNgayBatDau() {
        return ngayBatDau;
    }
    
    public void setNgayBatDau(LocalDate ngayBatDau) {
        this.ngayBatDau = ngayBatDau;
    }
} 