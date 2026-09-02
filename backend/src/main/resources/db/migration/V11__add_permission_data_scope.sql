alter table tool_permission_rules
    add column if not exists data_scope varchar(30) not null default 'ALL_SECTORS';

create table if not exists tool_permission_visible_sectors
(
    permission_id uuid not null references tool_permission_rules (id) on delete cascade,
    sector_id uuid not null references sectors (id) on delete cascade,
    primary key (permission_id, sector_id)
);
