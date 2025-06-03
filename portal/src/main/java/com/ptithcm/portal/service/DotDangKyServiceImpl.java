package com.ptithcm.portal.service;

import com.ptithcm.portal.dto.DotDangKyDTO;
import com.ptithcm.portal.entity.DotDangKy;
import com.ptithcm.portal.entity.HocKy;
import com.ptithcm.portal.repository.DotDangKyRepository;
import com.ptithcm.portal.repository.SemesterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DotDangKyServiceImpl implements DotDangKyService {

    @Autowired
    private DotDangKyRepository dotDangKyRepository;

    @Autowired
    private SemesterRepository hocKyRepository;

    @Override
    public List<DotDangKyDTO> getAllDotDangKy() {
        List<DotDangKy> dotDangKys = dotDangKyRepository.findAll();
        return dotDangKys.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<DotDangKyDTO> getDotDangKyById(Integer id) {
        return dotDangKyRepository.findById(id)
                .map(this::convertToDTO);
    }

    @Override
    public DotDangKyDTO createDotDangKy(DotDangKyDTO dotDangKyDTO) {
        // Kiểm tra xem đã có đợt đăng ký nào với học kỳ này chưa
        if (dotDangKyDTO.getHocKyId() != null &&
                dotDangKyRepository.existsByHocKyId(dotDangKyDTO.getHocKyId())) {
            throw new IllegalArgumentException("Đã tồn tại đợt đăng ký cho học kỳ này.");
        }

        DotDangKy dotDangKy = convertToEntity(dotDangKyDTO);
        DotDangKy savedDotDangKy = dotDangKyRepository.save(dotDangKy);
        return convertToDTO(savedDotDangKy);
    }

    @Override
    public DotDangKyDTO updateDotDangKy(Integer id, DotDangKyDTO dotDangKyDTO) {
        Optional<DotDangKy> existingDotDangKy = dotDangKyRepository.findById(id);
        if (existingDotDangKy.isPresent()) {
            // Kiểm tra xem đã có đợt đăng ký nào khác với học kỳ này chưa
            if (dotDangKyDTO.getHocKyId() != null) {
                boolean existsWithSameHocKy = dotDangKyRepository.existsByHocKyIdAndIdNot(
                        dotDangKyDTO.getHocKyId(), id);
                if (existsWithSameHocKy) {
                    throw new IllegalArgumentException("Đã tồn tại đợt đăng ký khác cho học kỳ này.");
                }
            }

            dotDangKyDTO.setId(id);
            DotDangKy dotDangKy = convertToEntity(dotDangKyDTO);
            DotDangKy updatedDotDangKy = dotDangKyRepository.save(dotDangKy);
            return convertToDTO(updatedDotDangKy);
        } else {
            throw new IllegalArgumentException("Không tìm thấy đợt đăng ký với id: " + id);
        }
    }

    private DotDangKyDTO convertToDTO(DotDangKy dotDangKy) {
        return new DotDangKyDTO(dotDangKy);
    }

    private DotDangKy convertToEntity(DotDangKyDTO dotDangKyDTO) {
        DotDangKy dotDangKy = new DotDangKy();
        dotDangKy.setId(dotDangKyDTO.getId());
        dotDangKy.setThoiGianBatDau(dotDangKyDTO.getThoiGianBatDau());
        dotDangKy.setThoiGianKetThuc(dotDangKyDTO.getThoiGianKetThuc());

        // Set HocKy from hocKyId
        if (dotDangKyDTO.getHocKyId() != null) {
            Optional<HocKy> hocKyOpt = hocKyRepository.findById(dotDangKyDTO.getHocKyId());
            hocKyOpt.ifPresent(dotDangKy::setHocKy);
        }

        return dotDangKy;
    }
}