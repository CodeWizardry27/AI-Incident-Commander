package com.incidentcommander.agent.core;

import com.incidentcommander.entity.AgentLog;
import com.incidentcommander.entity.Incident;
import com.incidentcommander.entity.IncidentReport;
import com.incidentcommander.repository.AgentLogRepository;
import com.incidentcommander.repository.IncidentReportRepository;
import com.incidentcommander.repository.IncidentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AgentOrchestrator — Coordinates the multi-agent investigation pipeline.
 * 
 * Flow: Detective → Analyst → Fixer → Commander
 * 
 * Each agent's conclusion is passed as context to the next agent.
 * All steps are streamed to the frontend via SSE and persisted to DB.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AgentOrchestrator {

    private final IncidentRepository incidentRepository;
    private final AgentLogRepository agentLogRepository;
    private final IncidentReportRepository incidentReportRepository;

    /** Active SSE emitters for live streaming (incidentId → emitter) */
    private final Map<Long, SseEmitter> activeEmitters = new ConcurrentHashMap<>();

    /**
     * Register an SSE emitter for real-time streaming.
     */
    public void registerEmitter(Long incidentId, SseEmitter emitter) {
        activeEmitters.put(incidentId, emitter);
        emitter.onCompletion(() -> activeEmitters.remove(incidentId));
        emitter.onTimeout(() -> activeEmitters.remove(incidentId));
        emitter.onError(e -> activeEmitters.remove(incidentId));
    }

    /**
     * Run the full investigation pipeline.
     * 
     * @param incidentId  The incident to investigate
     * @param agents      Ordered list of agents [detective, analyst, fixer, commander]
     * @param context     Shared context with scenario data
     */
    public void runInvestigation(Long incidentId, List<AgentBase> agents, AgentContext context) {
        log.info("🚨 Starting investigation for incident #{}", incidentId);

        // Update incident status
        updateIncidentStatus(incidentId, Incident.IncidentStatus.INVESTIGATING);
        sendSSE(incidentId, "status", Map.of("status", "INVESTIGATING"));

        StringBuilder allFindings = new StringBuilder();

        for (AgentBase agent : agents) {
            log.info("--- Running {} agent ---", agent.getName());
            
            // Notify frontend which agent is starting
            sendSSE(incidentId, "agent_start", Map.of(
                "agent", agent.getName(),
                "role", agent.getRole()
            ));

            // Execute the agent with step callback for live streaming
            AgentResult result = agent.execute(context, step -> {
                // Save step to database
                saveAgentLog(incidentId, agent.getName(), step);
                
                // Stream step to frontend via SSE
                sendSSE(incidentId, "agent_step", Map.of(
                    "agent", agent.getName(),
                    "stepType", step.getType().name(),
                    "content", step.getContent() != null ? step.getContent() : "",
                    "toolName", step.getToolName() != null ? step.getToolName() : "",
                    "toolInput", step.getToolInput() != null ? step.getToolInput() : "",
                    "toolOutput", step.getToolOutput() != null ? step.getToolOutput() : ""
                ));
            });

            // Notify frontend that agent finished
            sendSSE(incidentId, "agent_complete", Map.of(
                "agent", agent.getName(),
                "success", String.valueOf(result.isSuccess()),
                "conclusion", result.getConclusion(),
                "iterations", String.valueOf(result.getIterationsUsed())
            ));

            allFindings.append("## ").append(agent.getName().toUpperCase()).append(" FINDINGS:\n");
            allFindings.append(result.getConclusion()).append("\n\n");

            if (!result.isSuccess()) {
                log.error("{} agent failed. Stopping pipeline.", agent.getName());
                updateIncidentStatus(incidentId, Incident.IncidentStatus.FAILED);
                sendSSE(incidentId, "investigation_failed", Map.of(
                    "agent", agent.getName(),
                    "reason", result.getConclusion()
                ));
                return;
            }
        }

        // Save the final incident report
        saveIncidentReport(incidentId, context);

        // Update incident status to resolved
        updateIncidentStatus(incidentId, Incident.IncidentStatus.RESOLVED);

        sendSSE(incidentId, "investigation_complete", Map.of(
            "status", "RESOLVED",
            "message", "Investigation complete! All agents have finished."
        ));

        log.info("✅ Investigation complete for incident #{}", incidentId);
    }

    private void saveAgentLog(Long incidentId, String agentName, AgentResult.AgentStep step) {
        AgentLog agentLog = AgentLog.builder()
                .incidentId(incidentId)
                .agentName(agentName)
                .stepType(mapStepType(step.getType()))
                .content(step.getContent())
                .toolName(step.getToolName())
                .toolInput(step.getToolInput())
                .toolOutput(step.getToolOutput())
                .build();
        agentLogRepository.save(agentLog);
    }

    private AgentLog.StepType mapStepType(AgentResult.AgentStep.Type type) {
        return switch (type) {
            case THINK -> AgentLog.StepType.THINK;
            case ACT -> AgentLog.StepType.ACT;
            case OBSERVE -> AgentLog.StepType.OBSERVE;
            case CONCLUSION -> AgentLog.StepType.CONCLUSION;
            case ERROR -> AgentLog.StepType.THINK; // Map errors to THINK for simplicity
        };
    }

    private void saveIncidentReport(Long incidentId, AgentContext context) {
        String detectiveFindings = context.getAgentFindings().getOrDefault("detective", "N/A");
        String analystFindings = context.getAgentFindings().getOrDefault("analyst", "N/A");
        String fixerFindings = context.getAgentFindings().getOrDefault("fixer", "N/A");
        String commanderFindings = context.getAgentFindings().getOrDefault("commander", "N/A");

        IncidentReport report = IncidentReport.builder()
                .incidentId(incidentId)
                .rootCause(analystFindings)
                .immediateFix(fixerFindings)
                .permanentFix(fixerFindings)
                .actionPlan(commanderFindings)
                .executiveSummary(commanderFindings)
                .build();

        incidentReportRepository.save(report);
    }

    private void updateIncidentStatus(Long incidentId, Incident.IncidentStatus status) {
        incidentRepository.findById(incidentId).ifPresent(incident -> {
            incident.setStatus(status);
            if (status == Incident.IncidentStatus.RESOLVED) {
                incident.setResolvedAt(LocalDateTime.now());
            }
            incidentRepository.save(incident);
        });
    }

    /**
     * Send a Server-Sent Event to the frontend.
     */
    private void sendSSE(Long incidentId, String eventType, Map<String, String> data) {
        SseEmitter emitter = activeEmitters.get(incidentId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventType)
                        .data(data));
            } catch (IOException e) {
                log.warn("SSE send failed for incident #{}: {}", incidentId, e.getMessage());
                activeEmitters.remove(incidentId);
            }
        }
    }
}
