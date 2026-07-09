package com.incidentcommander.agent.core;

import com.incidentcommander.service.GeminiService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * AgentBase — Abstract base class for all agents.
 * 
 * Design Pattern: Template Method Pattern.
 * - Subclasses define: name, role, system prompt, and tools
 * - Base class handles: the ReAct loop execution
 * 
 * To create a new agent:
 * 1. Extend AgentBase
 * 2. Implement getName(), getRole(), getTools()
 * 3. Override buildSystemPrompt() for custom instructions
 * 4. Register your tools
 * That's it — the framework handles the rest.
 */
@Slf4j
@Getter
public abstract class AgentBase {

    protected final GeminiService geminiService;

    protected AgentBase(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    /** Agent's unique name (e.g., "detective", "analyst") */
    public abstract String getName();

    /** Agent's role description (e.g., "Senior SRE diagnosing production incidents") */
    public abstract String getRole();

    /** List of tools available to this agent */
    public abstract List<Tool> getTools();

    /**
     * Execute this agent's investigation.
     * 
     * @param context       Shared context with incident data + previous findings
     * @param stepCallback  Callback for each step (for real-time SSE streaming)
     * @return AgentResult with conclusion and step trace
     */
    public AgentResult execute(AgentContext context, Consumer<AgentResult.AgentStep> stepCallback) {
        log.info("=== {} Agent starting investigation ===", getName().toUpperCase());

        String systemPrompt = buildSystemPrompt(context);
        String task = buildTask(context);

        AgentResult result = ReActEngine.run(
                geminiService, systemPrompt, task, getTools(), context, stepCallback
        );

        result.setAgentName(getName());

        // Store this agent's findings in context for the next agent
        context.addFinding(getName(), result.getConclusion());

        log.info("=== {} Agent finished ({} iterations, success={}) ===",
                getName().toUpperCase(), result.getIterationsUsed(), result.isSuccess());

        return result;
    }

    /**
     * Build the system prompt for this agent.
     * Includes: role, instructions, tool descriptions, output format.
     * 
     * Override in subclasses for custom prompts.
     */
    protected String buildSystemPrompt(AgentContext context) {
        StringBuilder prompt = new StringBuilder();

        // Role
        prompt.append("You are a ").append(getRole()).append(".\n\n");

        // Available tools
        prompt.append("You have access to the following tools:\n\n");
        for (Tool tool : getTools()) {
            prompt.append("- ").append(tool.getName()).append(": ").append(tool.getDescription()).append("\n");
            if (!tool.getParameters().isEmpty()) {
                prompt.append("  Parameters: ");
                prompt.append(tool.getParameters().entrySet().stream()
                        .map(e -> e.getKey() + " (" + e.getValue() + ")")
                        .collect(Collectors.joining(", ")));
                prompt.append("\n");
            }
        }

        // Output format instructions
        prompt.append("\n");
        prompt.append("=== HOW TO RESPOND ===\n");
        prompt.append("You must follow this EXACT format in every response:\n\n");
        prompt.append("THOUGHT: <your reasoning about what to investigate next>\n");
        prompt.append("ACTION: tool_name(param1=\"value1\", param2=\"value2\")\n\n");
        prompt.append("OR, when you have enough evidence:\n\n");
        prompt.append("THOUGHT: <your final reasoning>\n");
        prompt.append("FINAL_ANSWER: <your complete conclusion with all evidence>\n\n");
        prompt.append("IMPORTANT RULES:\n");
        prompt.append("- Always start with THOUGHT before ACTION or FINAL_ANSWER\n");
        prompt.append("- Call ONE tool at a time\n");
        prompt.append("- Use evidence from tool outputs to reason — don't guess\n");
        prompt.append("- Provide FINAL_ANSWER only when you have sufficient evidence\n");
        prompt.append("- Be specific and technical in your conclusions\n");

        return prompt.toString();
    }

    /**
     * Build the task message sent as the first user message.
     * Includes: incident description + previous agent findings.
     */
    protected String buildTask(AgentContext context) {
        StringBuilder task = new StringBuilder();
        task.append("=== INCIDENT ===\n");
        task.append(context.getIncidentDescription()).append("\n\n");
        
        // Include findings from previous agents
        String previousFindings = context.getPreviousFindingsSummary();
        if (!previousFindings.contains("No previous findings")) {
            task.append(previousFindings).append("\n\n");
        }

        task.append("Begin your investigation. Use your tools to gather evidence.\n");
        return task.toString();
    }
}
