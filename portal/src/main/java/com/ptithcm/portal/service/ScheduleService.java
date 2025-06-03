package com.ptithcm.portal.service;

import com.ptithcm.portal.dto.GetScheduleCurrentRequest;
import com.ptithcm.portal.dto.ScheduleEntryDto;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ScheduleService {
    Map<String, List<ScheduleEntryDto>> getStudentWeekSchedule(GetScheduleCurrentRequest request);
    Map<String, List<ScheduleEntryDto>> getLecturerWeekSchedule(GetScheduleCurrentRequest request); // Added method for lecturer schedule
    List<Map<String, Object>> getLecturerUpcomingSchedule(Integer lecturerId, LocalDate baseDate);
}
