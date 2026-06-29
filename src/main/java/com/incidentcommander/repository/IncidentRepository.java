package com.incidentcommander.repository;

import com.incidentcommander.entity.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IncidentRepository extends JpaRepository<Incident, Long> {
    List<Incident> findByUserIdOrderByStartedAtDesc(Long userId);
    long countByStatus(Incident.IncidentStatus status);
}
