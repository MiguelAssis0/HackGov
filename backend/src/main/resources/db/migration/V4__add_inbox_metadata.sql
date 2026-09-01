alter table if exists inbox_entries
    add column if not exists metadata text default '{}';
