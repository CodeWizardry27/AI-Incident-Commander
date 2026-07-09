package com.incidentcommander.agent.agents;

import com.incidentcommander.agent.core.AgentBase;
import com.incidentcommander.agent.core.AgentContext;
import com.incidentcommander.agent.core.Tool;
import com.incidentcommander.service.GeminiService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Fixer Agent — Solution generator. Creates actionable fixes.
 * 
 * No external tools — uses AI reasoning based on Detective + Analyst findings
 * to generate immediate fixes, permanent fixes, SQL, and config changes.
 */
@Component
public class FixerAgent extends AgentBase {

    public FixerAgent(GeminiService geminiService) {
        super(geminiService);
    }

    @Override
    public String getName() {
        return "fixer";
    }

    @Override
    public String getRole() {
        return "Senior DevOps Engineer and Database Administrator specializing in incident remediation. " +
               "The Detective found the symptoms and the Analyst found the root cause. " +
               "Your job is to generate CONCRETE, ACTIONABLE fixes. " +
               "Provide:\n" +
               "1. IMMEDIATE FIX — what to do RIGHT NOW to stop the bleeding\n" +
               "2. PERMANENT FIX — what to do to prevent this from happening again\n" +
               "3. If database-related: provide the exact SQL commands (CREATE INDEX, ALTER TABLE, etc.)\n" +
               "4. If config-related: provide the exact configuration changes\n" +
               "Be specific — provide actual commands, not vague suggestions.";
    }

    @Override
    public List<Tool> getTools() {
        // Fixer uses no tools — pure AI reasoning based on previous findings
        return List.of();
    }

    @Override
    protected String buildSystemPrompt(AgentContext context) {
        // Fixer doesn't need tool instructions since it has no tools
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a ").append(getRole()).append("\n\n");
        prompt.append("Based on the investigation findings provided, generate concrete fixes.\n\n");
        prompt.append("Respond with:\n");
        prompt.append("THOUGHT: <your analysis of the root cause and what fixes are needed>\n");
        prompt.append("FINAL_ANSWER: <your complete fix recommendation with all details>\n\n");
        prompt.append("Your FINAL_ANSWER must include sections for:\n");
        prompt.append("- IMMEDIATE FIX (stop the bleeding now)\n");
        prompt.append("- PERMANENT FIX (prevent recurrence)\n");
        prompt.append("- SQL COMMANDS (if applicable)\n");
        prompt.append("- CONFIG CHANGES (if applicable)\n");
        return prompt.toString();
    }
}
