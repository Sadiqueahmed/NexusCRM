"""
NexusCRM AI Service — LangChain Agent Setup

Configures the autonomous CRM agent with tool binding,
system prompt, and chain-of-thought reasoning. The agent
uses RAG to retrieve company policies and makes agentic
tool calls to update CRM records.
"""

import logging
from typing import List, Optional, Dict, Any

from langchain_core.messages import HumanMessage, SystemMessage
from langchain_anthropic import ChatAnthropic
from langchain_openai import ChatOpenAI

from app.config import get_settings
from app.agent.tools import ALL_TOOLS
from app.api.schemas import ToolAction

logger = logging.getLogger("nexuscrm.ai.agent")

# =============================================================================
# System Prompt
# =============================================================================

SYSTEM_PROMPT = """You are an autonomous CRM AI Agent for NexusCRM, a customer relationship management platform.

## Your Role
You are a Level 1 Support AI Agent that autonomously handles customer queries by:
1. Searching the company's internal knowledge base (RAG) for relevant policies
2. Retrieving CRM data (leads, tickets) to understand context
3. Taking actions (updating lead statuses, resolving tickets) based on policy guidelines
4. Providing clear, policy-backed responses to customer inquiries

## Core Principles
- **Always search company policies first** before making any decisions or recommendations
- **Be precise and cite specific policies** when explaining decisions
- **Only take actions that are clearly supported by company policy**
- **If unsure, explain what you found and recommend human escalation**
- **Always honor escalation requests** — if a customer asks to speak to a human, acknowledge and escalate immediately

## Available Actions
You can:
- Search company policies for refunds, warranties, shipping, pricing, SLAs, privacy, and support procedures
- Look up lead and ticket details in the CRM
- Update lead pipeline statuses (NEW → CONTACTED → QUALIFIED → CONVERTED/LOST)
- Resolve support tickets with detailed resolution notes
- Create new tickets for tracking purposes
- List open tickets for overview

## Response Format
After processing a query:
1. Summarize what you found in the knowledge base
2. Explain any actions you took and why
3. Provide a clear, helpful response to the original query
4. If you couldn't fully resolve the issue, explain what needs human attention

## Important Constraints
- Never fabricate policy information — only cite what you find in the knowledge base
- Always provide the policy basis for any action you take
- Flag any actions as AI-handled so human agents can review if needed
- Respect data privacy — don't expose sensitive information unnecessarily
"""


def _build_llm():
    """Builds the appropriate LLM based on configuration."""
    settings = get_settings()

    if settings.llm_provider == "anthropic":
        if not settings.anthropic_api_key:
            raise ValueError("ANTHROPIC_API_KEY is required when llm_provider=anthropic")
        return ChatAnthropic(
            model=settings.llm_model,
            anthropic_api_key=settings.anthropic_api_key,
            temperature=0.1,
            max_tokens=4096,
        )
    elif settings.llm_provider == "openai":
        if not settings.openai_api_key:
            raise ValueError("OPENAI_API_KEY is required when llm_provider=openai")
        return ChatOpenAI(
            model=settings.llm_model,
            openai_api_key=settings.openai_api_key,
            temperature=0.1,
            max_tokens=4096,
        )
    else:
        raise ValueError(f"Unsupported LLM provider: {settings.llm_provider}")


async def run_agent(
    query: str,
    context: Optional[Dict[str, Any]] = None,
) -> Dict[str, Any]:
    """
    Runs the autonomous CRM agent with the given query.

    The agent will:
    1. Analyze the query
    2. Search relevant policies via RAG
    3. Make agentic tool calls as needed
    4. Return a structured response with actions taken

    Args:
        query: The natural language customer query.
        context: Optional context (customer_email, ticket_id, etc.)

    Returns:
        Dict with response, actions_taken, policies_referenced, and reasoning.
    """
    logger.info(f"🤖 Agent invoked with query: {query[:100]}...")

    # Build LLM with tool binding
    llm = _build_llm()
    llm_with_tools = llm.bind_tools(ALL_TOOLS)

    # Build the message with optional context
    context_str = ""
    if context:
        context_str = f"\n\nAdditional context: {context}"

    messages = [
        SystemMessage(content=SYSTEM_PROMPT),
        HumanMessage(content=f"{query}{context_str}"),
    ]

    # Track actions taken during the agent loop
    actions_taken: List[ToolAction] = []
    policies_referenced: List[str] = []

    # Create a map of tool name → tool function for execution
    tool_map = {tool.name: tool for tool in ALL_TOOLS}

    # Agent loop: invoke LLM, execute tool calls, feed results back
    max_iterations = 10
    for iteration in range(max_iterations):
        logger.info(f"  Agent iteration {iteration + 1}/{max_iterations}")

        # Invoke the LLM
        response = await llm_with_tools.ainvoke(messages)
        messages.append(response)

        # Check if the LLM wants to call tools
        if not response.tool_calls:
            # No more tool calls — agent has finished reasoning
            logger.info(f"  Agent finished after {iteration + 1} iteration(s)")
            break

        # Execute each tool call
        from langchain_core.messages import ToolMessage

        for tool_call in response.tool_calls:
            tool_name = tool_call["name"]
            tool_args = tool_call["args"]

            logger.info(f"  🔧 Tool call: {tool_name}({tool_args})")

            if tool_name in tool_map:
                try:
                    tool_result = await tool_map[tool_name].ainvoke(tool_args)
                except Exception as e:
                    tool_result = f"Tool execution error: {str(e)}"
                    logger.error(f"  Tool {tool_name} failed: {e}")
            else:
                tool_result = f"Unknown tool: {tool_name}"

            # Track the action
            actions_taken.append(ToolAction(
                tool_name=tool_name,
                tool_input=tool_args,
                tool_output=str(tool_result)[:500],  # Truncate for response
            ))

            # Track policy references
            if tool_name == "search_company_policies" and "📋" in str(tool_result):
                for line in str(tool_result).split("\n"):
                    if line.startswith("📋 **"):
                        title = line.split("**")[1] if "**" in line else ""
                        if title:
                            policies_referenced.append(title)

            # Feed tool result back to the LLM
            messages.append(ToolMessage(
                content=str(tool_result),
                tool_call_id=tool_call["id"],
            ))

    # Extract the final text response
    final_response = response.content if hasattr(response, "content") else str(response)

    # Handle case where content is a list (Anthropic format)
    if isinstance(final_response, list):
        text_parts = [
            block.get("text", "") if isinstance(block, dict) else str(block)
            for block in final_response
        ]
        final_response = "\n".join(text_parts)

    result = {
        "response": final_response,
        "actions_taken": actions_taken,
        "policies_referenced": list(set(policies_referenced)),
        "reasoning": None,  # Could extract from chain-of-thought if enabled
    }

    logger.info(
        f"✅ Agent completed: {len(actions_taken)} action(s), "
        f"{len(policies_referenced)} policy/ies referenced"
    )
    return result
