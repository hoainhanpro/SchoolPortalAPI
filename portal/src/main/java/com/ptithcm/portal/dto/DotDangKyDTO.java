package com.ptithcm.portal.dto;

import com.ptithcm.portal.entity.DotDangKy;
import com.ptithcm.portal.entity.HocKy;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DotDangKyDTO {
    private Integer id;
    private Integer hocKyId;
    private String hocKyTen;
    private Integer hocKyNamHoc;
    private Integer hocKyThuTu;
    private LocalDateTime thoiGianBatDau;
    private LocalDateTime thoiGianKetThuc;
    private String thoiGianBatDauFormatted;
    private String thoiGianKetThucFormatted;

    // Constructor từ Entity
    public DotDangKyDTO(DotDangKy dotDangKy) {
        this.id = dotDangKy.getId();

        // Lấy thông tin từ HocKy
        HocKy hocKy = dotDangKy.getHocKy();
        if (hocKy != null) {
            this.hocKyId = hocKy.getId();
            this.hocKyTen = hocKy.getTen();
            this.hocKyNamHoc = hocKy.getNamHoc();
            this.hocKyThuTu = hocKy.getThuTu();
        }

        // Lưu thời gian dạng LocalDateTime để dễ xử lý
        this.thoiGianBatDau = dotDangKy.getThoiGianBatDau();
        this.thoiGianKetThuc = dotDangKy.getThoiGianKetThuc();

        // Format thời gian để hiển thị
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (this.thoiGianBatDau != null) {
            this.thoiGianBatDauFormatted = this.thoiGianBatDau.format(formatter);
        }
        if (this.thoiGianKetThuc != null) {
            this.thoiGianKetThucFormatted = this.thoiGianKetThuc.format(formatter);
        }
    }

    // Empty constructor for deserialization
    public DotDangKyDTO() {
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getHocKyId() {
        return hocKyId;
    }

    public void setHocKyId(Integer hocKyId) {
        this.hocKyId = hocKyId;
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

    public Integer getHocKyThuTu() {
        return hocKyThuTu;
    }

    public void setHocKyThuTu(Integer hocKyThuTu) {
        this.hocKyThuTu = hocKyThuTu;
    }

    public LocalDateTime getThoiGianBatDau() {
        return thoiGianBatDau;
    }

    public void setThoiGianBatDau(LocalDateTime thoiGianBatDau) {
        this.thoiGianBatDau = thoiGianBatDau;
        if (thoiGianBatDau != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            this.thoiGianBatDauFormatted = thoiGianBatDau.format(formatter);
        }
    }

    public LocalDateTime getThoiGianKetThuc() {
        return thoiGianKetThuc;
    }

    public void setThoiGianKetThuc(LocalDateTime thoiGianKetThuc) {
        this.thoiGianKetThuc = thoiGianKetThuc;
        if (thoiGianKetThuc != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            this.thoiGianKetThucFormatted = thoiGianKetThuc.format(formatter);
        }
    }

    public String getThoiGianBatDauFormatted() {
        return thoiGianBatDauFormatted;
    }

    public String getThoiGianKetThucFormatted() {
        return thoiGianKetThucFormatted;
    }
}