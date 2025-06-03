package com.ptithcm.portal.service;

import com.ptithcm.portal.entity.NhanVien;
import com.ptithcm.portal.entity.SinhVien;
import com.ptithcm.portal.repository.NhanVienRepository;
import com.ptithcm.portal.repository.SinhVienRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private SinhVienRepository sinhVienRepository;

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public boolean changePassword(String username, String currentPassword, String newPassword) throws Exception {
        // Kiểm tra xem tài khoản thuộc sinh viên hay nhân viên (giảng viên)
        Optional<SinhVien> sinhVienOpt = sinhVienRepository.findByMssv(username);
        if (sinhVienOpt.isPresent()) {
            return changePasswordForSinhVien(sinhVienOpt.get(), currentPassword, newPassword);
        }

        Optional<NhanVien> nhanVienOpt = nhanVienRepository.findByEmail(username);
        if (nhanVienOpt.isPresent()) {
            return changePasswordForNhanVien(nhanVienOpt.get(), currentPassword, newPassword);
        }

        throw new Exception("Không tìm thấy tài khoản với tên đăng nhập: " + username);
    }

    private boolean changePasswordForSinhVien(SinhVien sinhVien, String currentPassword, String newPassword) {
        // Kiểm tra mật khẩu hiện tại
        if (!passwordEncoder.matches(currentPassword, sinhVien.getPwdHash())) {
            throw new BadCredentialsException("Mật khẩu hiện tại không đúng");
        }

        // Mã hóa và lưu mật khẩu mới
        sinhVien.setPwdHash(passwordEncoder.encode(newPassword));
        sinhVienRepository.save(sinhVien);
        return true;
    }

    private boolean changePasswordForNhanVien(NhanVien nhanVien, String currentPassword, String newPassword) {
        // Kiểm tra mật khẩu hiện tại
        if (!passwordEncoder.matches(currentPassword, nhanVien.getPwdHash())) {
            throw new BadCredentialsException("Mật khẩu hiện tại không đúng");
        }

        // Mã hóa và lưu mật khẩu mới
        nhanVien.setPwdHash(passwordEncoder.encode(newPassword));
        nhanVienRepository.save(nhanVien);
        return true;
    }

    @Override
    public boolean verifyPassword(String username, String currentPassword) {
        // Kiểm tra xem tài khoản thuộc sinh viên hay nhân viên (giảng viên)
        Optional<SinhVien> sinhVienOpt = sinhVienRepository.findByMssv(username);
        if (sinhVienOpt.isPresent()) {
            return passwordEncoder.matches(currentPassword, sinhVienOpt.get().getPwdHash());
        }

        Optional<NhanVien> nhanVienOpt = nhanVienRepository.findByEmail(username); 
        if (nhanVienOpt.isPresent()) {
            return passwordEncoder.matches(currentPassword, nhanVienOpt.get().getPwdHash());
        }

        throw new BadCredentialsException("Không tìm thấy tài khoản với tên đăng nhập: " + username);
    }
} 