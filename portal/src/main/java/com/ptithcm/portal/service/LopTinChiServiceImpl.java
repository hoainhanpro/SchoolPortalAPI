package com.ptithcm.portal.service;

import com.ptithcm.portal.dto.LopTinChiRequest;
import com.ptithcm.portal.dto.LopTinChiResponse;
import com.ptithcm.portal.entity.*;
import com.ptithcm.portal.repository.*;
import com.ptithcm.portal.util.ScheduleUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class LopTinChiServiceImpl implements LopTinChiService {

    private final LopTinChiRepository lopTinChiRepository;
    private final MonRepository monRepository;
    private final SemesterRepository semesterRepository;
    private final NhanVienRepository nhanVienRepository;
    private final BuoiRepository buoiRepository;
    private final PhongHocRepository phongHocRepository;
    private final LichRepository lichRepository;
    private final ChiTietGiangDayRepository chiTietGiangDayRepository;

    public LopTinChiServiceImpl(
            LopTinChiRepository lopTinChiRepository,
            MonRepository monRepository,
            SemesterRepository semesterRepository,
            NhanVienRepository nhanVienRepository,
            BuoiRepository buoiRepository,
            PhongHocRepository phongHocRepository,
            LichRepository lichRepository,
            ChiTietGiangDayRepository chiTietGiangDayRepository) {
        this.lopTinChiRepository = lopTinChiRepository;
        this.monRepository = monRepository;
        this.semesterRepository = semesterRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.buoiRepository = buoiRepository;
        this.phongHocRepository = phongHocRepository;
        this.lichRepository = lichRepository;
        this.chiTietGiangDayRepository = chiTietGiangDayRepository;
    }

    @Override
    @Transactional
    public LopTinChiResponse createLopTinChi(LopTinChiRequest request) {
        // Kiểm tra các điều kiện đầu vào
        if (request.getMonId() == null || request.getHocKyId() == null || 
            request.getNhom() == null || request.getSiSoToiDa() == null || 
            request.getGiangVienId() == null || request.getLoaiGiangDay() == null || 
            request.getThuInWeek() == null || request.getBuoiId() == null || 
            request.getPhongHocId() == null) {
            return new LopTinChiResponse(false, "Thông tin đầu vào không đầy đủ");
        }

        // Kiểm tra đã tồn tại lớp tín chỉ với môn học, học kỳ và nhóm này chưa
        if (lopTinChiRepository.existsByMonAndHocKyAndNhom(
                request.getMonId(), request.getHocKyId(), request.getNhom())) {
            return new LopTinChiResponse(false, "Lớp tín chỉ với môn học, học kỳ và nhóm này đã tồn tại");
        }

        // Tìm các entity cần thiết
        Optional<Mon> monOpt = monRepository.findById(request.getMonId());
        Optional<HocKy> hocKyOpt = semesterRepository.findById(request.getHocKyId());
        Optional<NhanVien> giangVienOpt = nhanVienRepository.findById(request.getGiangVienId());
        Optional<Buoi> buoiOpt = buoiRepository.findById(request.getBuoiId());
        Optional<PhongHoc> phongHocOpt = phongHocRepository.findById(request.getPhongHocId());

        if (monOpt.isEmpty() || hocKyOpt.isEmpty() || giangVienOpt.isEmpty() || 
            buoiOpt.isEmpty() || phongHocOpt.isEmpty()) {
            return new LopTinChiResponse(false, "Không tìm thấy môn học, học kỳ, giảng viên, buổi học hoặc phòng học");
        }

        Mon mon = monOpt.get();
        HocKy hocKy = hocKyOpt.get();
        NhanVien giangVien = giangVienOpt.get();
        Buoi buoi = buoiOpt.get();
        PhongHoc phongHoc = phongHocOpt.get();

        // Tính toán các ngày học trong học kỳ
        LocalDate startDate = request.getNgayBatDau() != null ? request.getNgayBatDau() : LocalDate.now();
        LocalDate endDate = hocKy.getEndDate();
        
        // Đảm bảo ngày bắt đầu không nhỏ hơn ngày bắt đầu học kỳ
        if (startDate.isBefore(hocKy.getStartDate())) {
            startDate = hocKy.getStartDate();
        }
        
        List<LocalDate> scheduleDates = ScheduleUtils.calculateScheduleDates(startDate, endDate, request.getThuInWeek());
        
        if (scheduleDates.isEmpty()) {
            return new LopTinChiResponse(false, "Không thể tạo lịch học cho khoảng thời gian đã chọn");
        }

        // Kiểm tra xung đột lịch giảng dạy của giảng viên cho tất cả các ngày học
        for (LocalDate ngayHoc : scheduleDates) {
            List<Lich> conflictingSchedules = lichRepository.findConflictingSchedules(
                    request.getGiangVienId(), 
                    request.getBuoiId(), 
                    ngayHoc);
            
            if (!conflictingSchedules.isEmpty()) {
                return new LopTinChiResponse(false, 
                        "Giảng viên đã có lịch dạy vào " + ScheduleUtils.getDayName(request.getThuInWeek()) + 
                        " buổi " + (request.getBuoiId() == 1 ? "sáng" : "chiều") + 
                        " trong học kỳ này (xung đột ngày " + ngayHoc + ")");
            }
        }

        // Tạo lớp tín chỉ mới
        LopTinChi lopTinChi = new LopTinChi();
        lopTinChi.setMon(mon);
        lopTinChi.setHocKy(hocKy);
        lopTinChi.setNhom(request.getNhom());
        lopTinChi.setSiSoToiDa(request.getSiSoToiDa());

        // Lưu lớp tín chỉ
        lopTinChi = lopTinChiRepository.save(lopTinChi);

        // Tạo chi tiết giảng dạy
        ChiTietGiangDay chiTietGiangDay = new ChiTietGiangDay();
        ChiTietGiangDayId chiTietGiangDayId = new ChiTietGiangDayId();
        chiTietGiangDayId.setLopTcId(lopTinChi.getId());
        chiTietGiangDayId.setNvId(giangVien.getId());
        
        chiTietGiangDay.setId(chiTietGiangDayId);
        chiTietGiangDay.setLopTinChi(lopTinChi);
        chiTietGiangDay.setNhanVien(giangVien);
        chiTietGiangDay.setLoai(request.getLoaiGiangDay());

        chiTietGiangDayRepository.save(chiTietGiangDay);

        // Tạo lịch học cho tất cả các ngày đã tính toán
        List<Lich> lichHocs = new ArrayList<>();
        for (LocalDate ngayHoc : scheduleDates) {
            Lich lich = new Lich();
            lich.setLopTinChi(lopTinChi);
            lich.setBuoi(buoi);
            lich.setNgayHoc(Date.valueOf(ngayHoc));
            lich.setPhongHoc(phongHoc);
            
            lichHocs.add(lich);
        }
        
        lichRepository.saveAll(lichHocs);

        // Trả về kết quả với thông tin chi tiết
        String scheduleInfo = String.format("%s, %s (Từ %s đến %s - %d buổi học)", 
                ScheduleUtils.getDayName(request.getThuInWeek()),
                request.getBuoiId() == 1 ? "Sáng" : "Chiều",
                startDate,
                endDate,
                scheduleDates.size());
                
        return new LopTinChiResponse(
                lopTinChi.getId(),
                mon.getTenMon(),
                hocKy.getTen(),
                lopTinChi.getNhom(),
                lopTinChi.getSiSoToiDa(),
                scheduleInfo
        );
    }
} 