package com.incidentcommander.agent.agents;

import com.incidentcommander.agent.core.AgentBase;
import com.incidentcommander.agent.core.Tool;
import com.incidentcommander.agent.tools.*;
import com.incidentcommander.service.GeminiService;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Analyst Agent — Root cause analyst. Finds WHY it broke.
 * 
 * Tools: read_git_commits, check_slow_queries, read_deployment_history
 * 
 * Goal: Cross-reference Detective's findings with code changes and deployments 
 *       to find the ROOT CAUSE.
 */
@Component
public class AnalystAgent extends AgentBase {

    private final List<Tool> tools;

    public AnalystAgent(GeminiService geminiService) {
        super(geminiService);
        this.tools = List.of(
            new ReadGitCommitsTool(),
            new CheckSlowQueriesTool(),
            new ReadDeploymentHistoryTool()
        );
    }

    @Override
    public String getName() {
        return "analyst";
    }

    @Override
    public String getRole() {
        return "Senior Software Engineer specializing in root cause analysis. " +
               "The Detective has already identified the symptoms. " +
               "Your job is to find the ROOT CAUSE — what specific code change, deployment, " +
               "or configuration change caused this incident. " +
               "Check recent git commits, slow query logs, and deployment history. " +
               "Cross-reference timestamps: when did the issue start vs when was the last deployment?";
    }

    @Override
    public List<Tool> getTools() {
        return tools;
    }
}
