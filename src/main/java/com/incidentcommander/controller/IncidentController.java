package com.incidentcommander.controller;

import com.incidentcommander.agent.agents.*;
import com.incidentcommander.agent.core.AgentBase;
import com.incidentcommander.agent.core.AgentContext;
import com.incidentcommander.agent.core.AgentOrchestrator;
import com.incidentcommander.entity.Incident;
import com.incidentcommander.entity.IncidentReport;
import com.incidentcommander.entity.AgentLog;
import com.incidentcommander.repository.AgentLogRepository;
import com.incidentcommander.repository.IncidentReportRepository;
import com.incidentcommander.repository.IncidentRepository;
import com.incidentcommander.simulation.SimulatedEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class IncidentController {

    private final IncidentRepository incidentRepository;
    private final AgentLogRepository agentLogRepository;
    private final IncidentReportRepository incidentReportRepository;
    private final AgentOrchestrator orchestrator;
    private final SimulatedEnvironment simulatedEnvironment;
    private final DetectiveAgent detectiveAgent;
    private final AnalystAgent analystAgent;
    private final FixerAgent fixerAgent;
    private final CommanderAgent commanderAgent;

    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    /** GET /api/scenarios — List available demo scenarios */
    @GetMapping("/scenarios")
    public ResponseEntity<List<SimulatedEnvironment.Scenario>> getScenarios() {
        return ResponseEntity.ok(simulatedEnvironment.getScenarios());
    }

    /** POST /api/incidents — Create a new incident and start investigation */
    @PostMapping("/incidents")
    public ResponseEntity<Map<String, Object>> createIncident(@RequestBody Map<String, String> request) {
        String scenarioId = request.getOrDefault("scenarioId", "db_pool_exhaustion");
        String description = request.getOrDefault("description", "Production incident reported");

        // Find scenario title
        String title = simulatedEnvironment.getScenarios().stream()
                .filter(s -> s.id().equals(scenarioId))
                .findFirst()
                .map(SimulatedEnvironment.Scenario::title)
                .orElse("Custom Incident");

        String severity = simulatedEnvironment.getScenarios().stream()
                .filter(s -> s.id().equals(scenarioId))
                .findFirst()
                .map(SimulatedEnvironment.Scenario::severity)
                .orElse("HIGH");

        // Create incident record
        Incident incident = Incident.builder()
                .userId(1L) // Simplified — no auth for now
                .title(title)
                .description(description)
                .scenarioId(scenarioId)
                .severity(Incident.Severity.valueOf(severity))
                .build();
        incident = incidentRepository.save(incident);

        final Long incidentId = incident.getId();

        // Load scenario data
        AgentContext context = simulatedEnvironment.loadScenario(scenarioId, incidentId, description);

        // Run investigation async (so the API returns immediately)
        executorService.submit(() -> {
            List<AgentBase> agents = List.of(detectiveAgent, analystAgent, fixerAgent, commanderAgent);
            orchestrator.runInvestigation(incidentId, agents, context);
        });

        return ResponseEntity.ok(Map.of(
                "incidentId", incidentId,
                "status", "SUBMITTED",
                "message", "Investigation started! Connect to SSE stream for live updates."
        ));
    }

    /** GET /api/incidents/{id}/stream — SSE stream for live agent activity */
    @GetMapping(value = "/incidents/{id}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamIncident(@PathVariable Long id) {
        SseEmitter emitter = new SseEmitter(300_000L); // 5 minute timeout
        orchestrator.registerEmitter(id, emitter);
        return emitter;
    }

    /** GET /api/incidents — List all incidents */
    @GetMapping("/incidents")
    public ResponseEntity<List<Incident>> getAllIncidents() {
        return ResponseEntity.ok(incidentRepository.findAll());
    }

    /** GET /api/incidents/{id} — Get incident details */
    @GetMapping("/incidents/{id}")
    public ResponseEntity<Incident> getIncident(@PathVariable Long id) {
        return incidentRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** GET /api/incidents/{id}/logs — Get all agent logs for an incident */
    @GetMapping("/incidents/{id}/logs")
    public ResponseEntity<List<AgentLog>> getAgentLogs(@PathVariable Long id) {
        return ResponseEntity.ok(agentLogRepository.findByIncidentIdOrderByCreatedAtAsc(id));
    }

    /** GET /api/incidents/{id}/report — Get the final incident report */
    @GetMapping("/incidents/{id}/report")
    public ResponseEntity<IncidentReport> getReport(@PathVariable Long id) {
        return incidentReportRepository.findByIncidentId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
