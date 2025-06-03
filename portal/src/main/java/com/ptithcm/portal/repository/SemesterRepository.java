package com.ptithcm.portal.repository;

import com.ptithcm.portal.entity.HocKy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SemesterRepository extends JpaRepository<HocKy, Integer> {

    @Query(value = "SELECT * FROM hocky WHERE CURRENT_DATE BETWEEN start_date AND end_date ORDER BY nam_hoc DESC, thu_tu DESC LIMIT 1", nativeQuery = true)
    Optional<HocKy> findCurrentSemester();

    @Query(value = "SELECT * FROM hocky ORDER BY nam_hoc DESC, thu_tu DESC", nativeQuery = true)
    List<HocKy> findAllOrderByNameHocAndThuTuDesc();

    int countByNamHocAndThuTu(Integer namHoc, Integer thuTu);

    int countByNamHocAndThuTuAndIdNot(Integer namHoc, Integer thuTu, Integer id);
}