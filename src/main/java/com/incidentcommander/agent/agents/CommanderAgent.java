package com.incidentcommander.agent.agents;

import com.incidentcommander.agent.core.AgentBase;
import com.incidentcommander.agent.core.AgentContext;
import com.incidentcommander.agent.core.Tool;
import com.incidentcommander.service.GeminiService;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Commander Agent — The orchestrator. Compiles everything into a final action plan.
 * 
 * Takes findings from all 3 agents and produces:
 * - Executive summary
 * - Prioritized action plan
 * - Risk assessment
 * - Prevention recommendations
 */
@Component
public class CommanderAgent extends AgentBase {

    public CommanderAgent(GeminiService geminiService) {
        super(geminiService);
    }

    @Override
    public String getName() {
        return "commander";
    }

    @Override
    public String getRole() {
        return "VP of Engineering compiling an incident post-mortem report. " +
               "Three agents have already investigated this incident:\n" +
               "- Detective: identified symptoms\n" +
               "- Analyst: found root cause\n" +
               "- Fixer: generated fixes\n\n" +
               "Your job is to compile their findings into a clear, executive-level report.";
    }

    @Override
    public List<Tool> getTools() {
        return List.of();
    }

    @Override
    protected String buildSystemPrompt(AgentContext context) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a ").append(getRole()).append("\n\n");
        prompt.append("Compile the investigation findings into a structured report.\n\n");
        prompt.append("Respond with:\n");
        prompt.append("THOUGHT: <your analysis of all findings>\n");
        prompt.append("FINAL_ANSWER: <your complete executive report>\n\n");
        prompt.append("Your FINAL_ANSWER must be a structured report with:\n");
        prompt.append("## EXECUTIVE SUMMARY\n");
        prompt.append("A 2-3 sentence non-technical summary.\n\n");
        prompt.append("## INCIDENT TIMELINE\n");
        prompt.append("What happened and when.\n\n");
        prompt.append("## ROOT CAUSE\n");
        prompt.append("Technical root cause.\n\n");
        prompt.append("## IMPACT\n");
        prompt.append("What was affected and for how long.\n\n");
        prompt.append("## ACTION ITEMS\n");
        prompt.append("Prioritized list: P0 (do now), P1 (this week), P2 (this month).\n\n");
        prompt.append("## PREVENTION\n");
        prompt.append("How to prevent this from happening again.\n");
        return prompt.toString();
    }
}
