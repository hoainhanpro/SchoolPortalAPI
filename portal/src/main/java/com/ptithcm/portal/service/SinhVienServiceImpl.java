package com.ptithcm.portal.service;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.ptithcm.portal.dto.DashboardSVDto;
import com.ptithcm.portal.dto.SinhVienDTO;
import com.ptithcm.portal.dto.StudentScoreDto;
import com.ptithcm.portal.dto.LopInfoDTO;
import com.ptithcm.portal.entity.SinhVien;
import com.ptithcm.portal.entity.DangKy;
import com.ptithcm.portal.entity.Lop;
import com.ptithcm.portal.repository.DangKyRepository;
import com.ptithcm.portal.repository.SinhVienRepository;
import com.ptithcm.portal.repository.LopRepository;

@Service
public class SinhVienServiceImpl implements SinhVienService {
    @Autowired
    private SinhVienRepository sinhVienRepository;

    @Autowired
    private DangKyRepository dangKyRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private LopRepository lopRepository;

    @Override
    public DashboardSVDto getDashBoardInfo(String id) {
        DashboardSVDto sinhVienDto = new DashboardSVDto();
        SinhVien sinhVien = sinhVienRepository.findByMssv(id).orElse(null);
        if (sinhVien != null) {
            sinhVienDto.setMssv(sinhVien.getMssv());
            sinhVienDto.setLopTen(sinhVien.getLop().getTen());
            sinhVienDto.setKhoaTen(sinhVien.getLop().getKhoa().getTen());
            sinhVienDto.setNgaySinh(sinhVien.getNgaySinh());
            sinhVienDto.setGioiTinh(sinhVien.getGioiTinh());
            sinhVienDto.setDiaChi(sinhVien.getDiaChi());
            sinhVienDto.setSdt(sinhVien.getSdt());
            sinhVienDto.setEmail(sinhVien.getEmail());
            sinhVienDto.setHoTen(sinhVien.getHo().trim() + " " + sinhVien.getTen().trim());
            String coVanTen = sinhVien.getLop().getCoVans().get(0).getNhanVien().getHo().trim() + " "
                    + sinhVien.getLop().getCoVans().get(0).getNhanVien().getTen().trim();
            sinhVienDto.setCoVanTen(coVanTen);
        }
        return sinhVienDto;
    }

    @Override
    public void updateStudent(String mssv, String diaChi, String sdt, LocalDate ngaySinh) throws Exception {
        SinhVien sinhVien = sinhVienRepository.findByMssv(mssv).orElse(null);
        if (sinhVien != null) {
            sinhVien.setDiaChi(diaChi);
            sinhVien.setSdt(sdt);
            sinhVien.setNgaySinh(ngaySinh);
            sinhVienRepository.save(sinhVien);
        } else {
            throw new Exception("Sinh viên không tồn tại");
        }
    }

