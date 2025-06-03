package com.ptithcm.portal.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "hocky")
public class HocKy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String ten;

    @Column(name = "nam_hoc")
    private Integer namHoc;

    @Column(name = "thu_tu")
    private Integer thuTu;
    
    @Column(name = "start_date")
    private LocalDate startDate;
    
    @Column(name = "end_date")
    private LocalDate endDate;

    @OneToMany(mappedBy = "hocKy")
    private List<LopTinChi> lopTinChis;

    @OneToMany(mappedBy = "hocKy")
    private List<DotDangKy> dotDangKys;

    @OneToMany(mappedBy = "hocKy")
    private List<DotDongHocPhi> dotDongHocPhis;
    
    @OneToMany(mappedBy = "hocKy")
    private List<HocPhi> hocPhis;


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

    public Integer getNamHoc() {
        return namHoc;
    }

    public void setNamHoc(Integer namHoc) {
        this.namHoc = namHoc;
    }

    public Integer getThuTu() {
        return thuTu;
    }

    public void setThuTu(Integer thuTu) {
        this.thuTu = thuTu;
    }
    
    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public List<LopTinChi> getLopTinChis() {
        return lopTinChis;
    }

    public void setLopTinChis(List<LopTinChi> lopTinChis) {
        this.lopTinChis = lopTinChis;
    }

    public List<DotDangKy> getDotDangKys() {
        return dotDangKys;
    }

    public void setDotDangKys(List<DotDangKy> dotDangKys) {
        this.dotDangKys = dotDangKys;
    }

    public List<DotDongHocPhi> getDotDongHocPhis() {
        return dotDongHocPhis;
    }

    public void setDotDongHocPhis(List<DotDongHocPhi> dotDongHocPhis) {
        this.dotDongHocPhis = dotDongHocPhis;
    }

    public List<HocPhi> getHocPhis() {
        return hocPhis;
    }

    public void setHocPhis(List<HocPhi> hocPhis) {
        this.hocPhis = hocPhis;
    }
}
