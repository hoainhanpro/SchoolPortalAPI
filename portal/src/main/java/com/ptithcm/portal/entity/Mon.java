package com.ptithcm.portal.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "mon")
public class Mon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ten_mon")
    private String tenMon;

    @Column(name = "so_tin_chi")
    private Integer soTinChi;

    @ManyToOne
    @JoinColumn(name = "khoa_id")
    private Khoa khoa;

    @OneToMany(mappedBy = "mon")
    private List<LopTinChi> lopTinChis;

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTenMon() {
        return tenMon;
    }

    public void setTenMon(String tenMon) {
        this.tenMon = tenMon;
    }

    public Integer getSoTinChi() {
        return soTinChi;
    }

    public void setSoTinChi(Integer soTinChi) {
        this.soTinChi = soTinChi;
    }

    public Khoa getKhoa() {
        return khoa;
    }

    public void setKhoa(Khoa khoa) {
        this.khoa = khoa;
    }

    public List<LopTinChi> getLopTinChis() {
        return lopTinChis;
    }

    public void setLopTinChis(List<LopTinChi> lopTinChis) {
        this.lopTinChis = lopTinChis;
    }
}
