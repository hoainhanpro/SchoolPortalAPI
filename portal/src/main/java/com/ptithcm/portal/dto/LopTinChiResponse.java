package com.ptithcm.portal.dto;

public class LopTinChiResponse {
    private Integer id;
    private String tenMon;
    private String tenHocKy;
    private Integer nhom;
    private Integer siSoToiDa;
    private String scheduleInfo; // Thông tin lịch học
    private String message;
    private boolean success;
    
    public LopTinChiResponse() {
    }
    
    public LopTinChiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public LopTinChiResponse(Integer id, String tenMon, String tenHocKy, Integer nhom, Integer siSoToiDa) {
        this.id = id;
        this.tenMon = tenMon;
        this.tenHocKy = tenHocKy;
        this.nhom = nhom;
        this.siSoToiDa = siSoToiDa;
        this.success = true;
    }
    
    public LopTinChiResponse(Integer id, String tenMon, String tenHocKy, Integer nhom, Integer siSoToiDa, String scheduleInfo) {
        this.id = id;
        this.tenMon = tenMon;
        this.tenHocKy = tenHocKy;
        this.nhom = nhom;
        this.siSoToiDa = siSoToiDa;
        this.scheduleInfo = scheduleInfo;
        this.success = true;
    }
    
    // Getters and Setters
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
    
    public String getTenHocKy() {
        return tenHocKy;
    }
    
    public void setTenHocKy(String tenHocKy) {
        this.tenHocKy = tenHocKy;
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
    
    public String getScheduleInfo() {
        return scheduleInfo;
    }
    
    public void setScheduleInfo(String scheduleInfo) {
        this.scheduleInfo = scheduleInfo;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
} 