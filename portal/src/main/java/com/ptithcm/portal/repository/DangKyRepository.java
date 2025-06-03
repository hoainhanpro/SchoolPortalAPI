package com.ptithcm.portal.repository;

import com.ptithcm.portal.entity.DangKy;
import com.ptithcm.portal.entity.LopTinChi;
import com.ptithcm.portal.entity.Mon;
import com.ptithcm.portal.entity.HocKy;
import com.ptithcm.portal.entity.SinhVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DangKyRepository extends JpaRepository<DangKy, Integer> {
      /**
     * Lấy danh sách đăng ký với trạng thái "Da xac nhan" của một lớp tín chỉ cụ thể
     * @param lopTcId ID của lớp tín chỉ
     * @return danh sách đăng ký đã xác nhận
     */
    @Query("SELECT dk FROM DangKy dk WHERE dk.lopTinChi.id = :lopTcId AND dk.trangThai = 'Da xac nhan'")
    List<DangKy> findByLopTinChiAndTrangThaiDaXacNhan(@Param("lopTcId") Integer lopTcId);
    
    /**
     * Lấy danh sách đăng ký của một sinh viên theo MSSV
     * @param mssv Mã số sinh viên
     * @return danh sách đăng ký của sinh viên
     */
    @Query("SELECT dk FROM DangKy dk WHERE dk.sinhVien.mssv = :mssv AND dk.trangThai = 'Da xac nhan'")
    List<DangKy> findByMssvAndTrangThaiDaXacNhan(@Param("mssv") String mssv);
    
    Long countByLopTinChiAndTrangThaiNot(LopTinChi lopTinChi, String trangThaiToExclude);
    boolean existsBySinhVienAndLopTinChiAndTrangThaiNot(SinhVien sinhVien, LopTinChi lopTinChi, String trangThaiToExclude);

    @Query("SELECT CASE WHEN COUNT(dk) > 0 THEN TRUE ELSE FALSE END " +
           "FROM DangKy dk JOIN dk.lopTinChi ltc " +
           "WHERE dk.sinhVien = :sinhVien AND ltc.mon = :mon AND ltc.hocKy = :hocKy AND dk.trangThai <> :trangThaiToExclude")
    boolean existsBySinhVienAndMonAndHocKyAndTrangThaiNot(
            @Param("sinhVien") SinhVien sinhVien,
            @Param("mon") Mon mon,
            @Param("hocKy") HocKy hocKy,
            @Param("trangThaiToExclude") String trangThaiToExclude);

    @Query("SELECT dk FROM DangKy dk " +
           "JOIN FETCH dk.lopTinChi ltc " +
           "JOIN FETCH ltc.mon " +
           "JOIN FETCH ltc.hocKy " +
           "WHERE dk.sinhVien = :sinhVien AND dk.trangThai <> :trangThaiToExclude")
    List<DangKy> findBySinhVienAndTrangThaiNotWithDetails(
            @Param("sinhVien") SinhVien sinhVien,
            @Param("trangThaiToExclude") String trangThaiToExclude);

    @Query("SELECT dk FROM DangKy dk " +
           "JOIN FETCH dk.lopTinChi ltc " +
           "JOIN FETCH ltc.mon " +
           "JOIN FETCH ltc.hocKy hk " +
           "WHERE dk.sinhVien = :sinhVien AND hk.id = :hocKyId AND dk.trangThai <> :trangThaiToExclude")
    List<DangKy> findBySinhVienAndHocKyIdAndTrangThaiNotWithDetails(
            @Param("sinhVien") SinhVien sinhVien,
            @Param("hocKyId") Integer hocKyId,
            @Param("trangThaiToExclude") String trangThaiToExclude);

    // Find a registration for a specific student and class, regardless of status
    Optional<DangKy> findBySinhVienAndLopTinChi(SinhVien sinhVien, LopTinChi lopTinChi);

    List<DangKy> findBySinhVienAndLopTinChiHocKyAndTrangThaiIn(SinhVien sinhVien, HocKy hocKy, List<String> trangThaiList); // Added method

    @Query("SELECT dk FROM DangKy dk " +
           "JOIN dk.lopTinChi ltc " +
           "JOIN ltc.mon m " +
           "WHERE dk.sinhVien = :sinhVien AND m = :mon AND dk.diemthi >= 4.0")
    List<DangKy> findBySinhVienAndMonAndPassed(@Param("sinhVien") SinhVien sinhVien, @Param("mon") Mon mon);

    @Modifying
    @Query("UPDATE DangKy dk SET dk.trangThai = :newStatus " +
           "WHERE dk.lopTinChi.hocKy = :hocKy AND dk.trangThai = :oldStatus")
    int updateStatusByHocKyAndOldStatus(@Param("hocKy") HocKy hocKy,
                                        @Param("oldStatus") String oldStatus,
                                        @Param("newStatus") String newStatus);
}