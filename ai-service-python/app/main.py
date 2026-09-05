"""
NexusCRM AI Service — FastAPI Application Entry Point

Initializes the FastAPI app, registers routes, and starts the
uvicorn server. Handles startup/shutdown lifecycle events for
database connections and embedding initialization.
"""

import logging
from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.config import get_settings
from app.api.routes import router as api_router
from app.rag.vectorstore import init_db_pool, close_db_pool

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s | %(levelname)-8s | %(name)s | %(message)s",
    datefmt="%Y-%m-%d %H:%M:%S",
)
logger = logging.getLogger("nexuscrm.ai")


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Manages application startup and shutdown lifecycle."""
    logger.info("🚀 NexusCRM AI Service starting up...")
    settings = get_settings()

    # Initialize async database connection pool
    await init_db_pool(settings.database_url)
    logger.info("✅ Database connection pool initialized")

    logger.info(f"🤖 LLM Provider: {settings.llm_provider} ({settings.llm_model})")
    logger.info(f"📡 CRM API Base: {settings.crm_api_base_url}")

    yield

    # Shutdown
    await close_db_pool()
    logger.info("🛑 NexusCRM AI Service shut down")


# --- FastAPI Application ---
app = FastAPI(
    title="NexusCRM AI Agent Service",
    description=(
        "Autonomous AI agent that processes customer queries, "
        "performs RAG against company policies, and executes "
        "agentic tool calls to update CRM records."
    ),
    version="1.0.0",
    lifespan=lifespan,
)

# --- CORS Middleware ---
app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        "http://localhost:3000",   # React dev server
        "http://localhost:5173",   # Vite dev server
        "http://frontend:80",     # Docker internal
    ],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# --- Register Routes ---
app.include_router(api_router)


if __name__ == "__main__":
    import uvicorn

    settings = get_settings()
    uvicorn.run(
        "app.main:app",
        host=settings.host,
        port=settings.port,
        reload=True,
    )
