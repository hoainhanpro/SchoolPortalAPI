package com.ptithcm.portal.dto;

import java.time.LocalDate;

public class HocKyDto {
    private Integer id;
    private String ten;
    private Integer namHoc;
    private Integer thuTu;
    private LocalDate startDate;
    private LocalDate endDate;
    
    // Constructors
    public HocKyDto() {}
    
    public HocKyDto(Integer id, String ten, Integer namHoc, Integer thuTu, 
                    LocalDate startDate, LocalDate endDate) {
        this.id = id;
        this.ten = ten;
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
    
    public String getTen() {
        return ten;
    }
    
    public void setTen(String ten) {
        this.ten = ten;
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
