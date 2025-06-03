// NhanVienService.java
package com.ptithcm.portal.service;

import com.ptithcm.portal.dto.DashboardNVDto;
import com.ptithcm.portal.dto.NhanVienDto;
import com.ptithcm.portal.dto.NhanVienResponseDto;
import com.ptithcm.portal.entity.Khoa;
import com.ptithcm.portal.entity.NhanVien;
import com.ptithcm.portal.repository.KhoaRepository;
import com.ptithcm.portal.repository.NhanVienRepository;
import jakarta.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Map;

@Service
public class NhanVienService {

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Autowired
    private KhoaRepository khoaRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public List<NhanVienDto> getAllNhanVien() {
        List<NhanVien> nhanViens = nhanVienRepository.findAll();
        return nhanViens.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<NhanVienDto> getNhanVienById(Integer id) {
        return nhanVienRepository.findById(id)
                .map(this::convertToDTO);
    }

    private NhanVienDto convertToDTO(NhanVien nhanVien) {
        NhanVienDto dto = new NhanVienDto();
        dto.setId(nhanVien.getId());
        dto.setHo(nhanVien.getHo());
        dto.setTen(nhanVien.getTen());
        dto.setEmail(nhanVien.getEmail());
        dto.setChucVu(nhanVien.getChucVu());
        dto.setKhoaId(nhanVien.getKhoa().getId());
        dto.setKhoaTen(nhanVien.getKhoa().getTen());
        dto.setNgaySinh(nhanVien.getNgaySinh());
        dto.setDiaChi(nhanVien.getDiaChi());
        dto.setSdt(nhanVien.getSdt());
        dto.setGioiTinh(nhanVien.getGioiTinh());
        if (nhanVien.getKhoa() != null) {
            dto.setKhoaId(nhanVien.getKhoa().getId());
            dto.setKhoaTen(nhanVien.getKhoa().getTen());
        }

        return dto;
    }

    @Transactional
    public NhanVienResponseDto createNhanVien(NhanVienDto nhanVienDTO) {
        // Validate data
        validateData(nhanVienDTO);

        // Kiểm tra trùng email
        if (nhanVienRepository.existsByEmail(nhanVienDTO.getEmail())) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }

        // Kiểm tra trùng số điện thoại
        if (nhanVienRepository.existsBySdt(nhanVienDTO.getSdt())) {
            throw new IllegalArgumentException("Số điện thoại đã tồn tại");
        }

        // Find Khoa
        Khoa khoa = khoaRepository.findById(nhanVienDTO.getKhoaId())
                .orElseThrow(() -> new IllegalArgumentException("Mã khoa không hợp lệ"));

        // Create NhanVien entity
        NhanVien nhanVien = new NhanVien();
        nhanVien.setHo(nhanVienDTO.getHo());
        nhanVien.setTen(nhanVienDTO.getTen());
        nhanVien.setNgaySinh(nhanVienDTO.getNgaySinh());
        nhanVien.setGioiTinh(nhanVienDTO.getGioiTinh());
        nhanVien.setDiaChi(nhanVienDTO.getDiaChi());
        nhanVien.setSdt(nhanVienDTO.getSdt());
        nhanVien.setEmail(nhanVienDTO.getEmail());
        nhanVien.setChucVu(nhanVienDTO.getChucVu());
        nhanVien.setKhoa(khoa);
        nhanVien.setPwdHash(passwordEncoder.encode(nhanVienDTO.getMatKhau()));

        NhanVien savedNhanVien = nhanVienRepository.save(nhanVien);

        return mapToResponseDTO(savedNhanVien);
    }

    private void validateData(NhanVienDto nhanVienDTO) {
        // Check for null or empty fields
        if (nhanVienDTO.getHo() == null || nhanVienDTO.getHo().isEmpty()) {
            throw new IllegalArgumentException("Họ không được để trống");
        }
        if (nhanVienDTO.getTen() == null || nhanVienDTO.getTen().isEmpty()) {
            throw new IllegalArgumentException("Tên không được để trống");
        }
        if (nhanVienDTO.getNgaySinh() == null) {
            throw new IllegalArgumentException("Ngày sinh không được để trống");
        }
        if (nhanVienDTO.getGioiTinh() == null || nhanVienDTO.getGioiTinh().isEmpty()) {
            throw new IllegalArgumentException("Giới tính không được để trống");
        }
        if (nhanVienDTO.getSdt() == null || nhanVienDTO.getSdt().isEmpty()) {
            throw new IllegalArgumentException("Số điện thoại không được để trống");
        }
        if (nhanVienDTO.getEmail() == null || nhanVienDTO.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email không được để trống");
        }
        if (nhanVienDTO.getChucVu() == null || nhanVienDTO.getChucVu().isEmpty()) {
            throw new IllegalArgumentException("Chức vụ không được để trống");
        }
        if (nhanVienDTO.getKhoaId() == null) {
            throw new IllegalArgumentException("Mã khoa không được để trống");
        }
    }

