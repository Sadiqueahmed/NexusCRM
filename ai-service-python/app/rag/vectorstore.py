"""
NexusCRM AI Service — pgvector Vector Store Operations

Manages the async connection pool and provides vector similarity
search against company policy embeddings stored in PostgreSQL/pgvector.
"""

import logging
from typing import Optional, List, Tuple

import asyncpg

logger = logging.getLogger("nexuscrm.ai.vectorstore")

# Module-level connection pool
_pool: Optional[asyncpg.Pool] = None


async def init_db_pool(database_url: str):
    """Initialize the async connection pool for pgvector operations."""
    global _pool
    # Convert SQLAlchemy-style URL to asyncpg format
    url = database_url.replace("postgresql+asyncpg://", "postgresql://")
    _pool = await asyncpg.create_pool(url, min_size=2, max_size=10)
    logger.info("pgvector connection pool initialized")


async def close_db_pool():
    """Close the database connection pool."""
    global _pool
    if _pool:
        await _pool.close()
        _pool = None
        logger.info("pgvector connection pool closed")


async def vector_search(
    query_embedding: List[float],
    top_k: int = 3,
    category_filter: Optional[str] = None,
) -> List[dict]:
    """
    Performs cosine similarity search against company_policies embeddings.

    Args:
        query_embedding: The embedding vector of the search query.
        top_k: Number of top results to return.
        category_filter: Optional category to filter by (REFUND, WARRANTY, etc.)

    Returns:
        List of matching policies with similarity scores.
    """
    if _pool is None:
        raise RuntimeError("Database pool not initialized. Call init_db_pool first.")

    # Build the query with optional category filter
    if category_filter:
        query = """
            SELECT id, title, content, category,
                   1 - (embedding <=> $1::vector) AS similarity
            FROM company_policies
            WHERE embedding IS NOT NULL AND category = $2
            ORDER BY embedding <=> $1::vector
            LIMIT $3
        """
        params = [str(query_embedding), category_filter.upper(), top_k]
    else:
        query = """
            SELECT id, title, content, category,
                   1 - (embedding <=> $1::vector) AS similarity
            FROM company_policies
            WHERE embedding IS NOT NULL
            ORDER BY embedding <=> $1::vector
            LIMIT $2
        """
        params = [str(query_embedding), top_k]

    async with _pool.acquire() as conn:
        rows = await conn.fetch(query, *params)

    results = []
    for row in rows:
        results.append({
            "id": row["id"],
            "title": row["title"],
            "content": row["content"],
            "category": row["category"],
            "similarity_score": float(row["similarity"]),
        })

    logger.info(
        f"Vector search returned {len(results)} results "
        f"(top similarity: {results[0]['similarity_score']:.4f})"
        if results else "Vector search returned 0 results"
    )
    return results


async def store_embedding(
    policy_id: int, embedding: List[float]
) -> None:
    """Stores an embedding vector for a company policy."""
    if _pool is None:
        raise RuntimeError("Database pool not initialized.")

    async with _pool.acquire() as conn:
        await conn.execute(
            "UPDATE company_policies SET embedding = $1::vector WHERE id = $2",
            str(embedding),
            policy_id,
        )
    logger.info(f"Stored embedding for policy ID={policy_id}")


async def insert_policy(
    title: str,
    content: str,
    category: str,
    embedding: Optional[List[float]] = None,
) -> int:
    """Inserts a new company policy and optionally stores its embedding."""
    if _pool is None:
        raise RuntimeError("Database pool not initialized.")

    async with _pool.acquire() as conn:
        if embedding:
            row = await conn.fetchrow(
                """
                INSERT INTO company_policies (title, content, category, embedding)
                VALUES ($1, $2, $3, $4::vector)
                RETURNING id
                """,
                title, content, category, str(embedding),
            )
        else:
            row = await conn.fetchrow(
                """
                INSERT INTO company_policies (title, content, category)
                VALUES ($1, $2, $3)
                RETURNING id
                """,
                title, content, category,
            )

    policy_id = row["id"]
    logger.info(f"Inserted policy '{title}' with ID={policy_id}")
    return policy_id


async def get_policies_without_embeddings() -> List[dict]:
    """Retrieves all policies that don't have embeddings yet."""
    if _pool is None:
        raise RuntimeError("Database pool not initialized.")

    async with _pool.acquire() as conn:
        rows = await conn.fetch(
            """
            SELECT id, title, content, category
            FROM company_policies
            WHERE embedding IS NULL
            ORDER BY id
            """
        )

    return [dict(row) for row in rows]
