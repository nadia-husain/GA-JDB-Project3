-- =============================================
-- PROFILES
-- =============================================
INSERT INTO profiles (first_name, last_name, phone_number, profile_pic) VALUES
('Alice', 'Johnson', 3001234567, NULL),
('Bob', 'Smith', 3009876543, NULL),
('Carol', 'White', 3005556789, NULL),
('David', 'Brown', 3004441234, NULL)
ON CONFLICT DO NOTHING;

-- =============================================
-- USERS
-- password is BCrypt hash of 'Password1!'
-- =============================================
INSERT INTO users (email_address, password, role, verified, user_status, profile_id) VALUES
('admin@petadoption.com',  '$2a$10$NdDlUGUSzqi2ltWv6nBqMeEADWcFYdWpdbukSVbrEzwCxHttuiAq2', 'ADMIN',    TRUE, 'ACTIVE', 1),
('alice@example.com',      '$2a$10$NdDlUGUSzqi2ltWv6nBqMeEADWcFYdWpdbukSVbrEzwCxHttuiAq2', 'CUSTOMER', TRUE, 'ACTIVE', 2),
('bob@example.com',        '$2a$10$NdDlUGUSzqi2ltWv6nBqMeEADWcFYdWpdbukSVbrEzwCxHttuiAq2', 'CUSTOMER', TRUE, 'ACTIVE', 3),
('carol@example.com',      '$2a$10$NdDlUGUSzqi2ltWv6nBqMeEADWcFYdWpdbukSVbrEzwCxHttuiAq2', 'CUSTOMER', FALSE,'ACTIVE', 4)
ON CONFLICT DO NOTHING;

-- =============================================
-- PETS
-- =============================================
INSERT INTO pet (name, type, age, photo, pet_status, created_at, updated_at) VALUES
('Buddy',   'Dog', 3,  NULL, 'AVAILABLE',   NOW(), NOW()),
('Whiskers','Cat', 5,  NULL, 'AVAILABLE',   NOW(), NOW()),
('Max',     'Dog', 2,  NULL, 'AVAILABLE',   NOW(), NOW()),
('Luna',    'Cat', 1,  NULL, 'UNAVAILABLE', NOW(), NOW()),
('Charlie', 'Dog', 4,  NULL, 'AVAILABLE',   NOW(), NOW()),
('Mittens', 'Cat', 3,  NULL, 'AVAILABLE',   NOW(), NOW())
ON CONFLICT DO NOTHING;

-- =============================================
-- ADOPTION REQUESTS
-- =============================================
INSERT INTO adoption_request (pet_id, user_id, status, created_at, updated_at) VALUES
(4, 2, 'PENDING',  NOW(), NOW()),
(2, 3, 'APPROVED', NOW(), NOW())
ON CONFLICT DO NOTHING;

-- =============================================
-- VOLUNTEER EVENTS
-- =============================================
INSERT INTO volunteer_event (task, location, date, capacity, is_full, created_at, updated_at, user_id, version) VALUES
('Dog walking',         'Central Park Shelter',   '2025-06-01 09:00:00', 10, FALSE, NOW(), NOW(), 1, 0),
('Cat socialization',   'Downtown Cat Cafe',       '2025-06-05 10:00:00', 5,  FALSE, NOW(), NOW(), 1, 0),
('Shelter cleaning',    'North Side Shelter',      '2025-06-10 08:00:00', 8,  FALSE, NOW(), NOW(), 1, 0),
('Adoption event help', 'City Convention Center',  '2025-06-15 11:00:00', 1,  TRUE,  NOW(), NOW(), 1, 0)
ON CONFLICT DO NOTHING;

-- =============================================
-- VOLUNTEERS
-- =============================================
INSERT INTO volunteer (user_id, volunteer_event_id, has_attended, created_at, updated_at) VALUES
(2, 1, FALSE, NOW(), NOW()),
(3, 1, FALSE, NOW(), NOW()),
(2, 2, FALSE, NOW(), NOW()),
(3, 3, TRUE,  NOW(), NOW())
ON CONFLICT DO NOTHING;