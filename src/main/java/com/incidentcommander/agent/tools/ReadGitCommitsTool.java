package com.incidentcommander.agent.tools;

import com.incidentcommander.agent.core.AgentContext;
import com.incidentcommander.agent.core.Tool;
import java.util.Map;

/** Reads recent git commits with diffs to find what code changed. */
public class ReadGitCommitsTool implements Tool {
    @Override public String getName() { return "read_git_commits"; }
    @Override public String getDescription() { return "Returns recent git commits with author, timestamp, message, and code diff. Helps identify what code change caused the issue."; }
    @Override public Map<String, String> getParameters() { return Map.of("hours", "How many hours back to search (e.g., 24)"); }
    @Override public String execute(Map<String, String> params, AgentContext context) {
        return context.getScenarioData().getOrDefault("git_commits", "No git commit data available.");
    }
}
