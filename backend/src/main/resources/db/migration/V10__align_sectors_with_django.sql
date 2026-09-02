alter table sectors add column if not exists slug varchar(140);

update sectors
set slug = trim(both '-' from regexp_replace(lower(unaccent(name)), '[^a-z0-9]+', '-', 'g'))
where slug is null or trim(slug) = '';

create unique index if not exists sectors_city_slug_uk on sectors (city_hall_id, slug) where slug is not null;
