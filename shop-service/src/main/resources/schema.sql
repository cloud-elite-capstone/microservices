CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE IF NOT EXISTS shops (
    id          uuid PRIMARY KEY,
    name        varchar(255) NOT NULL,
    description varchar(255),
    seller_id   uuid NOT NULL,
    location    geometry(Point, 4326)
);
