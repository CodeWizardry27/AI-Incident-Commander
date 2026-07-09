package com.incidentcommander.agent.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * The result produced by an agent after completing its investigation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentResult {

    /** Name of the agent that produced this result */
    private String agentName;

    /** The agent's final conclusion / finding */
    private String conclusion;

    /** Whether the agent completed successfully */
    @Builder.Default
    private boolean success = true;

    /** Number of ReAct iterations it took */
    private int iterationsUsed;

    /** Log of all steps (THINK → ACT → OBSERVE → ...) for display */
    @Builder.Default
    private List<AgentStep> steps = new ArrayList<>();

    /**
     * Represents a single step in the agent's reasoning process.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgentStep {
        
        public enum Type {
            THINK, ACT, OBSERVE, CONCLUSION, ERROR
        }

        private Type type;
        private String content;
        
        /** Only for ACT steps — which tool was called */
        private String toolName;
        
        /** Only for ACT steps — what parameters were passed */
        private String toolInput;
        
        /** Only for OBSERVE steps — what the tool returned */
        private String toolOutput;
    }
}
