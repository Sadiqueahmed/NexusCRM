import { Bot, Users, Ticket, Zap } from 'lucide-react';

/**
 * Activity Feed — Displays real-time SSE events showing
 * AI agent actions and CRM data modifications as they happen.
 *
 * @param {Array} events - Array of SSE events
 * @param {boolean} compact - If true, renders in compact sidebar mode
 */
export default function ActivityFeed({ events = [], compact = false }) {
  const getEventIcon = (type) => {
    switch (type) {
      case 'LEAD_UPDATED': return { Icon: Users, className: 'lead' };
      case 'TICKET_CREATED': return { Icon: Ticket, className: 'ticket' };
      case 'TICKET_RESOLVED': return { Icon: Ticket, className: 'ai' };
      case 'AI_ACTION': return { Icon: Bot, className: 'ai' };
      default: return { Icon: Zap, className: 'ai' };
    }
  };

  const formatTime = (timestamp) => {
    if (!timestamp) return '';
    const date = new Date(timestamp);
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' });
  };

  const content = (
    <>
      <div className="activity-feed-header">
        <h3>{compact ? 'Live Activity' : 'Activity Feed'}</h3>
        <div className="live-dot">Live</div>
      </div>

      <div className="feed-items" style={compact ? { maxHeight: 350 } : { maxHeight: 'calc(100vh - 250px)' }}>
        {events.length === 0 ? (
          <div className="empty-state" style={{ padding: 'var(--space-xl)' }}>
            <Zap />
            <p style={{ marginTop: 'var(--space-sm)', fontSize: '0.8rem' }}>
              No activity yet. Events will appear here in real-time when the AI agent or users modify CRM data.
            </p>
          </div>
        ) : (
          events.map((event, i) => {
            const { Icon, className } = getEventIcon(event.type);
            return (
              <div key={i} className="feed-item">
                <div className={`feed-item-icon ${className}`}>
                  <Icon size={14} />
                </div>
                <div className="feed-item-content">
                  <p>
                    {event.data?.message || event.type}
                    {event.data?.aiGenerated && (
                      <span className="ai-tag">
                        <Bot size={10} /> AI
                      </span>
                    )}
                  </p>
                  <div className="timestamp">{formatTime(event.timestamp || event.data?.timestamp)}</div>
                </div>
              </div>
            );
          })
        )}
      </div>
    </>
  );

  if (compact) {
    return <div className="activity-feed">{content}</div>;
  }

  return (
    <>
      <div className="page-header">
        <h2>Activity Feed</h2>
        <p>Real-time stream of all CRM events and AI agent actions</p>
      </div>
      <div className="activity-feed">{content}</div>
    </>
  );
}
