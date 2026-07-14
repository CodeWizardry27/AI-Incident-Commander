export default function ScenarioTriggerModal({
    customDesc,
    customSeverity,
    customTitle,
    isTriggering,
    selectedScenario,
    onClose,
    onCustomDescChange,
    onCustomSeverityChange,
    onCustomTitleChange,
    onTrigger,
                                             })
{
    if(!selectedScenario) {
        return null;
    }

    return (
        <div
            style={{
                position: 'fixed',
                top: 0,
                left: 0,
                right: 0,
                bottom: 0,
                background: 'rgba(0, 0, 0, 0.6)',
                display: 'flex',
                justifyContent:'center',
                alignItems: 'center',
                zIndex: 100,
                padding: '20px',
            }}
            >
            <div className="glass-panel"
                 style={{width: '100%', maxWidth: '560px', animation: 'fadeIn 0.25s ease'}}
                 >
                <h3 style={{
                    fontSize: '1.25rem',
                    marginBottom: '16px',
                    borderBottom: '1px solid rgba(255, 255, 255, 0.08)',
                    paddingBottom: '10px',
                }}
                    >
                    Trigger Incident: {selectedScenario.title}
                </h3>

                <div className="form-group">
                    <label>Incident Ticket Title</label>
                    <input
                    type="text"
                    className="form-input"
                    value={customTitle}
                    onChange={(event) => onCustomTitleChange(event.target.value)}
                    />
                </div>

                <div className="form-group">
                    <label>Outage Symptoms & Details</label>
                    <textarea
                        className="form-input"
                        rows="4"
                        value={customDesc}
                        onChange={(event) => onCustomDescChange(event.target.value)}
                        style={{resize: 'vertical', fontFamily: 'inherit'}}
                        />
                </div>

                <div className="form-group">
                    <label>Initial Severity Assessment</label>
                    <select
                        className="form-input"
                        value={customSeverity}
                        onChange={(event) => onCustomTitleChange(event.target.value)}
                    >
                        <option value="LOW">LOW (Non-Blocking)</option>
                        <option value="MEDIUM">MEDIUM (Degraded Performance)</option>
                        <option value="HIGH">HIGH (Partial Outage)</option>
                        <option value="CRITICAL"> CRITICAL (Total System Down)</option>
                    </select>

                    <div style={{
                        display: 'flex',
                        gap: '12px',
                        marginTop: '24px'
                    }}>
                        <button onClick={onTrigger} disabled={isTriggering} className="btn-primary" style={{ flaxGrow: 1 }}>
                            {isTriggering ? 'Initializing Agents...' : 'Deploy Incident Commanders'}
                        </button>
                        <button onClick={onClose} className="btn-secondary">
                            Cancel
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );

}