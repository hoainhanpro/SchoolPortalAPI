package com.ptithcm.portal.service;

import com.ptithcm.portal.entity.NhanVien;
import com.ptithcm.portal.repository.NhanVienRepository;
import com.ptithcm.portal.repository.SinhVienRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class AuthServiceTest {

    @Mock
    private SinhVienRepository sinhVienRepository;

    @Mock
    private NhanVienRepository nhanVienRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private NhanVien nhanVien;
    private final String email = "giaovien@example.com";
    private final String currentPassword = "password123";
    private final String newPassword = "newPassword123";
    private final String hashedPassword = "hashedPassword";
    private final String newHashedPassword = "newHashedPassword";

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        nhanVien = new NhanVien();
        nhanVien.setId(1);
        nhanVien.setEmail(email);
        nhanVien.setPwdHash(hashedPassword);
        nhanVien.setChucVu("Giao vien");

        when(nhanVienRepository.findByEmail(email)).thenReturn(Optional.of(nhanVien));
        when(passwordEncoder.matches(currentPassword, hashedPassword)).thenReturn(true);
        when(passwordEncoder.matches(anyString(), eq(hashedPassword))).thenReturn(false);
        when(passwordEncoder.encode(newPassword)).thenReturn(newHashedPassword);
        when(nhanVienRepository.save(any(NhanVien.class))).thenReturn(nhanVien);
    }

    @Test
    public void testChangePassword_ForGiaoVien_WithCorrectPassword_ShouldSucceed() throws Exception {
        boolean result = authService.changePassword(email, currentPassword, newPassword);

        assertTrue(result);
        verify(passwordEncoder).matches(currentPassword, hashedPassword);
        verify(passwordEncoder).encode(newPassword);
        verify(nhanVienRepository).save(nhanVien);
        assertEquals(newHashedPassword, nhanVien.getPwdHash());
    }

    @Test
    public void testChangePassword_WithIncorrectPassword_ShouldThrowException() {
        when(passwordEncoder.matches(currentPassword, hashedPassword)).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> {
            authService.changePassword(email, currentPassword, newPassword);
        });

        verify(passwordEncoder).matches(currentPassword, hashedPassword);
        verify(nhanVienRepository, never()).save(any(NhanVien.class));
    }

    @Test
    public void testChangePassword_WithNonExistentUser_ShouldThrowException() {
        when(nhanVienRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(sinhVienRepository.findByMssv(anyString())).thenReturn(Optional.empty());

        Exception exception = assertThrows(Exception.class, () -> {
            authService.changePassword("nonexistent@example.com", currentPassword, newPassword);
        });

        assertTrue(exception.getMessage().contains("Không tìm thấy tài khoản"));
        verify(nhanVienRepository, never()).save(any(NhanVien.class));
    }
} 