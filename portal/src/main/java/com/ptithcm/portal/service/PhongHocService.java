package com.ptithcm.portal.service;

import com.ptithcm.portal.entity.PhongHoc;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PhongHocService {
    List<PhongHoc> getAllPhongHoc();

    Optional<PhongHoc> getPhongHocById(Integer id);

    PhongHoc createPhongHoc(PhongHoc phongHoc);

    PhongHoc updatePhongHoc(Integer id, PhongHoc phongHoc);

    /**
     * Xóa phòng học nếu không có tham chiếu trong bảng lich
     * 
     * @param id ID của phòng học cần xóa
     * @return Map chứa kết quả xóa với các key: "success" và "message"
     */
    Map<String, Object> deletePhongHoc(Integer id);
}