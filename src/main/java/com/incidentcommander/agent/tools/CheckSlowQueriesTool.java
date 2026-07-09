package com.incidentcommander.agent.tools;

import com.incidentcommander.agent.core.AgentContext;
import com.incidentcommander.agent.core.Tool;
import java.util.Map;

/** Returns MySQL slow query log entries. */
public class CheckSlowQueriesTool implements Tool {
    @Override public String getName() { return "check_slow_queries"; }
    @Override public String getDescription() { return "Returns MySQL slow query log. Shows queries that took longer than 1 second, with execution time and affected rows."; }
    @Override public Map<String, String> getParameters() { return Map.of(); }
    @Override public String execute(Map<String, String> params, AgentContext context) {
        return context.getScenarioData().getOrDefault("slow_queries", "No slow queries found.");
    }
}
