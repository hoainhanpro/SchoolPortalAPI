package com.ptithcm.portal.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "hocphi")
public class HocPhi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "sv_id")
    private SinhVien sinhVien;

    @ManyToOne
    @JoinColumn(name = "hoc_ky_id")
    private HocKy hocKy;

    @Column(name = "tong_tien")
    private BigDecimal tongTien;

    @Column(name = "da_dong", columnDefinition = "NUMERIC DEFAULT 0")
    private BigDecimal daDong;

    @Column(name = "ngay_dong")
    private LocalDate ngayDong;

    @Column(name = "trang_thai", columnDefinition = "TEXT DEFAULT 'Chua dong'")
    private String trangThai;

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public SinhVien getSinhVien() {
        return sinhVien;
    }

    public void setSinhVien(SinhVien sinhVien) {
        this.sinhVien = sinhVien;
    }

    public HocKy getHocKy() {
        return hocKy;
    }

    public void setHocKy(HocKy hocKy) {
        this.hocKy = hocKy;
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

    @PrePersist
    protected void onCreate() {
        if (this.tongTien == null) {
            this.tongTien = BigDecimal.ZERO;
        }
        if (this.daDong == null) {
            this.daDong = BigDecimal.ZERO;
        }
        if (this.trangThai == null) {
            this.trangThai = "Chua dong";
        }
    }
}
