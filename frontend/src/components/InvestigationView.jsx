export default function InvestigationView({
    activeIncident,
    consoleEndRef,
    expandedLogs,
    getAgentColor,
    isInvestigationFinished,
    logs,
    pipelineState,
    onBackToDashboard,
    onToggleLogToll,
    onViewReport,
                                          }) {
    const pipelineProgressWidth=
        pipelineState.Commander === 'complete' ? '80%'
            : pipelineState.Fixer === 'complete' ? '60%'
                : pipelineState.Analyst === 'complete' ? '40%'
                    : pipelineState.Detective === 'complete' ? '20%'
                        : '0%';

    return (
        <div className="layout">
            <div className="header" style={{marginBottom: '20px'}}>
                <div className="header-title">
                    <span className={`badge badge-${activeIncident.severity.toLowerCase()}`} style={{marginsBottom: '8px'}}>
                        {activeIncident.severity} INCIDENT
                    </span>
                    <h1 style={{display: 'flex', alignItems: 'center', gap: '10px'}}>
                        {!isInvestigationFinished && <span className="dot-pulse" /> }
                        {activeIncident.title}
                    </h1>
                    <p style={{marginTop: '4px'}}>Active automated diagnosis & recovery session</p>
                </div>
                <div>
                    {isInvestigationFinished ? (
                        <button
                            onClick={onViewReport}
                            className="btn-primary"
                            style={{
                                background: 'linear-gradient(135deg, var(--color-success) 0% #059669 100%)',
                                boxShadow: '0 4px 12px rgba(16, 185, 129, 0.25)',
                            }}
                            >
                            Open Remediation Report
                        </button>
                    ) : (
                        <button
                            disabled
                            className="btn-secondary"
                            style={{cursor: 'not-allowed', color: 'var(--color-text-muted'}}
                            >
                            Awaiting Agent Resolution...
                        </button>
                    )}
                </div>
            </div>

            <div className="glass-panel" style={{padding: '24px 20px', marginBottom: '24px'}}>
                <div className="pipeline-container">
                    <div className="pipeline-line"/>
                    <div className="pipeline-progress" style={{width: pipelineProgressWidth}}/>

                    <div className={`pipeline-node node-${pipelineState.Detective}`}
                         style={{'--agent-color': 'var(--color-detective)', '--agent-color-glow': 'rgba(6,182,212,0.3)'}}
                         >
                        <div className="node-circle">D</div>
                        <div className="node-label">detective (RCA)</div>
                    </div>

                    <div className={`pipeline-node node-${pipelineState.Analyst}`}
                         style={{'--agent-color': 'var(--color-analyst)', '--agent-color-glow': 'rgba(236, 72,153, 0.3)'}}
                         >
                        <div className="node-circle">A</div>
                        <div className="node-label">Analyst (RCA)</div>
                    </div>

                    <div className={`pipeline-node node-${pipelineState.Fixer}`}
                         style={{'--agent-color': 'var(--color-fixer)', '--agent-color-glow': 'rgba(249, 115,22, 0.3)'}}
                    >
                        <div className="node-circle">F</div>
                        <div className="node-label">Fixer (DevOps)</div>
                    </div>

                    <div className={`pipeline-node node-${pipelineState.Commander}`}
                         style={{'--agent-color': 'var(--color-commander)', '--agent-color-glow': 'rgba(168, 85,247, 0.3)'}}
                    >
                        <div className="node-circle">A</div>
                        <div className="node-label">Analyst (RCA</div>
                    </div>
                </div>
            </div>

            <div className="glass-panel console-panel">
                <div className="console-header">
                    <div className="console-title">
                        <span className="dot-pulse"
                              style={{background: isInvestigationFinished ? 'var(--color-text-muted' : 'var(--color-success)',}}/>
                        <span>OPERATOR_SESSION:~/agent_orchestrator$ tail -f activity.log</span>
                    </div>
                    <div style={{fontSize: '0.8rem', color: 'var(--color-text-muted)'}}>
                        Incident ID: #{activeIncident.id}
                    </div>
                </div>

                <div className="console-body">
                    {logs.length === 0 && (
                        <div style={{
                            color: 'var(--color-text-muted)',
                            fontStyle: 'italic',
                            textAlign: 'center',
                            padding: '40px 0',
                        }}
                             >
                            Initializing Agent Reasoning engine... (poling first step)
                        </div>
                    )}

                    {logs.map((log, index) => (
                        <div key={index} className="console-entry">
                            <div>
                                <span className={`agent-tag tag-${log.agentName.toLowerCase()}`}>
                                    {log.agentName.toUpperCase()}
                                </span>
                                <span className="console-text"
                                      style={{
                                fontWeight: log.stepType === 'CONCLUSION' ? 600 : 'normal',
                                      color: log.stepType === 'CONCLUSION' ? getAgentColor(log.agentName) : '#f1f5f9,'}}
                                >
                                    {log.content}
                                </span>
                            </div>

                            {log.stepType === 'THINK' && (
                                <div className="console-thught">&gt; {log.content}</div>
                            )}

                            {log.toolName && (
                                <div className="console-tool">
                                    <div style = {{
                                        color: 'var(--color-text-secondary)',
                                        display: 'flex',
                                        gap: '8px'
                                    }}
                                         >
                                        <span style={{color: 'var(--color-warning)'}}>[TOOL_CALL]</span>
                                        <span>
                                            Executing {log.toolName} with params:{' '}
                                            <code style={{
                                                color: '#e2e8f0',
                                                background: 'rgba(255,255,255,0.05)',
                                                padding: '2px 4px',
                                                borderRadius: '4px',
                                            }}
                                                  >
                                                {log.toolInput}
                                            </code>
                                        </span>
                                    </div>

                                    {log.toolOutput && (
                                        <div>
                                            <div className="tool-output-toggle" onClick={() => onToggleLogTool(index)}>
                                                <svg
                                                    width="12"
                                                    height="12"
                                                    viewBox="0 0 24 24"
                                                    fill="none"
                                                    stroke="currnetColor"
                                                    strokeWidth="3"
                                                    style={{
                                                        transform: expandedLogs[index] ? 'rotate(90deg)' : 'none',
                                                        transition: 'transform 0.15s ease',
                                                    }}
                                                    >
                                                    <polyline points="9 18 15 12 9 6"/>
                                                </svg>
                                                <span>
                                                    {expandedLogs[index] ? 'Hide' : 'Show'} Tool Output ({log.toolOUtput.length} bytes)
                                                </span>
                                            </div>
                                            {expandedLogs[index] && (
                                                <div className="tool-output-box">{log.toolOutput}</div>
                                            )}
                                        </div>
                                    )}
                                </div>
                            )}
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
}
