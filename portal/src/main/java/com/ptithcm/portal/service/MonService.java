package com.ptithcm.portal.service;

import com.ptithcm.portal.dto.MonDTO;
import com.ptithcm.portal.entity.Khoa;
import com.ptithcm.portal.entity.Mon;
import com.ptithcm.portal.repository.MonRepository;
import com.ptithcm.portal.repository.KhoaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Objects;

@Service
public class MonService {

    @Autowired
    private MonRepository monRepository;

    @Autowired
    private KhoaRepository khoaRepository;

    // Lấy tất cả môn học dạng DTO (không có vòng lặp vô hạn)
    public List<MonDTO> getAllMonDTO() {
        List<Mon> mons = monRepository.findAll();
        return mons.stream()
                .map(MonDTO::new)
                .collect(Collectors.toList());
    }

    // Lấy chi tiết môn học theo ID dạng DTO
    public Optional<MonDTO> getMonDTOById(Integer id) {
        return monRepository.findById(id)
                .map(MonDTO::new);
    }

    // Tạo môn học mới từ DTO
    public MonDTO createMon(MonDTO monDTO) {
        // Check for existing subject with the same name and khoa_id
        Optional<Mon> existingMon = monRepository.findByTenMonAndKhoaId(monDTO.getTenMon(), monDTO.getKhoa_id());

        if (existingMon.isPresent()) {
            throw new IllegalArgumentException("Môn học với tên và khoa đã tồn tại.");
        }

        Mon mon = new Mon();
        mon.setTenMon(monDTO.getTenMon());
        mon.setSoTinChi(monDTO.getSoTinChi());

        if (monDTO.getKhoa_id() != null) {
            Khoa khoa = khoaRepository.findById(monDTO.getKhoa_id()).orElse(null);
            mon.setKhoa(khoa);
        }

        Mon savedMon = monRepository.save(mon);
        return new MonDTO(savedMon);
    }

    // Cập nhật môn học từ DTO
    public Optional<MonDTO> updateMon(Integer id, MonDTO monDTO) {
        Optional<Mon> existingMonOptional = monRepository.findById(id);

        if (existingMonOptional.isPresent()) {
            Mon existingMon = existingMonOptional.get();

            // Only check for duplicates if the name or khoa_id is changing
            if (!existingMon.getTenMon().equals(monDTO.getTenMon())
                    || !Objects.equals(existingMon.getKhoa().getId(), monDTO.getKhoa_id())) {
                Optional<Mon> duplicateMon = monRepository.findByTenMonAndKhoaId(monDTO.getTenMon(),
                        monDTO.getKhoa_id());
                if (duplicateMon.isPresent() && duplicateMon.get().getId() != id) { // Check for duplicates, excluding
                                                                                    // the subject being updated
                    throw new IllegalArgumentException("Môn học với tên và khoa đã tồn tại.");
                }
            }

            existingMon.setTenMon(monDTO.getTenMon());
            existingMon.setSoTinChi(monDTO.getSoTinChi());

            if (monDTO.getKhoa_id() != null) {
                Optional<Khoa> khoaOptional = khoaRepository.findById(monDTO.getKhoa_id());
                khoaOptional.ifPresent(existingMon::setKhoa);
            } else {
                existingMon.setKhoa(null);
            }

            Mon updatedMon = monRepository.save(existingMon);
            return Optional.of(new MonDTO(updatedMon));
        }

        return Optional.empty();
    }

    public void deleteMon(Integer id) {
        monRepository.deleteById(id);
    }

    // Kiểm tra xem môn học có đang được sử dụng trong lớp tín chỉ không
    public boolean isMonUsedInLopTinChi(Integer id) {
        Optional<Mon> monOptional = monRepository.findById(id);
        if (monOptional.isPresent()) {
            Mon mon = monOptional.get();
            return mon.getLopTinChis() != null && !mon.getLopTinChis().isEmpty();
        }
        return false;
    }

    // Lấy số lượng lớp tín chỉ đang sử dụng môn học
    public long countLopTinChiUsingMon(Integer id) {
        Optional<Mon> monOptional = monRepository.findById(id);
        if (monOptional.isPresent()) {
            Mon mon = monOptional.get();
            return mon.getLopTinChis() != null ? mon.getLopTinChis().size() : 0;
        }
        return 0;
    }
}