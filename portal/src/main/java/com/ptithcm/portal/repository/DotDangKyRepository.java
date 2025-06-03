package com.ptithcm.portal.repository;

import com.ptithcm.portal.entity.DotDangKy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DotDangKyRepository extends JpaRepository<DotDangKy, Integer> {
    // Find an active registration period for a given semester ID and current time
    @Query("SELECT ddk FROM DotDangKy ddk WHERE ddk.hocKy.id = :hocKyId AND ddk.thoiGianBatDau <= :currentTime AND ddk.thoiGianKetThuc >= :currentTime")
    Optional<DotDangKy> findActiveByHocKyId(@Param("hocKyId") Integer hocKyId,
            @Param("currentTime") LocalDateTime currentTime);

    // Find registration periods that have ended and not yet processed
    @Query("SELECT ddk FROM DotDangKy ddk WHERE ddk.thoiGianKetThuc < :currentTime AND ddk.processed = false")
    List<DotDangKy> findEndedAndUnprocessed(@Param("currentTime") LocalDateTime currentTime);

    boolean existsByHocKyId(Integer hocKyId);

    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM DotDangKy d WHERE d.hocKy.id = :hocKyId AND d.id != :id")
    boolean existsByHocKyIdAndIdNot(@Param("hocKyId") Integer hocKyId, @Param("id") Integer id);
}
