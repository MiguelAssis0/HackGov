-- Risco do evento de auditoria (BAIXO/MEDIO/ALTO). Nulo = registro legado,
-- que mantém o hash original (o canônico só inclui o risco quando presente).
alter table if exists audit_events
    add column if not exists risk varchar(10);
