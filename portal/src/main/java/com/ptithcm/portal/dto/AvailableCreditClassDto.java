package com.ptithcm.portal.dto;

public class AvailableCreditClassDto {
    private Integer lopTinChiId;
    private String monTen;
    private Integer monSoTinChi;
    private Integer nhom;
    private Integer siSoToiDa;
    private Long daDangKy;
    private String giangVien; // Simplified: e.g., "Nguyen Van A"
    // private List<ScheduleDetailDto> lichHoc; // For later enhancement

    public AvailableCreditClassDto() {}

    public AvailableCreditClassDto(Integer lopTinChiId, String monTen, Integer monSoTinChi, Integer nhom, Integer siSoToiDa, Long daDangKy, String giangVien) {
        this.lopTinChiId = lopTinChiId;
        this.monTen = monTen;
        this.monSoTinChi = monSoTinChi;
        this.nhom = nhom;
        this.siSoToiDa = siSoToiDa;
        this.daDangKy = daDangKy;
        this.giangVien = giangVien;
    }

    // Getters and Setters
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

    public Integer getSiSoToiDa() {
        return siSoToiDa;
    }

    public void setSiSoToiDa(Integer siSoToiDa) {
        this.siSoToiDa = siSoToiDa;
    }

    public Long getDaDangKy() {
        return daDangKy;
    }

    public void setDaDangKy(Long daDangKy) {
        this.daDangKy = daDangKy;
    }

    public String getGiangVien() {
        return giangVien;
    }

    public void setGiangVien(String giangVien) {
        this.giangVien = giangVien;
    }
}
