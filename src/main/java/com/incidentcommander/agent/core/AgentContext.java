package com.incidentcommander.agent.core;

import lombok.Data;
import java.util.HashMap;
import java.util.Map;

/**
 * Shared context passed between all agents during an investigation.
 * 
 * Contains:
 * - The incident details (description, scenario ID)
 * - Findings from previous agents (Detective's output → Analyst's input)
 * - Scenario-specific simulated data
 */
@Data
public class AgentContext {

    /** The incident ID being investigated */
    private Long incidentId;

    /** The original incident description from the user */
    private String incidentDescription;

    /** The scenario ID (e.g., "db_pool_exhaustion") — determines simulated data */
    private String scenarioId;

    /** 
     * Findings from previous agents.
     * Key = agent name (e.g., "detective", "analyst")
     * Value = that agent's conclusion
     */
    private Map<String, String> agentFindings = new HashMap<>();

    /** 
     * Scenario-specific data that tools read from.
     * Key = data type (e.g., "logs", "metrics", "git_commits")
     * Value = the simulated data string
     */
    private Map<String, String> scenarioData = new HashMap<>();

    /**
     * Add findings from an agent (passed to the next agent as context).
     */
    public void addFinding(String agentName, String finding) {
        agentFindings.put(agentName, finding);
    }

    /**
     * Get a summary of all previous findings (injected into the next agent's prompt).
     */
    public String getPreviousFindingsSummary() {
        if (agentFindings.isEmpty()) {
            return "No previous findings — you are the first agent investigating.";
        }
        
        StringBuilder sb = new StringBuilder("=== FINDINGS FROM PREVIOUS AGENTS ===\n");
        agentFindings.forEach((agent, finding) -> {
            sb.append("\n--- ").append(agent.toUpperCase()).append(" AGENT ---\n");
            sb.append(finding).append("\n");
        });
        return sb.toString();
    }
}
