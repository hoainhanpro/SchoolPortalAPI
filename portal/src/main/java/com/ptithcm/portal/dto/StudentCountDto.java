package com.ptithcm.portal.dto;

public class StudentCountDto {
    private Integer studentCount;
    private Integer lecturerId;
    private Integer semesterId;

    public StudentCountDto(Integer studentCount, Integer lecturerId, Integer semesterId) {
        this.studentCount = studentCount;
        this.lecturerId = lecturerId;
        this.semesterId = semesterId;
    }

    public Integer getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(Integer studentCount) {
        this.studentCount = studentCount;
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