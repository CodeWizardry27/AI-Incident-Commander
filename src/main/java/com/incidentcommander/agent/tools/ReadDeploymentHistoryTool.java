package com.incidentcommander.agent.tools;

import com.incidentcommander.agent.core.AgentContext;
import com.incidentcommander.agent.core.Tool;
import java.util.Map;

/** Returns recent deployment history with timestamps. */
public class ReadDeploymentHistoryTool implements Tool {
    @Override public String getName() { return "read_deployment_history"; }
    @Override public String getDescription() { return "Returns recent deployment history showing service name, version, deployer, timestamp, and status."; }
    @Override public Map<String, String> getParameters() { return Map.of("hours", "How many hours back (e.g., 48)"); }
    @Override public String execute(Map<String, String> params, AgentContext context) {
        return context.getScenarioData().getOrDefault("deployments", "No deployment history available.");
    }
}
