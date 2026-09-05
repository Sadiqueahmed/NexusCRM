# =============================================================================
# NexusCRM AI Service — Dockerfile
# =============================================================================
# Lightweight Python container for the FastAPI + LangChain AI agent.
# =============================================================================

FROM python:3.12-slim

WORKDIR /app

# Install system dependencies for asyncpg and psycopg2
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        gcc \
        libpq-dev \
        curl \
    && rm -rf /var/lib/apt/lists/*

# Install Python dependencies
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy application code
COPY . .

# Create non-root user
RUN useradd -m nexuscrm && chown -R nexuscrm:nexuscrm /app
USER nexuscrm

# Expose FastAPI port
EXPOSE 8000

# Run the FastAPI server
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]
