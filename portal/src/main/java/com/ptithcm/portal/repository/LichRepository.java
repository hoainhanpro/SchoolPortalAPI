package com.ptithcm.portal.repository;

import com.ptithcm.portal.entity.Lich;
import com.ptithcm.portal.entity.PhongHoc;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LichRepository extends JpaRepository<Lich, Integer> {

    /**
     * Tìm kiếm các lịch học có xung đột với buổi học và ngày học đã cho
     * 
     * @param giangVienId ID của giảng viên
     * @param buoiId      ID của buổi học (sáng, chiều...)
     * @param ngayHoc     Ngày học
     * @return Danh sách các lịch học bị xung đột
     */
    @Query("SELECT l FROM Lich l JOIN l.lopTinChi ltc JOIN ltc.chiTietGiangDays ctgd " +
            "WHERE ctgd.nhanVien.id = :giangVienId AND l.buoi.id = :buoiId " +
            "AND l.ngayHoc = :ngayHoc")
    List<Lich> findConflictingSchedules(
            @Param("giangVienId") Integer giangVienId,
            @Param("buoiId") Integer buoiId,
            @Param("ngayHoc") LocalDate ngayHoc);

    /**
     * Tìm các lịch học của giảng viên trong học kỳ
     * 
     * @param giangVienId ID của giảng viên
     * @param hocKyId     ID của học kỳ
     * @return Danh sách các lịch học của giảng viên trong học kỳ
     */
    @Query("SELECT l FROM Lich l JOIN l.lopTinChi ltc JOIN ltc.chiTietGiangDays ctgd " +
            "WHERE ctgd.nhanVien.id = :giangVienId AND ltc.hocKy.id = :hocKyId")
    List<Lich> findByGiangVienAndHocKy(
            @Param("giangVienId") Integer giangVienId,
            @Param("hocKyId") Integer hocKyId);

    boolean existsByPhongHoc(PhongHoc phongHoc);

    long countByPhongHoc(PhongHoc phongHoc);
}