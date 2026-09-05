import { useState, useRef, useEffect } from 'react';
import { Send, Bot, User, Wrench, BookOpen, Loader2 } from 'lucide-react';
import { sendAgentQuery } from '../api';

/**
 * AI Agent Console — Chat interface for sending queries to the
 * autonomous AI agent. Displays the agent's responses, tool actions
 * taken, and policy references.
 */
export default function AgentConsole() {
  const [messages, setMessages] = useState([
    {
      role: 'ai',
      content: 'Hello! I\'m the NexusCRM AI Agent. I can help you with customer queries, look up company policies, update lead statuses, and resolve support tickets. How can I assist you today?',
      actions: [],
      policies: [],
    },
  ]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const messagesEndRef = useRef(null);

  // Auto-scroll to bottom on new messages
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!input.trim() || loading) return;

    const query = input.trim();
    setInput('');

    // Add user message
    setMessages((prev) => [...prev, { role: 'user', content: query }]);
    setLoading(true);

    try {
      const res = await sendAgentQuery(query);
      const data = res.data;

      setMessages((prev) => [
        ...prev,
        {
          role: 'ai',
          content: data.response,
          actions: data.actions_taken || [],
          policies: data.policies_referenced || [],
        },
      ]);
    } catch (err) {
      const errorMsg = err.response?.data?.detail || err.message || 'Agent processing failed';
      setMessages((prev) => [
        ...prev,
        {
          role: 'ai',
          content: `⚠️ Error: ${errorMsg}\n\nMake sure the AI service is running and an LLM API key is configured.`,
          actions: [],
          policies: [],
        },
      ]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <div className="page-header">
        <h2>AI Agent Console</h2>
        <p>Send queries to the autonomous CRM agent — it will search policies, update records, and resolve issues</p>
      </div>

      <div className="chat-container">
        <div className="chat-header">
          <div className="ai-indicator" />
          <div>
            <div style={{ fontWeight: 600, fontSize: '0.9rem' }}>NexusCRM Agent</div>
            <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
              {loading ? 'Processing...' : 'Online — Ready'}
            </div>
          </div>
        </div>

        <div className="chat-messages">
          {messages.map((msg, i) => (
            <div key={i} className={`chat-message ${msg.role}`}>
              {/* Message header icon */}
              <div style={{
                display: 'flex',
                alignItems: 'center',
                gap: 6,
                marginBottom: 6,
                fontSize: '0.75rem',
                color: msg.role === 'user' ? 'rgba(255,255,255,0.7)' : 'var(--text-muted)',
                fontWeight: 600,
              }}>
                {msg.role === 'ai' ? <Bot size={14} /> : <User size={14} />}
                {msg.role === 'ai' ? 'AI Agent' : 'You'}
              </div>

              {/* Message content */}
              <div style={{ whiteSpace: 'pre-wrap' }}>{msg.content}</div>

              {/* Tool actions taken */}
              {msg.actions && msg.actions.length > 0 && (
                <div className="actions-list">
                  <div style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: 4,
                    marginBottom: 4,
                    fontWeight: 600,
                  }}>
                    <Wrench size={12} /> Actions Taken:
                  </div>
                  {msg.actions.map((action, j) => (
                    <div key={j} className="action-item">
                      <span className="tool-name">{action.tool_name}</span>
                      <span>→ {action.tool_output.substring(0, 100)}
                        {action.tool_output.length > 100 ? '...' : ''}
                      </span>
                    </div>
                  ))}
                </div>
              )}

              {/* Referenced policies */}
              {msg.policies && msg.policies.length > 0 && (
                <div className="actions-list" style={{ marginTop: 6 }}>
                  <div style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: 4,
                    marginBottom: 4,
                    fontWeight: 600,
                  }}>
                    <BookOpen size={12} /> Policies Referenced:
                  </div>
                  {msg.policies.map((policy, j) => (
                    <div key={j} style={{ padding: '2px 0' }}>📋 {policy}</div>
                  ))}
                </div>
              )}
            </div>
          ))}

          {/* Loading indicator */}
          {loading && (
            <div className="chat-message ai" style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
              <Loader2 size={16} className="spinner" style={{ animation: 'spin 1s linear infinite' }} />
              <span>Agent is thinking...</span>
            </div>
          )}

          <div ref={messagesEndRef} />
        </div>

        <form className="chat-input-area" onSubmit={handleSubmit}>
          <input
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="Ask the AI agent anything... e.g., 'What's the refund policy for a double charge?'"
            disabled={loading}
          />
          <button type="submit" disabled={loading || !input.trim()}>
            <Send size={16} style={{ marginRight: 6 }} />
            Send
          </button>
        </form>
      </div>
    </>
  );
}
