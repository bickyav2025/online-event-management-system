-- EventFlow Initial Data Seed V2

-- Categories
INSERT INTO event_categories (id, name, slug, description, icon, is_active) VALUES
(1, 'Technology', 'technology', 'Tech summits, AI expos, coding workshops, and hackathons', 'bi-laptop', TRUE),
(2, 'Music', 'music', 'Concerts, acoustic evenings, electronic festivals, and symphony', 'bi-music-note-beamed', TRUE),
(3, 'Business', 'business', 'Entrepreneurship conferences, venture networking, and leadership summits', 'bi-briefcase', TRUE),
(4, 'Education', 'education', 'Academic seminars, masterclasses, webinars, and career bootcamps', 'bi-mortarboard', TRUE),
(5, 'Sports', 'sports', 'Marathons, tournaments, fitness conventions, and e-sports championships', 'bi-trophy', TRUE),
(6, 'Arts & Culture', 'arts-culture', 'Art exhibitions, theatrical performances, literary festivals, and comedy', 'bi-palette', TRUE);

-- System Settings
INSERT INTO system_settings (setting_key, setting_value, category, description) VALUES
('platform_name', 'EventFlow Online Event Management', 'general', 'The public brand name of the platform'),
('contact_email', 'support@eventflow.com', 'contact', 'Public contact inquiry email'),
('currency', 'USD', 'billing', 'Default system transaction currency'),
('currency_symbol', '$', 'billing', 'Currency visual symbol'),
('registration_allowed', 'true', 'policy', 'Allow public registration for new users'),
('auto_approve_events', 'false', 'policy', 'Require administrator review before event publication'),
('max_tickets_per_user', '5', 'booking', 'Default ticket purchase ceiling per order');
