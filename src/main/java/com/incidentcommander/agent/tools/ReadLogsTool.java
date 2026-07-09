package com.incidentcommander.agent.tools;

import com.incidentcommander.agent.core.AgentContext;
import com.incidentcommander.agent.core.Tool;

import java.util.Map;

/**
 * Reads application logs for a given service.
 * Returns simulated log entries from the scenario data.
 */
public class ReadLogsTool implements Tool {

    @Override
    public String getName() {
        return "read_logs";
    }

    @Override
    public String getDescription() {
        return "Reads application logs for a given service. Returns recent error/warning log entries.";
    }

    @Override
    public Map<String, String> getParameters() {
        return Map.of(
            "service", "Name of the service to read logs from (e.g., payment-service)",
            "minutes", "How many minutes back to search (e.g., 30)"
        );
    }

    @Override
    public String execute(Map<String, String> params, AgentContext context) {
        return context.getScenarioData().getOrDefault("logs",
            "[INFO] No logs available for this scenario.");
    }
}