    @Override
    public List<StudentScoreDto> getStudentScores(String mssv) {
        List<DangKy> dangKyList = dangKyRepository.findByMssvAndTrangThaiDaXacNhan(mssv);

        return dangKyList.stream().map(dangKy -> {
            SinhVien sinhVien = dangKy.getSinhVien();
            String hoTen = sinhVien.getHo().trim() + " " + sinhVien.getTen().trim();
            String maMon = dangKy.getLopTinChi().getMon().getTenMon(); // Using ten mon as ma mon for now
            String tenMon = dangKy.getLopTinChi().getMon().getTenMon();
            String hocKy = dangKy.getLopTinChi().getHocKy().getTen();
            String namHoc = dangKy.getLopTinChi().getHocKy().getNamHoc().toString();
            Integer soTinChi = dangKy.getLopTinChi().getMon().getSoTinChi();

            StudentScoreDto dto = new StudentScoreDto(
                    sinhVien.getMssv(),
                    hoTen,
                    maMon,
                    tenMon,
                    hocKy,
                    namHoc,
                    soTinChi,
                    dangKy.getDiemcc(),
                    dangKy.getDiemkt(),
                    dangKy.getDiemthi());

            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<SinhVien> getAllStudents() {
        return sinhVienRepository.findAll();
    }

    @Override
    public Map<String, Object> getStudentDTOById(String id) {
        Optional<SinhVien> optSinhVien = sinhVienRepository.findById(id);

        if (optSinhVien.isPresent()) {
            SinhVien sv = optSinhVien.get();
            Map<String, Object> result = new HashMap<>();

            // Thêm thông tin sinh viên
            result.put("sinhVien", new SinhVienDTO(sv));

            // Thêm thông tin lớp nếu có
            if (sv.getLop() != null) {
                result.put("lop", new LopInfoDTO(sv.getLop().getId(), sv.getLop().getTen()));
            } else {
                result.put("lop", null);
            }

            return result;
        }

        return null;
    }

    @Override
    public SinhVienDTO createSinhVienDTO(SinhVienDTO sinhVienDTO) {
        // Kiểm tra trùng SDT và email, MSSV
        if (sinhVienRepository.existsByMssv(sinhVienDTO.getMssv())) {
            throw new IllegalArgumentException("MSSV đã tồn tại");
        }
        if (sinhVienRepository.existsByEmail(sinhVienDTO.getEmail())) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }
        if (sinhVienRepository.existsBySdt(sinhVienDTO.getSdt())) {
            throw new IllegalArgumentException("Số điện thoại đã tồn tại");
        }

        SinhVien sinhVien = new SinhVien();
        mapSinhVienDTOToSinhVien(sinhVienDTO, sinhVien);

        // Mã hóa mật khẩu trước khi lưu
        String encodedPassword = passwordEncoder.encode(sinhVienDTO.getPwdHash());
        sinhVien.setPwdHash(encodedPassword);

        try {
            SinhVien savedSinhVien = sinhVienRepository.save(sinhVien);
            return new SinhVienDTO(savedSinhVien);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Lỗi khi tạo sinh viên: " + e.getMessage(), e);
        }
    }

    @Override
    public SinhVienDTO updateSinhVienDTO(String mssv, SinhVienDTO sinhVienDTO) {
        SinhVien existingSinhVien = sinhVienRepository.findById(mssv)
                .orElseThrow(() -> new IllegalArgumentException("Sinh viên không tồn tại."));

        if (!mssv.equals(sinhVienDTO.getMssv()) &&
                sinhVienRepository.existsByMssv(sinhVienDTO.getMssv())) {
            throw new IllegalArgumentException("MSSV đã tồn tại");
        }
        if (sinhVienRepository.existsByEmailAndMssvNot(sinhVienDTO.getEmail(), mssv)) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }
        if (sinhVienRepository.existsBySdtAndMssvNot(sinhVienDTO.getSdt(), mssv)) {
            throw new IllegalArgumentException("Số điện thoại đã tồn tại");
        }

        mapSinhVienDTOToSinhVien(sinhVienDTO, existingSinhVien);

        if (sinhVienDTO.getPwdHash() != null && !sinhVienDTO.getPwdHash().isEmpty()) {
            String encodedPassword = passwordEncoder.encode(sinhVienDTO.getPwdHash());
            existingSinhVien.setPwdHash(encodedPassword);
        }

        try {
            SinhVien updatedSinhVien = sinhVienRepository.save(existingSinhVien);
            return new SinhVienDTO(updatedSinhVien);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Lỗi khi cập nhật sinh viên: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteStudent(String mssv) {
        if (sinhVienRepository.existsByMssvInOtherTables(mssv)) {
            throw new IllegalArgumentException(
                    "Sinh viên này không thể xóa.");
        }

        sinhVienRepository.deleteById(mssv);
    }

    private void mapSinhVienDTOToSinhVien(SinhVienDTO sinhVienDTO, SinhVien sinhVien) {
        sinhVien.setMssv(sinhVienDTO.getMssv());
        sinhVien.setHo(sinhVienDTO.getHo());
        sinhVien.setTen(sinhVienDTO.getTen());
        sinhVien.setNgaySinh(sinhVienDTO.getNgaySinh());
        sinhVien.setGioiTinh(sinhVienDTO.getGioiTinh());
        sinhVien.setDiaChi(sinhVienDTO.getDiaChi());
        sinhVien.setSdt(sinhVienDTO.getSdt());
        sinhVien.setEmail(sinhVienDTO.getEmail());
        sinhVien.setPwdHash(passwordEncoder.encode(sinhVienDTO.getPwdHash()));

        if (sinhVienDTO.getLopId() != null) {
            Optional<Lop> optionalLop = lopRepository.findById(sinhVienDTO.getLopId());
            optionalLop.ifPresent(sinhVien::setLop);
        }
    }
}
