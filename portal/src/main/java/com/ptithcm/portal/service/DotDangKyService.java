package com.ptithcm.portal.service;

import com.ptithcm.portal.dto.DotDangKyDTO;
import java.util.List;
import java.util.Optional;

public interface DotDangKyService {
    List<DotDangKyDTO> getAllDotDangKy();

    Optional<DotDangKyDTO> getDotDangKyById(Integer id);

    DotDangKyDTO createDotDangKy(DotDangKyDTO dotDangKyDTO);

    DotDangKyDTO updateDotDangKy(Integer id, DotDangKyDTO dotDangKyDTO);

}