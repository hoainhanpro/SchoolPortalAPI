package com.ptithcm.portal.dto;

import java.time.LocalDate;

public class SemesterInfoDto {
    private Integer id;
    private String name;
    private Integer namHoc;
    private Integer thuTu;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean isCurrentSemester;
    
    public SemesterInfoDto() {
    }

    public SemesterInfoDto(Integer id, String name, Integer namHoc, Integer thuTu, 
                         LocalDate startDate, LocalDate endDate, boolean isCurrentSemester) {
        this.id = id;
        this.name = name;
        this.namHoc = namHoc;
        this.thuTu = thuTu;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isCurrentSemester = isCurrentSemester;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getNamHoc() {
        return namHoc;
    }

    public void setNamHoc(Integer namHoc) {
        this.namHoc = namHoc;
    }

    public Integer getThuTu() {
        return thuTu;
    }

    public void setThuTu(Integer thuTu) {
        this.thuTu = thuTu;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public boolean isCurrentSemester() {
        return isCurrentSemester;
    }

    public void setCurrentSemester(boolean currentSemester) {
        isCurrentSemester = currentSemester;
    }
} 