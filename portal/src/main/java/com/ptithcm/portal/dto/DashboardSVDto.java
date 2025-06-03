package com.ptithcm.portal.dto;

import java.time.LocalDate;

public class DashboardSVDto {
    private String mssv;
    private String hoTen;
    private LocalDate ngaySinh;
    private String gioiTinh;
    private String diaChi;
    private String sdt;
    private String email;
    private Integer lopId;
    private String lopTen;
    private String khoaTen; 
    private String CoVanTen;

    // Constructors
    public DashboardSVDto() {
    }

    public DashboardSVDto(String mssv, String hoTen, LocalDate ngaySinh, String gioiTinh, String diaChi, String sdt, String email, Integer lopId, String lopTen, String khoaTen) {
        this.mssv = mssv;
        this.hoTen = hoTen;
        this.ngaySinh = ngaySinh;
        this.gioiTinh = gioiTinh;
        this.diaChi = diaChi;
        this.sdt = sdt;
        this.email = email;
        this.lopId = lopId;
        this.lopTen = lopTen;
        this.khoaTen = khoaTen;
    }

    // Getters and Setters
    public String getMssv() {
        return mssv;
    }

    public void setMssv(String mssv) {
        this.mssv = mssv;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
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

    public String getKhoaTen() {
        return khoaTen;
    }

    public void setKhoaTen(String khoaTen) {
        this.khoaTen = khoaTen;
    }
    public String getCoVanTen() {
        return CoVanTen;
    }
    public void setCoVanTen(String coVanTen) {
        CoVanTen = coVanTen;
    }
    @Override
    public String toString() {
        return "DashboardSVDto{" +
                "mssv='" + mssv + '\'' +
                ", hoTen='" + hoTen + '\'' +
                ", ngaySinh=" + ngaySinh +
                ", gioiTinh='" + gioiTinh + '\'' +
                ", diaChi='" + diaChi + '\'' +
                ", sdt='" + sdt + '\'' +
                ", email='" + email + '\'' +
                ", lopId=" + lopId +
                ", lopTen='" + lopTen + '\'' +
                ", khoaTen='" + khoaTen + '\'' +
                '}';
    }
}
