package com.incidentcommander.agent.tools;

import com.incidentcommander.agent.core.AgentContext;
import com.incidentcommander.agent.core.Tool;
import java.util.Map;

/** Checks CPU, memory, request latency, error rate metrics. */
public class CheckMetricsTool implements Tool {
    @Override public String getName() { return "check_metrics"; }
    @Override public String getDescription() { return "Returns CPU usage, memory usage, request latency, and error rate for the service."; }
    @Override public Map<String, String> getParameters() { return Map.of("service", "Name of the service"); }
    @Override public String execute(Map<String, String> params, AgentContext context) {
        return context.getScenarioData().getOrDefault("metrics", "No metrics available.");
    }
}
