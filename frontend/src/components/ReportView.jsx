export default function ReportView({
    activeIncident,
    loadingReport,
    report,
    onBackToDashboard,
    onReplayReasoning,
                                   }) {
    return (
        <div className="layout">
            <div className="header" style={{marginBottom: '24px'}}>
                <div className="header-title">
                    <span className="badge badge-resolved" style={{marginBottom: '8px'}}>
                        Incident Resolved
                    </span>
                    <h1>Post-Mortem: {activeIncident.title}</h1>
                    <p style={{marginTop: '4px'}}>
                        Incident ID: #{activeIncident.id} | Resolved via multi-agent recovery
                    </p>
                </div>
                <div>
                    <button onClick={onBackToDashboard} className="btn-primary">
                        Return to Dashboard
                    </button>
                </div>
            </div>

            {loadingReport ? (
                <div className="glass-panel" style={{textAlign: 'center', padding: '40px'}}>
                    <div style={{color: 'var(--color-text-secondary)'}}>Loading report artifact data...</div>
                </div>
            ) : !report ? (
                <div className="glass-panel"
                     style={{textAlign: 'center', padding: '40px', color: 'var(--color-danger)'}}
                     >
                    Failed to load incident report details. Ensure the backend generated it successfully.
                </div>
            ) : (
                <div style={{display: 'grid', gridTemplateColumns: '1fr', gap: '24px'}}>
                    <div className="glass-panel">
                        <div className="section-title">
                            <svg
                                width="20"
                                height="20"
                                viewBox="0 0 24 24"
                                fill="none"
                                stroke="currentColor"
                                strokeWidth="2.5"
                                style={{color: 'var(--color-commander)'}}
                                >
                                <path d="M12 2L2 7110 5 10-5-10-5zM2 17110 5 10-5M2 12110 5 10-5"/>
                            </svg>
                            <h2>Executive Summary</h2>
                        </div>
                        <div className="report-section">
                            <p style={{
                                whiteSpace: 'pre-wrap',
                                color: 'var(--color-text-primary)',
                                fontSize: '1rem',
                            }}
                               >
                                {report.executiveSummary}
                            </p>
                        </div>
                    </div>

                    <div className="glass-panel"
                         style={{
                             display: 'grid',
                             gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))',
                             gap: '24px',
                         }}
                         >
                        <div>
                            <div className="section-title">
                                <svg
                                    width="18"
                                    height="18"
                                    viewBox="0 0 24 24"
                                    fill="none"
                                    stroke="currentColor"
                                    strokeWidth="2.5"
                                    style={{color: 'var(--color-analyst)'}}
                                    >
                                    <circle cx="12" cy="12" r="10" />
                                    <line x1="12" y1="16" x2="12" y2="12"/>
                                    <line x1="12" y1="8" x2="12.01" y2="8" />
                                </svg>
                                <h3>Root Cause Details</h3>
                            </div>
                            <div className="report-section">
                                <p style={{whiteSpace: 'pre-wrap'}}>{report.rootCause}</p>
                            </div>
                        </div>

                        <div>
                            <div className="section-title">
                                <svg
                                    width="18"
                                    height="18"
                                    viewBox="0 0 24 24"
                                    fill="none"
                                    stroke="currentColor"
                                    strokeWidth="2.5"
                                    style={{color: 'var(--color-fixer)'}}
                                    >
                                    <path d="M14.7 6.3a1 1 0 0 0 0 1.411.6 1.6a1 1 0 0 0 1.4 013.77-3.77a6 6 0 0 1-7.94 7.941-6.91 6.91a2.12 2.12 0 0 1-3-316.91-6.91a6 6 0 0 1 7.94-7.941-3.76 3.76z"/>
                                </svg>
                                <h3>Remediation Fix Plan</h3>
                            </div>
                            <div className="report-section">
                                <p style={{whiteSpace: 'pre-wrap'}}>{report.actionPlan}</p>
                            </div>
                        </div>
                    </div>

                    {(report.generatedSql || report.configChanges) && (
                        <div className="glass-panel">
                            <div className="section-title">
                                <svg
                                    width="20"
                                    height="20"
                                    viewBox="0 0 24 24"
                                    fill="none"
                                    stroke="currentColor"
                                    strokeWidth="2.5"
                                    style={{color: 'var(--color-accent)'}}
                                    >
                                    <polyline points="16 18 22 12 16 6"/>
                                    <polyline points="8 6 2 12 8 18"/>
                                </svg>
                                <h2>Generated Hot</h2>
                            </div>

                            {report.generatedSql && !report.generatedSql.includes('NO_SQL_REQUIRED') && (
                                <div className="report-section" style={{marginBottom: '20px'}}>
                                    <h4 style={{
                                        fontWeight: 600,
                                        marginBottom: '6px',
                                        color: 'var(--color-text-primary)',
                                    }}
                                        >
                                        Database Migration Patch(SQL DLL)
                                    </h4>
                                    <div className="code-container">{report.generatedSql}</div>
                                </div>
                            )}

                            {report.configChanges &&
                               !report.configChanges.includes('No configuration updates') && (
                                   <div className="report-section">
                                       <h4 style={{
                                           fontWeight: 600,
                                           marginBottom: '6px',
                                           color: 'var(--color-text-primary)'
                                       }}
                                           >
                                           Application Configuration Override(`application.properties`)
                                       </h4>
                                       <div className="code-container" style={{color: '#a855f7'}}>
                                           {report.configChanges}
                                       </div>
                                   </div>
                                )}
                        </div>
                    )}

                    <div className="glass-panel">
                        <div className="section-title">
                            <svg
                                width="20"
                                height="20"
                                viewBox="0 0 24 24"
                                fill="none"
                                stroke="currentColor"
                                strokeWidth="2.5"
                                style={{color: 'var(--color-text-secondary'}}
                                >
                                <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
                                <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
                            </svg>
                            <h2>Reasoning Activity Archive</h2>
                        </div>
                        <button
                            onClick={onReplayReasoning}
                            className="btn-secondary"
                            style={{display: 'inline-flex', alignItems: 'center', gap: '8px'}}
                            >
                            <svg
                                width="14"
                                height="14"
                                viewBox="0 0 24 24"
                                fill="none"
                                stroke="currentColor"
                                strokeWidth='2.5'
                                >
                                <path d="M23 4v6h-6M1 20v-6h6"/>
                                <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 1414.64 4.36A9 9 0 0 0 20.49 15"/>
                            </svg>
                            Replay Agent Reasoning Chain
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
}