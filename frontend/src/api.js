const API_BASE = 'http://localhost:8080/api';

export async function getScenarios() {
  const res = await fetch(`${API_BASE}/scenarios`);
  return res.json();
}

export async function createIncident(scenarioId, description) {
  const res = await fetch(`${API_BASE}/incidents`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ scenarioId, description }),
  });
  return res.json();
}

export async function getIncidents() {
  const res = await fetch(`${API_BASE}/incidents`);
  return res.json();
}

export async function getIncident(id) {
  const res = await fetch(`${API_BASE}/incidents/${id}`);
  return res.json();
}

export async function getAgentLogs(id) {
  const res = await fetch(`${API_BASE}/incidents/${id}/logs`);
  return res.json();
}

export async function getReport(id) {
  const res = await fetch(`${API_BASE}/incidents/${id}/report`);
  if (!res.ok) return null;
  return res.json();
}

export function subscribeToIncident(id, onEvent) {
  const eventSource = new EventSource(`${API_BASE}/incidents/${id}/stream`);
  
  const eventTypes = ['status', 'agent_start', 'agent_step', 'agent_complete', 'investigation_complete', 'investigation_failed'];
  
  eventTypes.forEach(type => {
    eventSource.addEventListener(type, (event) => {
      const data = JSON.parse(event.data);
      onEvent(type, data);
    });
  });

  eventSource.onerror = () => {
    console.log('SSE connection closed');
    eventSource.close();
  };

  return eventSource;
}
