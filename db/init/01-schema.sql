-- =============================================================================
-- NexusCRM Database Initialization Script
-- =============================================================================
-- This script runs automatically on the first `docker compose up` via the
-- docker-entrypoint-initdb.d volume mount. It creates the pgvector extension
-- and all core tables for the CRM system.
-- =============================================================================

-- Enable the pgvector extension for vector similarity search
CREATE EXTENSION IF NOT EXISTS vector;

-- =============================================================================
-- USERS TABLE
-- Represents CRM operators, agents, and administrators
-- =============================================================================
CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(100) UNIQUE NOT NULL,
    email         VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(50) DEFAULT 'AGENT'
                  CHECK (role IN ('ADMIN', 'AGENT', 'VIEWER')),
    created_at    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Index for fast lookups by email and role
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);

-- =============================================================================
-- LEADS TABLE
-- Tracks prospective customers through the sales pipeline
-- =============================================================================
CREATE TABLE leads (
    id              BIGSERIAL PRIMARY KEY,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    email           VARCHAR(255),
    phone           VARCHAR(50),
    company         VARCHAR(200),
    status          VARCHAR(50) DEFAULT 'NEW'
                    CHECK (status IN ('NEW', 'CONTACTED', 'QUALIFIED', 'LOST', 'CONVERTED')),
    source          VARCHAR(100)
                    CHECK (source IN ('WEBSITE', 'REFERRAL', 'AI_GENERATED', 'COLD_CALL', 'SOCIAL_MEDIA', 'EMAIL_CAMPAIGN')),
    assigned_to     BIGINT REFERENCES users(id) ON DELETE SET NULL,
    notes           TEXT,
    ai_summary      TEXT,          -- AI-generated lead intelligence summary
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for common query patterns
CREATE INDEX idx_leads_status ON leads(status);
CREATE INDEX idx_leads_assigned_to ON leads(assigned_to);
CREATE INDEX idx_leads_email ON leads(email);

-- =============================================================================
-- TICKETS TABLE
-- Customer support tickets with AI resolution tracking
-- =============================================================================
CREATE TABLE tickets (
    id              BIGSERIAL PRIMARY KEY,
    subject         VARCHAR(500) NOT NULL,
    description     TEXT NOT NULL,
    status          VARCHAR(50) DEFAULT 'OPEN'
                    CHECK (status IN ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED')),
    priority        VARCHAR(20) DEFAULT 'MEDIUM'
                    CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    category        VARCHAR(100),
    customer_email  VARCHAR(255),
    assigned_to     BIGINT REFERENCES users(id) ON DELETE SET NULL,
    resolution      TEXT,          -- Human or AI resolution notes
    ai_handled      BOOLEAN DEFAULT FALSE, -- TRUE if resolved by the AI agent
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for common query patterns
CREATE INDEX idx_tickets_status ON tickets(status);
CREATE INDEX idx_tickets_priority ON tickets(priority);
CREATE INDEX idx_tickets_assigned_to ON tickets(assigned_to);
CREATE INDEX idx_tickets_customer_email ON tickets(customer_email);

-- =============================================================================
-- COMPANY POLICIES TABLE
-- RAG knowledge base with pgvector embeddings for semantic search
-- =============================================================================
CREATE TABLE company_policies (
    id              BIGSERIAL PRIMARY KEY,
    title           VARCHAR(500) NOT NULL,
    content         TEXT NOT NULL,
    category        VARCHAR(100)
                    CHECK (category IN ('REFUND', 'WARRANTY', 'SHIPPING', 'PRICING', 'SLA', 'GENERAL', 'PRIVACY', 'SUPPORT')),
    embedding       vector(1536), -- Dimensions match text-embedding-3-small
    metadata        JSONB DEFAULT '{}',
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- IVFFlat index for fast approximate nearest-neighbor vector search
-- Note: This index requires at least 100 rows to be effective; it's created
-- here for schema completeness and will activate once data is seeded.
CREATE INDEX idx_policies_embedding ON company_policies
    USING ivfflat (embedding vector_cosine_ops) WITH (lists = 10);

CREATE INDEX idx_policies_category ON company_policies(category);

-- =============================================================================
-- UPDATED_AT TRIGGER
-- Automatically updates the updated_at timestamp on row modification
-- =============================================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_leads_updated_at
    BEFORE UPDATE ON leads
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_tickets_updated_at
    BEFORE UPDATE ON tickets
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_policies_updated_at
    BEFORE UPDATE ON company_policies
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
