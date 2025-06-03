package com.ptithcm.portal.repository;

import com.ptithcm.portal.entity.Buoi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BuoiRepository extends JpaRepository<Buoi, Integer> {
} 