package com.ptithcm.portal.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "nhanvien")
public class NhanVien {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

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

    @Column(name = "chuc_vu")
    private String chucVu;

    @ManyToOne
    @JoinColumn(name = "khoa_id")
    private Khoa khoa;

    @OneToMany(mappedBy = "nhanVien")
    private List<CoVan> coVans;

    @OneToMany(mappedBy = "nhanVien")
    private List<ChiTietGiangDay> chiTietGiangDays;

    @OneToMany(mappedBy = "nhanVien")
    private List<ThongBao> thongBaos;

    @OneToMany(mappedBy = "nhanVien")
    private List<TinNhan> tinNhans;

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

    public String getChucVu() {
        return chucVu;
    }

    public void setChucVu(String chucVu) {
        this.chucVu = chucVu;
    }

    public Khoa getKhoa() {
        return khoa;
    }

    public void setKhoa(Khoa khoa) {
        this.khoa = khoa;
    }

    public List<CoVan> getCoVans() {
        return coVans;
    }

    public void setCoVans(List<CoVan> coVans) {
        this.coVans = coVans;
    }

    public List<ChiTietGiangDay> getChiTietGiangDays() {
        return chiTietGiangDays;
    }

    public void setChiTietGiangDays(List<ChiTietGiangDay> chiTietGiangDays) {
        this.chiTietGiangDays = chiTietGiangDays;
    }

    public List<ThongBao> getThongBaos() {
        return thongBaos;
    }

    public void setThongBaos(List<ThongBao> thongBaos) {
        this.thongBaos = thongBaos;
    }

    public List<TinNhan> getTinNhans() {
        return tinNhans;
    }

    public void setTinNhans(List<TinNhan> tinNhans) {
        this.tinNhans = tinNhans;
    }
}
