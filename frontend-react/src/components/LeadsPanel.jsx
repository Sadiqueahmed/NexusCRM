import { useState, useEffect } from 'react';
import { UserPlus, Mail, Phone, Building2 } from 'lucide-react';
import { fetchLeads } from '../api';

/**
 * Leads Panel — Displays the sales pipeline with filtering
 * and status badges for each lead.
 */
export default function LeadsPanel() {
  const [leads, setLeads] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('');

  const statuses = ['', 'NEW', 'CONTACTED', 'QUALIFIED', 'CONVERTED', 'LOST'];

  useEffect(() => {
    loadLeads();
  }, [filter]);

  const loadLeads = async () => {
    setLoading(true);
    try {
      const res = await fetchLeads(filter || undefined);
      setLeads(res.data);
    } catch (err) {
      console.error('Failed to load leads:', err);
    } finally {
      setLoading(false);
    }
  };

  const getStatusClass = (status) => {
    return (status || '').toLowerCase().replace('_', '');
  };

  return (
    <>
      <div className="page-header">
        <h2>Sales Pipeline</h2>
        <p>Track leads from initial contact through conversion</p>
      </div>

      <div className="data-table-container">
        <div className="table-header">
          <h3>
            <UserPlus size={16} style={{ marginRight: 8, verticalAlign: 'middle' }} />
            All Leads ({leads.length})
          </h3>
          <div className="table-filters">
            {statuses.map((s) => (
              <button
                key={s || 'all'}
                className={`filter-btn ${filter === s ? 'active' : ''}`}
                onClick={() => setFilter(s)}
              >
                {s || 'All'}
              </button>
            ))}
          </div>
        </div>

        {loading ? (
          <div className="loading-spinner"><div className="spinner" /></div>
        ) : leads.length === 0 ? (
          <div className="empty-state">
            <UserPlus />
            <p>No leads found</p>
          </div>
        ) : (
          <table>
            <thead>
              <tr>
                <th>Name</th>
                <th>Contact</th>
                <th>Company</th>
                <th>Status</th>
                <th>Source</th>
                <th>Agent</th>
              </tr>
            </thead>
            <tbody>
              {leads.map((lead) => (
                <tr key={lead.id}>
                  <td style={{ color: 'var(--text-primary)', fontWeight: 500 }}>
                    {lead.firstName} {lead.lastName}
                  </td>
                  <td>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                      {lead.email && (
                        <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                          <Mail size={12} /> {lead.email}
                        </span>
                      )}
                      {lead.phone && (
                        <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                          <Phone size={12} /> {lead.phone}
                        </span>
                      )}
                    </div>
                  </td>
                  <td>
                    <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                      <Building2 size={14} /> {lead.company || '—'}
                    </span>
                  </td>
                  <td>
                    <span className={`status-badge ${getStatusClass(lead.status)}`}>
                      {lead.status}
                    </span>
                  </td>
                  <td style={{ fontSize: '0.8rem' }}>{lead.source || '—'}</td>
                  <td style={{ fontSize: '0.8rem' }}>{lead.assignedToUsername || '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </>
  );
}
