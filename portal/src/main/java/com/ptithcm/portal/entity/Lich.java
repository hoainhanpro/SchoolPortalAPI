package com.ptithcm.portal.entity;

import jakarta.persistence.*;

import java.sql.Date;

@Entity
@Table(name = "lich", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"lop_tc_id", "buoi_id", "thu"})
})
public class Lich {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "lop_tc_id")
    private LopTinChi lopTinChi;

    @ManyToOne
    @JoinColumn(name = "buoi_id")
    private Buoi buoi;

    @Column(name = "ngayhoc")
    private Date ngayHoc;

    @ManyToOne
    @JoinColumn(name = "phong_hoc_id")
    private PhongHoc phongHoc;

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LopTinChi getLopTinChi() {
        return lopTinChi;
    }

    public void setLopTinChi(LopTinChi lopTinChi) {
        this.lopTinChi = lopTinChi;
    }

    public Buoi getBuoi() {
        return buoi;
    }

    public void setBuoi(Buoi buoi) {
        this.buoi = buoi;
    }

    public Date getNgayHoc() {
        return ngayHoc;
    }

    public void setNgayHoc(Date ngayHoc) {
        this.ngayHoc = ngayHoc;
    }

    public PhongHoc getPhongHoc() {
        return phongHoc;
    }

    public void setPhongHoc(PhongHoc phongHoc) {
        this.phongHoc = phongHoc;
    }

    @Override
    public String toString() {
        return "Lich{" +
                "id=" + id +
                ", lopTinChi=" + lopTinChi.getId() +
                ", giobd=" + buoi.getGioBatDau() +
                ", giokt=" + buoi.getGioKetThuc() +
                ", ngayHoc=" + ngayHoc +
                ", phong=" + phongHoc.getTen() +
                '}';
    }
}
