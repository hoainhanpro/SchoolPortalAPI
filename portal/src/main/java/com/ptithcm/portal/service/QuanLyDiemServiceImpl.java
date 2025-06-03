package com.ptithcm.portal.service;

import com.ptithcm.portal.dto.LopTinChiDto;
import com.ptithcm.portal.dto.SinhVienDiemDto;
import com.ptithcm.portal.entity.DangKy;
import com.ptithcm.portal.entity.LopTinChi;
import com.ptithcm.portal.repository.ChiTietGiangDayRepository;
import com.ptithcm.portal.repository.DangKyRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuanLyDiemServiceImpl implements QuanLyDiemService {

    @Autowired
    private ChiTietGiangDayRepository chiTietGiangDayRepository;
    
    @Autowired
    private DangKyRepository dangKyRepository;
    
    @Override
    public List<LopTinChiDto> getLopTinChiByGiangVien(Integer giangVienId, Integer hocKyId) {
        List<LopTinChi> lopTinChis = chiTietGiangDayRepository.findLopTinChiByGiangVien(giangVienId, hocKyId);
        
        return lopTinChis.stream()
                .map(loptc -> new LopTinChiDto(
                        loptc.getId(),
                        loptc.getMon().getTenMon(),
                        loptc.getMon().getSoTinChi(),
                        loptc.getNhom(),
                        loptc.getSiSoToiDa(),
                        loptc.getDangKys() != null ? (int) loptc.getDangKys().stream()
                                .filter(dk -> "Da xac nhan".equals(dk.getTrangThai()))
                                .count() : 0,
                        loptc.getHocKy().getTen(),
                        loptc.getHocKy().getNamHoc()
                ))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<SinhVienDiemDto> getSinhVienDiemByLopTinChi(Integer lopTcId) {
        List<DangKy> dangKys = dangKyRepository.findByLopTinChiAndTrangThaiDaXacNhan(lopTcId);
        
        return dangKys.stream()
                .map(dk -> new SinhVienDiemDto(
                        dk.getId(),
                        dk.getSinhVien().getMssv(),
                        dk.getSinhVien().getHo(),
                        dk.getSinhVien().getTen(),
                        dk.getSinhVien().getLop().getTen(),
                        dk.getDiemcc(),
                        dk.getDiemkt(),
                        dk.getDiemthi()
                ))
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public SinhVienDiemDto updateDiem(Integer dangKyId, Double diemCC, Double diemKT, Double diemThi) {
        DangKy dangKy = dangKyRepository.findById(dangKyId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy bản ghi đăng ký với ID: " + dangKyId));
        
        // Kiểm tra trạng thái đăng ký
        if (!"Da xac nhan".equals(dangKy.getTrangThai())) {
            throw new IllegalStateException("Chỉ có thể cập nhật điểm cho sinh viên đã xác nhận đăng ký");
        }
        
        // Cập nhật điểm
        if (diemCC != null) {
            dangKy.setDiemcc(BigDecimal.valueOf(diemCC));
        }
        
        if (diemKT != null) {
            dangKy.setDiemkt(BigDecimal.valueOf(diemKT));
        }
        
        if (diemThi != null) {
            dangKy.setDiemthi(BigDecimal.valueOf(diemThi));
        }
        
        // Lưu vào database
        dangKy = dangKyRepository.save(dangKy);
        
        // Trả về đối tượng DTO
        return new SinhVienDiemDto(
                dangKy.getId(),
                dangKy.getSinhVien().getMssv(),
                dangKy.getSinhVien().getHo(),
                dangKy.getSinhVien().getTen(),
                dangKy.getSinhVien().getLop().getTen(),
                dangKy.getDiemcc(),
                dangKy.getDiemkt(),
                dangKy.getDiemthi()
        );
    }
} 