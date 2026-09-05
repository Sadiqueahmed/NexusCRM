"""
NexusCRM AI Service — Embedding Generation

Generates text embeddings using OpenAI's text-embedding-3-small model.
Used for both query embedding (at search time) and document embedding
(at ingestion time) in the RAG pipeline.
"""

import logging
from typing import List

from langchain_openai import OpenAIEmbeddings

from app.config import get_settings

logger = logging.getLogger("nexuscrm.ai.embeddings")

# Module-level embedding model instance (lazy init)
_embeddings: OpenAIEmbeddings | None = None


def get_embedding_model() -> OpenAIEmbeddings:
    """Returns a cached OpenAI embedding model instance."""
    global _embeddings
    if _embeddings is None:
        settings = get_settings()
        _embeddings = OpenAIEmbeddings(
            model=settings.embedding_model,
            openai_api_key=settings.openai_api_key,
            dimensions=settings.embedding_dimensions,
        )
        logger.info(f"Initialized embedding model: {settings.embedding_model}")
    return _embeddings


async def generate_embedding(text: str) -> List[float]:
    """
    Generates a single embedding vector for the given text.

    Args:
        text: The text to embed.

    Returns:
        A list of floats representing the embedding vector.
    """
    model = get_embedding_model()
    embedding = await model.aembed_query(text)
    logger.debug(f"Generated embedding for text ({len(text)} chars) → {len(embedding)} dims")
    return embedding


async def generate_embeddings_batch(texts: List[str]) -> List[List[float]]:
    """
    Generates embeddings for a batch of texts.

    Args:
        texts: List of texts to embed.

    Returns:
        List of embedding vectors.
    """
    model = get_embedding_model()
    embeddings = await model.aembed_documents(texts)
    logger.info(f"Generated {len(embeddings)} embeddings in batch")
    return embeddings
