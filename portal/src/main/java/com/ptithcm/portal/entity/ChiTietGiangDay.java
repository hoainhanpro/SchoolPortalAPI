package com.ptithcm.portal.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "chitietgiangday")
public class ChiTietGiangDay {
    @EmbeddedId
    private ChiTietGiangDayId id;

    @ManyToOne
    @MapsId("lopTcId")
    @JoinColumn(name = "lop_tc_id")
    private LopTinChi lopTinChi;

    @ManyToOne
    @MapsId("nvId")
    @JoinColumn(name = "nv_id")
    private NhanVien nhanVien;

    private String loai;

    // Getters and Setters
    public ChiTietGiangDayId getId() {
        return id;
    }

    public void setId(ChiTietGiangDayId id) {
        this.id = id;
    }

    public LopTinChi getLopTinChi() {
        return lopTinChi;
    }

    public void setLopTinChi(LopTinChi lopTinChi) {
        this.lopTinChi = lopTinChi;
    }

    public NhanVien getNhanVien() {
        return nhanVien;
    }

    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
    }

    public String getLoai() {
        return loai;
    }

    public void setLoai(String loai) {
        this.loai = loai;
    }
}
