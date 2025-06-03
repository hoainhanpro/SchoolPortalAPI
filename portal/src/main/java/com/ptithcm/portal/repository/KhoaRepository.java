package com.ptithcm.portal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ptithcm.portal.entity.Khoa;

public interface KhoaRepository extends JpaRepository<Khoa, Integer> {

    boolean existsByTenAndIdNot(String ten, Integer id);

    boolean existsByTen(String ten);
}
