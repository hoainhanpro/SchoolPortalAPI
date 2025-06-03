package com.ptithcm.portal.repository;

import com.ptithcm.portal.entity.NhanVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NhanVienRepository extends JpaRepository<NhanVien, Integer> {
    Optional<NhanVien> findByEmail(String email);

    @Query("SELECT DISTINCT nv FROM NhanVien nv JOIN nv.chiTietGiangDays ctgd WHERE nv.chucVu = 'Giao vien'")
    List<NhanVien> findAllGiangVienWithSchedule();

    boolean existsByEmail(String email);

    boolean existsBySdt(String sdt);

    @Query("SELECT CASE WHEN COUNT(nv) > 0 THEN true ELSE false END FROM NhanVien nv WHERE nv.email = :email AND nv.id != :id")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("id") Integer id);

    @Query("SELECT CASE WHEN COUNT(nv) > 0 THEN true ELSE false END FROM NhanVien nv WHERE nv.sdt = :sdt AND nv.id != :id")
    boolean existsBySdtAndIdNot(@Param("sdt") String sdt, @Param("id") Integer id);

    long countByKhoaId(Integer khoaId);
}
