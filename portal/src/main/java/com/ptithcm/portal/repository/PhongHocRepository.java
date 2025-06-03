package com.ptithcm.portal.repository;

import com.ptithcm.portal.entity.PhongHoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PhongHocRepository extends JpaRepository<PhongHoc, Integer> {
    @Query("SELECT COUNT(p) FROM PhongHoc p WHERE p.ten = :ten")
    int countByTen(@Param("ten") String ten);
}