"""
NexusCRM AI Service — Application Configuration

Loads environment variables and provides typed configuration
for the FastAPI application, LLM providers, and database connections.
"""

from pydantic_settings import BaseSettings
from functools import lru_cache


class Settings(BaseSettings):
    """Application settings loaded from environment variables."""

    # --- LLM Configuration ---
    llm_provider: str = "anthropic"  # "anthropic" or "openai"
    llm_model: str = "claude-sonnet-4-20250514"
    anthropic_api_key: str = ""
    openai_api_key: str = ""

    # --- Database ---
    database_url: str = "postgresql+asyncpg://nexus:nexus_secret_2024@localhost:5432/nexuscrm"
    database_url_sync: str = "postgresql://nexus:nexus_secret_2024@localhost:5432/nexuscrm"

    # --- CRM API (Spring Boot Backend) ---
    crm_api_base_url: str = "http://localhost:8080/api/v1"

    # --- Embedding Model ---
    embedding_model: str = "text-embedding-3-small"
    embedding_dimensions: int = 1536

    # --- Server ---
    host: str = "0.0.0.0"
    port: int = 8000

    class Config:
        env_file = ".env"
        case_sensitive = False


@lru_cache()
def get_settings() -> Settings:
    """Cached settings singleton."""
    return Settings()
