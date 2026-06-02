package com.scalelink.repository;

import com.scalelink.entity.ClickEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Click Event Repository
 */
@Repository
public interface ClickEventRepository extends JpaRepository<ClickEvent, Long> {
}
