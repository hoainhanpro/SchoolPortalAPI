package com.ptithcm.portal.service;

import com.ptithcm.portal.dto.GetScheduleCurrentRequest;
import com.ptithcm.portal.dto.ScheduleEntryDto;
import com.ptithcm.portal.entity.*;
import com.ptithcm.portal.repository.NhanVienRepository;
import com.ptithcm.portal.repository.SinhVienRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
public class ScheduleServiceImpl implements ScheduleService {

    @Autowired
    private SinhVienRepository sinhVienRepository;

    @Autowired
    private NhanVienRepository nhanVienRepository;

    private String getDayStringFromDate(java.sql.Date sqlDate) {
        if (sqlDate == null) return "unknown";
        LocalDate localDate = sqlDate.toLocalDate();
        DayOfWeek dayOfWeek = localDate.getDayOfWeek();
        switch (dayOfWeek) {
            case MONDAY: return "monday";
            case TUESDAY: return "tuesday";
            case WEDNESDAY: return "wednesday";
            case THURSDAY: return "thursday";
            case FRIDAY: return "friday";
            case SATURDAY: return "saturday";
            case SUNDAY: return "sunday";
            default: return "unknown";
        }
    }
    
    private String getDayVietnameseFromDayOfWeek(DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
            case MONDAY: return "Thứ 2";
            case TUESDAY: return "Thứ 3";
            case WEDNESDAY: return "Thứ 4";
            case THURSDAY: return "Thứ 5";
            case FRIDAY: return "Thứ 6";
            case SATURDAY: return "Thứ 7";
            case SUNDAY: return "Chủ Nhật";
            default: return "N/A";
        }
    }

    private String formatTime(LocalTime startTime, LocalTime endTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        if (startTime == null || endTime == null) {
            return "N/A";
        }
        return startTime.format(formatter) + " - " + endTime.format(formatter);
    }

    // Hàm tạo mã lớp từ LopTinChi
    private String generateClassCode(LopTinChi ltc) {
        if (ltc == null) return "N/A";
        return "LTC" + ltc.getId() + "_N" + ltc.getNhom();
    }

    @Override
    public Map<String, List<ScheduleEntryDto>> getStudentWeekSchedule(GetScheduleCurrentRequest request) {
        Map<String, List<ScheduleEntryDto>> scheduleResponse = createEmptyScheduleMap();
        Integer weekOffset = request.getWeekOffset(); // Default is 0 if null

        LocalDate today = LocalDate.now();
        LocalDate targetDate = today.plusWeeks(weekOffset);
        LocalDate startOfWeek = targetDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = targetDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        SinhVien sv;
        if (request.getStudentId() != null && !request.getStudentId().trim().isEmpty()) {
            sv = sinhVienRepository.findById(request.getStudentId())
                    .orElseThrow(() -> new RuntimeException("Student not found with ID: " + request.getStudentId()));
        } else if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            sv = sinhVienRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Student not found with Email: " + request.getEmail()));
        } else {
            throw new IllegalArgumentException("Student ID or Email is required.");
        }

        List<DangKy> listDangKys = sv.getDangKys() != null ? sv.getDangKys() : Collections.emptyList();
        //filter out registrations that are canceled
        listDangKys.removeIf(dk -> dk == null || dk.getTrangThai() == null || dk.getTrangThai().equals("Da huy"));

        for (DangKy dk : listDangKys) {
            if (dk == null || dk.getLopTinChi() == null) continue;
            LopTinChi ltc = dk.getLopTinChi();
            List<Lich> lichTrongLTC = ltc.getLichs() != null ? ltc.getLichs() : Collections.emptyList();

            for (Lich lich : lichTrongLTC) {
                if (lich == null || lich.getNgayHoc() == null) continue;

                LocalDate lichDate = lich.getNgayHoc().toLocalDate();
                if (lichDate.isBefore(startOfWeek) || lichDate.isAfter(endOfWeek)) {
                    continue; // Skip if not in the target week
                }

                String dayKey = getDayStringFromDate(lich.getNgayHoc());
                if ("unknown".equals(dayKey) || !scheduleResponse.containsKey(dayKey)) {
                    continue;
                }

                Buoi buoi = lich.getBuoi();
                String time = "N/A";
                if (buoi != null) {
                    time = formatTime(buoi.getGioBatDau(), buoi.getGioKetThuc());
                }

                String subject = "N/A";
                Mon mon = ltc.getMon();
                if (mon != null) {
                    subject = mon.getTenMon();
                }

                String room = "N/A";
                PhongHoc phongHoc = lich.getPhongHoc();
                if (phongHoc != null) {
                    room = phongHoc.getTen();
                }
                String teacherName = "N/A"; // Placeholder, adjust if teacher info is available differently for student schedule
                if (ltc.getChiTietGiangDays() != null && !ltc.getChiTietGiangDays().isEmpty()) {
                    NhanVien gv = ltc.getChiTietGiangDays().get(0).getNhanVien(); // Get first assigned teacher
                    if (gv != null) {
                        teacherName = gv.getHo() + " " + gv.getTen();
                    }
                }

                String classCode = generateClassCode(ltc);

                scheduleResponse.get(dayKey).add(new ScheduleEntryDto(time, subject, room, teacherName, classCode));
            }
        }
        return scheduleResponse;
    }

    @Override
    public Map<String, List<ScheduleEntryDto>> getLecturerWeekSchedule(GetScheduleCurrentRequest request) {
        Map<String, List<ScheduleEntryDto>> scheduleResponse = createEmptyScheduleMap();
        Integer weekOffset = request.getWeekOffset(); // Default is 0 if null

        LocalDate today = LocalDate.now();
        LocalDate targetDate = today.plusWeeks(weekOffset);
        LocalDate startOfWeek = targetDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = targetDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        NhanVien nv;
        if (request.getLecturerId() != null && !request.getLecturerId().trim().isEmpty()) {
            try {
                Integer lecturerIdInt = Integer.parseInt(request.getLecturerId());
                nv = nhanVienRepository.findById(lecturerIdInt)
                        .orElseThrow(() -> new RuntimeException("Lecturer not found with ID: " + request.getLecturerId()));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid Lecturer ID format: " + request.getLecturerId());
            }
        } else if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            nv = nhanVienRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Lecturer not found with Email: " + request.getEmail()));
        } else {
            throw new IllegalArgumentException("Lecturer ID or Email is required.");
        }

        List<ChiTietGiangDay> teachingDetails = nv.getChiTietGiangDays() != null ? nv.getChiTietGiangDays() : Collections.emptyList();

        for (ChiTietGiangDay ctgd : teachingDetails) {
            if (ctgd == null || ctgd.getLopTinChi() == null) continue;
            LopTinChi ltc = ctgd.getLopTinChi();
            List<Lich> lichTrongLTC = ltc.getLichs() != null ? ltc.getLichs() : Collections.emptyList();

            for (Lich lich : lichTrongLTC) {
                if (lich == null || lich.getNgayHoc() == null) continue;

                LocalDate lichDate = lich.getNgayHoc().toLocalDate();
                if (lichDate.isBefore(startOfWeek) || lichDate.isAfter(endOfWeek)) {
                    continue; // Skip if not in the target week
                }

                String dayKey = getDayStringFromDate(lich.getNgayHoc());
                if ("unknown".equals(dayKey) || !scheduleResponse.containsKey(dayKey)) {
                    continue;
                }

                Buoi buoi = lich.getBuoi();
                String time = "N/A";
                if (buoi != null) {
                    time = formatTime(buoi.getGioBatDau(), buoi.getGioKetThuc());
                }

                String subject = "N/A";
                Mon mon = ltc.getMon();
                if (mon != null) {
                    subject = mon.getTenMon();
                }

                String room = "N/A";
                PhongHoc phongHoc = lich.getPhongHoc();
                if (phongHoc != null) {
                    room = phongHoc.getTen();
                }
                // For lecturer schedule, the teacher is the lecturer themselves
                String teacherName = nv.getHo() + " " + nv.getTen();

                String classCode = generateClassCode(ltc);

                scheduleResponse.get(dayKey).add(new ScheduleEntryDto(time, subject, room, teacherName, classCode));
            }
        }
        return scheduleResponse;
    }
    
    /**
     * Lấy danh sách lớp học trong 3 ngày tới của giảng viên
     * @param lecturerId ID của giảng viên
     * @param baseDate Ngày bắt đầu tính (mặc định là ngày hiện tại nếu null)
     * @return Danh sách các lớp học trong 3 ngày tới, sắp xếp theo thời gian
     */
    public List<Map<String, Object>> getLecturerUpcomingSchedule(Integer lecturerId, LocalDate baseDate) {
        if (baseDate == null) {
            baseDate = LocalDate.now();
        }
        
        // Xác định khoảng thời gian 3 ngày tới
        LocalDate startDate = baseDate;
        LocalDate endDate = baseDate.plusDays(2); // Hôm nay + 2 ngày = 3 ngày tổng cộng
        
        NhanVien nv = nhanVienRepository.findById(lecturerId)
                .orElseThrow(() -> new RuntimeException("Lecturer not found with ID: " + lecturerId));
                
        List<ChiTietGiangDay> teachingDetails = nv.getChiTietGiangDays() != null ? 
                nv.getChiTietGiangDays() : Collections.emptyList();
                
        List<Map<String, Object>> upcomingClasses = new ArrayList<>();
        
        for (ChiTietGiangDay ctgd : teachingDetails) {
            if (ctgd == null || ctgd.getLopTinChi() == null) continue;
            LopTinChi ltc = ctgd.getLopTinChi();
            List<Lich> lichTrongLTC = ltc.getLichs() != null ? ltc.getLichs() : Collections.emptyList();
            
            for (Lich lich : lichTrongLTC) {
                if (lich == null || lich.getNgayHoc() == null) continue;
                
                LocalDate lichDate = lich.getNgayHoc().toLocalDate();
                
                // Chỉ lấy lịch học trong khoảng 3 ngày tới
                if (lichDate.isBefore(startDate) || lichDate.isAfter(endDate)) {
                    continue;
                }
                
                Buoi buoi = lich.getBuoi();
                String time = "N/A";
                if (buoi != null) {
                    time = formatTime(buoi.getGioBatDau(), buoi.getGioKetThuc());
                }
                
                String subject = "N/A";
                Mon mon = ltc.getMon();
                if (mon != null) {
                    subject = mon.getTenMon();
                }
                
                String room = "N/A";
                PhongHoc phongHoc = lich.getPhongHoc();
                if (phongHoc != null) {
                    room = phongHoc.getTen();
                }
                
                String classCode = generateClassCode(ltc);
                String dayName = getDayVietnameseFromDayOfWeek(lichDate.getDayOfWeek());
                
                Map<String, Object> classInfo = new HashMap<>();
                classInfo.put("id", classCode);
                classInfo.put("name", subject);
                classInfo.put("time", time);
                classInfo.put("room", room);
                classInfo.put("day", dayName);
                classInfo.put("date", lichDate);
                
                upcomingClasses.add(classInfo);
            }
        }
        
        // Sắp xếp lớp học theo ngày và giờ
        upcomingClasses.sort((c1, c2) -> {
            LocalDate date1 = (LocalDate) c1.get("date");
            LocalDate date2 = (LocalDate) c2.get("date");
            int dateCompare = date1.compareTo(date2);
            
            if (dateCompare != 0) {
                return dateCompare;
            }
            
            // Nếu cùng ngày, sắp xếp theo giờ học
            String time1 = (String) c1.get("time");
            String time2 = (String) c2.get("time");
            return time1.compareTo(time2);
        });
        
        // Loại bỏ trường date khỏi kết quả trả về
        upcomingClasses.forEach(cls -> cls.remove("date"));
        
        return upcomingClasses;
    }

    private Map<String, List<ScheduleEntryDto>> createEmptyScheduleMap() {
        Map<String, List<ScheduleEntryDto>> scheduleMap = new HashMap<>();
        String[] days = {"monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"};
        for (String day : days) {
            scheduleMap.put(day, new ArrayList<>());
        }
        return scheduleMap;
    }
}
