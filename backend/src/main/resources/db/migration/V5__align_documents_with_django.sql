alter table municipal_documents add column if not exists kind varchar(20) not null default 'SEND';
alter table municipal_documents add column if not exists number varchar(30);
alter table municipal_documents add column if not exists document_year integer;
alter table municipal_documents add column if not exists document_date date;
alter table municipal_documents add column if not exists purpose varchar(30);
alter table municipal_documents add column if not exists keywords varchar(255) not null default '';
alter table municipal_documents add column if not exists tags varchar(255) not null default '';
alter table municipal_documents add column if not exists structured_content text not null default '';
alter table municipal_documents add column if not exists source_document_id uuid;
alter table municipal_documents drop constraint if exists municipal_documents_signature_status_check;
alter table municipal_documents add constraint municipal_documents_signature_status_check
    check (signature_status in ('NONE', 'PENDING', 'HOMOLOGATION', 'SIGNED'));
alter table municipal_documents add constraint municipal_documents_source_document_fk
    foreign key (source_document_id) references municipal_documents(id);
