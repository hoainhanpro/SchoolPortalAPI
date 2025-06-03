package com.ptithcm.portal.service;

import com.ptithcm.portal.dto.KhoaDTO;
import com.ptithcm.portal.dto.LopInfoDTO;
import com.ptithcm.portal.entity.Lop;
import com.ptithcm.portal.repository.LopRepository;
import com.ptithcm.portal.repository.CoVanRepository;
import com.ptithcm.portal.repository.SinhVienRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class LopService {

    @Autowired
    private LopRepository lopRepository;

    @Autowired
    private SinhVienRepository SinhVienRepository;

    @Autowired
    private CoVanRepository CoVanRepository;

    public List<Lop> getAllLop() {
        return lopRepository.findAll();
    }

    public Optional<Lop> getLopById(Integer id) {
        return lopRepository.findById(id);
    }

    public Lop createLop(Lop lop) {
        // Check for existing Lop with the same name and khoa_id
        Optional<Lop> existingLop = lopRepository.findByTenAndKhoaId(lop.getTen(), lop.getKhoa().getId());
        if (existingLop.isPresent()) {
            throw new IllegalArgumentException("Lớp học với tên và khoa đã tồn tại.");
        }
        return lopRepository.save(lop);
    }

    public Lop updateLop(Integer id, Lop lop) {
        Optional<Lop> existingLop = lopRepository.findById(id);
        if (existingLop.isEmpty()) {
            return null;
        }

        Optional<Lop> existingLopWithSameNameAndKhoa = lopRepository.findByTenAndKhoaIdAndIdNot(lop.getTen(),
                lop.getKhoa().getId(), id);
        if (existingLopWithSameNameAndKhoa.isPresent()) {
            throw new IllegalArgumentException("Lớp học với tên và khoa đã tồn tại.");
        }

        existingLop.get().setTen(lop.getTen());
        existingLop.get().setKhoa(lop.getKhoa());

        return lopRepository.save(existingLop.get());
    }

    public void deleteLop(Integer id) {
        // Kiểm tra xem lớp có sinh viên không
        boolean hasSinhVien = SinhVienRepository.existsByLopId(id);

        // Kiểm tra xem lớp có cố vấn không
        boolean hasCoVan = CoVanRepository.existsByLopId(id);

        if (hasSinhVien) {
            throw new IllegalStateException("Không thể xóa lớp vì vẫn còn sinh viên thuộc lớp này.");
        }

        if (hasCoVan) {
            throw new IllegalStateException("Không thể xóa lớp vì vẫn có cố vấn phụ trách lớp này.");
        }

        // Nếu không có sinh viên và cố vấn, thì tiến hành xóa
        lopRepository.deleteById(id);
    }

    public List<Map<String, Object>> getAllLopWithKhoa() {
        List<Lop> lops = lopRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Lop lop : lops) {
            Map<String, Object> lopMap = new HashMap<>();

            // Tạo LopInfoDTO
            LopInfoDTO lopInfoDTO = new LopInfoDTO(lop.getId(), lop.getTen());

            // Tạo KhoaDTO nếu khoa tồn tại
            KhoaDTO khoaDTO = null;
            if (lop.getKhoa() != null) {
                khoaDTO = new KhoaDTO(lop.getKhoa().getId(), lop.getKhoa().getTen());
            }

            lopMap.put("lop", lopInfoDTO);
            lopMap.put("khoa", khoaDTO);

            result.add(lopMap);
        }

        return result;
    }

}