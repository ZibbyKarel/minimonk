create table products (
    id uuid primary key,
    sku varchar(80) not null unique,
    name varchar(160) not null,
    description text not null,
    price numeric(12,2) not null,
    available_quantity integer not null,
    reserved_quantity integer not null,
    version bigint not null
);

create table stock_reservations (
    id uuid primary key,
    order_id uuid not null,
    product_id uuid not null references products(id),
    quantity integer not null
);

create index idx_stock_reservations_order_id on stock_reservations(order_id);

insert into products (id, sku, name, description, price, available_quantity, reserved_quantity, version) values
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'BOT-LIFT-01', 'Autonomous lift bot', 'Compact warehouse lift robot for pallet movement.', 1499.00, 8, 0, 0),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2', 'SEN-GATE-02', 'Smart gate sensor', 'RFID and camera gate sensor for aisle checkpoints.', 249.00, 30, 0, 0),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa3', 'BIN-TRACK-03', 'Tracked storage bin', 'Reusable bin with location beacon and rugged label.', 39.00, 120, 0, 0);
