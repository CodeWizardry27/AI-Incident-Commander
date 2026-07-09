package com.incidentcommander.agent.agents;

import com.incidentcommander.agent.core.AgentBase;
import com.incidentcommander.agent.core.Tool;
import com.incidentcommander.agent.tools.*;
import com.incidentcommander.service.GeminiService;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Detective Agent — First responder. Diagnoses the incident.
 * 
 * Tools: read_logs, check_metrics, check_db_status, check_api_health
 * 
 * Goal: Identify WHAT is broken and gather initial evidence.
 */
@Component
public class DetectiveAgent extends AgentBase {

    private final List<Tool> tools;

    public DetectiveAgent(GeminiService geminiService) {
        super(geminiService);
        this.tools = List.of(
            new ReadLogsTool(),
            new CheckMetricsTool(),
            new CheckDbStatusTool(),
            new CheckApiHealthTool()
        );
    }

    @Override
    public String getName() {
        return "detective";
    }

    @Override
    public String getRole() {
        return "Senior Site Reliability Engineer (SRE) specializing in incident diagnosis. " +
               "You are the FIRST responder. Your job is to quickly gather evidence about what is broken. " +
               "Check logs, metrics, database status, and API health. " +
               "Do NOT try to find the root cause — that's the Analyst's job. " +
               "Focus on SYMPTOMS: what errors are happening, which services are affected, what metrics are abnormal.";
    }

    @Override
    public List<Tool> getTools() {
        return tools;
    }
}
