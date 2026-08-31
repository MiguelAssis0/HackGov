create table if not exists tool_categories
(
    active
    boolean
    not
    null,
    display_order
    integer
    not
    null,
    city_hall_id
    uuid
    not
    null,
    id
    uuid
    not
    null,
    icon
    varchar
(
    80
) not null,
    slug varchar
(
    80
) not null,
    name varchar
(
    120
) not null,
    description varchar
(
    500
),
    primary key
(
    id
),
    constraint tool_category_city_slug_uk unique
(
    city_hall_id,
    slug
),
    constraint tool_category_city_fk foreign key
(
    city_hall_id
) references cityhalls
    );

alter table tool_configurations
    add column if not exists custom_category_id uuid;
alter table tool_configurations
    add constraint tool_configuration_category_fk
        foreign key (custom_category_id) references tool_categories on delete set null;
