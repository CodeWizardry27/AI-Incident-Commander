package com.incidentcommander.agent.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.incidentcommander.service.GeminiService;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ReActEngine — The Reasoning + Acting Loop.
 * 
 * This is the CORE of the agentic AI system.
 * 
 * Flow:
 * 1. Send system prompt + task + available tools to Gemini
 * 2. Gemini responds with THOUGHT + ACTION (tool call)
 * 3. Java executes the tool, gets result
 * 4. Feed result back to Gemini as OBSERVATION
 * 5. Gemini reasons again — may call another tool or give FINAL_ANSWER
 * 6. Repeat until FINAL_ANSWER or max iterations reached
 * 
 * This is the same pattern used by LangChain/CrewAI internally,
 * but built from scratch in Java.
 */
@Slf4j
public class ReActEngine {

    private static final int MAX_ITERATIONS = 10;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Pattern to extract tool calls from Gemini's response
    private static final Pattern TOOL_CALL_PATTERN = Pattern.compile(
        "ACTION:\\s*([a-z_]+)\\s*\\((.*)\\)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );
    private static final Pattern FINAL_ANSWER_PATTERN = Pattern.compile(
        "FINAL_ANSWER:\\s*(.*)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    /**
     * Run the ReAct loop.
     *
     * @param geminiService  The Gemini API wrapper
     * @param systemPrompt   The agent's system prompt (role, instructions, tool descriptions)
     * @param task           The task/question for the agent
     * @param tools          Available tools for this agent
     * @param context        Shared context with scenario data
     * @param stepCallback   Callback fired for each step (for SSE streaming)
     * @return AgentResult with conclusion and full step trace
     */
    public static AgentResult run(
            GeminiService geminiService,
            String systemPrompt,
            String task,
            List<Tool> tools,
            AgentContext context,
            Consumer<AgentResult.AgentStep> stepCallback
    ) {
        List<AgentResult.AgentStep> steps = new ArrayList<>();
        List<Map<String, String>> conversation = new ArrayList<>();
        
        // First message: the task
        conversation.add(Map.of("role", "user", "text", task));

        for (int i = 0; i < MAX_ITERATIONS; i++) {
            log.info("ReAct iteration {}/{}", i + 1, MAX_ITERATIONS);

            // THINK: Ask Gemini what to do
            String aiResponse = geminiService.chat(systemPrompt, conversation);
            
            if (aiResponse.startsWith("ERROR:")) {
                AgentResult.AgentStep errorStep = AgentResult.AgentStep.builder()
                        .type(AgentResult.AgentStep.Type.ERROR)
                        .content(aiResponse)
                        .build();
                steps.add(errorStep);
                fireCallback(stepCallback, errorStep);
                
                return AgentResult.builder()
                        .success(false)
                        .conclusion("Agent failed: " + aiResponse)
                        .iterationsUsed(i + 1)
                        .steps(steps)
                        .build();
            }

            // Record the THINK step
            AgentResult.AgentStep thinkStep = AgentResult.AgentStep.builder()
                    .type(AgentResult.AgentStep.Type.THINK)
                    .content(extractThought(aiResponse))
                    .build();
            steps.add(thinkStep);
            fireCallback(stepCallback, thinkStep);

            // Check if this is a FINAL_ANSWER
            Matcher finalMatcher = FINAL_ANSWER_PATTERN.matcher(aiResponse);
            if (finalMatcher.find()) {
                String conclusion = finalMatcher.group(1).trim();
                
                AgentResult.AgentStep conclusionStep = AgentResult.AgentStep.builder()
                        .type(AgentResult.AgentStep.Type.CONCLUSION)
                        .content(conclusion)
                        .build();
                steps.add(conclusionStep);
                fireCallback(stepCallback, conclusionStep);

                return AgentResult.builder()
                        .success(true)
                        .conclusion(conclusion)
                        .iterationsUsed(i + 1)
                        .steps(steps)
                        .build();
            }

            // Check if there's a tool call (ACTION)
            Matcher toolMatcher = TOOL_CALL_PATTERN.matcher(aiResponse);
            if (toolMatcher.find()) {
                String toolName = toolMatcher.group(1).trim();
                String toolArgsRaw = toolMatcher.group(2).trim();
                Map<String, String> toolParams = parseToolParams(toolArgsRaw);

                // Record ACT step
                AgentResult.AgentStep actStep = AgentResult.AgentStep.builder()
                        .type(AgentResult.AgentStep.Type.ACT)
                        .content("Calling tool: " + toolName)
                        .toolName(toolName)
                        .toolInput(toolArgsRaw)
                        .build();
                steps.add(actStep);
                fireCallback(stepCallback, actStep);

                // Execute the tool
                String toolOutput = executeTool(toolName, toolParams, tools, context);

                // Record OBSERVE step
                AgentResult.AgentStep observeStep = AgentResult.AgentStep.builder()
                        .type(AgentResult.AgentStep.Type.OBSERVE)
                        .content("Tool output received")
                        .toolName(toolName)
                        .toolOutput(toolOutput)
                        .build();
                steps.add(observeStep);
                fireCallback(stepCallback, observeStep);

                // Add AI response and observation to conversation for next iteration
                conversation.add(Map.of("role", "model", "text", aiResponse));
                conversation.add(Map.of("role", "user", "text", 
                    "OBSERVATION from " + toolName + ":\n" + toolOutput + 
                    "\n\nContinue your investigation. Use another tool or provide FINAL_ANSWER."));
            } else {
                // No tool call and no final answer — AI might have just reasoned
                // Add response and ask it to continue
                conversation.add(Map.of("role", "model", "text", aiResponse));
                conversation.add(Map.of("role", "user", "text",
                    "Please either call a tool using ACTION: tool_name(params) or provide your FINAL_ANSWER: <conclusion>"));
            }
        }

        // Max iterations reached
        log.warn("ReAct loop reached max iterations ({})", MAX_ITERATIONS);
        String lastThoughts = steps.stream()
                .filter(s -> s.getType() == AgentResult.AgentStep.Type.THINK)
                .reduce((a, b) -> b)
                .map(AgentResult.AgentStep::getContent)
                .orElse("Investigation incomplete");

        return AgentResult.builder()
                .success(true)
                .conclusion("Investigation completed (max iterations reached). Last findings: " + lastThoughts)
                .iterationsUsed(MAX_ITERATIONS)
                .steps(steps)
                .build();
    }

    /**
     * Extract the THOUGHT portion from the AI's response.
     */
    private static String extractThought(String response) {
        // Try to find explicit THOUGHT: prefix
        int thoughtIdx = response.toUpperCase().indexOf("THOUGHT:");
        if (thoughtIdx >= 0) {
            String afterThought = response.substring(thoughtIdx + 8);
            int actionIdx = afterThought.toUpperCase().indexOf("ACTION:");
            int finalIdx = afterThought.toUpperCase().indexOf("FINAL_ANSWER:");
            int end = afterThought.length();
            if (actionIdx > 0) end = Math.min(end, actionIdx);
            if (finalIdx > 0) end = Math.min(end, finalIdx);
            return afterThought.substring(0, end).trim();
        }
        
        // If no explicit THOUGHT prefix, use the first part before ACTION/FINAL_ANSWER
        int actionIdx = response.toUpperCase().indexOf("ACTION:");
        int finalIdx = response.toUpperCase().indexOf("FINAL_ANSWER:");
        int end = response.length();
        if (actionIdx > 0) end = Math.min(end, actionIdx);
        if (finalIdx > 0) end = Math.min(end, finalIdx);
        return response.substring(0, end).trim();
    }

    /**
     * Parse tool parameters from the raw string.
     * Supports: tool_name(key1="value1", key2="value2")
     * Also supports: tool_name(value1, value2) — positional
     */
    private static Map<String, String> parseToolParams(String raw) {
        Map<String, String> params = new HashMap<>();
        if (raw == null || raw.isBlank()) return params;

        // Try key=value parsing first
        Pattern kvPattern = Pattern.compile("(\\w+)\\s*=\\s*\"([^\"]*)\"");
        Matcher kvMatcher = kvPattern.matcher(raw);
        boolean found = false;
        while (kvMatcher.find()) {
            params.put(kvMatcher.group(1), kvMatcher.group(2));
            found = true;
        }
        
        // Also try key=value without quotes
        if (!found) {
            Pattern kvNoQuotePattern = Pattern.compile("(\\w+)\\s*=\\s*([^,\\)]+)");
            Matcher kvNoQuoteMatcher = kvNoQuotePattern.matcher(raw);
            while (kvNoQuoteMatcher.find()) {
                params.put(kvNoQuoteMatcher.group(1).trim(), kvNoQuoteMatcher.group(2).trim().replace("\"", ""));
                found = true;
            }
        }

        // Fallback: treat entire string as a single "input" param
        if (!found && !raw.isBlank()) {
            params.put("input", raw.replace("\"", "").trim());
        }

        return params;
    }

    /**
     * Find and execute the requested tool.
     */
    private static String executeTool(String toolName, Map<String, String> params, 
                                       List<Tool> tools, AgentContext context) {
        Optional<Tool> tool = tools.stream()
                .filter(t -> t.getName().equalsIgnoreCase(toolName))
                .findFirst();

        if (tool.isPresent()) {
            try {
                log.info("Executing tool: {} with params: {}", toolName, params);
                return tool.get().execute(params, context);
            } catch (Exception e) {
                log.error("Tool execution failed: {}", e.getMessage(), e);
                return "ERROR: Tool '" + toolName + "' failed - " + e.getMessage();
            }
        } else {
            log.warn("Tool not found: {}", toolName);
            return "ERROR: Tool '" + toolName + "' not found. Available tools: " +
                    tools.stream().map(Tool::getName).reduce((a, b) -> a + ", " + b).orElse("none");
        }
    }

    private static void fireCallback(Consumer<AgentResult.AgentStep> callback, AgentResult.AgentStep step) {
        if (callback != null) {
            try {
                callback.accept(step);
            } catch (Exception e) {
                log.error("Step callback failed: {}", e.getMessage());
            }
        }
    }
}
