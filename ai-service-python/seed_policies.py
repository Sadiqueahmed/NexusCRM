"""
NexusCRM AI Service — Seed Policies Script

Generates and stores embeddings for seed company policies that were
inserted via SQL without embeddings. Run this script after the
database has been initialized with seed data.

Usage:
    python -m seed_policies
"""

import asyncio
import logging
import sys

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s | %(levelname)-8s | %(name)s | %(message)s",
)
logger = logging.getLogger("nexuscrm.ai.seed")


async def main():
    """Backfill embeddings for all seeded company policies."""
    from app.config import get_settings
    from app.rag.vectorstore import init_db_pool, close_db_pool
    from app.rag.ingestion import backfill_embeddings

    settings = get_settings()

    logger.info("🚀 Starting policy embedding seed script...")
    logger.info(f"📊 Database: {settings.database_url_sync.split('@')[1]}")

    # Initialize database connection
    await init_db_pool(settings.database_url)

    try:
        count = await backfill_embeddings()
        if count > 0:
            logger.info(f"✅ Successfully embedded {count} company policies")
        else:
            logger.info("ℹ️  All policies already have embeddings — nothing to do")
    except Exception as e:
        logger.error(f"❌ Seed script failed: {e}", exc_info=True)
        sys.exit(1)
    finally:
        await close_db_pool()

    logger.info("🏁 Seed script complete")


if __name__ == "__main__":
    asyncio.run(main())
