package com.ptithcm.portal.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ptithcm.portal.dto.AvailableCreditClassDto;
import com.ptithcm.portal.dto.CreditRegistrationRequestDto;
import com.ptithcm.portal.dto.ProfileEditRequest;
import com.ptithcm.portal.dto.StudentScoreDto;
import com.ptithcm.portal.dto.TuitionResponseDto;
import com.ptithcm.portal.service.SinhVienService;
import com.ptithcm.portal.service.TuitionService;
import com.ptithcm.portal.service.CreditRegistrationService;
import com.ptithcm.portal.util.JWTUtil;

import jakarta.persistence.EntityNotFoundException;

import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import com.ptithcm.portal.dto.RegisteredCreditClassDto;

import com.ptithcm.portal.dto.DashboardSVDto;
import com.ptithcm.portal.repository.NhanVienRepository;
import com.ptithcm.portal.entity.NhanVien;
import com.ptithcm.portal.dto.SinhVienDTO;
import com.ptithcm.portal.entity.SinhVien;

@RestController
@RequestMapping("/api/student")
public class StudentController {
    @Autowired
    private SinhVienService sinhVienService;

    @Autowired
    private TuitionService tuitionService;

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Autowired
    private CreditRegistrationService creditRegistrationService;

    @Autowired
    private JWTUtil jwtUtil;

    @PutMapping(path = "profile/update", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> updateStudent(@RequestBody ProfileEditRequest entity,
            @RequestHeader("Authorization") String token) {
        String mssv = jwtUtil.getSubject(token.substring(7));
        if (mssv == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        if (!entity.getMssv().equals(mssv)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        try {
            sinhVienService.updateStudent(entity.getMssv(), entity.getDiaChi(), entity.getSdt(), entity.getNgaySinh());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
        return ResponseEntity.ok("Update student successfully");
    }

    private boolean isNhanVien() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            String email = userDetails.getUsername();
            Optional<NhanVien> nhanVienOpt = nhanVienRepository.findByEmail(email);
            if (nhanVienOpt.isPresent()) {
                NhanVien nhanVien = nhanVienOpt.get();
                return "Nhan vien".equals(nhanVien.getChucVu());
            }
        }
        return false;
    }

    @GetMapping
    public ResponseEntity<List<SinhVienDTO>> getAllStudents() {
        List<SinhVien> students = sinhVienService.getAllStudents();

        List<SinhVienDTO> studentDTOs = students.stream()
                .map(student -> new SinhVienDTO(student))
                .collect(Collectors.toList());

        return ResponseEntity.ok(studentDTOs);
    }

    @GetMapping("/{mssv}")
    public ResponseEntity<?> getStudentByMssv(@PathVariable String mssv) {
        Map<String, Object> student = sinhVienService.getStudentDTOById(mssv);
        if (student != null) {
            return ResponseEntity.ok(student);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Object> createSinhVienDTO(@RequestBody @Validated SinhVienDTO sinhVienDTO) {
        if (!isNhanVien()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "forbidden", "message", "Bạn không có quyền thực hiện hành động này"));
        }
        try {
            SinhVienDTO createdSinhVienDTO = sinhVienService.createSinhVienDTO(sinhVienDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdSinhVienDTO);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "conflict");
            Map<String, Boolean> details = new HashMap<>();
            errorResponse.put("details", details);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{mssv}")
    public ResponseEntity<?> deleteStudent(@PathVariable String mssv) {
        if (!isNhanVien()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            sinhVienService.deleteStudent(mssv);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @PutMapping("/{mssv}")
    public ResponseEntity<Object> updateSinhVienDTO(@PathVariable String mssv,
            @RequestBody @Validated SinhVienDTO sinhVienDTO) {
        if (!isNhanVien()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "forbidden", "message", "Bạn không có quyền thực hiện hành động này"));
        }
        try {
            SinhVienDTO updatedSinhVienDTO = sinhVienService.updateSinhVienDTO(mssv, sinhVienDTO);
            return ResponseEntity.ok(updatedSinhVienDTO);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "conflict");
            Map<String, Boolean> details = new HashMap<>();
            errorResponse.put("details", details);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "not_found", "message", "Sinh viên không tồn tại"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "internal_server_error", "message", "Lỗi hệ thống"));
        }
    }

