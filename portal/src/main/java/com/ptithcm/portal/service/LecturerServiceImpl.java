package com.ptithcm.portal.service;

import com.ptithcm.portal.dto.DashboardGVDto;
import com.ptithcm.portal.dto.LecturerDto;
import com.ptithcm.portal.dto.NhanVienDto;
import com.ptithcm.portal.entity.NhanVien;
import com.ptithcm.portal.repository.NhanVienRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import jakarta.persistence.EntityNotFoundException;

@Service
public class LecturerServiceImpl implements LecturerService {

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Override
    public List<LecturerDto> getAllLecturersWithSchedule() {
        List<NhanVien> lecturers = nhanVienRepository.findAllGiangVienWithSchedule();
        
        return lecturers.stream()
                .map(lecturer -> new LecturerDto(
                        lecturer.getId(),
                        lecturer.getHo(),
                        lecturer.getTen(),
                        lecturer.getEmail(),
                        lecturer.getKhoa() != null ? lecturer.getKhoa().getTen() : "Chưa có khoa"
                ))
                .collect(Collectors.toList());
    }

    @Override
    public NhanVienDto getLecturerById(Integer id) {
        NhanVien nhanVien = nhanVienRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy giảng viên với ID: " + id));
        
        // Kiểm tra xem có phải là giảng viên không
        if (!"Giao vien".equals(nhanVien.getChucVu())) {
            throw new EntityNotFoundException("Người dùng này không phải là giảng viên!");
        }
        
        return convertToDto(nhanVien);
    }

    @Override
    public NhanVienDto updateLecturer(Integer id, NhanVienDto nhanVienDto) {
        NhanVien nhanVien = nhanVienRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy giảng viên với ID: " + id));
        
        // Kiểm tra xem có phải là giảng viên không
        if (!"Giao vien".equals(nhanVien.getChucVu())) {
            throw new EntityNotFoundException("Người dùng này không phải là giảng viên!");
        }
        
        // Cập nhật thông tin
        nhanVien.setHo(nhanVienDto.getHo());
        nhanVien.setTen(nhanVienDto.getTen());
        nhanVien.setNgaySinh(nhanVienDto.getNgaySinh());
        nhanVien.setGioiTinh(nhanVienDto.getGioiTinh());
        nhanVien.setDiaChi(nhanVienDto.getDiaChi());
        nhanVien.setSdt(nhanVienDto.getSdt());
        nhanVien.setEmail(nhanVienDto.getEmail());
        
        // Lưu vào database
        nhanVien = nhanVienRepository.save(nhanVien);
        
        return convertToDto(nhanVien);
    }
    
    @Override
    public DashboardGVDto getDashBoardInfo(String username) {
        NhanVien gv = nhanVienRepository.findByEmail(username)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy giảng viên với email: " + username));
        
        // Kiểm tra xem có phải là giảng viên không
        if (!"Giao vien".equals(gv.getChucVu())) {
            throw new EntityNotFoundException("Người dùng này không phải là giảng viên!");
        }
        
        DashboardGVDto dashboardGVDto = new DashboardGVDto();
        dashboardGVDto.setId(gv.getId());
        // Giả sử mã giảng viên là GV + id
        dashboardGVDto.setMaGV("GV" + gv.getId());
        dashboardGVDto.setHoTen(gv.getHo() + " " + gv.getTen());
        dashboardGVDto.setNgaySinh(gv.getNgaySinh());
        dashboardGVDto.setGioiTinh(gv.getGioiTinh());
        dashboardGVDto.setDiaChi(gv.getDiaChi());
        dashboardGVDto.setSdt(gv.getSdt());
        dashboardGVDto.setEmail(gv.getEmail());
        dashboardGVDto.setChucVu(gv.getChucVu());
        
        if (gv.getKhoa() != null) {
            dashboardGVDto.setKhoaId(gv.getKhoa().getId());
            dashboardGVDto.setKhoaTen(gv.getKhoa().getTen());
        }
        
        // Các thông tin có thể bổ sung thêm sau này
        dashboardGVDto.setHocHam(""); // Có thể thêm trường này vào entity NhanVien nếu cần
        dashboardGVDto.setHocVi(""); // Có thể thêm trường này vào entity NhanVien nếu cần
        
        return dashboardGVDto;
    }
    
    private NhanVienDto convertToDto(NhanVien nhanVien) {
        return new NhanVienDto(
                nhanVien.getId(),
                nhanVien.getHo(),
                nhanVien.getTen(),
                nhanVien.getNgaySinh(),
                nhanVien.getGioiTinh(),
                nhanVien.getDiaChi(),
                nhanVien.getSdt(),
                nhanVien.getEmail(),
                nhanVien.getChucVu(),
                nhanVien.getKhoa() != null ? nhanVien.getKhoa().getId() : null,
                nhanVien.getKhoa() != null ? nhanVien.getKhoa().getTen() : null
        );
    }
} 