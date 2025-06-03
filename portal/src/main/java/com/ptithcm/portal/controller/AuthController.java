package com.ptithcm.portal.controller;

import com.ptithcm.portal.dto.AuthRequest;
import com.ptithcm.portal.dto.AuthResponse;
import com.ptithcm.portal.dto.DashboardGVDto;
import com.ptithcm.portal.dto.DashboardNVDto;
import com.ptithcm.portal.dto.DashboardSVDto;
import com.ptithcm.portal.dto.PasswordChangeRequest;
import com.ptithcm.portal.dto.PasswordChangeResponse;
import com.ptithcm.portal.service.SinhVienService;
import com.ptithcm.portal.service.LecturerService;
import com.ptithcm.portal.service.NhanVienService;
import com.ptithcm.portal.service.AuthService;
import com.ptithcm.portal.service.UserDetailsServiceImpl;
import com.ptithcm.portal.util.JWTUtil;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @Autowired
    private SinhVienService sinhVienService;

    @Autowired
    private LecturerService lecturerService;

    @Autowired
    private AuthService authService;

    @Autowired
    private NhanVienService nhanVienService;

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthRequest authenticationRequest)
            throws Exception {
        authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());
        final UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername(authenticationRequest.getUsername());
        final String token = jwtUtil.generateToken(userDetails.getUsername());
        return ResponseEntity.ok(new AuthResponse(token, userDetails.getAuthorities().toArray()[0].toString()));
    }

    @GetMapping(path = "/me", produces = "application/json")
    public ResponseEntity<?> getUserInfo(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.substring(7);
        String username = jwtUtil.getSubject(token);
        if (username != null) {
            UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername(username);
            if (userDetails != null) {
                String role = userDetails.getAuthorities().toArray()[0].toString();
                if (role.equals("ROLE_SINHVIEN")) {
                    DashboardSVDto dashboardSVDto = sinhVienService.getDashBoardInfo(username);
                    System.out.println(dashboardSVDto);
                    return ResponseEntity.ok(dashboardSVDto);
                } else if (role.equals("ROLE_NHANVIEN")) {
                    DashboardNVDto dashboardNVDto = nhanVienService.getDashBoardInfo(username);
                    System.out.println(dashboardNVDto);
                    return ResponseEntity.ok(dashboardNVDto);
                } else if (role.equals("ROLE_GIAOVIEN")) {
                    DashboardGVDto dashboardGVDto = lecturerService.getDashBoardInfo(username);
                    System.out.println(dashboardGVDto);
                    return ResponseEntity.ok(dashboardGVDto);
                }
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody PasswordChangeRequest request, Principal principal) {
        try {
            String username = principal.getName();
            boolean result = authService.changePassword(username, request.getCurrentPassword(),
                    request.getNewPassword());

            if (result) {
                return ResponseEntity.ok(new PasswordChangeResponse(true, "Đổi mật khẩu thành công"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new PasswordChangeResponse(false, "Không thể đổi mật khẩu"));
            }
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new PasswordChangeResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new PasswordChangeResponse(false, "Lỗi server: " + e.getMessage()));
        }
    }

    @PostMapping("/verify-password")
    public ResponseEntity<?> verifyPassword(@RequestBody PasswordChangeRequest request, Principal principal) {
        try {
            String username = principal.getName();
            boolean result = authService.verifyPassword(username, request.getCurrentPassword());

            if (result) {
                return ResponseEntity.ok(new PasswordChangeResponse(true, "Mật khẩu đúng"));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new PasswordChangeResponse(false, "Mật khẩu không đúng"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new PasswordChangeResponse(false, "Lỗi server: " + e.getMessage()));
        }
    }

    private void authenticate(String username, String password) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }
}
