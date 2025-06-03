package com.ptithcm.portal.dto;

public class ScheduleEntryDto {
    private String time;
    private String subject;
    private String room;
    private String teacher; // Added teacher field
    private String classCode; // Added classCode field

    public ScheduleEntryDto(String time, String subject, String room, String teacher, String classCode) { // Updated constructor
        this.time = time;
        this.subject = subject;
        this.room = room;
        this.teacher = teacher;
        this.classCode = classCode;
    }

    public String getTime() {
        return time;
    }

    public String getSubject() {
        return subject;
    }

    public String getRoom() {
        return room;
    }

    public String getTeacher() { // Added getter for teacher
        return teacher;
    }
    
    public String getClassCode() { // Added getter for classCode
        return classCode;
    }
}
