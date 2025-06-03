package com.ptithcm.portal.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "loptinchi")
public class LopTinChi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "mon_id")
    private Mon mon;

    @ManyToOne
    @JoinColumn(name = "hoc_ky_id")
    private HocKy hocKy;

    private Integer nhom;

    @Column(name = "si_so_toi_da")
    private Integer siSoToiDa;

    @OneToMany(mappedBy = "lopTinChi")
    private List<ChiTietGiangDay> chiTietGiangDays;

    @OneToMany(mappedBy = "lopTinChi")
    private List<Lich> lichs;

    @OneToMany(mappedBy = "lopTinChi")
    private List<DangKy> dangKys;

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Mon getMon() {
        return mon;
    }

    public void setMon(Mon mon) {
        this.mon = mon;
    }

    public HocKy getHocKy() {
        return hocKy;
    }

    public void setHocKy(HocKy hocKy) {
        this.hocKy = hocKy;
    }

    public Integer getNhom() {
        return nhom;
    }

    public void setNhom(Integer nhom) {
        this.nhom = nhom;
    }

    public Integer getSiSoToiDa() {
        return siSoToiDa;
    }

    public void setSiSoToiDa(Integer siSoToiDa) {
        this.siSoToiDa = siSoToiDa;
    }

    public List<ChiTietGiangDay> getChiTietGiangDays() {
        return chiTietGiangDays;
    }

    public void setChiTietGiangDays(List<ChiTietGiangDay> chiTietGiangDays) {
        this.chiTietGiangDays = chiTietGiangDays;
    }

    public List<Lich> getLichs() {
        return lichs;
    }

    public void setLichs(List<Lich> lichs) {
        this.lichs = lichs;
    }

    public List<DangKy> getDangKys() {
        return dangKys;
    }

    public void setDangKys(List<DangKy> dangKys) {
        this.dangKys = dangKys;
    }
}
