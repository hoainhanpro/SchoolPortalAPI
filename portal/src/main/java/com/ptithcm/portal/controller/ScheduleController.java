package com.ptithcm.portal.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ptithcm.portal.dto.GetScheduleCurrentRequest;
import com.ptithcm.portal.dto.ScheduleEntryDto;
import com.ptithcm.portal.service.ScheduleService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/schedule")
@CrossOrigin(origins = "*")
public class ScheduleController {
    @Autowired
    private ScheduleService scheduleService; 

    @PostMapping(path = "/current", produces = "application/json")
    public ResponseEntity<Map<String, List<ScheduleEntryDto>>> getWeekSchedule(@RequestBody GetScheduleCurrentRequest request) {
        try {
            if (request == null || request.getUserType() == null || request.getUserType().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createEmptyScheduleResponseWithMessage("User type is required."));
            }

            Map<String, List<ScheduleEntryDto>> scheduleResponse;
            String userType = request.getUserType().toLowerCase();

            if ("student".equals(userType)) {
                scheduleResponse = scheduleService.getStudentWeekSchedule(request);
            } else if ("lecturer".equals(userType)) {
                scheduleResponse = scheduleService.getLecturerWeekSchedule(request);
            } else {
                return ResponseEntity.badRequest().body(createEmptyScheduleResponseWithMessage("Invalid user type specified."));
            }
            
            return ResponseEntity.ok(scheduleResponse);

        } catch (IllegalArgumentException e) {
            System.err.println("Bad request for schedule: " + e.getMessage());
            return ResponseEntity.badRequest().body(createEmptyScheduleResponseWithMessage(e.getMessage()));
        } catch (RuntimeException e) {
            System.err.println("Error fetching schedule: " + e.getMessage());
            HttpStatus status = (e.getMessage() != null && e.getMessage().toLowerCase().contains("not found"))
                                ? HttpStatus.NOT_FOUND
                                : HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(createEmptyScheduleResponseWithMessage("Error: " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("Unexpected error fetching schedule: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createEmptyScheduleResponseWithMessage("Unexpected error occurred."));
        }
    }
    
    @GetMapping("/lecturer/{lecturerId}")
    public ResponseEntity<Map<String, List<ScheduleEntryDto>>> getLecturerSchedule(
            @PathVariable String lecturerId,
            @RequestParam(required = false, defaultValue = "0") Integer weekOffset) {
        try {
            GetScheduleCurrentRequest request = new GetScheduleCurrentRequest();
            request.setLecturerId(lecturerId);
            request.setUserType("lecturer");
            request.setWeekOffset(weekOffset);
            
            Map<String, List<ScheduleEntryDto>> scheduleResponse = scheduleService.getLecturerWeekSchedule(request);
            return ResponseEntity.ok(scheduleResponse);
            
        } catch (IllegalArgumentException e) {
            System.err.println("Bad request for lecturer schedule: " + e.getMessage());
            return ResponseEntity.badRequest().body(createEmptyScheduleResponseWithMessage(e.getMessage()));
        } catch (RuntimeException e) {
            System.err.println("Error fetching lecturer schedule: " + e.getMessage());
            HttpStatus status = (e.getMessage() != null && e.getMessage().toLowerCase().contains("not found"))
                                ? HttpStatus.NOT_FOUND
                                : HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(createEmptyScheduleResponseWithMessage("Error: " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("Unexpected error fetching lecturer schedule: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createEmptyScheduleResponseWithMessage("Unexpected error occurred."));
        }
    }

    @GetMapping("/lecturer/{lecturerId}/upcoming")
    public ResponseEntity<List<Map<String, Object>>> getLecturerUpcomingSchedule(
            @PathVariable Integer lecturerId,
            @RequestParam(required = false) LocalDate baseDate) {
        try {
            List<Map<String, Object>> upcomingClasses = scheduleService.getLecturerUpcomingSchedule(lecturerId, baseDate);
            return ResponseEntity.ok(upcomingClasses);
        } catch (RuntimeException e) {
            System.err.println("Lỗi nghiệp vụ khi lấy lịch sắp tới: " + e.getMessage());
            e.printStackTrace();
            HttpStatus status = (e.getMessage() != null && e.getMessage().toLowerCase().contains("not found"))
                                ? HttpStatus.NOT_FOUND 
                                : HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(null);
        } catch (Exception e) {
            System.err.println("Lỗi không xác định khi lấy lịch sắp tới: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    private Map<String, List<ScheduleEntryDto>> createEmptyScheduleResponse() {
        Map<String, List<ScheduleEntryDto>> emptySchedule = new HashMap<>();
        String[] days = {"monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"};
        for (String day : days) {
            emptySchedule.put(day, new ArrayList<>());
        }
        return emptySchedule;
    }

    private Map<String, List<ScheduleEntryDto>> createEmptyScheduleResponseWithMessage(String message) {
        Map<String, List<ScheduleEntryDto>> response = createEmptyScheduleResponse();
        System.out.println("Error response message: " + message); 
        return response;
    }
}
