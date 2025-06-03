package com.ptithcm.portal.dto;

public class ClassCountDto {
    private Integer classCount;
    private Integer lecturerId;
    private Integer semesterId;

    public ClassCountDto(Integer classCount, Integer lecturerId, Integer semesterId) {
        this.classCount = classCount;
        this.lecturerId = lecturerId;
        this.semesterId = semesterId;
    }

    public Integer getClassCount() {
        return classCount;
    }

    public void setClassCount(Integer classCount) {
        this.classCount = classCount;
    }

    public Integer getLecturerId() {
        return lecturerId;
    }

    public void setLecturerId(Integer lecturerId) {
        this.lecturerId = lecturerId;
    }

    public Integer getSemesterId() {
        return semesterId;
    }

    public void setSemesterId(Integer semesterId) {
        this.semesterId = semesterId;
    }
} 