package com.ptithcm.portal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.ptithcm.portal.entity.CoVan;
import com.ptithcm.portal.entity.CoVanId;
import com.ptithcm.portal.entity.Lop;

import java.util.List;

@Repository
public interface CoVanRepository extends JpaRepository<CoVan, CoVanId> {

    /**
     * Tìm tất cả các lớp mà nhân viên (giảng viên) đang cố vấn
     * 
     * @param nvId ID của nhân viên (giảng viên)
     * @return Danh sách các lớp được cố vấn
     */
    @Query("SELECT cv.lop FROM CoVan cv WHERE cv.nhanVien.id = :nvId")
    List<Lop> findLopsByNhanVienId(@Param("nvId") Integer nvId);

    /**
     * Tìm tất cả các lớp mà nhân viên (giảng viên) đang cố vấn trong một năm học cụ
     * thể
     * 
     * @param nvId   ID của nhân viên (giảng viên)
     * @param namHoc Năm học
     * @return Danh sách các lớp được cố vấn trong năm học đó
     */
    @Query("SELECT cv.lop FROM CoVan cv WHERE cv.nhanVien.id = :nvId AND cv.id.namHoc = :namHoc")
    List<Lop> findLopsByNhanVienIdAndNamHoc(@Param("nvId") Integer nvId, @Param("namHoc") Integer namHoc);

    boolean existsByLopId(Integer lopId);
}
