alter table job_levels add column if not exists slug varchar(140);
alter table job_levels add column if not exists active boolean not null default true;
alter table job_levels add column if not exists city_hall_id uuid;

update job_levels occupation
set city_hall_id = sector.city_hall_id
from sectors sector
where occupation.sector_id_id = sector.id
  and occupation.city_hall_id is null;

create index if not exists job_levels_city_hall_idx on job_levels (city_hall_id);
create unique index if not exists job_levels_city_sector_slug_uk
    on job_levels (city_hall_id, sector_id_id, slug)
    where slug is not null;

do $$
begin
    if not exists (
        select 1 from pg_constraint where conname = 'job_levels_city_hall_fk'
    ) then
        alter table job_levels
            add constraint job_levels_city_hall_fk foreign key (city_hall_id) references cityhalls(id);
    end if;
end $$;
