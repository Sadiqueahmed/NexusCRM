# 🚀 NexusCRM — Agentic AI CRM with Autonomous RAG Pipelines

An enterprise-grade, AI-powered Customer Relationship Management system where an **autonomous AI agent** intercepts customer queries, retrieves relevant company policies via **RAG (Retrieval-Augmented Generation)**, and **autonomously updates CRM records** through agentic tool calling — all visible in **real-time** on a React dashboard.

---

## 🏗 Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                    Docker Compose Ecosystem                      │
│                                                                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────┐  │
│  │   React SPA  │  │ Spring Boot  │  │  Python AI Service   │  │
│  │  (Nginx:80)  │──│  (API:8080)  │──│  (FastAPI:8000)      │  │
│  │              │  │              │  │                      │  │
│  │ • Dashboard  │  │ • REST API   │  │ • LangChain Agent    │  │
│  │ • Leads      │  │ • JPA/ORM    │  │ • RAG Pipeline       │  │
│  │ • Tickets    │  │ • SSE Events │  │ • Agentic Tools      │  │
│  │ • AI Console │  │ • Swagger    │  │ • pgvector Search    │  │
│  │ • Activity   │  │              │  │                      │  │
│  └──────┬───────┘  └──────┬───────┘  └──────────┬───────────┘  │
│         │     SSE          │    JPA/SQL           │  HTTP+RAG   │
│         └─────────┐  ┌────┘                ┌─────┘             │
│                   ▼  ▼                     ▼                    │
│              ┌──────────────────────────────────┐              │
│              │   PostgreSQL 16 + pgvector        │              │
│              │   (Port 5432)                     │              │
│              │   • users, leads, tickets tables  │              │
│              │   • company_policies + embeddings │              │
│              └──────────────────────────────────┘              │
│                          │                                      │
│              ┌───────────┘                                      │
│              ▼                                                  │
│         ┌──────────┐                                            │
│         │ pgAdmin4 │                                            │
│         │ (:5050)  │                                            │
│         └──────────┘                                            │
└─────────────────────────────────────────────────────────────────┘
```

---

## ✨ Key Features

### 🤖 Autonomous AI Agent
- **LangChain-powered** agent with 7 strictly-defined tools
- Autonomously searches policies, updates leads, resolves tickets
- Chain-of-thought reasoning with full action audit trail

### 📚 RAG Pipeline (Retrieval-Augmented Generation)
- **pgvector** for vector similarity search
- OpenAI embeddings (`text-embedding-3-small`, 1536 dimensions)
- 8 pre-seeded company policies covering refunds, warranties, shipping, pricing, SLAs, privacy, and support

### 📡 Real-Time SSE Event Stream
- **Server-Sent Events** broadcast every CRM mutation
- React dashboard updates live when AI agent modifies records
- Events tagged as AI-generated vs human-initiated

### 🎯 Full CRM Functionality
- **Leads Pipeline**: NEW → CONTACTED → QUALIFIED → CONVERTED/LOST
- **Ticket Management**: OPEN → IN_PROGRESS → RESOLVED → CLOSED
- **Dashboard KPIs**: Real-time metrics, charts (Recharts), activity feed

---

## 🛠 Tech Stack

| Layer | Technology |
|-------|-----------|
| **Frontend** | React 18 (Vite), Vanilla CSS, Recharts, Lucide Icons |
| **Core API** | Java 17, Spring Boot 3.3, Spring Data JPA, SpringDoc OpenAPI |
| **AI Service** | Python 3.12, FastAPI, LangChain, Claude/OpenAI |
| **Database** | PostgreSQL 16 + pgvector extension |
| **Real-time** | Server-Sent Events (SSE) via Spring `SseEmitter` |
| **Embeddings** | OpenAI `text-embedding-3-small` |
| **Infrastructure** | Docker Compose, Nginx |

---

## 🚀 Quick Start

### Prerequisites
- Docker & Docker Compose
- An API key for **Anthropic (Claude)** and/or **OpenAI**

### 1. Clone & Configure

```bash
git clone https://github.com/YOUR_USERNAME/NexusCRM.git
cd NexusCRM

# Copy environment template and add your API keys
cp .env.example .env
# Edit .env and set ANTHROPIC_API_KEY and/or OPENAI_API_KEY
```

### 2. Start Everything

```bash
docker compose up --build
```

### 3. Access the Services

| Service | URL |
|---------|-----|
| **React Dashboard** | [http://localhost:3000](http://localhost:3000) |
| **Spring Boot API** | [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) |
| **AI Agent API** | [http://localhost:8000/docs](http://localhost:8000/docs) |
| **pgAdmin** | [http://localhost:5050](http://localhost:5050) (admin@nexuscrm.io / admin) |

### 4. Seed AI Embeddings (First Time Only)

After the services are running, generate embeddings for the seed company policies:

```bash
docker exec nexuscrm-ai-service python -m seed_policies
```

---

## 🔄 How Agentic Tool Calling Works

```
1. User sends query via React AI Console
       ↓
