package com.ptithcm.portal.service;

import com.ptithcm.portal.entity.Khoa;
import com.ptithcm.portal.entity.Lop;
import com.ptithcm.portal.entity.Mon;
import com.ptithcm.portal.entity.NhanVien;
import com.ptithcm.portal.repository.KhoaRepository;
import com.ptithcm.portal.repository.LopRepository;
import com.ptithcm.portal.repository.NhanVienRepository;
import com.ptithcm.portal.repository.MonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class KhoaService {

    @Autowired
    private KhoaRepository khoaRepository;

    @Autowired
    private LopRepository lopRepository;

    @Autowired
    private MonRepository monRepository;

    @Autowired
    private NhanVienRepository nhanVienRepository;

    public List<Khoa> getAllKhoa() {
        return khoaRepository.findAll();
    }

    public Optional<Khoa> getKhoaById(Integer id) {
        return khoaRepository.findById(id);
    }

    public Khoa createKhoa(Khoa khoa) {
        if (khoaRepository.existsByTen(khoa.getTen())) {
            throw new IllegalArgumentException("Tên khoa đã tồn tại.");
        }
        return khoaRepository.save(khoa);
    }

    public Khoa updateKhoa(Integer id, Khoa khoa) {
        Optional<Khoa> existingKhoa = khoaRepository.findById(id);

        if (existingKhoa.isPresent()) {
            // Check for name conflicts, excluding the current khoa
            if (khoaRepository.existsByTenAndIdNot(khoa.getTen(), id)) {
                throw new IllegalArgumentException("Tên khoa đã tồn tại.");
            }
            khoa.setId(id);
            return khoaRepository.save(khoa);
        } else {
            return null;
        }
    }

    public String deleteKhoa(Integer id) {
        try {
            if (!canDeleteKhoa(id)) {
                return "Không thể xóa khoa này vì có dữ liệu liên quan (lớp, môn học hoặc nhân viên) đang tham chiếu đến khoa này";
            }
            khoaRepository.deleteById(id);
            return null; // Xóa thành công
        } catch (DataIntegrityViolationException e) {
            return "Không thể xóa khoa này vì có dữ liệu liên quan đang tham chiếu đến khoa này";
        } catch (Exception e) {
            return "Lỗi khi xóa khoa: " + e.getMessage();
        }
    }

    public boolean canDeleteKhoa(Integer id) {
        // Kiểm tra xem có Lớp nào liên quan đến khoa này không
        long lopCount = lopRepository.countByKhoaId(id);
        if (lopCount > 0) {
            return false;
        }

        // Kiểm tra xem có Môn học nào liên quan đến khoa này không
        long monCount = monRepository.countByKhoaId(id);
        if (monCount > 0) {
            return false;
        }

        // Kiểm tra xem có Nhân viên nào thuộc khoa này không
        long nhanVienCount = nhanVienRepository.countByKhoaId(id);
        if (nhanVienCount > 0) {
            return false;
        }

        return true;
    }
}