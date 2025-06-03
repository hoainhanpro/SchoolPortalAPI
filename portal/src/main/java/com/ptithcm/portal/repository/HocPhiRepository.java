package com.ptithcm.portal.repository;

import com.ptithcm.portal.entity.HocPhi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HocPhiRepository extends JpaRepository<HocPhi, Integer> {

    /**
     * Lấy thông tin học phí của sinh viên theo MSSV
     * 
     * @param mssv Mã số sinh viên
     * @return Danh sách học phí của sinh viên
     */
    @Query("SELECT hp FROM HocPhi hp WHERE hp.sinhVien.mssv = :mssv ORDER BY hp.hocKy.namHoc DESC, hp.hocKy.thuTu DESC")
    List<HocPhi> findByStudentId(@Param("mssv") String mssv);

    /**
     * Lấy thông tin học phí của sinh viên theo MSSV và học kỳ
     * 
     * @param mssv    Mã số sinh viên
     * @param hocKyId ID học kỳ
     * @return Danh sách học phí của sinh viên trong học kỳ
     */
    @Query("SELECT hp FROM HocPhi hp WHERE hp.sinhVien.mssv = :mssv AND hp.hocKy.id = :hocKyId")
    List<HocPhi> findByStudentIdAndSemester(@Param("mssv") String mssv, @Param("hocKyId") Integer hocKyId);

    Optional<HocPhi> findBySinhVienAndHocKy(com.ptithcm.portal.entity.SinhVien sinhVien,
            com.ptithcm.portal.entity.HocKy hocKy);

    /**
     * Kiểm tra xem sinh viên đã có bản ghi học phí cho học kỳ chưa
     * 
     * @param mssv    Mã số sinh viên
     * @param hocKyId ID học kỳ
     * @return true nếu đã tồn tại, false nếu chưa
     */
    @Query("SELECT COUNT(hp) > 0 FROM HocPhi hp WHERE hp.sinhVien.mssv = :mssv AND hp.hocKy.id = :hocKyId")
    boolean existsByStudentIdAndSemester(@Param("mssv") String mssv, @Param("hocKyId") Integer hocKyId);

    boolean existsByHocKyId(Integer hocKyId);

    @Query("SELECT hp FROM HocPhi hp JOIN hp.hocKy hk ORDER BY hk.namHoc DESC, hk.thuTu DESC")
    List<HocPhi> findAllOrderByHocKyDesc();
}
