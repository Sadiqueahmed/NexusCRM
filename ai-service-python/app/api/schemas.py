"""
NexusCRM AI Service — Pydantic API Schemas

Request/response models for the FastAPI endpoints.
"""

from pydantic import BaseModel, Field
from typing import Optional, List
from datetime import datetime


# =============================================================================
# Agent Endpoint Schemas
# =============================================================================

class AgentQueryRequest(BaseModel):
    """Request body for the AI agent query endpoint."""
    query: str = Field(
        ...,
        description="Natural language query for the AI agent to process",
        min_length=1,
        max_length=2000,
        examples=["What is the refund policy for a customer charged twice?"],
    )
    context: Optional[dict] = Field(
        default=None,
        description="Optional context (e.g., customer email, ticket ID) to help the agent",
    )


class ToolAction(BaseModel):
    """Represents a single tool action taken by the agent."""
    tool_name: str
    tool_input: dict
    tool_output: str


class AgentQueryResponse(BaseModel):
    """Response from the AI agent after processing a query."""
    response: str = Field(description="The agent's final natural language response")
    actions_taken: List[ToolAction] = Field(
        default_factory=list,
        description="List of agentic tool calls the agent made during processing",
    )
    policies_referenced: List[str] = Field(
        default_factory=list,
        description="Titles of company policies retrieved via RAG",
    )
    reasoning: Optional[str] = Field(
        default=None,
        description="The agent's chain-of-thought reasoning (if available)",
    )


# =============================================================================
# RAG Endpoint Schemas
# =============================================================================

class PolicyIngestRequest(BaseModel):
    """Request body for ingesting a new company policy document."""
    title: str = Field(..., max_length=500)
    content: str = Field(..., min_length=10)
    category: str = Field(
        ...,
        description="Policy category",
        examples=["REFUND", "WARRANTY", "SHIPPING", "PRICING", "SLA", "GENERAL", "PRIVACY", "SUPPORT"],
    )


class PolicyIngestResponse(BaseModel):
    """Response after successfully ingesting a policy."""
    id: int
    title: str
    category: str
    message: str = "Policy ingested and embedded successfully"


class RAGSearchRequest(BaseModel):
    """Request body for standalone RAG search."""
    query: str = Field(..., min_length=1, max_length=1000)
    top_k: int = Field(default=3, ge=1, le=10)


class PolicyResult(BaseModel):
    """A single policy result from RAG search."""
    id: int
    title: str
    content: str
    category: str
    similarity_score: float


class RAGSearchResponse(BaseModel):
    """Response from a RAG search query."""
    query: str
    results: List[PolicyResult]
    total_results: int


# =============================================================================
# Health Check
# =============================================================================

class HealthResponse(BaseModel):
    """Health check response."""
    status: str = "healthy"
    service: str = "nexuscrm-ai-service"
    version: str = "1.0.0"
    timestamp: datetime = Field(default_factory=datetime.utcnow)
