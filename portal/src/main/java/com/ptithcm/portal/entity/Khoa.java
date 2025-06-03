package com.ptithcm.portal.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.List;

@Entity
@Table(name = "khoa")
public class Khoa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String ten;

    @OneToMany(mappedBy = "khoa")
    private List<Lop> lops;

    @OneToMany(mappedBy = "khoa")
    private List<NhanVien> nhanViens;

    @OneToMany(mappedBy = "khoa")
    private List<Mon> mons;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTen() {
        return ten;
    }

    public void setTen(String ten) {
        this.ten = ten;
    }

    public List<Lop> getLops() {
        return lops;
    }

    public void setLops(List<Lop> lops) {
        this.lops = lops;
    }

    public List<NhanVien> getNhanViens() {
        return nhanViens;
    }

    public void setNhanViens(List<NhanVien> nhanViens) {
        this.nhanViens = nhanViens;
    }

    public List<Mon> getMons() {
        return mons;
    }

    public void setMons(List<Mon> mons) {
        this.mons = mons;
    }

    @Override
    public String toString() {
        return "{" +
                "\"id\":" + id +
                ",\"ten\":\"" + ten + "\"" +
                "}";
    }
}
