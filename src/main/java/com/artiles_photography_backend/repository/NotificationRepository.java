package com.artiles_photography_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.artiles_photography_backend.models.Notification;

/**
 *
 * @author arojas
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
	List<Notification> findTop10ByOrderByCreatedAtDesc();
}