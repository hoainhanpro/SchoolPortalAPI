package com.ptithcm.portal.service;

import com.ptithcm.portal.dto.DotDongHocPhiDto;

import java.util.List;
import java.util.Optional;

public interface DotDongHocPhiService {

    List<DotDongHocPhiDto> getAllDotDongHocPhi();

    Optional<DotDongHocPhiDto> getDotDongHocPhiById(Integer id);

    DotDongHocPhiDto createDotDongHocPhi(DotDongHocPhiDto dotDongHocPhiDto);

    DotDongHocPhiDto updateDotDongHocPhi(Integer id, DotDongHocPhiDto dotDongHocPhiDto);

}