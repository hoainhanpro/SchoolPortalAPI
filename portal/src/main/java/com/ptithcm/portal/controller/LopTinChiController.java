package com.ptithcm.portal.controller;

import com.ptithcm.portal.dto.LopTinChiRequest;
import com.ptithcm.portal.dto.LopTinChiResponse;
import com.ptithcm.portal.service.LopTinChiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lop-tin-chi")
public class LopTinChiController {

    private final LopTinChiService lopTinChiService;

    public LopTinChiController(LopTinChiService lopTinChiService) {
        this.lopTinChiService = lopTinChiService;
    }

    /**
     * API đăng ký lớp tín chỉ (tạo lớp tín chỉ) cho giảng viên
     * @param request Thông tin lớp tín chỉ cần tạo
     * @return Kết quả tạo lớp tín chỉ
     */
    @PostMapping
    public ResponseEntity<LopTinChiResponse> registerLopTinChi(@RequestBody LopTinChiRequest request) {
        LopTinChiResponse response = lopTinChiService.createLopTinChi(request);
        
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        
        return ResponseEntity.ok(response);
    }
} 