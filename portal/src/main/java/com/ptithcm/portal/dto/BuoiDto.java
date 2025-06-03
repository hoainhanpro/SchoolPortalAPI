package com.ptithcm.portal.dto;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class BuoiDto {
    private Integer id;
    private String ten;
    private String startTime;
    private String endTime;
    
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    
    public BuoiDto() {
    }
    
    public BuoiDto(Integer id, LocalTime gioBatDau, LocalTime gioKetThuc) {
        this.id = id;
        
        // Đặt tên buổi dựa vào thời gian bắt đầu
        if (gioBatDau.getHour() < 12) {
            this.ten = "Buổi sáng";
        } else if (gioBatDau.getHour() < 18) {
            this.ten = "Buổi chiều";
        } else {
            this.ten = "Buổi tối";
        }
        
        // Format giờ bắt đầu và kết thúc
        this.startTime = gioBatDau.format(TIME_FORMATTER);
        this.endTime = gioKetThuc.format(TIME_FORMATTER);
    }
    
    // Getters and Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getTen() {
        return ten;
    }
    
    public void setTen(String ten) {
        this.ten = ten;
    }
    
    public String getStartTime() {
        return startTime;
    }
    
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
    
    public String getEndTime() {
        return endTime;
    }
    
    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
} 