package com.ptithcm.portal.dto;

import java.math.BigDecimal;

public class SinhVienDiemDto {
    private Integer dangKyId;
    private String mssv;
    private String ho;
    private String ten;
    private String hoTen;
    private String tenLop;
    private BigDecimal diemCC;
    private BigDecimal diemKT;
    private BigDecimal diemThi;
    private BigDecimal diemTB;
    
    public SinhVienDiemDto() {
    }
    
    public SinhVienDiemDto(Integer dangKyId, String mssv, String ho, String ten, 
                          String tenLop, BigDecimal diemCC, BigDecimal diemKT, 
                          BigDecimal diemThi) {
        this.dangKyId = dangKyId;
        this.mssv = mssv;
        this.ho = ho;
        this.ten = ten;
        this.hoTen = ho + " " + ten;
        this.tenLop = tenLop;
        this.diemCC = diemCC;
        this.diemKT = diemKT;
        this.diemThi = diemThi;
        this.calculateDiemTB();
    }
    
    // Tính điểm trung bình (10% chuyên cần, 20% kiểm tra, 70% thi)
    public void calculateDiemTB() {
        if (diemCC != null && diemKT != null && diemThi != null) {
            BigDecimal weight1 = new BigDecimal("0.1");
            BigDecimal weight2 = new BigDecimal("0.2");
            BigDecimal weight3 = new BigDecimal("0.7");
            
            this.diemTB = diemCC.multiply(weight1)
                         .add(diemKT.multiply(weight2))
                         .add(diemThi.multiply(weight3));
        } else {
            this.diemTB = BigDecimal.ZERO;
        }
    }
    
    // Getters và Setters
    public Integer getDangKyId() {
        return dangKyId;
    }
    
    public void setDangKyId(Integer dangKyId) {
        this.dangKyId = dangKyId;
    }
    
    public String getMssv() {
        return mssv;
    }
    
    public void setMssv(String mssv) {
        this.mssv = mssv;
    }
    
    public String getHo() {
        return ho;
    }
    
    public void setHo(String ho) {
        this.ho = ho;
        this.hoTen = this.ho + " " + this.ten;
    }
    
    public String getTen() {
        return ten;
    }
    
    public void setTen(String ten) {
        this.ten = ten;
        this.hoTen = this.ho + " " + this.ten;
    }
    
    public String getHoTen() {
        return hoTen;
    }
    
    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }
    
    public String getTenLop() {
        return tenLop;
    }
    
    public void setTenLop(String tenLop) {
        this.tenLop = tenLop;
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
} 