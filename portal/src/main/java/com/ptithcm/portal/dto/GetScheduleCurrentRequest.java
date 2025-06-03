package com.ptithcm.portal.dto;

public class GetScheduleCurrentRequest {
    private String studentId;
    private String email; 
    private String userType; 
    private String lecturerId; 
    private Integer weekOffset = 0; // Ensured: weekOffset field, defaults to 0

    public GetScheduleCurrentRequest() {
    }

    // Constructor for student by ID
    public GetScheduleCurrentRequest(String studentId, String userType) {
        this.studentId = studentId;
        this.userType = userType;
    }

    // Overloaded constructor for email (could be student or lecturer)
    public GetScheduleCurrentRequest(String identifier, String userType, boolean isEmail) {
        this.userType = userType;
        if (isEmail) {
            this.email = identifier;
        } else {
            if ("student".equalsIgnoreCase(userType)) {
                this.studentId = identifier;
            } else if ("lecturer".equalsIgnoreCase(userType)) {
                this.lecturerId = identifier;
            }
        }
    }

    public GetScheduleCurrentRequest(String userCode, String userType, Integer weekOffset) {
        this.userType = userType;
        if ("student".equalsIgnoreCase(userType)) {
            this.studentId = userCode;
        } else if ("lecturer".equalsIgnoreCase(userType)) {
            this.lecturerId = userCode;
        }
        this.weekOffset = (weekOffset == null) ? 0 : weekOffset;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getLecturerId() {
        return lecturerId;
    }

    public void setLecturerId(String lecturerId) {
        this.lecturerId = lecturerId;
    }

    // Ensured: Getter for weekOffset
    public Integer getWeekOffset() {
        return (this.weekOffset == null) ? 0 : this.weekOffset;
    }

    // Ensured: Setter for weekOffset
    public void setWeekOffset(Integer weekOffset) {
        this.weekOffset = (weekOffset == null) ? 0 : weekOffset;
    }
}
