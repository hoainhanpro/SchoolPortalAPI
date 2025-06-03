package com.ptithcm.portal.dto;

import com.ptithcm.portal.entity.Mon;

public class MonDTO {
    private Integer id;
    private String tenMon;
    private Integer soTinChi;
    private Integer khoa_id;
    private String ten_khoa;

    // Constructor rỗng
    public MonDTO() {
    }

    // Constructor với các tham số
    public MonDTO(Integer id, String tenMon, Integer soTinChi, Integer khoa_id, String ten_khoa) {
        this.id = id;
        this.tenMon = tenMon;
        this.soTinChi = soTinChi;
        this.khoa_id = khoa_id;
        this.ten_khoa = ten_khoa;
    }

    // Constructor chuyển đổi từ entity Mon
    public MonDTO(Mon mon) {
        this.id = mon.getId();
        this.tenMon = mon.getTenMon();
        this.soTinChi = mon.getSoTinChi();

        if (mon.getKhoa() != null) {
            this.khoa_id = mon.getKhoa().getId();
            this.ten_khoa = mon.getKhoa().getTen();
        }
    }

    // Getters và Setters
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

    public Integer getKhoa_id() {
        return khoa_id;
    }

    public void setKhoa_id(Integer khoa_id) {
        this.khoa_id = khoa_id;
    }

    public String getTen_khoa() {
        return ten_khoa;
    }

    public void setTen_khoa(String ten_khoa) {
        this.ten_khoa = ten_khoa;
    }
}