"""
NexusCRM AI Service — LangChain Agentic Tools

Strictly defined tools that allow the LangChain agent to interact
with the CRM system. Each tool maps to a specific operation on
the Spring Boot API or the local pgvector knowledge base.

The agent autonomously decides which tools to call based on the
customer query and retrieved policy context.
"""

import json
import logging
from typing import Optional

from langchain_core.tools import tool

from app.services.crm_client import crm_client
from app.rag.embeddings import generate_embedding
from app.rag.vectorstore import vector_search

logger = logging.getLogger("nexuscrm.ai.tools")


# =============================================================================
# RAG Tools
# =============================================================================

@tool
async def search_company_policies(query: str, category: Optional[str] = None) -> str:
    """Search the company's internal knowledge base for relevant policies.

    Use this tool to find company policies about refunds, warranties,
    shipping, pricing, SLAs, privacy, support procedures, or general
    terms of service. Always search policies before making decisions
    about customer requests.

    Args:
        query: Natural language search query about the policy topic.
        category: Optional category filter. One of: REFUND, WARRANTY,
                  SHIPPING, PRICING, SLA, GENERAL, PRIVACY, SUPPORT.
    """
    try:
        embedding = await generate_embedding(query)
        results = await vector_search(
            query_embedding=embedding,
            top_k=3,
            category_filter=category,
        )

        if not results:
            return "No relevant company policies found for this query."

        formatted = []
        for r in results:
            formatted.append(
                f"📋 **{r['title']}** (Category: {r['category']}, "
                f"Relevance: {r['similarity_score']:.2%})\n{r['content']}"
            )

        return "\n\n---\n\n".join(formatted)

    except Exception as e:
        logger.error(f"Policy search failed: {e}")
        return f"Error searching policies: {str(e)}"


# =============================================================================
# Lead Tools
# =============================================================================

@tool
async def get_lead_details(lead_id: int) -> str:
    """Retrieve full details of a specific lead from the CRM.

    Use this tool to look up a lead's information including their
    contact details, company, current pipeline status, assigned agent,
    and any notes.

    Args:
        lead_id: The numeric ID of the lead to retrieve.
    """
    try:
        lead = await crm_client.get_lead(lead_id)
        return json.dumps(lead, indent=2, default=str)
    except Exception as e:
        logger.error(f"Failed to get lead {lead_id}: {e}")
        return f"Error retrieving lead {lead_id}: {str(e)}"


@tool
async def update_lead_status(lead_id: int, new_status: str, reason: str) -> str:
    """Update the pipeline status of a lead in the CRM.

    Use this tool to move a lead through the sales pipeline.
    Always provide a clear reason for the status change.

    Args:
        lead_id: The numeric ID of the lead to update.
        new_status: The new status. Must be one of:
                    NEW, CONTACTED, QUALIFIED, LOST, CONVERTED.
        reason: A clear explanation for why the status is being changed.
    """
    valid_statuses = {"NEW", "CONTACTED", "QUALIFIED", "LOST", "CONVERTED"}
    if new_status.upper() not in valid_statuses:
        return f"Invalid status '{new_status}'. Must be one of: {', '.join(valid_statuses)}"

    try:
        result = await crm_client.update_lead_status(
            lead_id=lead_id,
            status=new_status.upper(),
            reason=reason,
        )
        return (
            f"✅ Lead {lead_id} status updated to {new_status.upper()}. "
            f"Lead: {result.get('firstName', '')} {result.get('lastName', '')} "
            f"({result.get('company', 'N/A')})"
        )
    except Exception as e:
        logger.error(f"Failed to update lead {lead_id} status: {e}")
        return f"Error updating lead {lead_id}: {str(e)}"


# =============================================================================
# Ticket Tools
# =============================================================================

@tool
async def get_ticket_details(ticket_id: int) -> str:
    """Retrieve full details of a specific support ticket from the CRM.

    Use this tool to look up a ticket's subject, description, status,
    priority, category, customer email, assigned agent, and resolution.

    Args:
        ticket_id: The numeric ID of the ticket to retrieve.
    """
    try:
        ticket = await crm_client.get_ticket(ticket_id)
        return json.dumps(ticket, indent=2, default=str)
    except Exception as e:
        logger.error(f"Failed to get ticket {ticket_id}: {e}")
        return f"Error retrieving ticket {ticket_id}: {str(e)}"


@tool
async def resolve_ticket(ticket_id: int, resolution: str) -> str:
    """Resolve a support ticket with resolution notes.

    Use this tool to mark a ticket as RESOLVED and provide
    detailed resolution notes explaining how the issue was addressed.
    This action is flagged as AI-handled in the CRM.

    Args:
        ticket_id: The numeric ID of the ticket to resolve.
        resolution: Detailed resolution notes explaining how the issue
                    was resolved, including any policy references.
    """
    try:
        result = await crm_client.resolve_ticket(
            ticket_id=ticket_id,
            resolution=resolution,
            ai_handled=True,
        )
        return (
            f"✅ Ticket {ticket_id} resolved (AI-handled). "
            f"Subject: {result.get('subject', 'N/A')}"
        )
    except Exception as e:
        logger.error(f"Failed to resolve ticket {ticket_id}: {e}")
        return f"Error resolving ticket {ticket_id}: {str(e)}"


@tool
async def create_ticket(
    subject: str,
    description: str,
    priority: str = "MEDIUM",
    category: str = "GENERAL",
    customer_email: Optional[str] = None,
) -> str:
    """Create a new support ticket in the CRM.

    Use this tool when a customer issue requires tracking or
    escalation and no existing ticket covers it.

    Args:
        subject: Brief subject line for the ticket (max 500 chars).
        description: Detailed description of the issue.
        priority: Urgency level. One of: LOW, MEDIUM, HIGH, CRITICAL.
        category: Issue category. One of: SUPPORT, REFUND, WARRANTY,
                  SHIPPING, GENERAL.
        customer_email: The customer's email address if known.
    """
    try:
        ticket_data = {
            "subject": subject,
            "description": description,
            "priority": priority.upper(),
            "category": category,
        }
        if customer_email:
            ticket_data["customerEmail"] = customer_email

        result = await crm_client.create_ticket(ticket_data)
        return (
            f"✅ New ticket created (ID: {result.get('id', 'N/A')}). "
            f"Subject: {subject}, Priority: {priority.upper()}"
        )
    except Exception as e:
        logger.error(f"Failed to create ticket: {e}")
        return f"Error creating ticket: {str(e)}"


@tool
async def list_open_tickets() -> str:
    """List all currently open support tickets in the CRM.

    Use this tool to get an overview of pending customer issues
    that may need attention or to find relevant existing tickets.
    """
    try:
        tickets = await crm_client.list_tickets(status="OPEN")

        if not tickets:
            return "No open tickets found."

        lines = [f"📋 Found {len(tickets)} open ticket(s):\n"]
        for t in tickets:
            lines.append(
                f"  • #{t['id']} [{t.get('priority', 'N/A')}] "
                f"{t.get('subject', 'N/A')} — {t.get('customerEmail', 'N/A')}"
            )

        return "\n".join(lines)
    except Exception as e:
        logger.error(f"Failed to list open tickets: {e}")
        return f"Error listing tickets: {str(e)}"


# =============================================================================
# Tool Registry
# =============================================================================

ALL_TOOLS = [
    search_company_policies,
    get_lead_details,
    update_lead_status,
    get_ticket_details,
    resolve_ticket,
    create_ticket,
    list_open_tickets,
]
