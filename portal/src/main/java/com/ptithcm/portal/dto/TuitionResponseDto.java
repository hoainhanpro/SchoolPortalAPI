package com.ptithcm.portal.dto;

import java.util.List;

public class TuitionResponseDto {
    private List<HocPhiDto> tuitionHistory;
    private List<DotDongHocPhiDto> paymentPeriods;
    private List<HocKyDto> availableSemesters;
    
    // Constructors
    public TuitionResponseDto() {}
    
    public TuitionResponseDto(List<HocPhiDto> tuitionHistory, 
                              List<DotDongHocPhiDto> paymentPeriods,
                              List<HocKyDto> availableSemesters) {
        this.tuitionHistory = tuitionHistory;
        this.paymentPeriods = paymentPeriods;
        this.availableSemesters = availableSemesters;
    }
    
    // Getters and Setters
    public List<HocPhiDto> getTuitionHistory() {
        return tuitionHistory;
    }
    
    public void setTuitionHistory(List<HocPhiDto> tuitionHistory) {
        this.tuitionHistory = tuitionHistory;
    }
    
    public List<DotDongHocPhiDto> getPaymentPeriods() {
        return paymentPeriods;
    }
    
    public void setPaymentPeriods(List<DotDongHocPhiDto> paymentPeriods) {
        this.paymentPeriods = paymentPeriods;
    }
    
    public List<HocKyDto> getAvailableSemesters() {
        return availableSemesters;
    }
    
    public void setAvailableSemesters(List<HocKyDto> availableSemesters) {
        this.availableSemesters = availableSemesters;
    }
}
