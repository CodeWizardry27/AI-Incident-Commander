package com.incidentcommander.agent.core;

import java.util.Map;

/**
 * Tool interface — every capability an agent has is a Tool.
 * 
 * Examples: read_logs, check_metrics, check_db_status, read_git_commits
 * 
 * Design Pattern: Strategy Pattern — tools are interchangeable and pluggable.
 * Adding a new tool = implement this interface + register it with an agent.
 */
public interface Tool {

    /**
     * Unique name of the tool (used by AI to call it).
     * Example: "read_logs", "check_db_status"
     */
    String getName();

    /**
     * Human-readable description (sent to AI so it knows WHEN to use this tool).
     * Example: "Reads application logs for a given service. Returns recent log entries."
     */
    String getDescription();

    /**
     * Parameter descriptions (sent to AI so it knows WHAT to pass).
     * Example: {"service": "name of the service", "minutes": "how many minutes back to search"}
     */
    Map<String, String> getParameters();

    /**
     * Execute the tool with given parameters and return the output.
     * 
     * @param params Key-value parameters passed by the AI
     * @param context Shared context across agents (contains scenario data, incident info)
     * @return Tool output as a string (fed back to the AI for reasoning)
     */
    String execute(Map<String, String> params, AgentContext context);
}
