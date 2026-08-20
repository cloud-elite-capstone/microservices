INSERT INTO categories (id, name, description) VALUES
    ('00000000-0000-0000-0000-000000002001', 'Electronics', 'Phones, laptops, and gadgets'),
    ('00000000-0000-0000-0000-000000002002', 'Storage & Accessories', 'Storage, power, and device accessories'),
    ('00000000-0000-0000-0000-000000002003', 'Wearables', 'Smartwatches and fitness trackers'),
    ('00000000-0000-0000-0000-000000002004', 'Audio', 'Speakers, buds, and headsets')
ON CONFLICT (id) DO NOTHING;

INSERT INTO products (id, name, description, price, shipping_fee, quantity, location, category_id, image_url, shop_id) VALUES
    ('00000000-0000-0000-0000-000000003001', 'Cartesian 120 Storage', 'High-speed network attached storage', 7800, 150, 25, ST_SetSRID(ST_MakePoint(121.0251, 14.5550), 4326), '00000000-0000-0000-0000-000000002002', '/test-images/image1.jpg', '00000000-0000-0000-0000-000000001001'),
    ('00000000-0000-0000-0000-000000003002', 'Smart Watch Elite Series', 'Health tracking & AMOLED display', 4500, 100, 40, ST_SetSRID(ST_MakePoint(121.0241, 14.5542), 4326), '00000000-0000-0000-0000-000000002003', '/test-images/image2.png', '00000000-0000-0000-0000-000000001001'),
    ('00000000-0000-0000-0000-000000003003', 'Active Pro Smartwatch', 'Waterproof sports edition with GPS', 3200, 100, 35, ST_SetSRID(ST_MakePoint(121.0493, 14.5551), 4326), '00000000-0000-0000-0000-000000002003', '/test-images/image3.png', '00000000-0000-0000-0000-000000001002'),
    ('00000000-0000-0000-0000-000000003004', 'Ultra Power Storage Hub', 'Compact battery pack with fast charging', 2100, 80, 60, ST_SetSRID(ST_MakePoint(121.0249, 14.5539), 4326), '00000000-0000-0000-0000-000000002002', '/test-images/image4.png', '00000000-0000-0000-0000-000000001001'),
    ('00000000-0000-0000-0000-000000003005', 'Ergo Fold Stand Case', 'Premium lightweight aluminum construction', 1650, 80, 50, ST_SetSRID(ST_MakePoint(121.0512, 14.6753), 4326), '00000000-0000-0000-0000-000000002002', '/test-images/image5.png', '00000000-0000-0000-0000-000000001003'),
    ('00000000-0000-0000-0000-000000003006', 'Ambient Smart Speaker', '360-degree spatial audio & voice assistant', 3900, 120, 30, ST_SetSRID(ST_MakePoint(121.0238, 14.5553), 4326), '00000000-0000-0000-0000-000000002004', '/test-images/image6.png', '00000000-0000-0000-0000-000000001001'),
    ('00000000-0000-0000-0000-000000003007', 'Edge HD Smartphone', 'OLED display with dual camera setup', 18500, 200, 15, ST_SetSRID(ST_MakePoint(121.0253, 14.5544), 4326), '00000000-0000-0000-0000-000000002001', '/test-images/image7.png', '00000000-0000-0000-0000-000000001001'),
    ('00000000-0000-0000-0000-000000003008', 'Slim Pro Laptop 15', 'Retina display & all-day battery life', 42000, 300, 10, ST_SetSRID(ST_MakePoint(121.0242, 14.5549), 4326), '00000000-0000-0000-0000-000000002001', '/test-images/image8.png', '00000000-0000-0000-0000-000000001001'),
    ('00000000-0000-0000-0000-000000003009', 'Cartesian Pro Wireless Buds', 'Active noise cancelling with deep bass', 5400, 90, 45, ST_SetSRID(ST_MakePoint(121.0502, 14.5558), 4326), '00000000-0000-0000-0000-000000002004', '/test-images/image1.jpg', '00000000-0000-0000-0000-000000001002'),
    ('00000000-0000-0000-0000-000000003010', 'Studio Monitor Headset X', 'Hi-Res spatial acoustics & memory foam', 6200, 110, 20, ST_SetSRID(ST_MakePoint(121.0505, 14.5549), 4326), '00000000-0000-0000-0000-000000002004', '/test-images/image2.png', '00000000-0000-0000-0000-000000001002'),
    ('00000000-0000-0000-0000-000000003011', 'Precision Wireless Mouse', 'Ergonomic multi-device tracking sensor', 1850, 70, 70, ST_SetSRID(ST_MakePoint(121.0504, 14.6764), 4326), '00000000-0000-0000-0000-000000002001', '/test-images/image3.png', '00000000-0000-0000-0000-000000001003'),
    ('00000000-0000-0000-0000-000000003012', 'Dual Turbo 65W GaN Charger', 'Ultra-compact fast power distribution', 1250, 70, 80, ST_SetSRID(ST_MakePoint(121.0514, 14.6761), 4326), '00000000-0000-0000-0000-000000002001', '/test-images/image4.png', '00000000-0000-0000-0000-000000001003')
ON CONFLICT (id) DO NOTHING;
