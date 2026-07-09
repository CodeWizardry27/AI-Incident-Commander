import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getScenarios, getIncidents, createIncident } from '../api';

const SCENARIO_ICONS = {
  db_pool_exhaustion: '💾',
  memory_leak: '🧠',
  api_rate_limit: '⚡',
};

export default function Dashboard() {
  const [scenarios, setScenarios] = useState([]);
  const [incidents, setIncidents] = useState([]);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    getScenarios().then(setScenarios).catch(() => {});
    getIncidents().then(setIncidents).catch(() => {});
  }, []);

  const handleInvestigate = async (scenario) => {
    setLoading(true);
    try {
      const result = await createIncident(scenario.id, scenario.description);
      navigate(`/investigate/${result.incidentId}`);
    } catch (err) {
      alert('Failed to start investigation. Is the backend running?');
      setLoading(false);
    }
  };

  const resolved = incidents.filter(i => i.status === 'RESOLVED').length;
  const total = incidents.length;

  return (
    <div>
      {/* Stats */}
      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-value">{total}</div>
          <div className="stat-label">Total Incidents</div>
        </div>
        <div className="stat-card">
          <div className="stat-value">{resolved}</div>
          <div className="stat-label">Resolved</div>
        </div>
        <div className="stat-card">
          <div className="stat-value">4</div>
          <div className="stat-label">AI Agents</div>
        </div>
        <div className="stat-card">
          <div className="stat-value">{total > 0 ? Math.round((resolved / total) * 100) : 0}%</div>
          <div className="stat-label">Success Rate</div>
        </div>
      </div>

      {/* Scenarios */}
      <h2 className="section-title">🚨 Quick Start Scenarios</h2>
      <div className="scenarios-grid">
        {scenarios.map(scenario => (
          <div
            key={scenario.id}
            className="scenario-card"
            onClick={() => !loading && handleInvestigate(scenario)}
          >
            <div className="scenario-icon">{SCENARIO_ICONS[scenario.id] || '🔧'}</div>
            <div className="scenario-title">{scenario.title}</div>
            <div className="scenario-desc">{scenario.description}</div>
            <span className={`severity-badge severity-${scenario.severity}`}>
              {scenario.severity}
            </span>
          </div>
        ))}
      </div>

      {loading && (
        <div className="loading">
          <div className="spinner"></div>
          <div>Starting investigation...</div>
        </div>
      )}

      {/* Recent Incidents */}
      {incidents.length > 0 && (
        <>
          <h2 className="section-title" style={{ marginTop: 32 }}>📋 Recent Incidents</h2>
          <div className="card">
            <table className="incidents-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Title</th>
                  <th>Severity</th>
                  <th>Status</th>
                  <th>Started</th>
                </tr>
              </thead>
              <tbody>
                {incidents.slice(0, 10).map(incident => (
                  <tr
                    key={incident.id}
                    style={{ cursor: 'pointer' }}
                    onClick={() => {
                      if (incident.status === 'RESOLVED') navigate(`/report/${incident.id}`);
                      else if (incident.status === 'INVESTIGATING') navigate(`/investigate/${incident.id}`);
                    }}
                  >
                    <td>#{incident.id}</td>
                    <td>{incident.title}</td>
                    <td><span className={`severity-badge severity-${incident.severity}`}>{incident.severity}</span></td>
                    <td><span className={`status-badge status-${incident.status}`}>{incident.status}</span></td>
                    <td style={{ color: 'var(--text-muted)', fontSize: '0.82rem' }}>
                      {new Date(incident.startedAt).toLocaleString()}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}
    </div>
  );
}
