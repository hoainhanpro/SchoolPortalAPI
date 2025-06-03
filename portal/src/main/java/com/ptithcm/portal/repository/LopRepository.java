package com.ptithcm.portal.repository;

import com.ptithcm.portal.entity.Lop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LopRepository extends JpaRepository<Lop, Integer> {
    long countByKhoaId(Integer khoaId);

    List<Lop> findByKhoaId(Integer khoaId);

    Optional<Lop> findByTenAndKhoaId(String tenLop, Integer khoaId);

    Optional<Lop> findByTenAndKhoaIdAndIdNot(String tenLop, Integer khoaId, Integer id);
}