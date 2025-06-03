package com.ptithcm.portal.dto;

public class LecturerDto {
    private Integer id;
    private String ho;
    private String ten;
    private String fullName;
    private String email;
    private String khoaName;

    public LecturerDto(Integer id, String ho, String ten, String email, String khoaName) {
        this.id = id;
        this.ho = ho;
        this.ten = ten;
        this.fullName = ho + " " + ten;
        this.email = email;
        this.khoaName = khoaName;
    }

    public Integer getId() {
        return id;
    }

    public String getHo() {
        return ho;
    }

    public String getTen() {
        return ten;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getKhoaName() {
        return khoaName;
    }
} 