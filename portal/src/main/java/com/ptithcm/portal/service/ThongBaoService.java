package com.ptithcm.portal.service;

import com.ptithcm.portal.dto.ThongBaoDTO;
import java.util.List;
import java.util.Optional;

public interface ThongBaoService {
    List<ThongBaoDTO> getAllThongBao();

    Optional<ThongBaoDTO> getThongBaoById(Integer id);

    ThongBaoDTO createThongBao(ThongBaoDTO thongBaoDTO);

    ThongBaoDTO updateThongBao(Integer id, ThongBaoDTO thongBaoDTO);

}