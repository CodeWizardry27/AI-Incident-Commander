package com.incidentcommander.repository;

import com.incidentcommander.entity.IncidentReport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface IncidentReportRepository extends JpaRepository<IncidentReport, Long> {
    Optional<IncidentReport> findByIncidentId(Long incidentId);
}
