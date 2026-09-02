create extension if not exists unaccent;

update job_levels
set slug = trim(both '-' from regexp_replace(lower(unaccent(name)), '[^a-z0-9]+', '-', 'g'))
where slug is null or trim(slug) = '';
