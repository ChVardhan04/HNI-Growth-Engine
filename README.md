# HNI AI Growth Engine

An **AI-powered Sales Intelligence Platform** for High-Net-Worth-Individual (HNI)
acquisition — not a CRUD app. It scores prospects, generates personalized
outreach, runs an approval workflow, tracks engagement, computes buying intent,
recommends next actions, routes advisors, and automates CRM/audit workflows.

Backend: **Java 21 · Spring Boot 3 · Spring Security (JWT) · Spring Data JPA · MySQL · Lombok · Maven**
Frontend: **React · Vite · Bootstrap 5 · Axios · React Router · Recharts**

> ⚠️ This project was authored in an offline sandbox, so `mvn` and `npm install`
> could not be run there to verify the build. The code is written to compile and
> run; follow the steps below in your own environment.

---

## Architecture

```
React (Vite)  ──HTTP/JWT──▶  Controllers ──▶ Services ──▶ AI Layer ──▶ Repositories ──▶ MySQL
                                                 │
                                                 └── AIService facade
                                                       ├── IcpScoringEngine
                                                       ├── MessageGenerator
                                                       ├── RecommendationEngine
                                                       └── LLMProvider
```

Layered (Controller → Service → Repository), DTOs never expose entities,
centralized exception handling, builder pattern on entities, and — critically —
**all AI logic sits behind interfaces** so it is swappable without touching
business code.

---

## Prerequisites

- JDK 21
- Maven 3.9+
- Node.js 18+ and npm
- MySQL 8 running locally

---

## 1. Database

Create the schema (or let the app auto-create it via `createDatabaseIfNotExist`):

```sql
CREATE DATABASE IF NOT EXISTS hni_growth_engine;
```

Default credentials expected by `application.yml` are `root` / `root`.
Override with environment variables if yours differ:

```bash
export DB_USERNAME=root
export DB_PASSWORD=yourpassword
```

---

## 2. Backend

```bash
cd backend
mvn spring-boot:run
```

The API starts on **http://localhost:8080**.

On first run a **DataSeeder** creates a default admin, two RMs (for advisor
routing), a manager, a compliance user, and four AI-scored sample leads.

**Default admin login:** `admin@hnigrowth.com` / `Admin@123`
(Seeded RMs use password `Rm@12345`.)

---

## 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

The UI starts on **http://localhost:5173** (CORS is pre-configured for it).

---

## Core workflow (end-to-end, implemented)

1. **Create a lead** → the AI **ICP Scoring Engine** assigns score (0–100),
   tier (A+/A/B/C/D), a human-readable **reason**, and **confidence**.
2. The **Recommendation Engine** sets the next best action + reasoning.
3. The **Advisor Routing** service assigns an RM (region / language /
   specialization / load).
4. **Generate a message** (Email/LinkedIn/WhatsApp/SMS) — editable.
5. **Approval workflow**: edit → approve/reject (with comments) → send.
6. **Track engagement** activities → the **Intent Engine** updates the score
   and band (HOT/HIGH/MEDIUM/LOW/COLD) and re-runs the recommendation.
7. Everything is written to **Audit Logs** and surfaced on the **Dashboard**.

---

## Swapping the AI provider (the whole point of the design)

Today the active provider is `rule-based` (`app.ai.provider` in
`application.yml`). Business services depend **only** on the `AIService` facade
and the engine interfaces — never on concrete AI classes.

To integrate OpenAI / Gemini / Claude / Azure OpenAI:

1. Implement `com.hnigrowth.ai.llm.LLMProvider` (e.g. `OpenAiLLMProvider`) and
   annotate it `@ConditionalOnProperty(name="app.ai.provider", havingValue="openai")`.
2. Optionally implement LLM-backed versions of `IcpScoringEngine`,
   `MessageGenerator`, and `RecommendationEngine` (reuse `PromptBuilder` for
   prompt assembly).
3. Set `app.ai.provider: openai` (and provide the API key via env/config).

No controller or service code changes. `GET /api/ai/provider` reports the
active provider.

---

## Module coverage

| # | Module | Status |
|---|--------|--------|
| 1 | Lead Management | ✅ Full CRUD + search + pagination |
| 2 | AI ICP Scoring | ✅ Weighted, explainable |
| 3 | Message Generation | ✅ 4 channels, editable |
| 4 | Approval Workflow | ✅ Edit/approve/reject/comments/audit |
| 5 | Outreach Automation | ✅ Send after approval (provider dispatch = TODO stub) |
| 6 | Campaign Management | ✅ CRUD + AI recommendation stub |
| 7 | Engagement Tracking | ✅ Every activity stored |
| 8 | Intent Engine | ✅ Weighted, auto-banding |
| 9 | Recommendation Engine | ✅ With reasoning |
| 10 | CRM Automation | ◑ Via lead assignment/status + audit |
| 11 | Advisor Routing | ✅ Region/language/specialization/load |
| 12 | Dashboard | ✅ Rich stats + 5 chart types |
| 13 | Audit Logs | ✅ Central AuditService |
| 14 | Scheduler | ✅ Spring @Scheduled jobs |
| 15 | AI Analytics | ◑ Predictive fields on dashboard |
| 16 | AI Explanation Engine | ✅ reason/confidence on every AI decision |
| 17 | Future LLM Integration | ✅ Interfaces in place, provider-switchable |

✅ implemented · ◑ scaffolded in the same pattern, ready to extend

---

## Key API endpoints

```
POST   /api/auth/register            POST /api/auth/login
GET    /api/leads?search=&page=&size=
POST   /api/leads                    GET/PUT/DELETE /api/leads/{id}
POST   /api/leads/{id}/rescore
POST   /api/engagement/{leadId}/track?type=MEETING
GET    /api/engagement/{leadId}/timeline
POST   /api/messages/generate        PUT /api/messages/{id}
POST   /api/messages/{id}/approve|reject|send
GET    /api/messages/pending
GET    /api/campaigns                POST /api/campaigns
GET    /api/dashboard
GET    /api/audit
GET    /api/ai/provider
```

All non-auth endpoints require `Authorization: Bearer <token>`.
