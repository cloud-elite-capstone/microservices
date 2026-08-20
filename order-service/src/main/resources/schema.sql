CREATE TABLE IF NOT EXISTS orders (
    id           uuid PRIMARY KEY,
    shop_id      uuid NOT NULL,
    buyer_id     uuid NOT NULL,
    created_on   timestamp NOT NULL,
    status       varchar(255) NOT NULL,
    subtotal     numeric(38, 2) NOT NULL,
    shipping_fee numeric(38, 2) NOT NULL,
    total        numeric(38, 2) NOT NULL
);

CREATE TABLE IF NOT EXISTS order_items (
    id         uuid PRIMARY KEY,
    order_id   uuid NOT NULL,
    product_id uuid NOT NULL,
    subtotal   numeric(38, 2) NOT NULL,
    quantity   integer NOT NULL
);

CREATE TABLE IF NOT EXISTS transactions (
    id              uuid PRIMARY KEY,
    order_id        uuid NOT NULL,
    created_on      timestamp NOT NULL,
    payment_gateway varchar(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS payments (
    transaction_id uuid PRIMARY KEY REFERENCES transactions (id) ON DELETE CASCADE,
    amount         numeric(38, 2) NOT NULL
);

CREATE TABLE IF NOT EXISTS refunds (
    transaction_id uuid PRIMARY KEY REFERENCES transactions (id) ON DELETE CASCADE
);
