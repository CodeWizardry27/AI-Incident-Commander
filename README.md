# ⚡ AI Incident Commander

> A Multi-Agent AI System for Autonomous Production Outage Resolution

## 🧠 What Is This?

AI Incident Commander uses **4 specialized AI agents** that collaborate autonomously to diagnose and resolve production server incidents — powered by Google Gemini and built from scratch in Java Spring Boot.

```
User: "Payment API returning 500 errors"
  → 🔍 Detective Agent   → reads logs, checks metrics
  → 🔬 Analyst Agent     → finds root cause in git commits
  → 🔧 Fixer Agent       → generates SQL fix + config changes
  → 👑 Commander Agent   → compiles action plan + incident report
```

## 🏗️ Architecture

- **Custom Agent Framework** — ReAct loop (Reasoning + Acting) built from scratch in Java
- **4 Agents × 12+ Tools** — each agent has specialized tools (read_logs, check_metrics, read_git_commits, etc.)
- **Real-time Streaming** — watch agents think and act live via Server-Sent Events
- **5 Demo Scenarios** — DB pool exhaustion, memory leak, API rate limiting, disk full, DNS failure

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 17 + Spring Boot 4.0 |
| AI Engine | Google Gemini API (free tier) |
| Database | MySQL 8 |
| Frontend | React 18 + Vite (coming soon) |
| Real-time | SSE (Server-Sent Events) |
| Auth | JWT + Spring Security |

## 📦 Project Structure

```
src/main/java/com/incidentcommander/
├── config/          Security, CORS, Gemini configs
├── entity/          User, Incident, AgentLog, IncidentReport
├── repository/      JPA repositories
├── service/         GeminiService (AI brain)
├── agent/           (coming) Agent framework + 4 agents
│   ├── core/        AgentBase, Tool, ReActEngine, Orchestrator
│   ├── agents/      Detective, Analyst, Fixer, Commander
│   └── tools/       12+ tool implementations
└── controller/      REST APIs + SSE endpoints
```

## 🚀 Setup

1. Clone the repo
2. Create MySQL database: `CREATE DATABASE incident_commander;`
3. Add your Gemini API key in `application.properties`
4. Run: `mvn spring-boot:run`

## 📄 License

MIT
