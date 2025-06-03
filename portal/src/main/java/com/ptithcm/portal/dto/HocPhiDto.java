package com.ptithcm.portal.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class HocPhiDto {
    private Integer id;
    private String svId;
    private Integer hocKyId;
    private BigDecimal tongTien;
    private BigDecimal daDong;
    private LocalDate ngayDong;
    private String trangThai;
    
    // HocKy information
    private String hocKyTen;
    private Integer namHoc;
    private Integer thuTu;
    private LocalDate startDate;
    private LocalDate endDate;
    
    // Constructors
    public HocPhiDto() {}
    
    public HocPhiDto(Integer id, String svId, Integer hocKyId, BigDecimal tongTien, 
                     BigDecimal daDong, LocalDate ngayDong, String trangThai,
                     String hocKyTen, Integer namHoc, Integer thuTu, 
                     LocalDate startDate, LocalDate endDate) {
        this.id = id;
        this.svId = svId;
        this.hocKyId = hocKyId;
        this.tongTien = tongTien;
        this.daDong = daDong;
        this.ngayDong = ngayDong;
        this.trangThai = trangThai;
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
    
    public String getSvId() {
        return svId;
    }
    
    public void setSvId(String svId) {
        this.svId = svId;
    }
    
    public Integer getHocKyId() {
        return hocKyId;
    }
    
    public void setHocKyId(Integer hocKyId) {
        this.hocKyId = hocKyId;
    }
    
    public BigDecimal getTongTien() {
        return tongTien;
    }
    
    public void setTongTien(BigDecimal tongTien) {
        this.tongTien = tongTien;
    }
    
    public BigDecimal getDaDong() {
        return daDong;
    }
    
    public void setDaDong(BigDecimal daDong) {
        this.daDong = daDong;
    }
    
    public LocalDate getNgayDong() {
        return ngayDong;
    }
    
    public void setNgayDong(LocalDate ngayDong) {
        this.ngayDong = ngayDong;
    }
    
    public String getTrangThai() {
        return trangThai;
    }
    
    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
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
