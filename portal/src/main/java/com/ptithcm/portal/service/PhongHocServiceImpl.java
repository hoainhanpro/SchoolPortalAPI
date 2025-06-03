package com.ptithcm.portal.service;

import com.ptithcm.portal.entity.PhongHoc;
import com.ptithcm.portal.repository.PhongHocRepository;
import com.ptithcm.portal.repository.LichRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Map;

@Service
public class PhongHocServiceImpl implements PhongHocService {

    @Autowired
    private PhongHocRepository phongHocRepository;

    @Autowired
    private LichRepository lichRepository;

    @Override
    public List<PhongHoc> getAllPhongHoc() {
        return phongHocRepository.findAll();
    }

    @Override
    public Optional<PhongHoc> getPhongHocById(Integer id) {
        return phongHocRepository.findById(id);
    }

    @Override
    public PhongHoc createPhongHoc(PhongHoc phongHoc) {
        if (phongHocRepository.countByTen(phongHoc.getTen()) == 0) {
            return phongHocRepository.save(phongHoc);
        } else {
            throw new IllegalArgumentException("Phong hoc with this name already exists");
        }
    }

    @Override
    public PhongHoc updatePhongHoc(Integer id, PhongHoc phongHoc) {
        Optional<PhongHoc> existingPhongHoc = phongHocRepository.findById(id);

        if (existingPhongHoc.isPresent()) {
            if (phongHocRepository.countByTen(phongHoc.getTen()) == 0
                    || existingPhongHoc.get().getTen().equals(phongHoc.getTen())) {
                phongHoc.setId(id);
                return phongHocRepository.save(phongHoc);
            } else {
                throw new IllegalArgumentException("Phong hoc with this name already exists");
            }
        } else {
            return null;
        }
    }

    @Override
    public Map<String, Object> deletePhongHoc(Integer id) {
        Map<String, Object> result = new HashMap<>();

        // Lấy phòng học theo ID
        Optional<PhongHoc> phongHocOptional = phongHocRepository.findById(id);

        if (!phongHocOptional.isPresent()) {
            result.put("success", false);
            result.put("message", "Không tìm thấy phòng học với ID: " + id);
            return result;
        }

        PhongHoc phongHoc = phongHocOptional.get();

        // Kiểm tra xem phòng học có đang được sử dụng trong bảng lich không
        if (lichRepository.existsByPhongHoc(phongHoc)) {
            long count = lichRepository.countByPhongHoc(phongHoc);
            result.put("success", false);
            result.put("message", "Không thể xóa phòng học " + phongHoc.getTen() +
                    " vì đang được sử dụng trong " + count + " lịch học/thi");
            return result;
        }

        // Nếu không có tham chiếu, thực hiện xóa
        phongHocRepository.deleteById(id);

        result.put("success", true);
        result.put("message", "Xóa phòng học thành công");
        return result;
    }
}