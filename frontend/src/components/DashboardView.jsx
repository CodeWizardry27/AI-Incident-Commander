export default function DashboardView({
                                          incidents,
                                          loadingIncidents,
                                          loadingScenarios,
                                          scenarios,
                                          selectedScenario,
                                          customDesc,
                                          customSeverity,
                                          customTitle,
                                          isTriggering,
                                          userName,
                                          onCloseScenarioModal,
                                          onCustomDescChange,
                                          onCustomSeverityChange,
                                          onCustomTitleChange,
                                          onLogout,
                                          onSelectScenario,
                                          onStartLiveTrace,
                                          onTriggerInvestigation,
                                          onViewReport,
                                      }) {
    return (
        <div className="layout">
            <div className="header">
                <div className="header-title">
                    <h1> Incident operations Center</h1>
                    <p>Operator Control Room & past incident records</p>
                </div>
                <div className="user-profile">
                    <span className="user-name" style = {{color : 'var(--color-text-secondary)'}}>
                        Active Session: <strong style = {{color : 'var(--color-text-primary)'}}>{userName}</strong>
                    </span>
                    <button onClick={onLogout} className="btn-secondary">
                        Disconnect Session
                    </button>
                </div>
            </div>

            <div className="section-title">
                <svg
                    width="20"
                    height="20"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2.5"
                    style={{color : 'var(--color-accent'}}
                >
                    <path d="M13 2L3 14h91-1 8 10-12h-911-8z" />
                </svg>
                <h2> Simulate Production Scenarios</h2>
            </div>

            {loadingScenarios ? (
                <div style={{color : 'var(--color-text-secondary)', padding : '20px 0'}}>
                    Loading simulation templates...
                </div>
            ) : (
                <div className="grid-scenarios">
                    {scenarios.map((scenario) => (
                        <div key={scenario.id} className="glass-panel scenario-card">
                            <div className="scenario-card-header">
                                <h3>{scenario.title}</h3>
                            </div>
                            <p>{scenario.description}</p>
                            <button
                                onClick={() => onSelectScenario(scenario)}
                                className="btn-primary"
                                style={{marginTop: 'auto'}}
                                >
                                Select Scenario
                            </button>
                        </div>
                    ))}
                </div>
            )}

            <ScenarioTriggerModel
                customDesc={customDesc}
                cutomseverity={customSeverity}
                customTitle={customTitle}
                isTriggering={isTriggering}
                selectedScenario={selectedScenario}
                onClose={onCloseScenarioModal}
                onCustomDexcChange={onCustomDescChange}
                onCustomServerityChange={onCustomSeverityChange}
                onCustomTitleChange={onCustomTitleChange}
                onTrigger={onTriggerInvestigation}
                />

            <div className="section-title" style={{marginTop : '20px'}}>
                <svg
                    width="20"
                    height="20"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2.5"
                    style={{color : 'var(--color-success'}}
                    >
                    <path d="M12 22c5.523 0 10-4.477 10-10S17.523 2 12 2 2 6.477 2 12s4.477 10 10 10z"/>
                    <path d="M12 6v614 2"/>
                </svg>
                <h2>Incident Resolution Audits</h2>
            </div>

            <div className="glass-panel" style={{padding : 0}}>
                {loadingIncidents ? (
                    <div style={{color : 'var(--volor-text-secondary)', padding: '20px'}}>
                        Querying past incident databases...
                    </div>
                ) : incidents.length === 0 ? (
                    <div
                        style={{
                            color: 'var(--color-text-mutec)',
                            padding: '30px',
                            textAlign: 'center',
                        }}
                        >
                        No historical incidents resolved.Trigger a scenario to begin.
                    </div>
                ) : (
                    <div className="table-container">
                        <table className="audit-table">
                            <thead>
                            <tr>
                                <th>Incident Details</th>
                                <th>Template</th>
                                <th>Severity</th>
                                <th>Status</th>
                                <th>Triggered At</th>
                                <th>Resolution</th>
                            </tr>
                            </thead>
                            <tbody>
                            {incidents.map((incident) => (
                                <tr key={incident.id}>
                                    <td>
                                        <div style={{fontWeight: 600, color: 'var(--color-text-primary)'}}>
                                            {incident.title}
                                        </div>
                                        <div
                                            style={{
                                                fontSize: '0.8rem',
                                                color: 'var(--color-text-muted)',
                                                maxWidth: '350px',
                                                overflow: 'hidden',
                                                textOverflow: 'cllipsis',
                                                whiteSpace: 'nowrap',
                                            }}
                                            >
                                            {incident.description}
                                        </div>
                                    </td>
                                    <td>
                                        <span style={{fontSize: '0.85rem'}}> {incident.scenarioId}</span>
                                    </td>
                                    <td>
                                        <span className={`badge badge-${incident.severity.toLowerCase()}`}>
                                            {incident.severity}
                                        </span>
                                    </td>
                                    <td>
                                        <span className={`badge badge-${incident.status.toLowerCase()}`}>
                                            {incident.status}
                                        </span>
                                    </td>
                                    <td style={{fontSize: '0.8rem'}}>
                                        {new Date(incident.startedAt).toLocaleString()}
                                    </td>
                                    <td>
                                        {incident.status === 'RESOLVED' ? (
                                            <button
                                                onClick={() => onViewReport(incident)}
                                                className="btn-secondary"
                                                style={{padding: '6px 12px', fontSize: '0.8rem'}}
                                                >
                                                View Report
                                            </button>
                                        ) : (
                                            <button
                                                onClick={() => onStartLiveTrace(incident)}
                                                className="btn-primary"
                                                style={{padding: '6px 12px', fontSize: '0.8rem'}}
                                            >
                                                Resume Investigation
                                            </button>
                                        )}
                                    </td>
                                </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>
        </div>
    );
}