2. FastAPI forwards to LangChain Agent
       ↓
3. Agent searches company policies via RAG (pgvector)
       ↓
4. Agent reasons about the query + policies
       ↓
5. Agent calls tools: update_lead_status(), resolve_ticket(), etc.
       ↓
6. Tools make HTTP calls to Spring Boot API
       ↓
7. Spring Boot updates PostgreSQL + publishes SSE event
       ↓
8. React dashboard receives SSE event → UI updates in real-time
```

### Available Agent Tools

| Tool | Description | CRM API Endpoint |
|------|-------------|------------------|
| `search_company_policies` | RAG search against policy embeddings | pgvector (local) |
| `get_lead_details` | Retrieve a lead's full details | `GET /api/v1/leads/{id}` |
| `update_lead_status` | Change a lead's pipeline status | `PATCH /api/v1/leads/{id}/status` |
| `get_ticket_details` | Retrieve a ticket's details | `GET /api/v1/tickets/{id}` |
| `resolve_ticket` | Resolve a support ticket | `PATCH /api/v1/tickets/{id}/resolve` |
| `create_ticket` | Create a new ticket | `POST /api/v1/tickets` |
| `list_open_tickets` | List all open tickets | `GET /api/v1/tickets?status=OPEN` |

---

## 📁 Project Structure

```
NexusCRM/
├── backend-java/              # Spring Boot Core API
│   ├── src/main/java/com/nexuscrm/backend/
│   │   ├── config/            # CORS, WebMvc configuration
│   │   ├── controller/        # REST controllers (5 controllers)
│   │   ├── dto/               # Request/Response DTOs (11 DTOs)
│   │   ├── entity/            # JPA entities (User, Lead, Ticket)
│   │   ├── event/             # SSE event publisher
│   │   ├── exception/         # Global exception handler
│   │   ├── repository/        # Spring Data JPA repositories
│   │   └── service/           # Business logic services
│   ├── Dockerfile
│   └── pom.xml
│
├── ai-service-python/         # Python AI Microservice
│   ├── app/
│   │   ├── agent/             # LangChain agent + tools
│   │   ├── api/               # FastAPI routes + schemas
│   │   ├── rag/               # Embeddings, vector store, ingestion
│   │   └── services/          # CRM HTTP client
│   ├── seed_policies.py       # Embedding seed script
│   ├── Dockerfile
│   └── requirements.txt
│
├── frontend-react/            # React Dashboard
│   ├── src/
│   │   ├── components/        # Dashboard, Leads, Tickets, AI Console, Activity
│   │   ├── api.js             # API client + SSE connection
│   │   └── App.jsx            # Main app with sidebar navigation
│   ├── Dockerfile
│   └── nginx.conf
│
├── db/init/                   # Database initialization
│   ├── 01-schema.sql          # Tables + indexes + triggers
│   └── 02-seed-data.sql       # Demo data (users, leads, tickets, policies)
│
├── docker-compose.yml         # Full ecosystem orchestration
├── .env.example               # Environment variables template
└── README.md                  # This file
```

---

## 🔌 API Documentation

### Spring Boot API (Port 8080)

Full interactive docs available at `/swagger-ui.html` when running.

| Resource | Endpoints |
|----------|-----------|
| Users | `GET/POST /api/v1/users`, `GET/PUT/DELETE /api/v1/users/{id}` |
| Leads | `GET/POST /api/v1/leads`, `GET/PUT/DELETE /api/v1/leads/{id}`, `PATCH /api/v1/leads/{id}/status` |
| Tickets | `GET/POST /api/v1/tickets`, `GET/PUT/DELETE /api/v1/tickets/{id}`, `PATCH /api/v1/tickets/{id}/resolve` |
| Dashboard | `GET /api/v1/dashboard/stats` |
| SSE Events | `GET /api/v1/events/stream` |

### AI Service API (Port 8000)

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/agent/query` | POST | Send query to autonomous AI agent |
| `/api/rag/ingest` | POST | Ingest new company policy |
| `/api/rag/search` | POST | Standalone RAG search |
| `/api/rag/backfill` | POST | Backfill missing embeddings |
| `/api/health` | GET | Health check |

---

## 🧪 Example AI Agent Queries

Try these in the AI Agent Console:

```
"A customer with email priya.p@healthplus.org was double charged $299. 
 What should we do according to our refund policy?"

"List all open tickets and resolve ticket #4 about the refund request 
 based on our company policy."

"What is our warranty policy for hardware products? Customer carlos.r@mediapulse.mx 
 needs a replacement for a defective unit."

"Update lead #3 (David Kim from InnovateAI) to CONVERTED status because 
 they signed an annual enterprise contract."
```

---

## 📄 License

This project is for portfolio/demonstration purposes.