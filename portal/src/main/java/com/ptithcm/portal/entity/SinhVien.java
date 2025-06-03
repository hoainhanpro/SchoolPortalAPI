package com.ptithcm.portal.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "sinhvien")
public class SinhVien {
    @Id
    private String mssv;

    private String ho;
    private String ten;

    @Column(name = "ngay_sinh")
    private LocalDate ngaySinh;

    @Column(name = "gioi_tinh")
    private String gioiTinh;

    @Column(name = "dia_chi")
    private String diaChi;

    private String sdt;
    private String email;

    @Column(name = "pwd_hash")
    private String pwdHash;

    @ManyToOne
    @JoinColumn(name = "lop_id")
    private Lop lop;

    @OneToMany(mappedBy = "sinhVien")
    private List<DangKy> dangKys;

    @OneToMany(mappedBy = "sinhVien")
    private List<HocPhi> hocPhis;

    @OneToMany(mappedBy = "sinhVien")
    private List<TinNhan> tinNhans;

    // Getters and Setters
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

    public String getPwdHash() {
        return pwdHash;
    }

    public void setPwdHash(String pwdHash) {
        this.pwdHash = pwdHash;
    }

    public Lop getLop() {
        return lop;
    }

    public void setLop(Lop lop) {
        this.lop = lop;
    }

    public List<DangKy> getDangKys() {
        return dangKys;
    }

    public void setDangKys(List<DangKy> dangKys) {
        this.dangKys = dangKys;
    }

    public List<HocPhi> getHocPhis() {
        return hocPhis;
    }

    public void setHocPhis(List<HocPhi> hocPhis) {
        this.hocPhis = hocPhis;
    }

    public List<TinNhan> getTinNhans() {
        return tinNhans;
    }

    public void setTinNhans(List<TinNhan> tinNhans) {
        this.tinNhans = tinNhans;
    }

    @Override
    public String toString() {
        return "SinhVien{" +
                "mssv='" + mssv + '\'' +
                ", ho='" + ho + '\'' +
                ", ten='" + ten + '\'' +
                ", ngaySinh=" + ngaySinh +
                ", gioiTinh='" + gioiTinh + '\'' +
                ", diaChi='" + diaChi + '\'' +
                ", sdt='" + sdt + '\'' +
                ", email='" + email + '\'' +
                ", pwdHash='" + pwdHash + '\'' +
                ", lop=" + lop.getId() +
                '}';
    }
}
