package com.ptithcm.portal.service;

import com.ptithcm.portal.dto.DotDongHocPhiDto;
import com.ptithcm.portal.entity.DotDongHocPhi;
import com.ptithcm.portal.entity.HocKy;
import com.ptithcm.portal.repository.DotDongHocPhiRepository;
import com.ptithcm.portal.repository.SemesterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DotDongHocPhiServiceImpl implements DotDongHocPhiService {

    @Autowired
    private DotDongHocPhiRepository dotDongHocPhiRepository;

    @Autowired
    private SemesterRepository hocKyRepository;

    @Override
    public List<DotDongHocPhiDto> getAllDotDongHocPhi() {
        List<DotDongHocPhi> dotDongHocPhis = dotDongHocPhiRepository.findAll();
        return dotDongHocPhis.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<DotDongHocPhiDto> getDotDongHocPhiById(Integer id) {
        return dotDongHocPhiRepository.findById(id)
                .map(this::convertToDto);
    }

    @Override
    public DotDongHocPhiDto createDotDongHocPhi(DotDongHocPhiDto dotDongHocPhiDto) {
        // Kiểm tra xem có tồn tại đợt đóng học phí cho học kỳ này chưa
        Optional<DotDongHocPhi> existingDotDongHocPhi = dotDongHocPhiRepository
                .findByHocKyId(dotDongHocPhiDto.getHocKyId());
        if (existingDotDongHocPhi.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Đã tồn tại đợt đóng học phí cho học kỳ này");
        }

        // Kiểm tra thời gian bắt đầu và kết thúc
        if (dotDongHocPhiDto.getNgayKetThuc().isBefore(dotDongHocPhiDto.getNgayBatDau())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ngày kết thúc phải sau ngày bắt đầu");
        }

        // Chuyển đổi DTO thành Entity
        DotDongHocPhi dotDongHocPhi = convertToEntity(dotDongHocPhiDto);

        // Lưu và chuyển đổi lại thành DTO
        DotDongHocPhi savedDotDongHocPhi = dotDongHocPhiRepository.save(dotDongHocPhi);
        return convertToDto(savedDotDongHocPhi);
    }

    @Override
    public DotDongHocPhiDto updateDotDongHocPhi(Integer id, DotDongHocPhiDto dotDongHocPhiDto) {
        // Kiểm tra xem đợt đóng học phí cần cập nhật có tồn tại không
        Optional<DotDongHocPhi> dotDongHocPhiOpt = dotDongHocPhiRepository.findById(id);
        if (!dotDongHocPhiOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đợt đóng học phí với id: " + id);
        }

        // Kiểm tra nếu đổi học kỳ, xem học kỳ mới đã có đợt đóng học phí chưa
        if (!dotDongHocPhiOpt.get().getHocKy().getId().equals(dotDongHocPhiDto.getHocKyId())) {
            Optional<DotDongHocPhi> existingDotDongHocPhi = dotDongHocPhiRepository
                    .findByHocKyId(dotDongHocPhiDto.getHocKyId());
            if (existingDotDongHocPhi.isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Đã tồn tại đợt đóng học phí cho học kỳ này");
            }
        }

        // Kiểm tra thời gian bắt đầu và kết thúc
        if (dotDongHocPhiDto.getNgayKetThuc().isBefore(dotDongHocPhiDto.getNgayBatDau())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ngày kết thúc phải sau ngày bắt đầu");
        }

        // Cập nhật ID từ path vào DTO
        dotDongHocPhiDto.setId(id);

        // Chuyển đổi DTO thành Entity
        DotDongHocPhi dotDongHocPhi = convertToEntity(dotDongHocPhiDto);

        // Lưu và chuyển đổi lại thành DTO
        DotDongHocPhi updatedDotDongHocPhi = dotDongHocPhiRepository.save(dotDongHocPhi);
        return convertToDto(updatedDotDongHocPhi);
    }

    // Chuyển đổi Entity thành DTO
    private DotDongHocPhiDto convertToDto(DotDongHocPhi dotDongHocPhi) {
        DotDongHocPhiDto dto = new DotDongHocPhiDto();
        dto.setId(dotDongHocPhi.getId());
        dto.setNgayBatDau(dotDongHocPhi.getNgayBatDau());
        dto.setNgayKetThuc(dotDongHocPhi.getNgayKetThuc());

        if (dotDongHocPhi.getHocKy() != null) {
            HocKy hocKy = dotDongHocPhi.getHocKy();
            dto.setHocKyId(hocKy.getId());
            dto.setHocKyTen(hocKy.getTen());
            dto.setNamHoc(hocKy.getNamHoc());
            dto.setThuTu(hocKy.getThuTu());
            dto.setStartDate(hocKy.getStartDate());
            dto.setEndDate(hocKy.getEndDate());
        }

        return dto;
    }

    // Chuyển đổi DTO thành Entity
    private DotDongHocPhi convertToEntity(DotDongHocPhiDto dto) {
        DotDongHocPhi entity = new DotDongHocPhi();
        entity.setId(dto.getId());
        entity.setNgayBatDau(dto.getNgayBatDau());
        entity.setNgayKetThuc(dto.getNgayKetThuc());

        if (dto.getHocKyId() != null) {
            HocKy hocKy = hocKyRepository.findById(dto.getHocKyId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Học kỳ không tồn tại"));
            entity.setHocKy(hocKy);
        }

        return entity;
    }
}