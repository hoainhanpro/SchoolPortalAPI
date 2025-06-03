package com.ptithcm.portal.dto;

import java.time.LocalDate;

public class DashboardGVDto {
    private Integer id;
    private String maGV;
    private String hoTen;
    private LocalDate ngaySinh;
    private String gioiTinh;
    private String diaChi;
    private String sdt;
    private String email;
    private String chucVu;
    private Integer khoaId;
    private String khoaTen;
    private String hocHam;
    private String hocVi;

    // Constructors
    public DashboardGVDto() {
    }

    public DashboardGVDto(Integer id, String maGV, String hoTen, LocalDate ngaySinh, String gioiTinh, String diaChi, 
                         String sdt, String email, String chucVu, Integer khoaId, String khoaTen, 
                         String hocHam, String hocVi) {
        this.id = id;
        this.maGV = maGV;
        this.hoTen = hoTen;
        this.ngaySinh = ngaySinh;
        this.gioiTinh = gioiTinh;
        this.diaChi = diaChi;
        this.sdt = sdt;
        this.email = email;
        this.chucVu = chucVu;
        this.khoaId = khoaId;
        this.khoaTen = khoaTen;
        this.hocHam = hocHam;
        this.hocVi = hocVi;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getMaGV() {
        return maGV;
    }

    public void setMaGV(String maGV) {
        this.maGV = maGV;
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

    public String getChucVu() {
        return chucVu;
    }

    public void setChucVu(String chucVu) {
        this.chucVu = chucVu;
    }

    public Integer getKhoaId() {
        return khoaId;
    }

    public void setKhoaId(Integer khoaId) {
        this.khoaId = khoaId;
    }

    public String getKhoaTen() {
        return khoaTen;
    }

    public void setKhoaTen(String khoaTen) {
        this.khoaTen = khoaTen;
    }
    
    public String getHocHam() {
        return hocHam;
    }

    public void setHocHam(String hocHam) {
        this.hocHam = hocHam;
    }

    public String getHocVi() {
        return hocVi;
    }

    public void setHocVi(String hocVi) {
        this.hocVi = hocVi;
    }

    @Override
    public String toString() {
        return "DashboardGVDto{" +
                "id=" + id +
                ", maGV='" + maGV + '\'' +
                ", hoTen='" + hoTen + '\'' +
                ", ngaySinh=" + ngaySinh +
                ", gioiTinh='" + gioiTinh + '\'' +
                ", diaChi='" + diaChi + '\'' +
                ", sdt='" + sdt + '\'' +
                ", email='" + email + '\'' +
                ", chucVu='" + chucVu + '\'' +
                ", khoaId=" + khoaId +
                ", khoaTen='" + khoaTen + '\'' +
                ", hocHam='" + hocHam + '\'' +
                ", hocVi='" + hocVi + '\'' +
                '}';
    }
} 