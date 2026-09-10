-- Backfill tardio: mocks inseridos após a V8 ficaram com city_hall_id nulo.
update job_levels occupation
set city_hall_id = sector.city_hall_id
from sectors sector
where occupation.sector_id_id = sector.id
  and occupation.city_hall_id is null;
