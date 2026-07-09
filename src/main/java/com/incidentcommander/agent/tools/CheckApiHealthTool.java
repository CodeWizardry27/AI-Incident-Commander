package com.incidentcommander.agent.tools;

import com.incidentcommander.agent.core.AgentContext;
import com.incidentcommander.agent.core.Tool;
import java.util.Map;

/** Checks HTTP health status of API endpoints. */
public class CheckApiHealthTool implements Tool {
    @Override public String getName() { return "check_api_health"; }
    @Override public String getDescription() { return "Checks HTTP status of API endpoints. Returns status code, response time, and error details."; }
    @Override public Map<String, String> getParameters() { return Map.of("endpoint", "The API endpoint to check (e.g., /api/payments)"); }
    @Override public String execute(Map<String, String> params, AgentContext context) {
        return context.getScenarioData().getOrDefault("api_health", "No API health data available.");
    }
}
