package com.ptithcm.portal.dto;

import java.time.LocalDate;

public class DashboardNVDto {
    private Integer id;
    private String ho;
    private String ten;
    private String hoTen;
    private LocalDate ngaySinh;
    private String gioiTinh;
    private String diaChi;
    private String sdt;
    private String email;
    private String chucVu;
    private Integer khoaId;
    private String khoaTen;

    // Constructors
    public DashboardNVDto() {
    }

    public DashboardNVDto(Integer id, String ho, String ten, LocalDate ngaySinh, String gioiTinh,
            String diaChi, String sdt, String email, String chucVu,
            Integer khoaId, String khoaTen) {
        this.id = id;
        this.ho = ho;
        this.ten = ten;
        this.hoTen = ho + " " + ten;
        this.ngaySinh = ngaySinh;
        this.gioiTinh = gioiTinh;
        this.diaChi = diaChi;
        this.sdt = sdt;
        this.email = email;
        this.chucVu = chucVu;
        this.khoaId = khoaId;
        this.khoaTen = khoaTen;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getHo() {
        return ho;
    }

    public void setHo(String ho) {
        this.ho = ho;
        updateHoTen();
    }

    public String getTen() {
        return ten;
    }

    public void setTen(String ten) {
        this.ten = ten;
        updateHoTen();
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    private void updateHoTen() {
        if (ho != null && ten != null) {
            this.hoTen = ho + " " + ten;
        }
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

    @Override
    public String toString() {
        return "DashboardNVDto{" +
                "id=" + id +
                ", ho='" + ho + '\'' +
                ", ten='" + ten + '\'' +
                ", hoTen='" + hoTen + '\'' +
                ", ngaySinh=" + ngaySinh +
                ", gioiTinh='" + gioiTinh + '\'' +
                ", diaChi='" + diaChi + '\'' +
                ", sdt='" + sdt + '\'' +
                ", email='" + email + '\'' +
                ", chucVu='" + chucVu + '\'' +
                ", khoaId=" + khoaId +
                ", khoaTen='" + khoaTen + '\'' +
                '}';
    }
}