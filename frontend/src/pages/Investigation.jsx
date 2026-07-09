import { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getIncident, subscribeToIncident, getAgentLogs } from '../api';

const AGENT_META = {
  detective: { icon: '🔍', label: 'Detective', color: 'detective' },
  analyst:   { icon: '🔬', label: 'Analyst',   color: 'analyst' },
  fixer:     { icon: '🔧', label: 'Fixer',     color: 'fixer' },
  commander: { icon: '👑', label: 'Commander',  color: 'commander' },
};

const AGENTS_ORDER = ['detective', 'analyst', 'fixer', 'commander'];

export default function Investigation() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [incident, setIncident] = useState(null);
  const [logs, setLogs] = useState([]);
  const [agentStates, setAgentStates] = useState({
    detective: 'waiting', analyst: 'waiting', fixer: 'waiting', commander: 'waiting',
  });
  const [completed, setCompleted] = useState(false);
  const [elapsed, setElapsed] = useState(0);
  const logEndRef = useRef(null);

  // Fetch incident details
  useEffect(() => {
    getIncident(id).then(setIncident).catch(() => {});
  }, [id]);

  // Timer
  useEffect(() => {
    if (completed) return;
    const timer = setInterval(() => setElapsed(e => e + 1), 1000);
    return () => clearInterval(timer);
  }, [completed]);

  // SSE subscription for live streaming
  useEffect(() => {
    const eventSource = subscribeToIncident(id, (type, data) => {
      if (type === 'agent_start') {
        setAgentStates(prev => ({ ...prev, [data.agent]: 'active' }));
        setLogs(prev => [...prev, {
          agent: data.agent,
          type: 'START',
          content: `${AGENT_META[data.agent]?.label || data.agent} agent starting...`,
        }]);
      }

      if (type === 'agent_step') {
        setLogs(prev => [...prev, {
          agent: data.agent,
          type: data.stepType,
          content: data.content,
          toolName: data.toolName,
          toolOutput: data.toolOutput,
        }]);
      }

      if (type === 'agent_complete') {
        setAgentStates(prev => ({ ...prev, [data.agent]: 'completed' }));
        setLogs(prev => [...prev, {
          agent: data.agent,
          type: 'DONE',
          content: `✅ ${AGENT_META[data.agent]?.label} completed (${data.iterations} iterations)`,
        }]);
      }

      if (type === 'investigation_complete') {
        setCompleted(true);
        setLogs(prev => [...prev, {
          agent: 'commander',
          type: 'CONCLUSION',
          content: '🎉 Investigation complete! All agents finished successfully.',
        }]);
      }

      if (type === 'investigation_failed') {
        setCompleted(true);
        setLogs(prev => [...prev, {
          agent: data.agent,
          type: 'ERROR',
          content: `❌ Investigation failed at ${data.agent}: ${data.reason}`,
        }]);
      }
    });

    // Also load any existing logs (if page refreshed mid-investigation)
    getAgentLogs(id).then(existingLogs => {
      if (existingLogs.length > 0) {
        const formatted = existingLogs.map(l => ({
          agent: l.agentName,
          type: l.stepType,
          content: l.content,
          toolName: l.toolName,
          toolOutput: l.toolOutput,
        }));
        setLogs(prev => prev.length === 0 ? formatted : prev);
      }
    }).catch(() => {});

    return () => eventSource.close();
  }, [id]);

  // Auto-scroll
  useEffect(() => {
    logEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [logs]);

  const formatTime = (s) => {
    const m = Math.floor(s / 60);
    const sec = s % 60;
    return `${m.toString().padStart(2, '0')}:${sec.toString().padStart(2, '0')}`;
  };

  return (
    <div>
      {/* Header */}
      <div className="incident-header">
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 6 }}>
            {!completed && <span className="live-badge">LIVE</span>}
            {completed && <span className="status-badge status-RESOLVED">RESOLVED</span>}
            <span style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>
              Incident #{id}
            </span>
          </div>
          <div className="incident-title">
            {incident?.description || 'Loading...'}
          </div>
        </div>
        <div className="incident-meta">
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '1.5rem', fontWeight: 800, fontFamily: 'JetBrains Mono' }}>
              {formatTime(elapsed)}
            </div>
            <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>elapsed</div>
          </div>
          {completed && (
            <button 
              className="investigate-btn" 
              style={{ background: 'linear-gradient(135deg, var(--accent-green), #059669)' }}
              onClick={() => navigate(`/report/${id}`)}
            >
              📋 View Report
            </button>
          )}
        </div>
      </div>

      {/* Pipeline */}
      <div className="pipeline">
        {AGENTS_ORDER.map((agent, i) => (
          <div key={agent} style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <div className={`pipeline-agent ${agentStates[agent]}`}>
              <span className="agent-icon">{AGENT_META[agent].icon}</span>
              <span className="agent-name">{AGENT_META[agent].label}</span>
              <span className="agent-status">
                {agentStates[agent] === 'completed' && '✅ Done'}
                {agentStates[agent] === 'active' && '🔄 Working...'}
                {agentStates[agent] === 'waiting' && '⏳ Waiting'}
              </span>
            </div>
            {i < AGENTS_ORDER.length - 1 && <span className="pipeline-arrow">→</span>}
          </div>
        ))}
      </div>

      {/* Activity Log */}
      <h2 className="section-title">📡 Agent Activity Log</h2>
      <div className="activity-log">
        {logs.length === 0 && (
          <div style={{ color: 'var(--text-muted)', padding: 20, textAlign: 'center' }}>
            Waiting for agents to start...
          </div>
        )}
        {logs.map((log, i) => (
          <div key={i} className={`log-entry ${log.agent}`}>
            <span className={`log-agent ${log.agent}`}>
              {AGENT_META[log.agent]?.icon} {AGENT_META[log.agent]?.label || log.agent}
            </span>
            <span className={`log-type ${log.type}`}>{log.type}</span>
            <span className="log-content">{log.content}</span>
            {log.toolName && (
              <span style={{ color: 'var(--accent-orange)', marginLeft: 8 }}>
                → {log.toolName}
              </span>
            )}
            {log.toolOutput && (
              <div className="tool-output">{log.toolOutput}</div>
            )}
          </div>
        ))}
        <div ref={logEndRef} />
      </div>
    </div>
  );
}
