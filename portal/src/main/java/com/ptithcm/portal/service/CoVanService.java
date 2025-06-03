package com.ptithcm.portal.service;

import com.ptithcm.portal.dto.LopDTO;
import com.ptithcm.portal.dto.mapper.LopMapper;
import com.ptithcm.portal.entity.Lop;
import com.ptithcm.portal.repository.CoVanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.List;

@Service
public class CoVanService {

    @Autowired
    private CoVanRepository coVanRepository;

    /**
     * Lấy danh sách các lớp mà nhân viên (giảng viên) đang cố vấn
     * 
     * @param nvId ID của nhân viên (giảng viên)
     * @return Danh sách lớp được cố vấn
     */
    public List<LopDTO> getLopCoVanByNhanVienId(Integer nvId) {
        List<Lop> lops = coVanRepository.findLopsByNhanVienId(nvId);
        return LopMapper.toDtoList(lops);
    }

    /**
     * Lấy danh sách các lớp mà giảng viên đang cố vấn trong năm học hiện tại
     * 
     * @param nvId ID của nhân viên (giảng viên)
     * @return Danh sách lớp được cố vấn trong năm học hiện tại
     */
    public List<LopDTO> getLopCoVanByNhanVienIdTrongNamHocHienTai(Integer nvId) {
        int namHocHienTai = Calendar.getInstance().get(Calendar.YEAR);
        List<Lop> lops = coVanRepository.findLopsByNhanVienIdAndNamHoc(nvId, namHocHienTai);
        return LopMapper.toDtoList(lops);
    }

    /**
     * Lấy danh sách các lớp mà giảng viên đang cố vấn trong một năm học cụ thể
     * 
     * @param nvId   ID của nhân viên (giảng viên)
     * @param namHoc Năm học cần tìm
     * @return Danh sách lớp được cố vấn trong năm học chỉ định
     */
    public List<LopDTO> getLopCoVanByNhanVienIdAndNamHoc(Integer nvId, Integer namHoc) {
        List<Lop> lops = coVanRepository.findLopsByNhanVienIdAndNamHoc(nvId, namHoc);
        return LopMapper.toDtoList(lops);
    }

}