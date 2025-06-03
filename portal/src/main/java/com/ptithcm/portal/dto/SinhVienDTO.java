package com.ptithcm.portal.dto;

import com.ptithcm.portal.entity.SinhVien;
import java.time.LocalDate;

public class SinhVienDTO {
    private String mssv;
    private String ho;
    private String ten;
    private LocalDate ngaySinh;
    private String gioiTinh;
    private String diaChi;
    private String sdt;
    private String email;
    private String pwdHash;

    // Thông tin lớp
    private Integer lopId;
    private String lopTen;

    // Constructor mặc định
    public SinhVienDTO() {
    }

    // Constructor với đối tượng SinhVien
    public SinhVienDTO(SinhVien sinhVien) {
        this.mssv = sinhVien.getMssv();
        this.ho = sinhVien.getHo();
        this.ten = sinhVien.getTen();
        this.ngaySinh = sinhVien.getNgaySinh();
        this.gioiTinh = sinhVien.getGioiTinh();
        this.diaChi = sinhVien.getDiaChi();
        this.sdt = sinhVien.getSdt();
        this.email = sinhVien.getEmail();
        this.pwdHash = sinhVien.getPwdHash();
        if (sinhVien.getLop() != null) {
            this.lopId = sinhVien.getLop().getId();
            this.lopTen = sinhVien.getLop().getTen();
        }
    }

    // Getters và Setters
    public String getMssv() {
        return mssv;
    }

    public void setMssv(String mssv) {
        this.mssv = mssv;
    }

    public String getHo() {
        return ho;
    }

    public void setHo(String ho) {
        this.ho = ho;
    }

    public String getTen() {
        return ten;
    }

    public void setTen(String ten) {
        this.ten = ten;
    }

    public LocalDate getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(LocalDate ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    public String getGioiTinh() {
        return gioiTinh;
    }

    public void setGioiTinh(String gioiTinh) {
        this.gioiTinh = gioiTinh;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getLopId() {
        return lopId;
    }

    public void setLopId(Integer lopId) {
        this.lopId = lopId;
    }

    public String getLopTen() {
        return lopTen;
    }

    public void setLopTen(String lopTen) {
        this.lopTen = lopTen;
    }

    public String getHoTen() {
        return this.ho + " " + this.ten;
    }

    public String getPwdHash() {
        return pwdHash;
    }

    public void setPwdHash(String pwdHash) {
        this.pwdHash = pwdHash;
    }
}