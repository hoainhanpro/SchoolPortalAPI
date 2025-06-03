package com.ptithcm.portal.dto;

import java.time.LocalDate;

public class ProfileEditRequest {
    private String mssv;
    private LocalDate ngaySinh;
    private String diaChi;
    private String sdt; 

    // Constructors
    public ProfileEditRequest() {
    }

    public ProfileEditRequest(String mssv, LocalDate ngaySinh, String diaChi, String sdt) {
        this.mssv = mssv;
        this.ngaySinh = ngaySinh;
        this.diaChi = diaChi;
        this.sdt = sdt;
    }

    // Getters and Setters

    public String getMssv() {
        return mssv;
    }

    public void setMssv(String mssv) {
        this.mssv = mssv;
    }

    public LocalDate getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(LocalDate ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public String getSdt() {
        return sdt;
    }

    public void setSdt(String sdt) {
        this.sdt = sdt;
    }
}
