package com.ptithcm.portal.dto;

public class KhoaDTO {
    private int id;
    private String ten;

    public KhoaDTO() {
    }

    public KhoaDTO(int id, String ten) {
        this.id = id;
        this.ten = ten;
    }

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
} 