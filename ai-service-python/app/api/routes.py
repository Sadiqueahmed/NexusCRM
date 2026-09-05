"""
NexusCRM AI Service — FastAPI Routes

Defines all API endpoints for the AI microservice:
- /api/agent/query  — Submit a query for the autonomous AI agent
- /api/rag/ingest   — Ingest a new company policy into the knowledge base
- /api/rag/search   — Perform standalone RAG search
- /api/health       — Service health check
"""

import logging
from datetime import datetime

from fastapi import APIRouter, HTTPException

from app.api.schemas import (
    AgentQueryRequest,
    AgentQueryResponse,
    PolicyIngestRequest,
    PolicyIngestResponse,
    RAGSearchRequest,
    RAGSearchResponse,
    PolicyResult,
    HealthResponse,
)
from app.agent.agent import run_agent
from app.rag.embeddings import generate_embedding
from app.rag.ingestion import ingest_policy, backfill_embeddings
from app.rag.vectorstore import vector_search

logger = logging.getLogger("nexuscrm.ai.routes")

router = APIRouter()


# =============================================================================
# Agent Endpoint
# =============================================================================

@router.post(
    "/api/agent/query",
    response_model=AgentQueryResponse,
    summary="Process a query with the AI agent",
    description=(
        "Submits a natural language query to the autonomous AI agent. "
        "The agent will search company policies via RAG, retrieve CRM "
        "data, and take agentic actions (update leads, resolve tickets) "
        "as appropriate."
    ),
    tags=["Agent"],
)
async def agent_query(request: AgentQueryRequest):
    """Process a customer query through the autonomous AI agent."""
    try:
        logger.info(f"Agent query received: {request.query[:100]}...")

        result = await run_agent(
            query=request.query,
            context=request.context,
        )

        return AgentQueryResponse(
            response=result["response"],
            actions_taken=result["actions_taken"],
            policies_referenced=result["policies_referenced"],
            reasoning=result.get("reasoning"),
        )

    except ValueError as e:
        logger.error(f"Agent configuration error: {e}")
        raise HTTPException(status_code=500, detail=str(e))
    except Exception as e:
        logger.error(f"Agent query failed: {e}", exc_info=True)
        raise HTTPException(
            status_code=500,
            detail=f"Agent processing failed: {str(e)}",
        )


# =============================================================================
# RAG Endpoints
# =============================================================================

@router.post(
    "/api/rag/ingest",
    response_model=PolicyIngestResponse,
    summary="Ingest a new company policy",
    description=(
        "Ingests a new company policy document into the pgvector "
        "knowledge base. Automatically generates and stores the "
        "embedding for future RAG searches."
    ),
    tags=["RAG"],
)
async def rag_ingest(request: PolicyIngestRequest):
    """Ingest a new company policy into the RAG knowledge base."""
    try:
        policy_id = await ingest_policy(
            title=request.title,
            content=request.content,
            category=request.category,
        )
        return PolicyIngestResponse(
            id=policy_id,
            title=request.title,
            category=request.category,
        )
    except Exception as e:
        logger.error(f"Policy ingestion failed: {e}", exc_info=True)
        raise HTTPException(
            status_code=500,
            detail=f"Failed to ingest policy: {str(e)}",
        )


@router.post(
    "/api/rag/search",
    response_model=RAGSearchResponse,
    summary="Search company policies via RAG",
    description=(
        "Performs a standalone vector similarity search against the "
        "company policy knowledge base. Returns the top-K most "
        "relevant policies."
    ),
    tags=["RAG"],
)
async def rag_search(request: RAGSearchRequest):
    """Perform a standalone RAG search against company policies."""
    try:
        embedding = await generate_embedding(request.query)
        results = await vector_search(
            query_embedding=embedding,
            top_k=request.top_k,
        )
        return RAGSearchResponse(
            query=request.query,
            results=[PolicyResult(**r) for r in results],
            total_results=len(results),
        )
    except Exception as e:
        logger.error(f"RAG search failed: {e}", exc_info=True)
        raise HTTPException(
            status_code=500,
            detail=f"RAG search failed: {str(e)}",
        )


@router.post(
    "/api/rag/backfill",
    summary="Backfill missing embeddings",
    description=(
        "Generates embeddings for any company policies that are "
        "missing them (e.g., seed data). This should be called "
        "once after initial database seeding."
    ),
    tags=["RAG"],
)
async def rag_backfill():
    """Backfill embeddings for policies inserted without them."""
    try:
        count = await backfill_embeddings()
        return {
            "message": f"Backfilled {count} policy embeddings",
            "count": count,
        }
    except Exception as e:
        logger.error(f"Embedding backfill failed: {e}", exc_info=True)
        raise HTTPException(
            status_code=500,
            detail=f"Backfill failed: {str(e)}",
        )


# =============================================================================
# Health Check
# =============================================================================

@router.get(
    "/api/health",
    response_model=HealthResponse,
    summary="Health check",
    tags=["System"],
)
async def health_check():
    """Service health check endpoint for Docker healthchecks."""
    return HealthResponse(timestamp=datetime.utcnow())