    @GetMapping(path = "scores", produces = "application/json")
    public ResponseEntity<?> getStudentScores(@RequestHeader("Authorization") String token) {
        String mssv = jwtUtil.getSubject(token.substring(7));
        if (mssv == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
          try {
            List<StudentScoreDto> scores = sinhVienService.getStudentScores(mssv);
            return ResponseEntity.ok(scores);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving scores: " + e.getMessage());
        }
    }

    /**
     * Get tuition data for the authenticated student
     * @param token JWT token containing student ID
     * @return TuitionResponseDto containing payment history, payment periods, and available semesters
     */
    @GetMapping(path = "tuition", produces = "application/json")
    public ResponseEntity<?> getTuitionData(@RequestHeader("Authorization") String token) {
        String mssv = jwtUtil.getSubject(token.substring(7));
        if (mssv == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        try {
            TuitionResponseDto response = tuitionService.getTuitionData(mssv);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving tuition data: " + e.getMessage());
        }
    }

    /**
     * Get tuition data for the authenticated student and specific semester
     * @param token JWT token containing student ID
     * @param semesterId The semester ID
     * @return TuitionResponseDto containing filtered data for the specific semester
     */
    @GetMapping(path = "tuition/semester", produces = "application/json")
    public ResponseEntity<?> getTuitionDataBySemester(
            @RequestHeader("Authorization") String token,
            @RequestParam String semesterId) {
        String mssv = jwtUtil.getSubject(token.substring(7));
        if (mssv == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        try {
            TuitionResponseDto response = tuitionService.getTuitionDataBySemester(mssv, semesterId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving tuition data: " + e.getMessage());
        }
    }

    @GetMapping("/credit-classes/available")
    // @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    public ResponseEntity<List<AvailableCreditClassDto>> getAvailableCreditClasses(
            @RequestHeader("Authorization") String token) {
        String studentMssv = jwtUtil.getSubject(token.substring(7));
        if (studentMssv == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        List<AvailableCreditClassDto> classes = creditRegistrationService.getAvailableCreditClasses(studentMssv);
        return ResponseEntity.ok(classes);
    }

    @PostMapping("/credit-classes/register")
    // @PreAuthorize("hasRole('STUDENT')") // Only students can register
    public ResponseEntity<?> registerCreditClass(
            @RequestHeader("Authorization") String token,
            @RequestBody CreditRegistrationRequestDto registrationRequest) {
        String studentMssv = jwtUtil.getSubject(token.substring(7));
        if (studentMssv == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized access.");
        }

        try {
            creditRegistrationService.registerCreditClass(studentMssv, registrationRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body("Đăng ký lớp tín chỉ thành công.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/credit-classes/registered")
    // @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    public ResponseEntity<?> getRegisteredCreditClasses( // Changed return type to ResponseEntity<?>
            @RequestHeader("Authorization") String token) {
        String studentMssv = jwtUtil.getSubject(token.substring(7));
        if (studentMssv == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        try {
            List<RegisteredCreditClassDto> classes = creditRegistrationService.getRegisteredCreditClasses(studentMssv);
            return ResponseEntity.ok(classes);
        } catch (Exception e) {
            // Log the exception for server-side review
            // logger.error("Error fetching registered classes for student {}: {}",
            // studentMssv, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving registered classes: " + e.getMessage());
        }
    }

    @DeleteMapping("/credit-classes/register")
    // @PreAuthorize("hasRole('STUDENT')") // Only students can cancel their own
    // registrations
    public ResponseEntity<?> cancelCreditClassRegistration(
            @RequestHeader("Authorization") String token,
            @RequestParam Integer dangKyId) {
        String studentMssv = jwtUtil.getSubject(token.substring(7));
        if (studentMssv == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized access.");
        }

        try {
            creditRegistrationService.cancelCreditClassRegistration(studentMssv, dangKyId);
            return ResponseEntity.ok("Hủy đăng ký lớp tín chỉ thành công.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
