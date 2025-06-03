package com.ptithcm.portal.dto;

import java.time.LocalDate;

public class DotDongHocPhiDto {
    private Integer id;
    private Integer hocKyId;
    private LocalDate ngayBatDau;
    private LocalDate ngayKetThuc;
    
    // HocKy information
    private String hocKyTen;
    private Integer namHoc;
    private Integer thuTu;
    private LocalDate startDate;
    private LocalDate endDate;
    
    // Constructors
    public DotDongHocPhiDto() {}
    
    public DotDongHocPhiDto(Integer id, Integer hocKyId, LocalDate ngayBatDau, LocalDate ngayKetThuc,
                            String hocKyTen, Integer namHoc, Integer thuTu, 
                            LocalDate startDate, LocalDate endDate) {
        this.id = id;
        this.hocKyId = hocKyId;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
        this.hocKyTen = hocKyTen;
        this.namHoc = namHoc;
        this.thuTu = thuTu;
        this.startDate = startDate;
        this.endDate = endDate;
    }
    
    // Getters and Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public Integer getHocKyId() {
        return hocKyId;
    }
    
    public void setHocKyId(Integer hocKyId) {
        this.hocKyId = hocKyId;
    }
    
    public LocalDate getNgayBatDau() {
        return ngayBatDau;
    }
    
    public void setNgayBatDau(LocalDate ngayBatDau) {
        this.ngayBatDau = ngayBatDau;
    }
    
    public LocalDate getNgayKetThuc() {
        return ngayKetThuc;
    }
    
    public void setNgayKetThuc(LocalDate ngayKetThuc) {
        this.ngayKetThuc = ngayKetThuc;
    }
    
    public String getHocKyTen() {
        return hocKyTen;
    }
    
    public void setHocKyTen(String hocKyTen) {
        this.hocKyTen = hocKyTen;
    }
    
    public Integer getNamHoc() {
        return namHoc;
    }
    
    public void setNamHoc(Integer namHoc) {
        this.namHoc = namHoc;
    }
    
    public Integer getThuTu() {
        return thuTu;
    }
    
    public void setThuTu(Integer thuTu) {
        this.thuTu = thuTu;
    }
    
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}
