create table app_users (
    id uuid primary key,
    username varchar(120) not null unique,
    password_hash varchar(120) not null
);

create table app_user_roles (
    app_user_id uuid not null references app_users(id),
    roles varchar(60) not null,
    primary key (app_user_id, roles)
);

insert into app_users (id, username, password_hash) values
    ('11111111-1111-1111-1111-111111111111', 'customer', '{noop}password'),
    ('22222222-2222-2222-2222-222222222222', 'operator', '{noop}password'),
    ('33333333-3333-3333-3333-333333333333', 'admin', '{noop}password');

insert into app_user_roles (app_user_id, roles) values
    ('11111111-1111-1111-1111-111111111111', 'CUSTOMER'),
    ('22222222-2222-2222-2222-222222222222', 'WAREHOUSE_OPERATOR'),
    ('33333333-3333-3333-3333-333333333333', 'ADMIN'),
    ('33333333-3333-3333-3333-333333333333', 'CUSTOMER');
