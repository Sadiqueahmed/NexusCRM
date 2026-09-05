import { useState, useEffect } from 'react';
import { Ticket, AlertTriangle, Bot } from 'lucide-react';
import { fetchTickets } from '../api';

/**
 * Tickets Panel — Displays support tickets with priority indicators,
 * AI-handled badges, and status/priority filtering.
 */
export default function TicketsPanel() {
  const [tickets, setTickets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState('');
  const [priorityFilter, setPriorityFilter] = useState('');

  const statuses = ['', 'OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'];
  const priorities = ['', 'LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];

  useEffect(() => {
    loadTickets();
  }, [statusFilter, priorityFilter]);

  const loadTickets = async () => {
    setLoading(true);
    try {
      const res = await fetchTickets(
        statusFilter || undefined,
        priorityFilter || undefined,
      );
      setTickets(res.data);
    } catch (err) {
      console.error('Failed to load tickets:', err);
    } finally {
      setLoading(false);
    }
  };

  const getStatusClass = (status) => {
    return (status || '').toLowerCase().replace('_', '_');
  };

  const getPriorityClass = (priority) => {
    return (priority || '').toLowerCase();
  };

  return (
    <>
      <div className="page-header">
        <h2>Support Tickets</h2>
        <p>Track and resolve customer support requests</p>
      </div>

      <div className="data-table-container">
        <div className="table-header">
          <h3>
            <Ticket size={16} style={{ marginRight: 8, verticalAlign: 'middle' }} />
            All Tickets ({tickets.length})
          </h3>
          <div className="table-filters">
            {statuses.map((s) => (
              <button
                key={s || 'all-status'}
                className={`filter-btn ${statusFilter === s ? 'active' : ''}`}
                onClick={() => setStatusFilter(s)}
              >
                {s || 'All Status'}
              </button>
            ))}
            <span style={{ borderLeft: '1px solid var(--border-subtle)', height: 20, margin: '0 4px' }} />
            {priorities.map((p) => (
              <button
                key={p || 'all-priority'}
                className={`filter-btn ${priorityFilter === p ? 'active' : ''}`}
                onClick={() => setPriorityFilter(p)}
              >
                {p || 'All Priority'}
              </button>
            ))}
          </div>
        </div>

        {loading ? (
          <div className="loading-spinner"><div className="spinner" /></div>
        ) : tickets.length === 0 ? (
          <div className="empty-state">
            <Ticket />
            <p>No tickets found</p>
          </div>
        ) : (
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Subject</th>
                <th>Status</th>
                <th>Priority</th>
                <th>Category</th>
                <th>Customer</th>
                <th>AI</th>
              </tr>
            </thead>
            <tbody>
              {tickets.map((ticket) => (
                <tr key={ticket.id}>
                  <td style={{ fontFamily: 'var(--font-mono)', fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                    #{ticket.id}
                  </td>
                  <td style={{ color: 'var(--text-primary)', fontWeight: 500, maxWidth: 300 }}>
                    <div style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                      {ticket.subject}
                    </div>
                  </td>
                  <td>
                    <span className={`status-badge ${getStatusClass(ticket.status)}`}>
                      {ticket.status}
                    </span>
                  </td>
                  <td>
                    <span className={`priority-badge ${getPriorityClass(ticket.priority)}`}>
                      {ticket.priority === 'CRITICAL' && <AlertTriangle size={10} />}
                      {ticket.priority}
                    </span>
                  </td>
                  <td style={{ fontSize: '0.8rem' }}>{ticket.category || '—'}</td>
                  <td style={{ fontSize: '0.8rem' }}>{ticket.customerEmail || '—'}</td>
                  <td>
                    {ticket.aiHandled && (
                      <span style={{
                        display: 'inline-flex',
                        alignItems: 'center',
                        gap: 4,
                        padding: '2px 6px',
                        borderRadius: 'var(--radius-sm)',
                        background: 'rgba(99, 102, 241, 0.1)',
                        color: 'var(--accent-indigo)',
                        fontSize: '0.7rem',
                        fontWeight: 600,
                      }}>
                        <Bot size={10} /> AI
                      </span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </>
  );
}
