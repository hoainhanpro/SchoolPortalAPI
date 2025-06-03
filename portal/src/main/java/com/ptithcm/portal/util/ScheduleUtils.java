package com.ptithcm.portal.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class để xử lý logic tính toán lịch học
 */
public class ScheduleUtils {
    
    /**
     * Tính toán tất cả các ngày học trong khoảng thời gian từ ngày bắt đầu đến ngày kết thúc
     * dựa trên thứ trong tuần được chỉ định
     * 
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @param thuInWeek Thứ trong tuần (2=Thứ 2, 3=Thứ 3, ..., 8=Chủ nhật)
     * @return Danh sách các ngày học
     */
    public static List<LocalDate> calculateScheduleDates(LocalDate startDate, LocalDate endDate, Integer thuInWeek) {
        List<LocalDate> scheduleDates = new ArrayList<>();
        
        if (startDate == null || endDate == null || thuInWeek == null) {
            return scheduleDates;
        }
        
        // Chuyển đổi thuInWeek sang DayOfWeek
        DayOfWeek targetDayOfWeek = convertThuInWeekToDayOfWeek(thuInWeek);
        if (targetDayOfWeek == null) {
            return scheduleDates;
        }
        
        // Tìm ngày đầu tiên trong tuần có thứ mong muốn từ startDate
        LocalDate currentDate = findNextOccurrenceOfDay(startDate, targetDayOfWeek);
        
        // Thêm tất cả các ngày có thứ mong muốn vào danh sách
        while (!currentDate.isAfter(endDate)) {
            scheduleDates.add(currentDate);
            currentDate = currentDate.plusWeeks(1); // Chuyển sang tuần tiếp theo
        }
        
        return scheduleDates;
    }
    
    /**
     * Chuyển đổi thuInWeek sang DayOfWeek
     * 
     * @param thuInWeek Thứ trong tuần (2=Thứ 2, 3=Thứ 3, ..., 8=Chủ nhật)
     * @return DayOfWeek tương ứng
     */
    private static DayOfWeek convertThuInWeekToDayOfWeek(Integer thuInWeek) {
        switch (thuInWeek) {
            case 2: return DayOfWeek.MONDAY;
            case 3: return DayOfWeek.TUESDAY;
            case 4: return DayOfWeek.WEDNESDAY;
            case 5: return DayOfWeek.THURSDAY;
            case 6: return DayOfWeek.FRIDAY;
            case 7: return DayOfWeek.SATURDAY;
            case 8: return DayOfWeek.SUNDAY;
            default: return null;
        }
    }
    
    /**
     * Tìm ngày tiếp theo có thứ mong muốn từ ngày cho trước
     * 
     * @param fromDate Ngày bắt đầu tìm
     * @param targetDay Thứ mong muốn
     * @return Ngày tiếp theo có thứ mong muốn
     */
    private static LocalDate findNextOccurrenceOfDay(LocalDate fromDate, DayOfWeek targetDay) {
        LocalDate date = fromDate;
        while (date.getDayOfWeek() != targetDay) {
            date = date.plusDays(1);
        }
        return date;
    }
    
    /**
     * Lấy tên thứ từ thuInWeek
     * 
     * @param thuInWeek Thứ trong tuần (2=Thứ 2, 3=Thứ 3, ..., 8=Chủ nhật)
     * @return Tên thứ
     */
    public static String getDayName(Integer thuInWeek) {
        switch (thuInWeek) {
            case 2: return "Thứ 2";
            case 3: return "Thứ 3";
            case 4: return "Thứ 4";
            case 5: return "Thứ 5";
            case 6: return "Thứ 6";
            case 7: return "Thứ 7";
            case 8: return "Chủ nhật";
            default: return "Không xác định";
        }
    }
} 