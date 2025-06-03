package com.ptithcm.portal.service;

import com.ptithcm.portal.dto.LopTinChiRequest;
import com.ptithcm.portal.dto.LopTinChiResponse;

public interface LopTinChiService {
    /**
     * Tạo mới lớp tín chỉ
     * @param request Thông tin lớp tín chỉ cần tạo
     * @return Kết quả tạo lớp tín chỉ
     */
    LopTinChiResponse createLopTinChi(LopTinChiRequest request);
} 