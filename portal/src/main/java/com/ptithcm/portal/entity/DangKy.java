package com.ptithcm.portal.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "dangky", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"sv_id", "lop_tc_id"})
})
public class DangKy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "sv_id")
    private SinhVien sinhVien;

    @ManyToOne
    @JoinColumn(name = "lop_tc_id")
    private LopTinChi lopTinChi;

    @Column(name = "ngay_dang_ky", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime ngayDangKy;

    @Column(name = "trang_thai", columnDefinition = "TEXT DEFAULT 'Chua xac nhan'")
    private String trangThai;

    @Column(name = "diemcc", columnDefinition = "NUMERIC DEFAULT 0")
    private BigDecimal diemcc;

    @Column(name = "diemkt", columnDefinition = "NUMERIC DEFAULT 0")
    private BigDecimal diemkt;

    @Column(name = "diemthi", columnDefinition = "NUMERIC DEFAULT 0")
    private BigDecimal diemthi;

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

    public LopTinChi getLopTinChi() {
        return lopTinChi;
    }

    public void setLopTinChi(LopTinChi lopTinChi) {
        this.lopTinChi = lopTinChi;
    }

    public LocalDateTime getNgayDangKy() {
        return ngayDangKy;
    }

    public void setNgayDangKy(LocalDateTime ngayDangKy) {
        this.ngayDangKy = ngayDangKy;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public BigDecimal getDiemcc() {
        return diemcc;
    }

    public void setDiemcc(BigDecimal diemcc) {
        this.diemcc = diemcc;
    }

    public BigDecimal getDiemkt() {
        return diemkt;
    }

    public void setDiemkt(BigDecimal diemkt) {
        this.diemkt = diemkt;
    }

    public BigDecimal getDiemthi() {
        return diemthi;
    }

    public void setDiemthi(BigDecimal diemthi) {
        this.diemthi = diemthi;
    }

    @PrePersist
    protected void onCreate() {
        if (ngayDangKy == null) {
            ngayDangKy = LocalDateTime.now();
        }
        if (trangThai == null) {
            trangThai = "Chua xac nhan";
        }
        if (diemcc == null) {
            diemcc = BigDecimal.ZERO;
        }
        if (diemkt == null) {
            diemkt = BigDecimal.ZERO;
        }
        if (diemthi == null) {
            diemthi = BigDecimal.ZERO;
        }
    }
}
