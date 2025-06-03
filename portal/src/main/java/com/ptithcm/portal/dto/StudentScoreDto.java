package com.ptithcm.portal.dto;

import java.math.BigDecimal;

public class StudentScoreDto {
    private String mssv;
    private String hoTen;
    private String maMon;
    private String tenMon;
    private String hocKy;
    private String namHoc;
    private BigDecimal diemCC;    // Attendance score
    private BigDecimal diemKT;    // Midterm score
    private BigDecimal diemThi;   // Final exam score
    private BigDecimal diemTB;    // Average score
    private Integer soTinChi;     // Credit hours

    public StudentScoreDto() {
    }

    public StudentScoreDto(String mssv, String hoTen, String maMon, String tenMon, 
                          String hocKy, String namHoc, Integer soTinChi,
                          BigDecimal diemCC, BigDecimal diemKT, BigDecimal diemThi) {
        this.mssv = mssv;
        this.hoTen = hoTen;
        this.maMon = maMon;
        this.tenMon = tenMon;
        this.hocKy = hocKy;
        this.namHoc = namHoc;
        this.soTinChi = soTinChi;
        this.diemCC = diemCC;
        this.diemKT = diemKT;
        this.diemThi = diemThi;
        this.calculateDiemTB();
    }

    // Calculate average score (10% attendance, 20% midterm, 70% final)
    public void calculateDiemTB() {
        if (diemCC != null && diemKT != null && diemThi != null) {
            BigDecimal weight1 = new BigDecimal("0.1");
            BigDecimal weight2 = new BigDecimal("0.2");
            BigDecimal weight3 = new BigDecimal("0.7");
            
            this.diemTB = diemCC.multiply(weight1)
                         .add(diemKT.multiply(weight2))
                         .add(diemThi.multiply(weight3));
        } else {
            this.diemTB = null;
        }
    }

    // Getters and Setters
    public String getMssv() {
        return mssv;
    }

    public void setMssv(String mssv) {
        this.mssv = mssv;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getMaMon() {
        return maMon;
    }

    public void setMaMon(String maMon) {
        this.maMon = maMon;
    }

    public String getTenMon() {
        return tenMon;
    }

    public void setTenMon(String tenMon) {
        this.tenMon = tenMon;
    }

    public String getHocKy() {
        return hocKy;
    }

    public void setHocKy(String hocKy) {
        this.hocKy = hocKy;
    }

    public String getNamHoc() {
        return namHoc;
    }

    public void setNamHoc(String namHoc) {
        this.namHoc = namHoc;
    }

    public BigDecimal getDiemCC() {
        return diemCC;
    }

    public void setDiemCC(BigDecimal diemCC) {
        this.diemCC = diemCC;
        this.calculateDiemTB();
    }

    public BigDecimal getDiemKT() {
        return diemKT;
    }

    public void setDiemKT(BigDecimal diemKT) {
        this.diemKT = diemKT;
        this.calculateDiemTB();
    }

    public BigDecimal getDiemThi() {
        return diemThi;
    }

    public void setDiemThi(BigDecimal diemThi) {
        this.diemThi = diemThi;
        this.calculateDiemTB();
    }

    public BigDecimal getDiemTB() {
        return diemTB;
    }

    public void setDiemTB(BigDecimal diemTB) {
        this.diemTB = diemTB;
    }

    public Integer getSoTinChi() {
        return soTinChi;
    }

    public void setSoTinChi(Integer soTinChi) {
        this.soTinChi = soTinChi;
    }
}
