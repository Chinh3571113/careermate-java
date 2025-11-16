package com.fpt.careermate.services.health_services.repository;

import com.fpt.careermate.services.health_services.domain.NotificationHeartbeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationHeartbeatRepo extends JpaRepository<NotificationHeartbeat, Integer> {
    Optional<NotificationHeartbeat> findByName(String name);
}
