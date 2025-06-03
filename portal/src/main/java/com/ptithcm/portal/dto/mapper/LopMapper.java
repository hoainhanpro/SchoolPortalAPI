package com.ptithcm.portal.dto.mapper;

import com.ptithcm.portal.dto.KhoaDTO;
import com.ptithcm.portal.dto.LopDTO;
import com.ptithcm.portal.entity.Khoa;
import com.ptithcm.portal.entity.Lop;

import java.util.List;
import java.util.stream.Collectors;

public class LopMapper {

    public static LopDTO toDto(Lop lop) {
        if (lop == null) {
            return null;
        }

        KhoaDTO khoaDTO = null;
        if (lop.getKhoa() != null) {
            Khoa khoa = lop.getKhoa();
            khoaDTO = new KhoaDTO(khoa.getId(), khoa.getTen());
        }

        return new LopDTO(lop.getId(), lop.getTen(), khoaDTO);
    }

    public static List<LopDTO> toDtoList(List<Lop> lops) {
        if (lops == null) {
            return null;
        }

        return lops.stream()
                .map(LopMapper::toDto)
                .collect(Collectors.toList());
    }
} 