    private NhanVienResponseDto mapToResponseDTO(NhanVien nhanVien) {
        return new NhanVienResponseDto(
                nhanVien.getId(),
                nhanVien.getHo(),
                nhanVien.getTen(),
                nhanVien.getNgaySinh(),
                nhanVien.getGioiTinh(),
                nhanVien.getDiaChi(),
                nhanVien.getSdt(),
                nhanVien.getEmail(),
                nhanVien.getChucVu(),
                nhanVien.getKhoa().getId());
    }

    @Transactional
    public NhanVienDto updateNhanVien(Integer id, NhanVienDto nhanVienDTO) {
        Optional<NhanVien> existingNhanVienOptional = nhanVienRepository.findById(id);

        if (existingNhanVienOptional.isPresent()) {
            NhanVien existingNhanVien = existingNhanVienOptional.get();

            // Validate data
            validateData(nhanVienDTO);

            if (nhanVienRepository.existsByEmailAndIdNot(nhanVienDTO.getEmail(), id)) {
                throw new IllegalArgumentException("Email đã tồn tại");
            }

            if (nhanVienRepository.existsBySdtAndIdNot(nhanVienDTO.getSdt(), id)) {
                throw new IllegalArgumentException("Số điện thoại đã tồn tại");
            }

            existingNhanVien.setHo(nhanVienDTO.getHo());
            existingNhanVien.setTen(nhanVienDTO.getTen());
            existingNhanVien.setNgaySinh(nhanVienDTO.getNgaySinh());
            existingNhanVien.setGioiTinh(nhanVienDTO.getGioiTinh());
            existingNhanVien.setDiaChi(nhanVienDTO.getDiaChi());
            existingNhanVien.setSdt(nhanVienDTO.getSdt());
            existingNhanVien.setEmail(nhanVienDTO.getEmail());
            existingNhanVien.setChucVu(nhanVienDTO.getChucVu());

            // Kiểm tra và cập nhật mật khẩu nếu có
            if (nhanVienDTO.getMatKhau() != null && !nhanVienDTO.getMatKhau().isEmpty()) {
                existingNhanVien.setPwdHash(passwordEncoder.encode(nhanVienDTO.getMatKhau()));
            }

            // Xử lý cập nhật khoa
            Optional<Khoa> khoaOptional = khoaRepository.findById(nhanVienDTO.getKhoaId());
            khoaOptional.ifPresent(existingNhanVien::setKhoa);

            // Lưu thay đổi vào database
            NhanVien updatedNhanVien = nhanVienRepository.save(existingNhanVien);

            // Chuyển đổi thành NhanVienDto để trả về
            return convertToDTO(updatedNhanVien);

        } else {
            throw new EntityNotFoundException("Nhân viên không tồn tại");
        }
    }

    public void deleteNhanVien(Integer id) {
        nhanVienRepository.deleteById(id);
    }

    public DashboardNVDto getDashBoardInfo(String email) {
        NhanVien nhanVien = nhanVienRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy nhân viên với email: " + email));

        DashboardNVDto dto = new DashboardNVDto();
        dto.setId(nhanVien.getId());
        dto.setHo(nhanVien.getHo());
        dto.setTen(nhanVien.getTen());
        dto.setNgaySinh(nhanVien.getNgaySinh());
        dto.setGioiTinh(nhanVien.getGioiTinh());
        dto.setDiaChi(nhanVien.getDiaChi());
        dto.setSdt(nhanVien.getSdt());
        dto.setEmail(nhanVien.getEmail());
        dto.setChucVu(nhanVien.getChucVu());

        Khoa khoa = nhanVien.getKhoa();
        if (khoa != null) {
            dto.setKhoaId(khoa.getId());
            dto.setKhoaTen(khoa.getTen());
        } else {
            dto.setKhoaId(null);
            dto.setKhoaTen(null);
        }

        return dto;
    }
}