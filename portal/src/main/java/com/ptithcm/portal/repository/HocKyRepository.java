package com.ptithcm.portal.repository;

import com.ptithcm.portal.entity.HocKy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime; // Import LocalDateTime

@Repository
public interface HocKyRepository extends JpaRepository<HocKy, Integer> {
    @Query("SELECT hk FROM HocKy hk WHERE :currentDate BETWEEN hk.startDate AND hk.endDate")
    Optional<HocKy> findHocKyByDate(@Param("currentDate") LocalDate currentDate);

    @Query("SELECT DISTINCT hk FROM DotDangKy ddk JOIN ddk.hocKy hk WHERE ddk.thoiGianBatDau <= :now AND ddk.thoiGianKetThuc >= :now ORDER BY hk.namHoc DESC, hk.thuTu DESC, hk.id DESC")
    List<HocKy> findHocKyWithActiveRegistrationPeriod(@Param("now") LocalDateTime now);

    @Query("SELECT DISTINCT hk FROM DotDangKy ddk JOIN ddk.hocKy hk ORDER BY hk.namHoc DESC, hk.thuTu DESC, hk.id DESC")
    List<HocKy> findLatestHocKyWithAnyRegistrationPeriod();
}
