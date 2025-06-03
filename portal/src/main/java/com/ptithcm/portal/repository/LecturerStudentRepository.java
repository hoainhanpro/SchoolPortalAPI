package com.ptithcm.portal.repository;

import com.ptithcm.portal.entity.NhanVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LecturerStudentRepository extends JpaRepository<NhanVien, Integer> {
    
    @Query(value = "SELECT COUNT(DISTINCT dk.sv_id) AS so_sinh_vien " +
            "FROM chitietgiangday ctgd " +
            "JOIN dangky dk ON ctgd.lop_tc_id = dk.lop_tc_id " +
            "JOIN loptinchi ltc ON ctgd.lop_tc_id = ltc.id " +
            "WHERE ctgd.nv_id = :lecturerId " +
            "AND ltc.hoc_ky_id = :semesterId " +
            "AND dk.trang_thai = 'Da xac nhan'", nativeQuery = true)
    Integer countStudentsByLecturerAndSemester(
            @Param("lecturerId") Integer lecturerId,
            @Param("semesterId") Integer semesterId);
} 