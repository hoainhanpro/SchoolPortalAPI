package com.ptithcm.portal.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "lop")
public class Lop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String ten;

    @ManyToOne
    @JoinColumn(name = "khoa_id")
    private Khoa khoa;

    @OneToMany(mappedBy = "lop")
    private List<SinhVien> sinhViens;

    @OneToMany(mappedBy = "lop")
    private List<CoVan> coVans;

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTen() {
        return ten;
    }

    public void setTen(String ten) {
        this.ten = ten;
    }

    public Khoa getKhoa() {
        return khoa;
    }

    public void setKhoa(Khoa khoa) {
        this.khoa = khoa;
    }

    public List<SinhVien> getSinhViens() {
        return sinhViens;
    }

    public void setSinhViens(List<SinhVien> sinhViens) {
        this.sinhViens = sinhViens;
    }

    public List<CoVan> getCoVans() {
        return coVans;
    }

    public void setCoVans(List<CoVan> coVans) {
        this.coVans = coVans;
    }
}
