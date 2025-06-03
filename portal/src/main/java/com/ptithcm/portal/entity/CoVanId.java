package com.ptithcm.portal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class CoVanId implements Serializable {
    @Column(name = "lop_id")
    private Integer lopId;

    @Column(name = "nv_id")
    private Integer nvId;

    @Column(name = "nam_hoc")
    private Integer namHoc;

    // Constructors
    public CoVanId() {
    }

    public CoVanId(Integer lopId, Integer nvId, Integer namHoc) {
        this.lopId = lopId;
        this.nvId = nvId;
        this.namHoc = namHoc;
    }

    // Getters and Setters
    public Integer getLopId() {
        return lopId;
    }

    public void setLopId(Integer lopId) {
        this.lopId = lopId;
    }

    public Integer getNvId() {
        return nvId;
    }

    public void setNvId(Integer nvId) {
        this.nvId = nvId;
    }

    public Integer getNamHoc() {
        return namHoc;
    }

    public void setNamHoc(Integer namHoc) {
        this.namHoc = namHoc;
    }

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CoVanId coVanId = (CoVanId) o;
        return Objects.equals(lopId, coVanId.lopId) &&
               Objects.equals(nvId, coVanId.nvId) &&
               Objects.equals(namHoc, coVanId.namHoc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lopId, nvId, namHoc);
    }
}
