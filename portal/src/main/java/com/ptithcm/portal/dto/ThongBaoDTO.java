package com.ptithcm.portal.dto;

import java.time.LocalDateTime;

public class ThongBaoDTO {
    private Integer id;
    private Integer nvId;
    private String tieuDe;
    private String noiDung;
    private LocalDateTime ngayGui;
    private String hoNhanVien;
    private String tenNhanVien;
    private String emailNhanVien;
    private String chucVuNhanVien;

    // Constructors
    public ThongBaoDTO() {
    }

    public ThongBaoDTO(Integer id, Integer nvId, String tieuDe, String noiDung,
            LocalDateTime ngayGui, String hoNhanVien, String tenNhanVien,
            String emailNhanVien, String chucVuNhanVien) {
        this.id = id;
        this.nvId = nvId;
        this.tieuDe = tieuDe;
        this.noiDung = noiDung;
        this.ngayGui = ngayGui;
        this.hoNhanVien = hoNhanVien;
        this.tenNhanVien = tenNhanVien;
        this.emailNhanVien = emailNhanVien;
        this.chucVuNhanVien = chucVuNhanVien;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getNvId() {
        return nvId;
    }

    public void setNvId(Integer nvId) {
        this.nvId = nvId;
    }

    public String getTieuDe() {
        return tieuDe;
    }

    public void setTieuDe(String tieuDe) {
        this.tieuDe = tieuDe;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public LocalDateTime getNgayGui() {
        return ngayGui;
    }

    public void setNgayGui(LocalDateTime ngayGui) {
        this.ngayGui = ngayGui;
    }

    public String getHoNhanVien() {
        return hoNhanVien;
    }

    public void setHoNhanVien(String hoNhanVien) {
        this.hoNhanVien = hoNhanVien;
    }

    public String getTenNhanVien() {
        return tenNhanVien;
    }

    public void setTenNhanVien(String tenNhanVien) {
        this.tenNhanVien = tenNhanVien;
    }

    public String getEmailNhanVien() {
        return emailNhanVien;
    }

    public void setEmailNhanVien(String emailNhanVien) {
        this.emailNhanVien = emailNhanVien;
    }

    public String getChucVuNhanVien() {
        return chucVuNhanVien;
    }

    public void setChucVuNhanVien(String chucVuNhanVien) {
        this.chucVuNhanVien = chucVuNhanVien;
    }
}