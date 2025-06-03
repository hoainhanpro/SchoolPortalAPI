package com.ptithcm.portal.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "covan")
public class CoVan {
    @EmbeddedId
    private CoVanId id;

    @ManyToOne
    @MapsId("lopId")
    @JoinColumn(name = "lop_id")
    private Lop lop;

    @ManyToOne
    @MapsId("nvId")
    @JoinColumn(name = "nv_id")
    private NhanVien nhanVien;

    // Getters and Setters
    public CoVanId getId() {
        return id;
    }

    public void setId(CoVanId id) {
        this.id = id;
    }

    public Lop getLop() {
        return lop;
    }

    public void setLop(Lop lop) {
        this.lop = lop;
    }

    public NhanVien getNhanVien() {
        return nhanVien;
    }

    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
    }
}
