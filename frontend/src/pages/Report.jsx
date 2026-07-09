import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getIncident, getReport, getAgentLogs } from '../api';

export default function Report() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [incident, setIncident] = useState(null);
  const [report, setReport] = useState(null);
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      getIncident(id),
      getReport(id),
      getAgentLogs(id),
    ]).then(([inc, rep, agentLogs]) => {
      setIncident(inc);
      setReport(rep);
      setLogs(agentLogs);
      setLoading(false);
    }).catch(() => setLoading(false));
  }, [id]);

  if (loading) {
    return (
      <div className="loading">
        <div className="spinner"></div>
        <div>Loading report...</div>
      </div>
    );
  }

  if (!report) {
    return (
      <div className="loading">
        <div>📋 Report not yet available. Investigation may still be in progress.</div>
        <button className="investigate-btn" onClick={() => navigate(`/investigate/${id}`)}>
          🔍 View Live Investigation
        </button>
      </div>
    );
  }

  const duration = incident?.startedAt && incident?.resolvedAt
    ? Math.round((new Date(incident.resolvedAt) - new Date(incident.startedAt)) / 1000)
    : null;

  return (
    <div>
      {/* Header */}
      <div className="report-header">
        <span className="resolved-badge">✅ INCIDENT RESOLVED</span>
        <h1 style={{ fontSize: '1.3rem', fontWeight: 700 }}>
          {incident?.title || 'Incident Report'}
        </h1>
      </div>

      {/* Meta Info */}
      <div className="stats-grid" style={{ marginBottom: 24 }}>
        <div className="stat-card">
          <div className="stat-value">#{id}</div>
          <div className="stat-label">Incident ID</div>
        </div>
        <div className="stat-card">
          <div className="stat-value" style={{ fontSize: '1.3rem' }}>
            {incident?.severity || 'N/A'}
          </div>
          <div className="stat-label">Severity</div>
        </div>
        <div className="stat-card">
          <div className="stat-value">
            {duration ? `${Math.floor(duration / 60)}m ${duration % 60}s` : 'N/A'}
          </div>
          <div className="stat-label">Resolution Time</div>
        </div>
        <div className="stat-card">
          <div className="stat-value">{logs.length}</div>
          <div className="stat-label">Agent Steps</div>
        </div>
      </div>

      {/* Executive Summary */}
      {report.executiveSummary && (
        <div className="report-section action-plan">
          <h3>📊 Executive Summary</h3>
          <div className="report-content">{report.executiveSummary}</div>
        </div>
      )}

      {/* Root Cause */}
      {report.rootCause && (
        <div className="report-section root-cause">
          <h3>🔴 Root Cause</h3>
          <div className="report-content">{report.rootCause}</div>
        </div>
      )}

      {/* Immediate Fix */}
      {report.immediateFix && (
        <div className="report-section immediate-fix">
          <h3>🟠 Immediate Fix</h3>
          <div className="report-content">{report.immediateFix}</div>
        </div>
      )}

      {/* Permanent Fix */}
      {report.permanentFix && report.permanentFix !== report.immediateFix && (
        <div className="report-section permanent-fix">
          <h3>🟢 Permanent Fix</h3>
          <div className="report-content">{report.permanentFix}</div>
        </div>
      )}

      {/* Generated SQL */}
      {report.generatedSql && (
        <div className="report-section" style={{ borderLeft: '4px solid var(--accent-purple)' }}>
          <h3>🗃️ Generated SQL</h3>
          <div className="tool-output" style={{ maxHeight: 'none' }}>{report.generatedSql}</div>
        </div>
      )}

      {/* Config Changes */}
      {report.configChanges && (
        <div className="report-section" style={{ borderLeft: '4px solid var(--accent-cyan)' }}>
          <h3>⚙️ Configuration Changes</h3>
          <div className="tool-output" style={{ maxHeight: 'none' }}>{report.configChanges}</div>
        </div>
      )}

      {/* Action Buttons */}
      <div style={{ display: 'flex', gap: 12, marginTop: 24 }}>
        <button className="investigate-btn" style={{ background: 'linear-gradient(135deg, var(--accent-blue), var(--accent-purple))' }} onClick={() => navigate('/')}>
          ← Back to Dashboard
        </button>
        <button className="investigate-btn" style={{ background: 'linear-gradient(135deg, var(--accent-cyan), var(--accent-blue))' }} onClick={() => navigate(`/investigate/${id}`)}>
          🔍 View Investigation Log
        </button>
      </div>
    </div>
  );
}
