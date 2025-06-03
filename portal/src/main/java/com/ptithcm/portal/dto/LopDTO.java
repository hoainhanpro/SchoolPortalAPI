package com.ptithcm.portal.dto;

public class LopDTO {
    private Integer id;
    private String ten;
    private KhoaDTO khoa;

    public LopDTO() {
    }

    public LopDTO(Integer id, String ten, KhoaDTO khoa) {
        this.id = id;
        this.ten = ten;
        this.khoa = khoa;
    }

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

    public KhoaDTO getKhoa() {
        return khoa;
    }

    public void setKhoa(KhoaDTO khoa) {
        this.khoa = khoa;
    }
} 