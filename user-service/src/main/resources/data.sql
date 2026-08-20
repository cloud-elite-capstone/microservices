INSERT INTO users (id, username, email, password_hash, sex) VALUES
    ('00000000-0000-0000-0000-000000000001', 'technest', 'seller.technest@example.com', 'demo-password', 'male'),
    ('00000000-0000-0000-0000-000000000002', 'gadgetcentral', 'seller.gadgetcentral@example.com', 'demo-password', 'female'),
    ('00000000-0000-0000-0000-000000000003', 'urbantech', 'seller.urbantech@example.com', 'demo-password', 'male'),
    ('00000000-0000-0000-0000-000000000101', 'juan_delacruz', 'juan.delacruz@example.com', 'demo-password', 'male'),
    ('00000000-0000-0000-0000-000000000102', 'maria_santos', 'maria.santos@example.com', 'demo-password', 'female'),
    ('00000000-0000-0000-0000-000000000103', 'pedro_reyes', 'pedro.reyes@example.com', 'demo-password', 'male')
ON CONFLICT (id) DO NOTHING;

INSERT INTO sellers (user_id) VALUES
    ('00000000-0000-0000-0000-000000000001'),
    ('00000000-0000-0000-0000-000000000002'),
    ('00000000-0000-0000-0000-000000000003')
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO buyers (user_id) VALUES
    ('00000000-0000-0000-0000-000000000101'),
    ('00000000-0000-0000-0000-000000000102'),
    ('00000000-0000-0000-0000-000000000103')
ON CONFLICT (user_id) DO NOTHING;
