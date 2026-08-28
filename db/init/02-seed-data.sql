-- =============================================================================
-- NexusCRM Seed Data
-- =============================================================================
-- Realistic demo data so the system works out-of-the-box on docker compose up.
-- Company policies are inserted WITHOUT embeddings here; the AI service's
-- seed_policies.py script generates and writes embeddings at startup.
-- =============================================================================

-- =============================================================================
-- SEED USERS (password_hash is bcrypt of 'password123')
-- =============================================================================
INSERT INTO users (username, email, password_hash, role) VALUES
    ('admin',       'admin@nexuscrm.io',        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN'),
    ('sarah.chen',  'sarah.chen@nexuscrm.io',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'AGENT'),
    ('james.wilson','james.wilson@nexuscrm.io', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'AGENT'),
    ('emily.davis', 'emily.davis@nexuscrm.io',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'VIEWER');

-- =============================================================================
-- SEED LEADS
-- =============================================================================
INSERT INTO leads (first_name, last_name, email, phone, company, status, source, assigned_to, notes) VALUES
    ('Alex',     'Thompson',  'alex.t@techstart.io',       '+1-415-555-0101', 'TechStart Inc.',        'NEW',        'WEBSITE',        2, 'Interested in enterprise plan. Visited pricing page 3 times.'),
    ('Maria',    'Garcia',    'maria.g@globalretail.com',  '+1-212-555-0202', 'Global Retail Corp',    'CONTACTED',  'REFERRAL',       2, 'Referred by existing client. Needs CRM for 500+ person sales team.'),
    ('David',    'Kim',       'david.k@innovateai.dev',    '+1-650-555-0303', 'InnovateAI',            'QUALIFIED',  'SOCIAL_MEDIA',   3, 'CTO at AI startup. Looking for API-first CRM solution.'),
    ('Priya',    'Patel',     'priya.p@healthplus.org',    '+1-312-555-0404', 'HealthPlus Solutions',  'NEW',        'EMAIL_CAMPAIGN', 2, 'Healthcare sector. Needs HIPAA-compliant features.'),
    ('Michael',  'Brown',     'michael.b@construct.co',    '+1-713-555-0505', 'ConstructCo',           'CONTACTED',  'COLD_CALL',      3, 'Construction company, 200 employees. Budget approved for Q3.'),
    ('Sophie',   'Laurent',   'sophie.l@eurofinance.eu',   '+33-1-5555-0606', 'EuroFinance SA',        'QUALIFIED',  'WEBSITE',        2, 'French fintech. Interested in multi-currency support.'),
    ('Raj',      'Krishnan',  'raj.k@nexgensoft.in',       '+91-80-5555-0707','NexGen Software',       'NEW',        'REFERRAL',       3, 'India-based SaaS company. Evaluating 3 CRM vendors.'),
    ('Lisa',     'Anderson',  'lisa.a@greenearth.eco',     '+1-503-555-0808', 'GreenEarth Renewables', 'LOST',       'SOCIAL_MEDIA',   2, 'Went with competitor. May revisit in 6 months.'),
    ('Carlos',   'Rivera',    'carlos.r@mediapulse.mx',    '+52-55-5555-0909','MediaPulse',            'CONVERTED',  'WEBSITE',        3, 'Successfully onboarded. Annual enterprise contract signed.'),
    ('Yuki',     'Tanaka',    'yuki.t@sakuratech.jp',      '+81-3-5555-1010', 'SakuraTech',            'CONTACTED',  'EMAIL_CAMPAIGN', 2, 'Japanese tech firm. Needs bilingual support dashboard.');

-- =============================================================================
-- SEED TICKETS
-- =============================================================================
INSERT INTO tickets (subject, description, status, priority, category, customer_email, assigned_to, resolution, ai_handled) VALUES
    ('Cannot access billing dashboard',
     'I''ve been trying to access the billing section of my account for the past 2 hours but keep getting a 403 error. My subscription is active and paid through December.',
     'OPEN', 'HIGH', 'SUPPORT', 'alex.t@techstart.io', 2, NULL, FALSE),

    ('Request for bulk data export',
     'We need to export all our lead data from the past 12 months in CSV format for our quarterly board review. Is there a bulk export feature?',
     'OPEN', 'MEDIUM', 'GENERAL', 'maria.g@globalretail.com', 3, NULL, FALSE),

    ('API rate limit hitting too early',
     'Our integration is hitting the 1000 req/min API rate limit during peak hours (9-11 AM EST). We''re on the Enterprise plan which should allow 5000 req/min. Please investigate.',
     'IN_PROGRESS', 'CRITICAL', 'SUPPORT', 'david.k@innovateai.dev', 2, NULL, FALSE),

    ('Refund request for double charge',
     'I was charged twice for my monthly subscription on June 15th. Transaction IDs: TXN-44821 and TXN-44822. Please refund the duplicate charge of $299.00.',
     'OPEN', 'HIGH', 'REFUND', 'priya.p@healthplus.org', NULL, NULL, FALSE),

    ('Feature request: Kanban board view',
     'Would love to see a Kanban-style board view for managing leads through pipeline stages. This would greatly improve our team''s workflow visualization.',
     'OPEN', 'LOW', 'GENERAL', 'michael.b@construct.co', NULL, NULL, FALSE),

    ('Shipping delay on hardware order',
     'Ordered 5 NexusCRM hardware tokens (Order #ORD-7891) on June 1st with express shipping. It''s been 15 business days and still hasn''t arrived. Tracking shows stuck in transit.',
     'OPEN', 'MEDIUM', 'SHIPPING', 'sophie.l@eurofinance.eu', 3, NULL, FALSE),

    ('SSL certificate warning on custom domain',
     'Getting browser warnings about an invalid SSL certificate when accessing our custom domain crm.nexgensoft.in. This started happening after the last maintenance window.',
     'RESOLVED', 'HIGH', 'SUPPORT', 'raj.k@nexgensoft.in', 2,
     'SSL certificate was not auto-renewed due to DNS validation failure. Manually renewed and verified. Customer confirmed access restored.',
     FALSE),

    ('Need warranty replacement for defective unit',
     'One of our 3 NexusCRM terminal units (Serial: NCT-2024-0847) stopped powering on after 4 months of use. Still under 12-month warranty. Requesting replacement unit.',
     'OPEN', 'MEDIUM', 'WARRANTY', 'carlos.r@mediapulse.mx', NULL, NULL, FALSE);

-- =============================================================================
-- SEED COMPANY POLICIES (without embeddings — seeded by ai-service at startup)
-- =============================================================================
INSERT INTO company_policies (title, content, category, metadata) VALUES
    ('Standard Refund Policy',
     'NexusCRM offers a full refund within 30 days of purchase for any reason. After 30 days, refunds are prorated based on remaining subscription time. Refunds for annual plans are calculated at the monthly rate minus a 10% early termination fee. Double charges and billing errors are always refunded in full within 3-5 business days regardless of the time elapsed. To process a refund, the support agent must verify the customer''s account, confirm the transaction ID, and submit the refund through the billing dashboard. Refunds over $1,000 require manager approval.',
     'REFUND',
     '{"version": "2.1", "effective_date": "2024-01-01", "approved_by": "CFO"}'),

    ('Product Warranty Terms',
     'All NexusCRM hardware products come with a standard 12-month manufacturer warranty covering defects in materials and workmanship. The warranty does not cover damage from misuse, unauthorized modifications, or natural disasters. Warranty claims must include the product serial number, purchase date, and a description of the defect. Approved warranty replacements are shipped within 5-7 business days via standard shipping at no cost. Extended warranty options (24-month and 36-month) are available for purchase within 30 days of the original product purchase. For warranty claims, agents should create a replacement order and reference the original order number.',
     'WARRANTY',
     '{"version": "1.5", "effective_date": "2024-03-15", "approved_by": "VP Operations"}'),

    ('Shipping and Delivery Policy',
     'Standard shipping is free for all orders over $50 and takes 5-7 business days within the continental US. Express shipping ($25) delivers within 2-3 business days. International shipping rates vary by destination and typically take 10-15 business days. All shipments include tracking numbers sent via email within 24 hours of dispatch. If a shipment is delayed beyond the estimated delivery window by more than 5 business days, customers are entitled to a full shipping refund and a 10% discount on their next order. Lost packages are investigated and either reshipped or refunded within 10 business days of the claim.',
     'SHIPPING',
     '{"version": "3.0", "effective_date": "2024-02-01", "approved_by": "Logistics Director"}'),

    ('Enterprise Pricing Tiers',
     'NexusCRM offers three pricing tiers: Starter ($49/month, up to 10 users, 1,000 API calls/min), Professional ($149/month, up to 50 users, 3,000 API calls/min), and Enterprise ($299/month, unlimited users, 5,000 API calls/min). Annual billing provides a 20% discount. All plans include basic support. Professional and Enterprise plans include priority support with 4-hour response SLA. Enterprise plans include a dedicated account manager, custom integrations, and 99.9% uptime SLA. Volume discounts available for 100+ user deployments — contact sales for custom pricing.',
     'PRICING',
     '{"version": "4.2", "effective_date": "2024-06-01", "approved_by": "VP Sales"}'),

    ('Service Level Agreement (SLA)',
     'NexusCRM commits to 99.9% uptime for Enterprise tier customers and 99.5% for Professional tier. Uptime is calculated monthly excluding scheduled maintenance windows (announced 72 hours in advance). SLA credits: <99.9% = 10% credit, <99.5% = 25% credit, <99.0% = 50% credit on monthly bill. Critical incidents (full service outage) have a 15-minute response time and 4-hour resolution target. High-priority incidents have a 1-hour response time and 8-hour resolution target. All SLA credits must be claimed within 30 days of the incident. Support ticket response times: Critical = 15 min, High = 1 hour, Medium = 4 hours, Low = 24 hours.',
     'SLA',
     '{"version": "2.0", "effective_date": "2024-01-01", "approved_by": "CTO"}'),

    ('Privacy and Data Protection Policy',
     'NexusCRM is committed to protecting customer data in compliance with GDPR, CCPA, and SOC 2 Type II standards. Customer data is encrypted at rest (AES-256) and in transit (TLS 1.3). Data retention: active account data is retained for the duration of the subscription plus 90 days. Upon account termination, customers may request a full data export within 30 days, after which data is permanently deleted. We do not sell or share customer data with third parties except as required by law. AI features process data in-memory and do not store conversation contents beyond the session. Customers can opt out of AI features at any time through their account settings.',
     'PRIVACY',
     '{"version": "1.8", "effective_date": "2024-04-01", "approved_by": "DPO"}'),

    ('Customer Support Escalation Procedures',
     'Level 1 Support (AI Agent): Handles common queries using the knowledge base — password resets, billing inquiries, feature questions, and standard policy lookups. Resolution target: immediate. Level 2 Support (Human Agent): Handles complex issues escalated by the AI or directly by customers — technical troubleshooting, account modifications, and edge-case billing disputes. Resolution target: 4 hours. Level 3 Support (Engineering): Handles critical bugs, infrastructure issues, and security incidents escalated by Level 2. Resolution target: varies by severity. Customers can request human agent escalation at any time by saying "speak to a human" or "escalate this issue". The AI agent must always honor escalation requests immediately.',
     'SUPPORT',
     '{"version": "2.3", "effective_date": "2024-05-01", "approved_by": "VP Customer Success"}'),

    ('General Terms of Service',
     'By using NexusCRM, customers agree to our terms of service. Acceptable use: NexusCRM may only be used for lawful business purposes. Prohibited activities include data scraping, reverse engineering, and using the platform to send unsolicited communications. Account sharing is not permitted — each user must have their own licensed account. We reserve the right to suspend accounts that violate these terms after providing 48 hours written notice. Customers own all data they input into the system. NexusCRM retains a limited license to process this data solely for the purpose of providing the service. Disputes are resolved through binding arbitration in San Francisco, CA.',
     'GENERAL',
     '{"version": "5.1", "effective_date": "2024-01-01", "approved_by": "General Counsel"}');
