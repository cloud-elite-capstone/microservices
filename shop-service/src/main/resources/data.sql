INSERT INTO shops (id, name, description, seller_id, location) VALUES
    ('00000000-0000-0000-0000-000000001001', 'TechNest Gadgets', 'Flagship electronics, laptops, and smart devices in Makati.', '00000000-0000-0000-0000-000000000001', ST_SetSRID(ST_MakePoint(121.0244, 14.5547), 4326)),
    ('00000000-0000-0000-0000-000000001002', 'Gadget Central', 'Wearables, audio, and everyday accessories in BGC.', '00000000-0000-0000-0000-000000000002', ST_SetSRID(ST_MakePoint(121.0499, 14.5555), 4326)),
    ('00000000-0000-0000-0000-000000001003', 'Urban Tech Hub', 'Home office tech, chargers, and peripherals in Quezon City.', '00000000-0000-0000-0000-000000000003', ST_SetSRID(ST_MakePoint(121.0509, 14.6760), 4326))
ON CONFLICT (id) DO NOTHING;
