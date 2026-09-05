import {
  Users, Ticket, CheckCircle, Bot, TrendingUp
} from 'lucide-react';
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip,
  PieChart, Pie, Cell, ResponsiveContainer
} from 'recharts';
import ActivityFeed from './ActivityFeed';

/**
 * Dashboard Overview — KPI cards, charts, and live activity feed.
 */
export default function Dashboard({ stats, events }) {
  // Default stats if not loaded yet
  const s = stats || {
    totalLeads: 0,
    totalTickets: 0,
    openTickets: 0,
    resolvedTickets: 0,
    aiHandledTickets: 0,
    leadsByStatus: {},
    ticketsByPriority: {},
  };

  // Chart data
  const leadsChartData = Object.entries(s.leadsByStatus || {}).map(([name, value]) => ({
    name,
    value,
  }));

  const priorityChartData = Object.entries(s.ticketsByPriority || {}).map(([name, value]) => ({
    name,
    value,
  }));

  const LEAD_COLORS = ['#6366f1', '#06b6d4', '#8b5cf6', '#10b981', '#f43f5e'];
  const PRIORITY_COLORS = ['#10b981', '#f59e0b', '#f97316', '#f43f5e'];

  const customTooltipStyle = {
    backgroundColor: '#1f2937',
    border: '1px solid rgba(255,255,255,0.1)',
    borderRadius: '8px',
    color: '#f1f5f9',
    fontSize: '0.8rem',
  };

  return (
    <>
      <div className="page-header">
        <h2>Dashboard Overview</h2>
        <p>Real-time CRM metrics and AI agent activity</p>
      </div>

      {/* KPI Cards */}
      <div className="kpi-grid">
        <div className="kpi-card indigo">
          <div className="kpi-icon">
            <Users size={20} />
          </div>
          <div className="kpi-value">{s.totalLeads}</div>
          <div className="kpi-label">Total Leads</div>
        </div>

        <div className="kpi-card cyan">
          <div className="kpi-icon">
            <Ticket size={20} />
          </div>
          <div className="kpi-value">{s.totalTickets}</div>
          <div className="kpi-label">Total Tickets</div>
        </div>

        <div className="kpi-card amber">
          <div className="kpi-icon">
            <TrendingUp size={20} />
          </div>
          <div className="kpi-value">{s.openTickets}</div>
          <div className="kpi-label">Open Tickets</div>
        </div>

        <div className="kpi-card emerald">
          <div className="kpi-icon">
            <CheckCircle size={20} />
          </div>
          <div className="kpi-value">{s.resolvedTickets}</div>
          <div className="kpi-label">Resolved</div>
        </div>

        <div className="kpi-card rose">
          <div className="kpi-icon">
            <Bot size={20} />
          </div>
          <div className="kpi-value">{s.aiHandledTickets}</div>
          <div className="kpi-label">AI Handled</div>
        </div>
      </div>

      {/* Charts + Activity Feed */}
      <div className="dashboard-grid">
        <div>
          <div className="charts-grid">
            {/* Leads by Status */}
            <div className="chart-card">
              <h3>Leads by Pipeline Status</h3>
              <ResponsiveContainer width="100%" height={250}>
                <BarChart data={leadsChartData}>
                  <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.06)" />
                  <XAxis
                    dataKey="name"
                    tick={{ fill: '#64748b', fontSize: 11 }}
                    axisLine={{ stroke: 'rgba(255,255,255,0.06)' }}
                  />
                  <YAxis
                    tick={{ fill: '#64748b', fontSize: 11 }}
                    axisLine={{ stroke: 'rgba(255,255,255,0.06)' }}
                  />
                  <Tooltip contentStyle={customTooltipStyle} />
                  <Bar dataKey="value" radius={[6, 6, 0, 0]}>
                    {leadsChartData.map((_, index) => (
                      <Cell key={index} fill={LEAD_COLORS[index % LEAD_COLORS.length]} />
                    ))}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            </div>

            {/* Tickets by Priority */}
            <div className="chart-card">
              <h3>Tickets by Priority</h3>
              <ResponsiveContainer width="100%" height={250}>
                <PieChart>
                  <Pie
                    data={priorityChartData}
                    cx="50%"
                    cy="50%"
                    innerRadius={60}
                    outerRadius={90}
                    paddingAngle={4}
                    dataKey="value"
                    label={({ name, value }) => `${name}: ${value}`}
                  >
                    {priorityChartData.map((_, index) => (
                      <Cell key={index} fill={PRIORITY_COLORS[index % PRIORITY_COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip contentStyle={customTooltipStyle} />
                </PieChart>
              </ResponsiveContainer>
            </div>
          </div>
        </div>

        {/* Live Activity Feed sidebar */}
        <ActivityFeed events={events} compact />
      </div>
    </>
  );
}
