"""
NexusCRM AI Service — Document Ingestion

Handles ingesting new company policy documents into the pgvector
knowledge base. Generates embeddings and stores them alongside
the document content for future RAG retrieval.
"""

import logging
from typing import Optional

from app.rag.embeddings import generate_embedding, generate_embeddings_batch
from app.rag.vectorstore import (
    insert_policy,
    store_embedding,
    get_policies_without_embeddings,
)

logger = logging.getLogger("nexuscrm.ai.ingestion")


async def ingest_policy(
    title: str,
    content: str,
    category: str,
) -> int:
    """
    Ingests a new company policy: stores the document and generates
    its embedding for vector search.

    Args:
        title: Policy title.
        content: Full policy text content.
        category: Policy category (REFUND, WARRANTY, etc.).

    Returns:
        The ID of the newly created policy.
    """
    # Generate embedding for the full document content
    embedding_text = f"{title}\n\n{content}"
    embedding = await generate_embedding(embedding_text)

    # Store in database with embedding
    policy_id = await insert_policy(
        title=title,
        content=content,
        category=category,
        embedding=embedding,
    )

    logger.info(f"Ingested policy '{title}' (ID={policy_id}, category={category})")
    return policy_id


async def backfill_embeddings() -> int:
    """
    Generates and stores embeddings for any policies that are
    missing them (e.g., seed data inserted without embeddings).

    Returns:
        Number of policies that were backfilled.
    """
    policies = await get_policies_without_embeddings()

    if not policies:
        logger.info("No policies need embedding backfill")
        return 0

    logger.info(f"Backfilling embeddings for {len(policies)} policies...")

    # Generate embeddings in batch for efficiency
    texts = [f"{p['title']}\n\n{p['content']}" for p in policies]
    embeddings = await generate_embeddings_batch(texts)

    # Store each embedding
    for policy, embedding in zip(policies, embeddings):
        await store_embedding(policy["id"], embedding)

    logger.info(f"✅ Backfilled {len(policies)} policy embeddings")
    return len(policies)
