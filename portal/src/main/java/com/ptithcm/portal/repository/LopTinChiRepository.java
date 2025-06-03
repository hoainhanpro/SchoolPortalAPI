package com.ptithcm.portal.repository;

import com.ptithcm.portal.entity.LopTinChi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LopTinChiRepository extends JpaRepository<LopTinChi, Integer> {

        /**
         * Kiểm tra xem đã tồn tại lớp tín chỉ cho môn và nhóm trong học kỳ chưa
         * 
         * @param monId   ID của môn học
         * @param hocKyId ID của học kỳ
         * @param nhom    Nhóm lớp
         * @return true nếu đã tồn tại, false nếu chưa
         */
        @Query("SELECT COUNT(ltc) > 0 FROM LopTinChi ltc " +
                        "WHERE ltc.mon.id = :monId AND ltc.hocKy.id = :hocKyId AND ltc.nhom = :nhom")
        boolean existsByMonAndHocKyAndNhom(
                        @Param("monId") Integer monId,
                        @Param("hocKyId") Integer hocKyId,
                        @Param("nhom") Integer nhom);

        @Query("SELECT ltc FROM LopTinChi ltc JOIN FETCH ltc.mon JOIN FETCH ltc.hocKy WHERE ltc.hocKy.id = :hocKyId")
        List<LopTinChi> findByHocKyIdWithDetails(@Param("hocKyId") Integer hocKyId);

        boolean existsByHocKyId(Integer hocKyId);
}