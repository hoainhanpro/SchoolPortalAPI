package com.ptithcm.portal.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RegisteredCreditClassDto {
    private Integer dangKyId;
    private Integer lopTinChiId;
    private String monTen;
    private Integer monSoTinChi;
    private Integer nhom;
    private String hocKyTen;
    private Integer hocKyNamHoc;
    private String trangThaiDangKy;
    private LocalDateTime ngayDangKy;
    private String giangVien; // e.g., "Nguyen Van A, Tran Thi B"
    private BigDecimal diemCC;
    private BigDecimal diemKT;
    private BigDecimal diemThi;
    // Add other relevant fields like schedule if needed later

    public RegisteredCreditClassDto() {
    }

    public RegisteredCreditClassDto(Integer dangKyId, Integer lopTinChiId, String monTen, Integer monSoTinChi,
                                    Integer nhom, String hocKyTen, Integer hocKyNamHoc, String trangThaiDangKy,
                                    LocalDateTime ngayDangKy, String giangVien, BigDecimal diemCC, BigDecimal diemKT, BigDecimal diemThi) {
        this.dangKyId = dangKyId;
        this.lopTinChiId = lopTinChiId;
        this.monTen = monTen;
        this.monSoTinChi = monSoTinChi;
        this.nhom = nhom;
        this.hocKyTen = hocKyTen;
        this.hocKyNamHoc = hocKyNamHoc;
        this.trangThaiDangKy = trangThaiDangKy;
        this.ngayDangKy = ngayDangKy;
        this.giangVien = giangVien;
        this.diemCC = diemCC;
        this.diemKT = diemKT;
        this.diemThi = diemThi;
    }

    // Getters and Setters
    public Integer getDangKyId() {
        return dangKyId;
    }

    public void setDangKyId(Integer dangKyId) {
        this.dangKyId = dangKyId;
    }

    public Integer getLopTinChiId() {
        return lopTinChiId;
    }

    public void setLopTinChiId(Integer lopTinChiId) {
        this.lopTinChiId = lopTinChiId;
    }

    public String getMonTen() {
        return monTen;
    }

    public void setMonTen(String monTen) {
        this.monTen = monTen;
    }

    public Integer getMonSoTinChi() {
        return monSoTinChi;
    }

    public void setMonSoTinChi(Integer monSoTinChi) {
        this.monSoTinChi = monSoTinChi;
    }

    public Integer getNhom() {
        return nhom;
    }

    public void setNhom(Integer nhom) {
        this.nhom = nhom;
    }

    public String getHocKyTen() {
        return hocKyTen;
    }

    public void setHocKyTen(String hocKyTen) {
        this.hocKyTen = hocKyTen;
    }

    public Integer getHocKyNamHoc() {
        return hocKyNamHoc;
    }

    public void setHocKyNamHoc(Integer hocKyNamHoc) {
        this.hocKyNamHoc = hocKyNamHoc;
    }

    public String getTrangThaiDangKy() {
        return trangThaiDangKy;
    }

    public void setTrangThaiDangKy(String trangThaiDangKy) {
        this.trangThaiDangKy = trangThaiDangKy;
    }

    public LocalDateTime getNgayDangKy() {
        return ngayDangKy;
    }

    public void setNgayDangKy(LocalDateTime ngayDangKy) {
        this.ngayDangKy = ngayDangKy;
    }

    public String getGiangVien() {
        return giangVien;
    }

    public void setGiangVien(String giangVien) {
        this.giangVien = giangVien;
    }

    public BigDecimal getDiemCC() {
        return diemCC;
    }

    public void setDiemCC(BigDecimal diemCC) {
        this.diemCC = diemCC;
    }

    public BigDecimal getDiemKT() {
        return diemKT;
    }

    public void setDiemKT(BigDecimal diemKT) {
        this.diemKT = diemKT;
    }

    public BigDecimal getDiemThi() {
        return diemThi;
    }

    public void setDiemThi(BigDecimal diemThi) {
        this.diemThi = diemThi;
    }
}
