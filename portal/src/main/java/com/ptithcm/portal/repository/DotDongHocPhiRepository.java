package com.ptithcm.portal.repository;

import com.ptithcm.portal.entity.DotDongHocPhi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DotDongHocPhiRepository extends JpaRepository<DotDongHocPhi, Integer> {

  /**
   * Lấy tất cả các đợt đóng học phí, sắp xếp theo học kỳ và ngày bắt đầu
   * 
   * @return Danh sách tất cả đợt đóng học phí
   */
  @Query("SELECT ddp FROM DotDongHocPhi ddp ORDER BY ddp.hocKy.namHoc DESC, ddp.hocKy.thuTu DESC, ddp.ngayBatDau DESC")
  List<DotDongHocPhi> findAllOrderBySemesterAndDate();

  /**
   * Lấy các đợt đóng học phí theo học kỳ
   * 
   * @param hocKyId ID học kỳ
   * @return Danh sách đợt đóng học phí trong học kỳ
   */
  @Query("SELECT ddp FROM DotDongHocPhi ddp WHERE ddp.hocKy.id = :hocKyId ORDER BY ddp.ngayBatDau")
  List<DotDongHocPhi> findBySemester(@Param("hocKyId") Integer hocKyId);

  /**
   * Lấy đợt đóng học phí hiện tại (đang trong thời gian đóng)
   * 
   * @param currentDate Ngày hiện tại
   * @return Danh sách đợt đóng học phí đang hoạt động
   */
  @Query("SELECT ddp FROM DotDongHocPhi ddp WHERE :currentDate BETWEEN ddp.ngayBatDau AND ddp.ngayKetThuc")
  List<DotDongHocPhi> findActivePaymentPeriods(@Param("currentDate") LocalDate currentDate);

  /**
   * Lấy các học kỳ có đợt đóng học phí
   * 
   * @return Danh sách ID học kỳ có đợt đóng học phí
   */
  @Query("SELECT DISTINCT ddp.hocKy.id, ddp.hocKy.namHoc, ddp.hocKy.thuTu FROM DotDongHocPhi ddp ORDER BY ddp.hocKy.namHoc DESC, ddp.hocKy.thuTu DESC")
  List<Object[]> findDistinctSemesterIdsWithOrder();

  boolean existsByHocKyId(Integer hocKyId);

  @Query("SELECT ddh FROM DotDongHocPhi ddh WHERE ddh.hocKy.id = :hocKyId")
  Optional<DotDongHocPhi> findByHocKyId(@Param("hocKyId") Integer hocKyId);
}
