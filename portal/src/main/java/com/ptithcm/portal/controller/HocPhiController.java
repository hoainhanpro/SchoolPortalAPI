package com.ptithcm.portal.controller;

import com.ptithcm.portal.dto.HocPhiDto;
import com.ptithcm.portal.service.TuitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hocphi")
public class HocPhiController {

    @Autowired
    private TuitionService tuitionService;

    @GetMapping
    public ResponseEntity<List<HocPhiDto>> getAllHocPhi() {
        List<HocPhiDto> hocPhiDtoList = tuitionService.getAllHocPhi();
        return new ResponseEntity<>(hocPhiDtoList, HttpStatus.OK);
    }

    @PutMapping("/markAsPaid")
    public ResponseEntity<HocPhiDto> markAsPaid(@RequestBody Map<String, String> payload) {
        String mssv = payload.get("mssv");
        Integer hocKyId = Integer.parseInt(payload.get("hocKyId"));

        HocPhiDto updatedHocPhi = tuitionService.markAsPaid(mssv, hocKyId);
        return new ResponseEntity<>(updatedHocPhi, HttpStatus.OK);
    }
}