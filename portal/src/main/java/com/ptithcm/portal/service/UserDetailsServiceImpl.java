package com.ptithcm.portal.service;

import com.ptithcm.portal.entity.NhanVien;
import com.ptithcm.portal.entity.SinhVien;
import com.ptithcm.portal.repository.NhanVienRepository;
import com.ptithcm.portal.repository.SinhVienRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private SinhVienRepository sinhVienRepository;

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<SinhVien> sinhVienOpt = sinhVienRepository.findByMssv(username);
        if (sinhVienOpt.isPresent()) {
            SinhVien sv = sinhVienOpt.get();
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_SINHVIEN"));
            return new User(sv.getMssv(), sv.getPwdHash(), authorities);
        }

        Optional<NhanVien> nhanVienOpt = nhanVienRepository.findByEmail(username);
        if (nhanVienOpt.isPresent()) {
            NhanVien nv = nhanVienOpt.get();
            List<GrantedAuthority> authorities = new ArrayList<>();
            // Example role mapping based on chucVu
            if ("Giao vien".equalsIgnoreCase(nv.getChucVu())) {
                authorities.add(new SimpleGrantedAuthority("ROLE_GIAOVIEN"));
            } else if ("Nhan vien".equalsIgnoreCase(nv.getChucVu())) {
                 authorities.add(new SimpleGrantedAuthority("ROLE_NHANVIEN"));
            } else {
                // Default role or more specific roles based on chucVu
                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            }
            // Add other roles if necessary
            return new User(nv.getEmail(), nv.getPwdHash(), authorities);
        }

        throw new UsernameNotFoundException("User not found with username: " + username);
    }
}
