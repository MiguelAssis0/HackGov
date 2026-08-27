update tool_configurations
set slug = 'relatorios',
    name = 'Relatórios',
    icon = 'bi-bar-chart-fill',
    description = 'Indicadores e relatórios gerenciais da prefeitura.',
    mandatory = false,
    route = '/gestao'
where slug = 'gestao'
  and not exists (
      select 1 from tool_configurations existing
      where existing.city_hall_id = tool_configurations.city_hall_id and existing.slug = 'relatorios'
  );

update user_tool_favorites set tool_slug = 'relatorios' where tool_slug = 'gestao';
update tool_permission_rules set tool_slug = 'relatorios' where tool_slug = 'gestao';
