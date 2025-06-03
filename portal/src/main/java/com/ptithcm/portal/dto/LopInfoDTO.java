package com.ptithcm.portal.dto;

public class LopInfoDTO {
    private Integer id;
    private String ten;

    public LopInfoDTO() {
    }

    public LopInfoDTO(Integer id, String ten) {
        this.id = id;
        this.ten = ten;
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
}