package com.ptithcm.portal.service;

import com.ptithcm.portal.dto.SemesterInfoDto;
import com.ptithcm.portal.entity.HocKy;
import com.ptithcm.portal.repository.SemesterRepository;
import com.ptithcm.portal.repository.DotDangKyRepository;
import com.ptithcm.portal.repository.DotDongHocPhiRepository;
import com.ptithcm.portal.repository.HocPhiRepository;
import com.ptithcm.portal.repository.LopTinChiRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SemesterService {

    private final SemesterRepository semesterRepository;

    @Autowired
    private HocPhiRepository hocPhiRepository;

    @Autowired
    private DotDongHocPhiRepository dotDongHocPhiRepository;

    @Autowired
    private DotDangKyRepository dotDangKyRepository;

    @Autowired
    private LopTinChiRepository lopTinChiRepository;

    public SemesterService(SemesterRepository semesterRepository) {
        this.semesterRepository = semesterRepository;
    }

    public SemesterInfoDto getCurrentSemester() {
        Optional<HocKy> currentSemester = semesterRepository.findCurrentSemester();

        if (currentSemester.isPresent()) {
            HocKy hocKy = currentSemester.get();
            return convertToDto(hocKy, true);
        } else {
            // Nếu không có học kỳ nào đang diễn ra, trả về học kỳ mới nhất
            List<HocKy> allSemesters = semesterRepository.findAllOrderByNameHocAndThuTuDesc();
            if (!allSemesters.isEmpty()) {
                return convertToDto(allSemesters.get(0), false);
            }
        }

        return null;
    }

    public List<SemesterInfoDto> getAllSemesters() {
        List<HocKy> allSemesters = semesterRepository.findAllOrderByNameHocAndThuTuDesc();
        Optional<HocKy> currentSemester = semesterRepository.findCurrentSemester();

        return allSemesters.stream()
                .map(hocKy -> {
                    boolean isCurrent = currentSemester.isPresent() &&
                            currentSemester.get().getId().equals(hocKy.getId());
                    return convertToDto(hocKy, isCurrent);
                })
                .collect(Collectors.toList());
    }

    private SemesterInfoDto convertToDto(HocKy hocKy, boolean isCurrentSemester) {
        return new SemesterInfoDto(
                hocKy.getId(),
                hocKy.getTen(),
                hocKy.getNamHoc(),
                hocKy.getThuTu(),
                hocKy.getStartDate(),
                hocKy.getEndDate(),
                isCurrentSemester);
    }

    public List<HocKy> getAllHocKy() {
        return semesterRepository.findAll();
    }

    public Optional<HocKy> getHocKyById(Integer id) {
        return semesterRepository.findById(id);
    }

    public HocKy createHocKy(HocKy hocKy) {
        if (semesterRepository.countByNamHocAndThuTu(hocKy.getNamHoc(), hocKy.getThuTu()) > 0) {
            throw new IllegalArgumentException("Học kỳ với năm học và thứ tự này đã tồn tại.");
        }
        return semesterRepository.save(hocKy);
    }

    public HocKy updateHocKy(Integer id, HocKy hocKy) {
        Optional<HocKy> existingHocKy = semesterRepository.findById(id);

        if (existingHocKy.isPresent()) {
            if (semesterRepository.countByNamHocAndThuTuAndIdNot(
                    hocKy.getNamHoc(),
                    hocKy.getThuTu(),
                    id) > 0) {
                throw new IllegalArgumentException("Học kỳ với năm học và thứ tự này đã tồn tại.");
            }

            hocKy.setId(id);
            return semesterRepository.save(hocKy);
        } else {
            return null;
        }
    }

    /**
     * Kiểm tra xem học kỳ có được tham chiếu bởi các bảng khác không
     * 
     * @param id ID học kỳ cần kiểm tra
     * @return Thông báo lỗi nếu có tham chiếu, null nếu không có
     */
    public String checkDependencies(Integer id) {
        if (hocPhiRepository.existsByHocKyId(id)) {
            return "Không thể xóa học kỳ này vì nó đang được sử dụng trong bảng Học Phí";
        }

        if (dotDongHocPhiRepository.existsByHocKyId(id)) {
            return "Không thể xóa học kỳ này vì nó đang được sử dụng trong bảng Đợt Đóng Học Phí";
        }

        if (dotDangKyRepository.existsByHocKyId(id)) {
            return "Không thể xóa học kỳ này vì nó đang được sử dụng trong bảng Đợt Đăng Ký";
        }

        if (lopTinChiRepository.existsByHocKyId(id)) {
            return "Không thể xóa học kỳ này vì nó đang được sử dụng trong bảng Lớp Tín Chỉ";
        }

        return null;
    }

    /**
     * Xóa học kỳ nếu không có tham chiếu từ bảng khác
     * 
     * @param id ID học kỳ cần xóa
     * @return Thông báo lỗi nếu không thể xóa, null nếu xóa thành công
     */
    public String deleteHocKy(Integer id) {
        String dependencyMessage = checkDependencies(id);
        if (dependencyMessage != null) {
            return dependencyMessage;
        }

        try {
            semesterRepository.deleteById(id);
            return null;
        } catch (Exception e) {
            return "Lỗi khi xóa học kỳ: " + e.getMessage();
        }
    }
}