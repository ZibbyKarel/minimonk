create table customer_orders (
    id uuid primary key,
    customer_id uuid not null,
    payment_card_number varchar(32) not null,
    total_amount numeric(12,2) not null,
    status varchar(60) not null,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table customer_order_items (
    id uuid primary key,
    order_id uuid not null references customer_orders(id),
    product_id uuid not null,
    sku varchar(80) not null,
    name varchar(160) not null,
    quantity integer not null,
    unit_price numeric(12,2) not null
);
