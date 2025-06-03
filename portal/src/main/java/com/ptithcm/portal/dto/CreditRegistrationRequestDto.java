package com.ptithcm.portal.dto;

public class CreditRegistrationRequestDto {
    private Integer lopTinChiId;

    public CreditRegistrationRequestDto() {
    }

    public CreditRegistrationRequestDto(Integer lopTinChiId) {
        this.lopTinChiId = lopTinChiId;
    }

    public Integer getLopTinChiId() {
        return lopTinChiId;
    }

    public void setLopTinChiId(Integer lopTinChiId) {
        this.lopTinChiId = lopTinChiId;
    }
}
