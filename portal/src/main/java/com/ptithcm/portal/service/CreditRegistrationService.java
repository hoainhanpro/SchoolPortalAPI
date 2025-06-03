package com.ptithcm.portal.service;

import com.ptithcm.portal.dto.AvailableCreditClassDto;
import com.ptithcm.portal.dto.CreditRegistrationRequestDto;
import com.ptithcm.portal.dto.RegisteredCreditClassDto; // Added import
import com.ptithcm.portal.entity.*;
import com.ptithcm.portal.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // Added import
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CreditRegistrationService {

    @Value("${tuition.price-per-credit}") // Added to inject price
    private BigDecimal pricePerCredit;

    @Autowired
    private DotDangKyRepository dotDangKyRepository;

    @Autowired
    private LopTinChiRepository lopTinChiRepository;

    @Autowired
    private DangKyRepository dangKyRepository;

    @Autowired
    private ChiTietGiangDayRepository chiTietGiangDayRepository;

    @Autowired
    private SinhVienRepository sinhVienRepository; // Assuming this exists for fetching SinhVien by mssv

    @Autowired
    private HocKyRepository hocKyRepository; // Added HocKyRepository

    @Autowired // Added HocPhiRepository
    private HocPhiRepository hocPhiRepository;

    private static final String TRANG_THAI_DA_HUY = "Da huy";
    private static final String TRANG_THAI_CHO_XAC_NHAN = "Chua xac nhan"; // Added status
    private static final String TRANG_THAI_DA_XAC_NHAN = "Da xac nhan"; // Added status for confirmed registration
    // Add other statuses if needed, e.g., "Đã đăng ký"

    @Transactional(readOnly = true)
    public List<AvailableCreditClassDto> getAvailableCreditClasses(String studentMssv) {
        LocalDateTime now = LocalDateTime.now();
        HocKy targetHocKy = null;

        // 1. Try to find the latest HocKy with an ACTIVE registration period
        List<HocKy> activeHocKys = hocKyRepository.findHocKyWithActiveRegistrationPeriod(now);

        if (!activeHocKys.isEmpty()) {
            targetHocKy = activeHocKys.get(0); // Get the latest one due to ORDER BY in query
        } else {
            // 2. If no active period, find the overall latest HocKy that ever had a registration period
            List<HocKy> latestHocKysWithAnyPeriod = hocKyRepository.findLatestHocKyWithAnyRegistrationPeriod();
            if (!latestHocKysWithAnyPeriod.isEmpty()) {
                targetHocKy = latestHocKysWithAnyPeriod.get(0); // Get the latest one
            }
        }

        if (targetHocKy == null) {
            // No suitable HocKy found (neither active nor any past registration period)
            return new ArrayList<>();
        }

        Integer hocKyId = targetHocKy.getId();

        Optional<SinhVien> sinhVienOpt = sinhVienRepository.findByMssv(studentMssv);
        if (sinhVienOpt.isEmpty()) {
            // Handle student not found, perhaps throw an exception or return empty list
            return new ArrayList<>();
        }
        SinhVien sinhVien = sinhVienOpt.get();

        List<LopTinChi> lopTinChiList = lopTinChiRepository.findByHocKyIdWithDetails(hocKyId);
        List<AvailableCreditClassDto> availableClasses = new ArrayList<>();

        for (LopTinChi ltc : lopTinChiList) {
            Long currentRegistrations = dangKyRepository.countByLopTinChiAndTrangThaiNot(ltc, TRANG_THAI_DA_HUY);

            if (ltc.getSiSoToiDa() != null && currentRegistrations >= ltc.getSiSoToiDa()) {
                continue; // Class is full
            }

            boolean alreadyRegistered = dangKyRepository.existsBySinhVienAndLopTinChiAndTrangThaiNot(sinhVien, ltc, TRANG_THAI_DA_HUY);
            if (alreadyRegistered) {
                continue; // Student already registered
            }

            Mon mon = ltc.getMon();
            String monTen = (mon != null) ? mon.getTenMon() : "N/A";
            Integer monSoTinChi = (mon != null) ? mon.getSoTinChi() : 0;

            List<ChiTietGiangDay> giangDayList = chiTietGiangDayRepository.findByLopTinChiWithNhanVien(ltc);
            String giangVienDisplay = giangDayList.stream()
                .map(ChiTietGiangDay::getNhanVien)
                .filter(nv -> nv != null)
                .map(nv -> nv.getHo() + " " + nv.getTen())
                .collect(Collectors.joining(", "));
            if (giangVienDisplay.isEmpty()) {
                giangVienDisplay = "N/A";
            }
            
            availableClasses.add(new AvailableCreditClassDto(
                ltc.getId(),
                monTen,
                monSoTinChi,
                ltc.getNhom(),
                ltc.getSiSoToiDa(),
                currentRegistrations,
                giangVienDisplay
            ));
        }
        return availableClasses;
    }

    @Transactional
    public DangKy registerCreditClass(String studentMssv, CreditRegistrationRequestDto registrationRequest) throws Exception {
        Optional<SinhVien> sinhVienOpt = sinhVienRepository.findByMssv(studentMssv);
        if (sinhVienOpt.isEmpty()) {
            throw new Exception("Sinh viên không tồn tại: " + studentMssv);
        }
        SinhVien sinhVien = sinhVienOpt.get();

        Optional<LopTinChi> lopTinChiOpt = lopTinChiRepository.findById(registrationRequest.getLopTinChiId());
        if (lopTinChiOpt.isEmpty()) {
            throw new Exception("Lớp tín chỉ không tồn tại: " + registrationRequest.getLopTinChiId());
        }
        LopTinChi lopTinChi = lopTinChiOpt.get();
        Mon mon = lopTinChi.getMon();
        HocKy hocKy = lopTinChi.getHocKy();

        // Check if student has already passed this subject
        if (mon != null) {
            List<DangKy> passedRegistrations = dangKyRepository.findBySinhVienAndMonAndPassed(sinhVien, mon);
            if (!passedRegistrations.isEmpty()) {
                throw new Exception("Sinh viên đã hoàn thành và qua môn " + mon.getTenMon() + " trước đó.");
            }
        }

        // Check for active registration period for the semester of the class
        Optional<DotDangKy> activeRegistrationPeriodOpt = dotDangKyRepository.findActiveByHocKyId(hocKy.getId(), LocalDateTime.now());
        if (activeRegistrationPeriodOpt.isEmpty()) {
            throw new Exception("Hiện tại không phải thời gian đăng ký học phần cho học kỳ này.");
        }

        // Check if already registered for the same Mon in the same HocKy (excluding cancelled)
        if (mon != null && hocKy != null) {
            boolean alreadyRegisteredSubjectInSemester = dangKyRepository.existsBySinhVienAndMonAndHocKyAndTrangThaiNot(sinhVien, mon, hocKy, TRANG_THAI_DA_HUY);
            if (alreadyRegisteredSubjectInSemester) {
                throw new Exception("Sinh viên đã đăng ký môn học " + mon.getTenMon() + " trong học kỳ này.");
            }
        }

        // Check if class is full
        Long currentRegistrations = dangKyRepository.countByLopTinChiAndTrangThaiNot(lopTinChi, TRANG_THAI_DA_HUY);
        if (lopTinChi.getSiSoToiDa() != null && currentRegistrations >= lopTinChi.getSiSoToiDa()) {
            throw new Exception("Lớp tín chỉ đã đủ sĩ số.");
        }

        DangKy savedRegistration;
        // Check for existing registration (including cancelled ones) to potentially reuse/update
        Optional<DangKy> existingRegistrationOpt = dangKyRepository.findBySinhVienAndLopTinChi(sinhVien, lopTinChi);

        if (existingRegistrationOpt.isPresent()) {
            DangKy existingRegistration = existingRegistrationOpt.get();
            if (!TRANG_THAI_DA_HUY.equals(existingRegistration.getTrangThai())) {
                throw new Exception("Sinh viên đã đăng ký lớp tín chỉ này và chưa hủy.");
            }
            existingRegistration.setTrangThai(TRANG_THAI_CHO_XAC_NHAN);
            existingRegistration.setNgayDangKy(LocalDateTime.now());
            savedRegistration = dangKyRepository.save(existingRegistration);
        } else {
            DangKy newRegistration = new DangKy();
            newRegistration.setSinhVien(sinhVien);
            newRegistration.setLopTinChi(lopTinChi);
            newRegistration.setNgayDangKy(LocalDateTime.now());
            newRegistration.setTrangThai(TRANG_THAI_CHO_XAC_NHAN);
            newRegistration.setDiemcc(BigDecimal.ZERO);
            newRegistration.setDiemkt(BigDecimal.ZERO);
            newRegistration.setDiemthi(BigDecimal.ZERO);
            savedRegistration = dangKyRepository.save(newRegistration);
        }

        // Calculate and update tuition fee
        updateTuitionFee(sinhVien, hocKy);

        return savedRegistration;
    }

    @Transactional(readOnly = true)
    public List<RegisteredCreditClassDto> getRegisteredCreditClasses(String studentMssv) throws Exception {
        Optional<SinhVien> sinhVienOpt = sinhVienRepository.findByMssv(studentMssv);
        if (sinhVienOpt.isEmpty()) {
            throw new Exception("Sinh viên không tồn tại: " + studentMssv);
        }
        SinhVien sinhVien = sinhVienOpt.get();

        LocalDateTime now = LocalDateTime.now();
        HocKy targetHocKy = null;

        // 1. Try to find the latest HocKy with an ACTIVE registration period
        List<HocKy> activeHocKys = hocKyRepository.findHocKyWithActiveRegistrationPeriod(now);

        if (!activeHocKys.isEmpty()) {
            targetHocKy = activeHocKys.get(0); // Get the latest one due to ORDER BY in query
        } else {
            // 2. If no active period, find the overall latest HocKy that ever had a registration period
            List<HocKy> latestHocKysWithAnyPeriod = hocKyRepository.findLatestHocKyWithAnyRegistrationPeriod();
            if (!latestHocKysWithAnyPeriod.isEmpty()) {
                targetHocKy = latestHocKysWithAnyPeriod.get(0); // Get the latest one
            }
        }

        List<DangKy> registrations;
        if (targetHocKy != null) {
            Integer targetHocKyId = targetHocKy.getId();
            registrations = dangKyRepository.findBySinhVienAndHocKyIdAndTrangThaiNotWithDetails(sinhVien, targetHocKyId, TRANG_THAI_DA_HUY);
        } else {
            // No suitable HocKy found (neither active nor any past registration period)
            registrations = new ArrayList<>();
        }

        return registrations.stream().map(dk -> {
            LopTinChi ltc = dk.getLopTinChi();
            Mon mon = ltc.getMon();
            HocKy hk = ltc.getHocKy();

            List<ChiTietGiangDay> giangDayList = chiTietGiangDayRepository.findByLopTinChiWithNhanVien(ltc);
            String giangVienDisplay = giangDayList.stream()
                .map(ChiTietGiangDay::getNhanVien)
                .filter(nv -> nv != null)
                .map(nv -> nv.getHo() + " " + nv.getTen())
                .collect(Collectors.joining(", "));
            if (giangVienDisplay.isEmpty()) {
                giangVienDisplay = "N/A";
            }

            return new RegisteredCreditClassDto(
                dk.getId(),
                ltc.getId(),
                mon != null ? mon.getTenMon() : "N/A",
                mon != null ? mon.getSoTinChi() : 0,
                ltc.getNhom(),
                hk != null ? hk.getTen() : "N/A",
                hk != null ? hk.getNamHoc() : 0,
                dk.getTrangThai(),
                dk.getNgayDangKy(),
                giangVienDisplay,
                dk.getDiemcc(),
                dk.getDiemkt(),
                dk.getDiemthi()
            );
        }).collect(Collectors.toList());
    }

    @Transactional
    public void cancelCreditClassRegistration(String studentMssv, Integer dangKyId) throws Exception { // Changed Long to Integer for dangKyId
        Optional<DangKy> dangKyOpt = dangKyRepository.findById(dangKyId);
        if (dangKyOpt.isEmpty()) {
            throw new Exception("Đăng ký không tồn tại với ID: " + dangKyId);
        }
        DangKy dangKy = dangKyOpt.get();

        Optional<SinhVien> sinhVienOpt = sinhVienRepository.findByMssv(studentMssv);
        if (sinhVienOpt.isEmpty()) {
            throw new Exception("Sinh viên không tồn tại: " + studentMssv);
        }
        SinhVien sinhVien = sinhVienOpt.get();

        // Compare using MSSV as SinhVien's identifier
        if (!dangKy.getSinhVien().getMssv().equals(sinhVien.getMssv())) {
            throw new Exception("Không có quyền hủy đăng ký này.");
        }

        if (TRANG_THAI_DA_XAC_NHAN.equals(dangKy.getTrangThai())) {
            throw new Exception("Không thể hủy đăng ký đã được xác nhận.");
        }

        if (TRANG_THAI_DA_HUY.equals(dangKy.getTrangThai())) {
            throw new Exception("Đăng ký này đã được hủy trước đó.");
        }

        LopTinChi lopTinChi = dangKy.getLopTinChi();
        if (lopTinChi == null || lopTinChi.getHocKy() == null) {
            throw new Exception("Thông tin lớp tín chỉ hoặc học kỳ không hợp lệ cho đăng ký này.");
        }
        HocKy hocKy = lopTinChi.getHocKy();

        Optional<DotDangKy> activeRegistrationPeriodOpt = dotDangKyRepository.findActiveByHocKyId(hocKy.getId(), LocalDateTime.now());
        if (activeRegistrationPeriodOpt.isEmpty()) {
            throw new Exception("Đã hết thời gian cho phép hủy đăng ký học phần cho học kỳ này.");
        }

        dangKy.setTrangThai(TRANG_THAI_DA_HUY);
        dangKyRepository.save(dangKy);

        // Recalculate and update tuition fee
        updateTuitionFee(sinhVien, hocKy);
    }

    private void updateTuitionFee(SinhVien sinhVien, HocKy hocKy) {
        List<String> activeStatuses = List.of(TRANG_THAI_CHO_XAC_NHAN, TRANG_THAI_DA_XAC_NHAN);
        // Corrected method name below
        List<DangKy> registrations = dangKyRepository.findBySinhVienAndLopTinChiHocKyAndTrangThaiIn(sinhVien, hocKy, activeStatuses);

        int totalCredits = 0;
        for (DangKy reg : registrations) {
            if (reg.getLopTinChi() != null && reg.getLopTinChi().getMon() != null && reg.getLopTinChi().getMon().getSoTinChi() != null) {
                totalCredits += reg.getLopTinChi().getMon().getSoTinChi();
            }
        }

        BigDecimal totalAmount = pricePerCredit.multiply(new BigDecimal(totalCredits));

        Optional<HocPhi> hocPhiOpt = hocPhiRepository.findBySinhVienAndHocKy(sinhVien, hocKy);
        HocPhi hocPhi;
        if (hocPhiOpt.isPresent()) {
            hocPhi = hocPhiOpt.get();
        } else {
            hocPhi = new HocPhi();
            hocPhi.setSinhVien(sinhVien);
            hocPhi.setHocKy(hocKy);
            hocPhi.setTrangThai("Chua dong"); // Default status from entity
        }

        hocPhi.setTongTien(totalAmount);
        // If tongSoTinChi was kept, it would be: hocPhi.setTongSoTinChi(totalCredits);
        
        // If totalAmount is zero and you want to delete the record or handle it differently:
        // if (totalAmount.compareTo(BigDecimal.ZERO) == 0 && hocPhi.getId() != null) { 
        //     hocPhiRepository.delete(hocPhi);
        // } else if (totalAmount.compareTo(BigDecimal.ZERO) > 0 || hocPhi.getId() == null) {
        //     hocPhiRepository.save(hocPhi);
        // }
        // For now, always save/update, even if it's zero, to reflect no dues.
        hocPhiRepository.save(hocPhi);
    }
}
