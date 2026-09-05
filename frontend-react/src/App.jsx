import { useState, useEffect, useCallback } from 'react';
import {
  LayoutDashboard, Users, Ticket, Bot, Activity
} from 'lucide-react';
import Dashboard from './components/Dashboard';
import LeadsPanel from './components/LeadsPanel';
import TicketsPanel from './components/TicketsPanel';
import AgentConsole from './components/AgentConsole';
import ActivityFeed from './components/ActivityFeed';
import { createSSEConnection, fetchDashboardStats } from './api';
import './App.css';

/**
 * NexusCRM Dashboard — Main Application
 *
 * Renders the sidebar navigation, active panel, and manages
 * SSE event stream for real-time activity feed updates.
 */
function App() {
  const [activeView, setActiveView] = useState('dashboard');
  const [events, setEvents] = useState([]);
  const [stats, setStats] = useState(null);

  // Load dashboard stats
  const loadStats = useCallback(async () => {
    try {
      const res = await fetchDashboardStats();
      setStats(res.data);
    } catch (err) {
      console.warn('Failed to load dashboard stats:', err.message);
    }
  }, []);

  // SSE event handler
  const handleSSEEvent = useCallback((event) => {
    setEvents((prev) => [event, ...prev].slice(0, 50)); // Keep last 50 events
    // Refresh stats when data changes
    loadStats();
  }, [loadStats]);

  useEffect(() => {
    loadStats();

    // Connect to SSE event stream
    const eventSource = createSSEConnection(handleSSEEvent);

    return () => {
      eventSource.close();
    };
  }, [loadStats, handleSSEEvent]);

  const navItems = [
    { id: 'dashboard', label: 'Dashboard', icon: LayoutDashboard },
    { id: 'leads', label: 'Leads', icon: Users },
    { id: 'tickets', label: 'Tickets', icon: Ticket },
    { id: 'agent', label: 'AI Agent', icon: Bot },
    { id: 'activity', label: 'Activity', icon: Activity },
  ];

  const renderContent = () => {
    switch (activeView) {
      case 'dashboard':
        return <Dashboard stats={stats} events={events} />;
      case 'leads':
        return <LeadsPanel />;
      case 'tickets':
        return <TicketsPanel />;
      case 'agent':
        return <AgentConsole />;
      case 'activity':
        return <ActivityFeed events={events} />;
      default:
        return <Dashboard stats={stats} events={events} />;
    }
  };

  return (
    <div className="app-layout">
      {/* Sidebar */}
      <aside className="sidebar">
        <div className="sidebar-logo">
          <h1>NexusCRM</h1>
          <span>Agentic AI Platform</span>
        </div>
        <nav className="sidebar-nav">
          {navItems.map((item) => (
            <button
              key={item.id}
              className={`nav-item ${activeView === item.id ? 'active' : ''}`}
              onClick={() => setActiveView(item.id)}
            >
              <item.icon />
              {item.label}
            </button>
          ))}
        </nav>

        {/* Connection Status */}
        <div style={{
          padding: 'var(--space-lg)',
          borderTop: '1px solid var(--border-subtle)',
          fontSize: '0.75rem',
          color: 'var(--text-muted)',
        }}>
          <div className="live-dot">SSE Connected</div>
        </div>
      </aside>

      {/* Main Content */}
      <main className="main-content">
        {renderContent()}
      </main>
    </div>
  );
}

export default App;
