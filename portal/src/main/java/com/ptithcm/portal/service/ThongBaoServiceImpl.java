package com.ptithcm.portal.service;

import com.ptithcm.portal.dto.ThongBaoDTO;
import com.ptithcm.portal.entity.NhanVien;
import com.ptithcm.portal.entity.ThongBao;
import com.ptithcm.portal.repository.NhanVienRepository;
import com.ptithcm.portal.repository.ThongBaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ThongBaoServiceImpl implements ThongBaoService {

    @Autowired
    private ThongBaoRepository thongBaoRepository;

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Override
    public List<ThongBaoDTO> getAllThongBao() {
        List<ThongBao> thongBaos = thongBaoRepository.findAll();
        return thongBaos.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ThongBaoDTO> getThongBaoById(Integer id) {
        return thongBaoRepository.findById(id)
                .map(this::convertToDTO);
    }

    @Override
    public ThongBaoDTO createThongBao(ThongBaoDTO thongBaoDTO) {
        // Kiểm tra tiêu đề không được để trống
        if (thongBaoDTO.getTieuDe() == null || thongBaoDTO.getTieuDe().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tiêu đề không được để trống");
        }

        // Kiểm tra nội dung không được để trống
        if (thongBaoDTO.getNoiDung() == null || thongBaoDTO.getNoiDung().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nội dung không được để trống");
        }

        // Kiểm tra ID nhân viên tồn tại
        if (thongBaoDTO.getNvId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID nhân viên không được để trống");
        }

        // Chuyển đổi DTO thành Entity
        ThongBao thongBao = convertToEntity(thongBaoDTO);

        // Lưu và chuyển đổi lại thành DTO
        ThongBao savedThongBao = thongBaoRepository.save(thongBao);
        return convertToDTO(savedThongBao);
    }

    @Override
    public ThongBaoDTO updateThongBao(Integer id, ThongBaoDTO thongBaoDTO) {
        // Kiểm tra thông báo tồn tại
        Optional<ThongBao> existingThongBaoOpt = thongBaoRepository.findById(id);
        if (!existingThongBaoOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thông báo với ID: " + id);
        }

        // Kiểm tra tiêu đề không được để trống
        if (thongBaoDTO.getTieuDe() == null || thongBaoDTO.getTieuDe().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tiêu đề không được để trống");
        }

        // Kiểm tra nội dung không được để trống
        if (thongBaoDTO.getNoiDung() == null || thongBaoDTO.getNoiDung().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nội dung không được để trống");
        }

        // Cập nhật ID từ path vào DTO
        thongBaoDTO.setId(id);

        // Giữ nguyên thời gian tạo và nhân viên từ bản ghi cũ nếu cần
        ThongBao existingThongBao = existingThongBaoOpt.get();
        if (thongBaoDTO.getNgayGui() == null) {
            thongBaoDTO.setNgayGui(existingThongBao.getNgayGui());
        }

        // Nếu không có ID nhân viên trong DTO, giữ nguyên nhân viên cũ
        if (thongBaoDTO.getNvId() == null) {
            thongBaoDTO.setNvId(existingThongBao.getNhanVien().getId());
        }

        // Chuyển đổi DTO thành Entity
        ThongBao thongBao = convertToEntity(thongBaoDTO);

        // Lưu và chuyển đổi lại thành DTO
        ThongBao updatedThongBao = thongBaoRepository.save(thongBao);
        return convertToDTO(updatedThongBao);
    }

    // Chuyển đổi Entity sang DTO
    private ThongBaoDTO convertToDTO(ThongBao thongBao) {
        ThongBaoDTO dto = new ThongBaoDTO();
        dto.setId(thongBao.getId());
        dto.setTieuDe(thongBao.getTieuDe());
        dto.setNoiDung(thongBao.getNoiDung());
        dto.setNgayGui(thongBao.getNgayGui());

        if (thongBao.getNhanVien() != null) {
            NhanVien nhanVien = thongBao.getNhanVien();
            dto.setNvId(nhanVien.getId());
            dto.setHoNhanVien(nhanVien.getHo());
            dto.setTenNhanVien(nhanVien.getTen());
            dto.setEmailNhanVien(nhanVien.getEmail());
            dto.setChucVuNhanVien(nhanVien.getChucVu());
        }

        return dto;
    }

    // Chuyển đổi DTO sang Entity
    private ThongBao convertToEntity(ThongBaoDTO dto) {
        ThongBao thongBao = new ThongBao();
        thongBao.setId(dto.getId());
        thongBao.setTieuDe(dto.getTieuDe());
        thongBao.setNoiDung(dto.getNoiDung());
        thongBao.setNgayGui(dto.getNgayGui());

        if (dto.getNvId() != null) {
            NhanVien nhanVien = nhanVienRepository.findById(dto.getNvId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nhân viên không tồn tại"));
            thongBao.setNhanVien(nhanVien);
        }

        return thongBao;
    }
}