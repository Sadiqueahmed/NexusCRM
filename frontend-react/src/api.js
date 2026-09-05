import axios from 'axios';

// API base URLs — configurable for Docker vs local development
const CRM_API = import.meta.env.VITE_CRM_API_URL || 'http://localhost:8080/api/v1';
const AI_API = import.meta.env.VITE_AI_API_URL || 'http://localhost:8000';

// =============================================================================
// CRM API Client (Spring Boot Backend)
// =============================================================================
const crmApi = axios.create({
  baseURL: CRM_API,
  headers: { 'Content-Type': 'application/json' },
});

// --- Leads ---
export const fetchLeads = (status) =>
  crmApi.get('/leads', { params: status ? { status } : {} });

export const fetchLeadById = (id) =>
  crmApi.get(`/leads/${id}`);

export const createLead = (data) =>
  crmApi.post('/leads', data);

export const updateLead = (id, data) =>
  crmApi.put(`/leads/${id}`, data);

export const deleteLead = (id) =>
  crmApi.delete(`/leads/${id}`);

// --- Tickets ---
export const fetchTickets = (status, priority) =>
  crmApi.get('/tickets', { params: { ...(status && { status }), ...(priority && { priority }) } });

export const fetchTicketById = (id) =>
  crmApi.get(`/tickets/${id}`);

export const createTicket = (data) =>
  crmApi.post('/tickets', data);

// --- Dashboard ---
export const fetchDashboardStats = () =>
  crmApi.get('/dashboard/stats');

// =============================================================================
// AI Service API Client (Python FastAPI)
// =============================================================================
const aiApi = axios.create({
  baseURL: AI_API,
  headers: { 'Content-Type': 'application/json' },
  timeout: 120000, // 2 min timeout for AI processing
});

export const sendAgentQuery = (query, context = null) =>
  aiApi.post('/api/agent/query', { query, context });

export const searchPolicies = (query, topK = 3) =>
  aiApi.post('/api/rag/search', { query, top_k: topK });

// =============================================================================
// SSE Event Stream
// =============================================================================
export const createSSEConnection = (onEvent) => {
  const eventSource = new EventSource(`${CRM_API}/events/stream`);

  // Listen for specific CRM event types
  const eventTypes = ['LEAD_UPDATED', 'TICKET_CREATED', 'TICKET_RESOLVED', 'AI_ACTION'];

  eventTypes.forEach((type) => {
    eventSource.addEventListener(type, (event) => {
      try {
        const data = JSON.parse(event.data);
        onEvent({ type, data, timestamp: new Date() });
      } catch (e) {
        console.error('Failed to parse SSE event:', e);
      }
    });
  });

  eventSource.onerror = (err) => {
    console.warn('SSE connection error, will auto-reconnect:', err);
  };

  return eventSource;
};

export { CRM_API, AI_API };
