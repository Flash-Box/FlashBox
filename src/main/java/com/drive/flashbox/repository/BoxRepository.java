package com.drive.flashbox.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.drive.flashbox.entity.Box;

@Repository
public interface BoxRepository extends JpaRepository<Box, Long> {
	List<Box> findAllByBoomDateBetween(LocalDateTime start, LocalDateTime end);

	List<Box> findAllByBoomDateBefore(LocalDateTime now);

    List<Box> findBoxesByUser_Id(Long uid);
}
