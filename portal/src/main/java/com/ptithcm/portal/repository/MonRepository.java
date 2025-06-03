package com.ptithcm.portal.repository;

import com.ptithcm.portal.entity.Mon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MonRepository extends JpaRepository<Mon, Integer> {
    long countByKhoaId(Integer khoaId);

    Optional<Mon> findByTenMonAndKhoaId(String tenMon, Integer khoaId);
}