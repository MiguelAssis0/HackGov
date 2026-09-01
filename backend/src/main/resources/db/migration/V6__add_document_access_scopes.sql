create table if not exists municipal_document_related_sectors (
    document_id uuid not null references municipal_documents(id) on delete cascade,
    sector_id uuid not null references sectors(id) on delete cascade,
    primary key (document_id, sector_id)
);

create table if not exists municipal_document_related_employees (
    document_id uuid not null references municipal_documents(id) on delete cascade,
    employee_id uuid not null references employees(id) on delete cascade,
    primary key (document_id, employee_id)
);

create table if not exists municipal_document_related_occupations (
    document_id uuid not null references municipal_documents(id) on delete cascade,
    occupation_id uuid not null references job_levels(id) on delete cascade,
    primary key (document_id, occupation_id)
);
