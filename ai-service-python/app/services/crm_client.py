"""
NexusCRM AI Service — CRM HTTP Client

Async HTTP client for interacting with the Spring Boot Core API.
Used by LangChain agentic tools to read/write CRM data.
"""

import logging
from typing import Optional, List, Dict, Any

import httpx

from app.config import get_settings

logger = logging.getLogger("nexuscrm.ai.crm_client")


class CRMClient:
    """Async HTTP client for the Spring Boot CRM API."""

    def __init__(self):
        settings = get_settings()
        self.base_url = settings.crm_api_base_url
        self._client: Optional[httpx.AsyncClient] = None

    async def _get_client(self) -> httpx.AsyncClient:
        """Lazily initialize the HTTP client."""
        if self._client is None or self._client.is_closed:
            self._client = httpx.AsyncClient(
                base_url=self.base_url,
                timeout=30.0,
                headers={"Content-Type": "application/json"},
            )
        return self._client

    async def close(self):
        """Close the HTTP client."""
        if self._client and not self._client.is_closed:
            await self._client.aclose()

    # =========================================================================
    # Lead Operations
    # =========================================================================

    async def get_lead(self, lead_id: int) -> Dict[str, Any]:
        """Retrieves a lead by ID from the CRM API."""
        client = await self._get_client()
        response = await client.get(f"/leads/{lead_id}")
        response.raise_for_status()
        logger.info(f"Retrieved lead ID={lead_id}")
        return response.json()

    async def list_leads(self, status: Optional[str] = None) -> List[Dict[str, Any]]:
        """Lists leads, optionally filtered by status."""
        client = await self._get_client()
        params = {"status": status} if status else {}
        response = await client.get("/leads", params=params)
        response.raise_for_status()
        return response.json()

    async def update_lead_status(
        self, lead_id: int, status: str, reason: str = ""
    ) -> Dict[str, Any]:
        """Updates a lead's pipeline status via PATCH."""
        client = await self._get_client()
        response = await client.patch(
            f"/leads/{lead_id}/status",
            json={"status": status, "reason": reason},
        )
        response.raise_for_status()
        logger.info(f"Updated lead ID={lead_id} status to {status}")
        return response.json()

    # =========================================================================
    # Ticket Operations
    # =========================================================================

    async def get_ticket(self, ticket_id: int) -> Dict[str, Any]:
        """Retrieves a ticket by ID from the CRM API."""
        client = await self._get_client()
        response = await client.get(f"/tickets/{ticket_id}")
        response.raise_for_status()
        logger.info(f"Retrieved ticket ID={ticket_id}")
        return response.json()

    async def list_tickets(
        self,
        status: Optional[str] = None,
        priority: Optional[str] = None,
    ) -> List[Dict[str, Any]]:
        """Lists tickets, optionally filtered by status and/or priority."""
        client = await self._get_client()
        params = {}
        if status:
            params["status"] = status
        if priority:
            params["priority"] = priority
        response = await client.get("/tickets", params=params)
        response.raise_for_status()
        return response.json()

    async def create_ticket(self, ticket_data: Dict[str, Any]) -> Dict[str, Any]:
        """Creates a new support ticket."""
        client = await self._get_client()
        response = await client.post("/tickets", json=ticket_data)
        response.raise_for_status()
        logger.info(f"Created ticket: {ticket_data.get('subject', 'N/A')}")
        return response.json()

    async def resolve_ticket(
        self, ticket_id: int, resolution: str, ai_handled: bool = True
    ) -> Dict[str, Any]:
        """Resolves a ticket with resolution notes via PATCH."""
        client = await self._get_client()
        response = await client.patch(
            f"/tickets/{ticket_id}/resolve",
            json={"resolution": resolution, "aiHandled": ai_handled},
        )
        response.raise_for_status()
        logger.info(f"Resolved ticket ID={ticket_id} (AI handled: {ai_handled})")
        return response.json()

    # =========================================================================
    # Dashboard
    # =========================================================================

    async def get_dashboard_stats(self) -> Dict[str, Any]:
        """Retrieves dashboard statistics."""
        client = await self._get_client()
        response = await client.get("/dashboard/stats")
        response.raise_for_status()
        return response.json()


# Singleton instance
crm_client = CRMClient()
