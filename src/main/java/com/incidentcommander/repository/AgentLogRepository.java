package com.incidentcommander.repository;

import com.incidentcommander.entity.AgentLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AgentLogRepository extends JpaRepository<AgentLog, Long> {
    List<AgentLog> findByIncidentIdOrderByCreatedAtAsc(Long incidentId);
}
