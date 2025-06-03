package com.ptithcm.portal.controller;

import com.ptithcm.portal.dto.BuoiDto;
import com.ptithcm.portal.entity.Buoi;
import com.ptithcm.portal.repository.BuoiRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/buoi")
public class BuoiController {

    private final BuoiRepository buoiRepository;
    
    public BuoiController(BuoiRepository buoiRepository) {
        this.buoiRepository = buoiRepository;
    }

    /**
     * API lấy danh sách tất cả buổi học
     * @return Danh sách buổi học
     */
    @GetMapping
    public ResponseEntity<List<BuoiDto>> getAllBuoi() {
        List<Buoi> buoiList = buoiRepository.findAll();
        
        List<BuoiDto> buoiDtoList = buoiList.stream()
                .map(buoi -> new BuoiDto(
                        buoi.getId(),
                        buoi.getGioBatDau(),
                        buoi.getGioKetThuc()
                ))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(buoiDtoList);
    }
} 