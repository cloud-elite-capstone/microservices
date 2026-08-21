CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE IF NOT EXISTS categories (
    id          uuid PRIMARY KEY,
    name        varchar(255) NOT NULL,
    description varchar(255)
);

CREATE TABLE IF NOT EXISTS products (
    id           uuid PRIMARY KEY,
    name         varchar(255) NOT NULL,
    description  varchar(255),
    price        numeric(38, 2),
    shipping_fee numeric(38, 2),
    quantity     integer NOT NULL,
    location     geometry(Point, 4326),
    category_id  uuid,
    image_url    varchar(255),
    shop_id      uuid
);
