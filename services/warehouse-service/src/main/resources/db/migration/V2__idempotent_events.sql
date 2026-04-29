alter table stock_reservations
    add column released boolean not null default false;

create table processed_events (
    event_id uuid primary key,
    event_type varchar(120) not null,
    processed_at timestamptz not null
);
