package com.incidentcommander.agent.tools;

import com.incidentcommander.agent.core.AgentContext;
import com.incidentcommander.agent.core.Tool;
import java.util.Map;

/** Checks database connection pool status, active queries, slow queries. */
public class CheckDbStatusTool implements Tool {
    @Override public String getName() { return "check_db_status"; }
    @Override public String getDescription() { return "Returns database connection pool status, active connections, waiting threads, and average query time."; }
    @Override public Map<String, String> getParameters() { return Map.of(); }
    @Override public String execute(Map<String, String> params, AgentContext context) {
        return context.getScenarioData().getOrDefault("db_status", "No database status available.");
    }
}
