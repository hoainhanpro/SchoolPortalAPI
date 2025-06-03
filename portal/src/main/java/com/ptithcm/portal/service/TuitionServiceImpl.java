package com.ptithcm.portal.service;

import com.ptithcm.portal.dto.TuitionResponseDto;
import com.ptithcm.portal.dto.HocPhiDto;
import com.ptithcm.portal.dto.DotDongHocPhiDto;
import com.ptithcm.portal.dto.HocKyDto;
import com.ptithcm.portal.entity.HocPhi;
import com.ptithcm.portal.entity.DotDongHocPhi;
import com.ptithcm.portal.entity.HocKy;
import com.ptithcm.portal.repository.HocPhiRepository;
import com.ptithcm.portal.repository.DotDongHocPhiRepository;
import com.ptithcm.portal.repository.SemesterRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TuitionServiceImpl implements TuitionService {

    @Autowired
    private HocPhiRepository hocPhiRepository;

    @Autowired
    private DotDongHocPhiRepository dotDongHocPhiRepository;

    @Autowired
    private SemesterRepository semesterRepository;

    @Override
    public TuitionResponseDto getTuitionInformation(String mssv) {
        return getTuitionInformationBySemester(mssv, null);
    }

    @Override
    public TuitionResponseDto getTuitionInformationBySemester(String mssv, Integer hocKyId) {
        // Lấy lịch sử học phí
        List<HocPhiDto> tuitionHistory = getTuitionHistoryInternal(mssv, hocKyId);

        // Lấy thông tin các đợt đóng học phí
        List<DotDongHocPhiDto> paymentPeriods = getPaymentPeriodsInternal(hocKyId);

        // Lấy danh sách học kỳ có liên quan
        List<HocKyDto> availableSemesters = getAvailableSemesters(mssv);

        return new TuitionResponseDto(tuitionHistory, paymentPeriods, availableSemesters);
    }

    @Override
    public List<HocPhiDto> getTuitionHistory(String mssv) {
        return getTuitionHistoryInternal(mssv, null);
    }

    @Override
    public List<DotDongHocPhiDto> getPaymentPeriods(Integer hocKyId) {
        return getPaymentPeriodsInternal(hocKyId);
    }

    @Override
    @Transactional
    public HocPhiDto updatePaymentStatus(Integer hocPhiId, BigDecimal soTienDong) {
        HocPhi hocPhi = hocPhiRepository.findById(hocPhiId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bản ghi học phí với ID: " + hocPhiId));

        // Cập nhật số tiền đã đóng
        BigDecimal newDaDong = hocPhi.getDaDong().add(soTienDong);
        hocPhi.setDaDong(newDaDong);

        // Cập nhật trạng thái và ngày đóng
        if (newDaDong.compareTo(hocPhi.getTongTien()) >= 0) {
            hocPhi.setTrangThai("Da dong");
            hocPhi.setNgayDong(LocalDate.now());
        } else {
            hocPhi.setTrangThai("Chua dong");
        }

        hocPhi = hocPhiRepository.save(hocPhi);

        return convertToHocPhiDto(hocPhi);
    }

    @Override
    public TuitionResponseDto getTuitionData(String studentId) {
        return getTuitionInformation(studentId);
    }

    @Override
    public TuitionResponseDto getTuitionDataBySemester(String studentId, String semesterId) {
        try {
            Integer hocKyId = Integer.parseInt(semesterId);
            return getTuitionInformationBySemester(studentId, hocKyId);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid semester ID format: " + semesterId);
        }
    }

    @Override
    @Transactional
    public void processPayment(String studentId, String semesterId, Double amount) {
        try {
            Integer hocKyId = Integer.parseInt(semesterId);
            BigDecimal paymentAmount = BigDecimal.valueOf(amount);

            // Find existing tuition record or create new one
            List<HocPhi> existingTuition = hocPhiRepository.findByStudentIdAndSemester(studentId, hocKyId);

            if (existingTuition.isEmpty()) {
                throw new RuntimeException(
                        "No tuition record found for student " + studentId + " in semester " + semesterId);
            }

            HocPhi hocPhi = existingTuition.get(0);
            updatePaymentStatus(hocPhi.getId(), paymentAmount);

        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid semester ID format: " + semesterId);
        }
    }

    private List<HocPhiDto> getTuitionHistoryInternal(String mssv, Integer hocKyId) {
        List<HocPhi> hocPhis;
        if (hocKyId != null) {
            hocPhis = hocPhiRepository.findByStudentIdAndSemester(mssv, hocKyId);
        } else {
            hocPhis = hocPhiRepository.findByStudentId(mssv);
        }

        return hocPhis.stream()
                .map(this::convertToHocPhiDto)
                .collect(Collectors.toList());
    }

    private List<DotDongHocPhiDto> getPaymentPeriodsInternal(Integer hocKyId) {
        List<DotDongHocPhi> dotDongHocPhis;
        if (hocKyId != null) {
            dotDongHocPhis = dotDongHocPhiRepository.findBySemester(hocKyId);
        } else {
            dotDongHocPhis = dotDongHocPhiRepository.findAllOrderBySemesterAndDate();
        }

        return dotDongHocPhis.stream()
                .map(this::convertToDotDongHocPhiDto)
                .collect(Collectors.toList());
    }

    private List<HocKyDto> getAvailableSemesters(String mssv) {
        // Lấy tất cả học kỳ có liên quan đến học phí của sinh viên hoặc có đợt đóng học
        // phí
        List<HocPhi> hocPhis = hocPhiRepository.findByStudentId(mssv);
        Set<Integer> hocKyIdsFromTuition = hocPhis.stream()
                .map(hp -> hp.getHocKy().getId())
                .collect(Collectors.toSet());

        // Lấy học kỳ từ đợt đóng học phí - extract ID from Object[]
        List<Object[]> semesterData = dotDongHocPhiRepository.findDistinctSemesterIdsWithOrder();
        Set<Integer> hocKyIdsFromPaymentPeriods = semesterData.stream()
                .map(data -> (Integer) data[0]) // data[0] is semester ID
                .collect(Collectors.toSet());

        // Kết hợp các học kỳ
        hocKyIdsFromTuition.addAll(hocKyIdsFromPaymentPeriods);

        return hocKyIdsFromTuition.stream()
                .map(id -> semesterRepository.findById(id).orElse(null))
                .filter(hocKy -> hocKy != null)
                .map(this::convertToHocKyDto)
                .sorted((a, b) -> {
                    if (!b.getNamHoc().equals(a.getNamHoc())) {
                        return b.getNamHoc().compareTo(a.getNamHoc());
                    }
                    return b.getThuTu().compareTo(a.getThuTu());
                })
                .collect(Collectors.toList());
    }

    private HocPhiDto convertToHocPhiDto(HocPhi hocPhi) {
        HocKy hocKy = hocPhi.getHocKy();
        return new HocPhiDto(
                hocPhi.getId(),
                hocPhi.getSinhVien().getMssv(),
                hocKy.getId(),
                hocPhi.getTongTien(),
                hocPhi.getDaDong(),
                hocPhi.getNgayDong(),
                hocPhi.getTrangThai(),
                hocKy.getTen(),
                hocKy.getNamHoc(),
                hocKy.getThuTu(),
                hocKy.getStartDate(),
                hocKy.getEndDate());
    }

    private DotDongHocPhiDto convertToDotDongHocPhiDto(DotDongHocPhi dotDongHocPhi) {
        HocKy hocKy = dotDongHocPhi.getHocKy();
        return new DotDongHocPhiDto(
                dotDongHocPhi.getId(),
                hocKy.getId(),
                dotDongHocPhi.getNgayBatDau(),
                dotDongHocPhi.getNgayKetThuc(),
                hocKy.getTen(),
                hocKy.getNamHoc(),
                hocKy.getThuTu(),
                hocKy.getStartDate(),
                hocKy.getEndDate());
    }

    private HocKyDto convertToHocKyDto(HocKy hocKy) {
        return new HocKyDto(
                hocKy.getId(),
                hocKy.getTen(),
                hocKy.getNamHoc(),
                hocKy.getThuTu(),
                hocKy.getStartDate(),
                hocKy.getEndDate());
    }

    @Override
    public List<HocPhiDto> getAllHocPhi() {
        List<HocPhi> allHocPhi = hocPhiRepository.findAllOrderByHocKyDesc();

        return allHocPhi.stream()
                .map(this::convertToHocPhiDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public HocPhiDto markAsPaid(String mssv, Integer hocKyId) {
        List<HocPhi> hocPhiList = hocPhiRepository.findByStudentIdAndSemester(mssv, hocKyId);

        if (hocPhiList.isEmpty()) {
            throw new RuntimeException(
                    "Không tìm thấy bản ghi học phí cho sinh viên " + mssv + " trong học kỳ " + hocKyId);
        }

        HocPhi hocPhi = hocPhiList.get(0);

        hocPhi.setTrangThai("Da dong");
        hocPhi.setNgayDong(LocalDate.now());
        hocPhi.setDaDong(hocPhi.getTongTien());

        HocPhi updatedHocPhi = hocPhiRepository.save(hocPhi);

        return convertToHocPhiDto(updatedHocPhi);
    }

}
