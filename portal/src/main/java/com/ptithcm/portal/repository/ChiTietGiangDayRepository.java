package com.ptithcm.portal.repository;

import com.ptithcm.portal.entity.ChiTietGiangDay;
import com.ptithcm.portal.entity.ChiTietGiangDayId;
import com.ptithcm.portal.entity.LopTinChi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChiTietGiangDayRepository extends JpaRepository<ChiTietGiangDay, ChiTietGiangDayId> {
    /**
     * Đếm số lượng lớp tín chỉ của giảng viên trong học kỳ
     */
    @Query("SELECT COUNT(DISTINCT ctgd.lopTinChi.id) FROM ChiTietGiangDay ctgd " +
           "WHERE ctgd.nhanVien.id = :lecturerId AND ctgd.lopTinChi.hocKy.id = :semesterId")
    Integer countClassesByLecturerAndSemester(@Param("lecturerId") Integer lecturerId, 
                                            @Param("semesterId") Integer semesterId);
    
    /**
     * Lấy danh sách lớp tín chỉ mà giảng viên đang dạy trong học kỳ
     * @param giangVienId ID của giảng viên
     * @param hocKyId ID của học kỳ (có thể null để lấy tất cả)
     * @return danh sách lớp tín chỉ
     */
    @Query("SELECT DISTINCT ctgd.lopTinChi FROM ChiTietGiangDay ctgd " +
           "WHERE ctgd.nhanVien.id = :giangVienId " +
           "AND (:hocKyId IS NULL OR ctgd.lopTinChi.hocKy.id = :hocKyId)")
    List<LopTinChi> findLopTinChiByGiangVien(
        @Param("giangVienId") Integer giangVienId,
        @Param("hocKyId") Integer hocKyId
    );

    @Query("SELECT ctg FROM ChiTietGiangDay ctg JOIN FETCH ctg.nhanVien WHERE ctg.lopTinChi = :lopTinChi")
    List<ChiTietGiangDay> findByLopTinChiWithNhanVien(@Param("lopTinChi") LopTinChi lopTinChi);
}