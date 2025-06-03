package com.ptithcm.portal.dto;

public class LopTinChiDto {
    private Integer id;
    private String tenMon;
    private Integer soTinChi;
    private Integer nhom;
    private Integer siSoToiDa;
    private Integer siSoDangKy;
    private String tenHocKy;
    private Integer namHoc;
    
    public LopTinChiDto() {
    }
    
    public LopTinChiDto(Integer id, String tenMon, Integer soTinChi, Integer nhom, 
                        Integer siSoToiDa, Integer siSoDangKy, String tenHocKy, Integer namHoc) {
        this.id = id;
        this.tenMon = tenMon;
        this.soTinChi = soTinChi;
        this.nhom = nhom;
        this.siSoToiDa = siSoToiDa;
        this.siSoDangKy = siSoDangKy;
        this.tenHocKy = tenHocKy;
        this.namHoc = namHoc;
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
    
    public Integer getSiSoDangKy() {
        return siSoDangKy;
    }
    
    public void setSiSoDangKy(Integer siSoDangKy) {
        this.siSoDangKy = siSoDangKy;
    }
    
    public String getTenHocKy() {
        return tenHocKy;
    }
    
    public void setTenHocKy(String tenHocKy) {
        this.tenHocKy = tenHocKy;
    }
    
    public Integer getNamHoc() {
        return namHoc;
    }
    
    public void setNamHoc(Integer namHoc) {
        this.namHoc = namHoc;
    }
} 