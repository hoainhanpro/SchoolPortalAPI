package com.ptithcm.portal.repository;

import com.ptithcm.portal.entity.SinhVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SinhVienRepository extends JpaRepository<SinhVien, String> {
    Optional<SinhVien> findByMssv(String mssv);

    Optional<SinhVien> findByEmail(String email); // In case login can be via email for SinhVien

    boolean existsByLopId(Integer lopId);

    boolean existsBySdt(String sdt);

    boolean existsByEmail(String email);

    boolean existsByMssv(String mssv);

    boolean existsBySdtAndMssvNot(String sdt, String mssv);

    boolean existsByEmailAndMssvNot(String email, String mssv);

    @Query("SELECT CASE WHEN COUNT(d) > 0 OR COUNT(h) > 0 THEN TRUE ELSE FALSE END " +
            "FROM DangKy d, HocPhi h " +
            "WHERE (d.sinhVien.mssv = :mssv OR h.sinhVien.mssv = :mssv)")
    boolean existsByMssvInOtherTables(@Param("mssv") String mssv);

}
