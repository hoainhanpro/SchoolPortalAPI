package com.ptithcm.portal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ChiTietGiangDayId implements Serializable {
    @Column(name = "lop_tc_id")
    private Integer lopTcId;

    @Column(name = "nv_id")
    private Integer nvId;

    // Constructors
    public ChiTietGiangDayId() {
    }

    public ChiTietGiangDayId(Integer lopTcId, Integer nvId) {
        this.lopTcId = lopTcId;
        this.nvId = nvId;
    }

    // Getters and Setters
    public Integer getLopTcId() {
        return lopTcId;
    }

    public void setLopTcId(Integer lopTcId) {
        this.lopTcId = lopTcId;
    }

    public Integer getNvId() {
        return nvId;
    }

    public void setNvId(Integer nvId) {
        this.nvId = nvId;
    }

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChiTietGiangDayId that = (ChiTietGiangDayId) o;
        return Objects.equals(lopTcId, that.lopTcId) &&
               Objects.equals(nvId, that.nvId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lopTcId, nvId);
    }
}
