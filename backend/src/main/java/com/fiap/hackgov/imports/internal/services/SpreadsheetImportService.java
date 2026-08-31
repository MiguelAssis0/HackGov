package com.fiap.hackgov.imports.internal.services;

import com.fiap.hackgov.auth.internal.entities.enums.Roles;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.entities.Sector;
import com.fiap.hackgov.cityhall_management.internal.repositories.EmployeeRepository;
import com.fiap.hackgov.cityhall_management.internal.repositories.OccupationRepository;
import com.fiap.hackgov.cityhall_management.internal.repositories.SectorRepository;
import com.fiap.hackgov.imports.internal.DTOs.ImportDTOs.*;
import com.fiap.hackgov.imports.internal.entities.ImportBatch;
import com.fiap.hackgov.imports.internal.repositories.ImportBatchRepository;
import com.fiap.hackgov.shared.infra.exceptions.BusinessException;
import com.fiap.hackgov.shared.infra.exceptions.ResourceNotFoundException;
import com.fiap.hackgov.shared.infra.exceptions.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SpreadsheetImportService {
    private static final long MAX = 5L * 1024 * 1024;
    private static final int MAX_ROWS = 5000, MAX_COLS = 80;
    private final ImportBatchRepository batchRepository;
    private final SectorRepository sectorRepository;
    private final EmployeeRepository employeeRepository;
    private final OccupationRepository occupationRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper json;
    private static final Map<String, List<Field>> TARGETS = Map.of("departments", List.of(new Field("nome", true, List.of("setor", "departamento", "secretaria")), new Field("descricao", false, List.of("descricao", "observacao", "detalhes")), new Field("ativo", false, List.of("status", "situacao"))), "employees", List.of(new Field("nome", true, List.of("nome completo", "servidor", "funcionario")), new Field("email", true, List.of("email institucional", "e-mail", "mail")), new Field("cpf", false, List.of("documento", "cpf servidor")), new Field("numero_de_registro", false, List.of("matricula", "registro")), new Field("setor", false, List.of("departamento", "secretaria", "lotacao")), new Field("cargo", false, List.of("funcao", "ocupacao")), new Field("carga_horaria", false, List.of("horas", "jornada")), new Field("salario", false, List.of("remuneracao", "vencimento")), new Field("admissao", false, List.of("data admissao", "contratacao"))));

    @Transactional
    public Preview preview(String target, MultipartFile file, Employee employee) {
        Employee current = admin(employee);
        List<Field> fields = TARGETS.get(target);
        if (fields == null) throw new BusinessException("Modulo de importacao indisponivel");
        validateFile(file);
        Parsed parsed = parse(file);
        ImportBatch batch = new ImportBatch();
        batch.setCityHall(current.getCityHallId());
        batch.setUploadedBy(current);
        batch.setOriginalFileName(safe(file.getOriginalFilename()));
        batch.setTargetModule(target);
        try {
            batch.setOriginalFile(file.getBytes());
            batch.setHeadersJson(json.writeValueAsString(parsed.headers));
            batch.setRowsJson(json.writeValueAsString(parsed.rows));
        } catch (Exception e) {
            throw new BusinessException("Nao foi possivel armazenar a planilha");
        }
        batch.setTotalRows(parsed.rows.size());
        batch.setIgnoredRows(parsed.ignored);
        batch = batchRepository.save(batch);
        Map<String, String> suggestion = new LinkedHashMap<>();
        for (Field field : fields) suggestion.put(field.name, best(parsed.headers, field));
        return new Preview(batch.getId(), batch.getOriginalFileName(), target, parsed.headers, parsed.rows.stream().limit(8).toList(), parsed.rows.size(), parsed.ignored, suggestion);
    }

    @Transactional
    public ValidationReport validate(UUID id, ValidateRequest request, Employee employee) {
        Employee current = admin(employee);
        ImportBatch batch = scoped(id, current);
        List<Field> fields = TARGETS.get(batch.getTargetModule());
        List<String> headers = readHeaders(batch);
        List<Row> rows = readRows(batch);
        List<RowError> errors = new ArrayList<>();
        List<String> missing = fields.stream().filter(Field::required).filter(f -> request.mapping().getOrDefault(f.name, "").isBlank()).map(Field::name).toList();
        missing.forEach(f -> errors.add(new RowError(null, f, "", "Campo obrigatorio nao foi mapeado.")));
        Set<Integer> duplicates = duplicates(rows);
        duplicates.forEach(n -> errors.add(new RowError(n, "linha", "", "Linha duplicada dentro da planilha.")));
        Map<String, Integer> indexes = new HashMap<>();
        for (int i = 0; i < headers.size(); i++) indexes.put(headers.get(i), i);
        for (Row row : rows)
            for (Field field : fields) {
                String header = request.mapping().get(field.name);
                if (header == null || header.isBlank()) continue;
                String value = value(row, indexes.get(header));
                if (field.required && value.isBlank())
                    errors.add(new RowError(row.number(), field.name, value, "Campo obrigatorio vazio."));
                if (field.name.equals("email") && !value.isBlank() && !value.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))
                    errors.add(new RowError(row.number(), field.name, value, "E-mail invalido."));
                if ((field.name.equals("salario") || field.name.equals("carga_horaria")) && !value.isBlank() && !decimal(value))
                    errors.add(new RowError(row.number(), field.name, value, "Numero invalido."));
                if (field.name.equals("admissao") && !value.isBlank() && date(value) == null)
                    errors.add(new RowError(row.number(), field.name, value, "Data invalida."));
                if (field.name.equals("setor") && !value.isBlank() && sectorRepository.findByNameAndCityHall_Id(value, city(current)).isEmpty())
                    errors.add(new RowError(row.number(), field.name, value, "Setor nao encontrado nesta prefeitura."));
                if (field.name.equals("cargo") && !value.isBlank() && occupationRepository.findFirstByNameIgnoreCaseAndSectorId_CityHall_Id(value, city(current)).isEmpty())
                    errors.add(new RowError(row.number(), field.name, value, "Cargo nao encontrado nesta prefeitura."));
            }
        Set<Integer> invalid = new HashSet<>(duplicates);
        errors.stream().map(RowError::rowNumber).filter(Objects::nonNull).forEach(invalid::add);
        if (!missing.isEmpty()) rows.forEach(r -> invalid.add(r.number()));
        Set<String> mapped = new HashSet<>(request.mapping().values());
        List<String> unknown = headers.stream().filter(h -> !mapped.contains(h)).toList();
        ValidationReport report = new ValidationReport(id, rows.size(), rows.size() - invalid.size(), invalid.size(), duplicates.size(), batch.getIgnoredRows(), unknown, missing, errors);
        batch.setImportMode(request.mode());
        batch.setMappingJson(write(request.mapping()));
        batch.setReportJson(write(report));
        batch.setStatus(errors.isEmpty() ? ImportBatch.Status.VALIDATED : ImportBatch.Status.VALIDATION_FAILED);
        batchRepository.save(batch);
        return report;
    }

    @Transactional
    public BatchResponse execute(UUID id, Employee employee) {
        Employee current = admin(employee);
        ImportBatch batch = scoped(id, current);
        if (batch.getImportMode() == null || batch.getMappingJson() == null)
            throw new BusinessException("Valide o mapeamento antes de importar");
        Map<String, String> mapping = read(batch.getMappingJson(), new TypeReference<>() {
        });
        List<String> headers = readHeaders(batch);
        Map<String, Integer> indexes = new HashMap<>();
        for (int i = 0; i < headers.size(); i++) indexes.put(headers.get(i), i);
        int created = 0, updated = 0, failed = 0;
        for (Row row : readRows(batch)) {
            try {
                Map<String, String> values = new HashMap<>();
                mapping.forEach((field, header) -> values.put(field, value(row, indexes.get(header))));
                boolean wasCreated = batch.getTargetModule().equals("departments") ? saveDepartment(values, batch.getImportMode(), current) : saveEmployee(values, batch.getImportMode(), current);
                if (wasCreated) created++;
                else updated++;
            } catch (Exception e) {
                failed++;
            }
        }
        batch.setCreatedRecords(created);
        batch.setUpdatedRecords(updated);
        batch.setSuccessfulRows(created + updated);
        batch.setFailedRows(failed);
        batch.setStatus(failed == 0 ? ImportBatch.Status.IMPORTED : ImportBatch.Status.IMPORT_FAILED);
        return response(batchRepository.save(batch));
    }

    @Transactional(readOnly = true)
    public List<BatchResponse> history(Employee employee) {
        Employee current = admin(employee);
        return batchRepository.findTop100ByCityHall_IdOrderByCreatedAtDesc(city(current)).stream().map(this::response).toList();
    }

    private boolean saveDepartment(Map<String, String> v, ImportBatch.Mode mode, Employee e) {
        String name = req(v, "nome");
        Optional<Sector> found = sectorRepository.findByNameAndCityHall_Id(name, city(e));
        if (mode == ImportBatch.Mode.CREATE && found.isPresent()) throw new BusinessException("Setor existente");
        if (mode == ImportBatch.Mode.UPDATE && found.isEmpty()) throw new BusinessException("Setor inexistente");
        Sector s = found.orElseGet(Sector::new);
        boolean created = s.getId() == null;
        s.setName(name);
        s.setDescription(v.getOrDefault("descricao", "").trim());
        s.setActive(bool(v.getOrDefault("ativo", "true")));
        s.setCityHall(e.getCityHallId());
        sectorRepository.save(s);
        return created;
    }

    private boolean saveEmployee(Map<String, String> v, ImportBatch.Mode mode, Employee e) {
        String email = req(v, "email").toLowerCase(Locale.ROOT);
        Optional<Employee> found = employeeRepository.findByEmail(email);
        if (mode == ImportBatch.Mode.CREATE && found.isPresent()) throw new BusinessException("Servidor existente");
        if (mode == ImportBatch.Mode.UPDATE && found.isEmpty()) throw new BusinessException("Servidor inexistente");
        Employee item = found.orElseGet(Employee::new);
        boolean created = item.getId() == null;
        String[] names = req(v, "nome").trim().split("\\s+", 2);
        item.setFirstName(names[0]);
        item.setLastName(names.length > 1 ? names[1] : "");
        item.setEmail(email);
        String cpf = digits(v.get("cpf"));
        if (!cpf.isBlank()) item.setCpf(cpf);
        item.setRegistrationNumber(v.getOrDefault("numero_de_registro", item.getRegistrationNumber()));
        item.setSalary(number(v.get("salario"), item.getSalary()));
        item.setHoursWorked(number(v.get("carga_horaria"), item.getHoursWorked()));
        LocalDate admission = date(v.get("admissao"));
        if (admission != null) item.setAdmissionDate(admission.atStartOfDay());
        String sector = v.getOrDefault("setor", "");
        if (!sector.isBlank())
            item.setSectorId(sectorRepository.findByNameAndCityHall_Id(sector, city(e)).orElseThrow());
        String occupation = v.getOrDefault("cargo", "");
        if (!occupation.isBlank())
            item.setOccupationId(occupationRepository.findFirstByNameIgnoreCaseAndSectorId_CityHall_Id(occupation, city(e)).orElseThrow());
        item.setCityHallId(e.getCityHallId());
        item.setRole(Roles.EMPLOYEE);
        item.setStatus(true);
        if (created) item.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        employeeRepository.save(item);
        return created;
    }

    private Parsed parse(MultipartFile file) {
        String name = safe(file.getOriginalFilename()).toLowerCase(Locale.ROOT);
        try {
            return name.endsWith(".csv") ? csv(file.getBytes()) : xlsx(file.getInputStream());
        } catch (Exception e) {
            throw new BusinessException("Planilha invalida: " + e.getMessage());
        }
    }

    private Parsed csv(byte[] bytes) {
        String text;
        try {
            text = new String(bytes, StandardCharsets.UTF_8);
            if (text.indexOf('\uFFFD') >= 0) text = new String(bytes, Charset.forName("ISO-8859-1"));
        } catch (Exception e) {
            throw new BusinessException("CSV invalido");
        }
        List<List<String>> raw = new ArrayList<>();
        for (String line : text.split("\\R")) raw.add(parseCsvLine(line));
        return parsed(raw);
    }

    private Parsed xlsx(InputStream input) throws Exception {
        List<List<String>> raw = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(input)) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter f = new DataFormatter();
            for (org.apache.poi.ss.usermodel.Row row : sheet) {
                List<String> values = new ArrayList<>();
                for (int i = 0; i < row.getLastCellNum(); i++)
                    values.add(f.formatCellValue(row.getCell(i, org.apache.poi.ss.usermodel.Row.MissingCellPolicy.RETURN_BLANK_AS_NULL)).trim());
                raw.add(values);
            }
        }
        return parsed(raw);
    }

    private Parsed parsed(List<List<String>> raw) {
        List<String> headers = null;
        List<Row> rows = new ArrayList<>();
        int ignored = 0;
        for (int i = 0; i < raw.size(); i++) {
            List<String> values = raw.get(i).stream().map(String::trim).toList();
            if (values.stream().allMatch(String::isBlank)) {
                if (headers != null) ignored++;
                continue;
            }
            if (headers == null) {
                headers = dedupe(values);
                if (headers.size() > MAX_COLS) throw new BusinessException("Limite de colunas excedido");
                continue;
            }
            List<String> padded = new ArrayList<>(values.stream().limit(headers.size()).toList());
            while (padded.size() < headers.size()) padded.add("");
            rows.add(new Row(i + 1, padded));
            if (rows.size() > MAX_ROWS) throw new BusinessException("Limite de linhas excedido");
        }
        if (headers == null) throw new BusinessException("Cabecalho nao encontrado");
        return new Parsed(headers, rows, ignored);
    }

    private void validateFile(MultipartFile f) {
        if (f == null || f.isEmpty()) throw new BusinessException("Selecione uma planilha");
        if (f.getSize() > MAX) throw new BusinessException("Limite de 5 MB excedido");
        String n = safe(f.getOriginalFilename()).toLowerCase(Locale.ROOT);
        if (!n.endsWith(".csv") && !n.endsWith(".xlsx")) throw new BusinessException("Envie um arquivo CSV ou XLSX");
    }

    private List<String> dedupe(List<String> values) {
        List<String> out = new ArrayList<>();
        Map<String, Integer> seen = new HashMap<>();
        for (int i = 0; i < values.size(); i++) {
            String base = values.get(i).isBlank() ? "Coluna " + (i + 1) : values.get(i);
            int count = seen.merge(base, 1, Integer::sum);
            out.add(count == 1 ? base : base + " (" + count + ")");
        }
        return out;
    }

    private List<String> parseCsvLine(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder cell = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    cell.append('"');
                    i++;
                } else quoted = !quoted;
            } else if ((c == ',' || c == ';') && !quoted) {
                out.add(cell.toString());
                cell.setLength(0);
            } else cell.append(c);
        }
        out.add(cell.toString());
        return out;
    }

    private Set<Integer> duplicates(List<Row> rows) {
        Map<List<String>, Integer> seen = new HashMap<>();
        Set<Integer> duplicate = new HashSet<>();
        for (Row r : rows) {
            List<String> key = r.values().stream().map(this::norm).toList();
            Integer prior = seen.putIfAbsent(key, r.number());
            if (prior != null) {
                duplicate.add(prior);
                duplicate.add(r.number());
            }
        }
        return duplicate;
    }

    private String best(List<String> headers, Field f) {
        return headers.stream().filter(h -> f.name.equals(norm(h)) || f.aliases.stream().map(this::norm).anyMatch(a -> a.equals(norm(h)))).findFirst().orElse("");
    }

    private String norm(String v) {
        return Normalizer.normalize(v == null ? "" : v, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT).replace('_', ' ').replace('-', ' ').trim().replaceAll("\\s+", " ");
    }

    private List<String> readHeaders(ImportBatch b) {
        return read(b.getHeadersJson(), new TypeReference<>() {
        });
    }

    private List<Row> readRows(ImportBatch b) {
        return read(b.getRowsJson(), new TypeReference<>() {
        });
    }

    private <T> T read(String value, TypeReference<T> type) {
        try {
            return json.readValue(value, type);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private String write(Object value) {
        try {
            return json.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private String value(Row r, Integer i) {
        return i == null || i < 0 || i >= r.values().size() ? "" : r.values().get(i).trim();
    }

    private String req(Map<String, String> v, String k) {
        String x = v.getOrDefault(k, "").trim();
        if (x.isBlank()) throw new BusinessException(k + " obrigatorio");
        return x;
    }

    private boolean decimal(String v) {
        try {
            Double.parseDouble(normalizeNumber(v));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Double number(String v, Double fallback) {
        return v == null || v.isBlank() ? fallback : Double.parseDouble(normalizeNumber(v));
    }

    private String normalizeNumber(String value) {
        String v = value.trim().replace(" ", "");
        if (v.contains(",") && v.contains("."))
            return v.lastIndexOf(',') > v.lastIndexOf('.') ? v.replace(".", "").replace(',', '.') : v.replace(",", "");
        return v.contains(",") ? v.replace(',', '.') : v;
    }

    private boolean bool(String v) {
        return Set.of("1", "sim", "s", "true", "ativo", "ativa", "yes").contains(norm(v));
    }

    private LocalDate date(String v) {
        if (v == null || v.isBlank()) return null;
        for (String pattern : List.of("yyyy-MM-dd", "dd/MM/yyyy", "dd-MM-yyyy", "yyyy/MM/dd"))
            try {
                return LocalDate.parse(v, DateTimeFormatter.ofPattern(pattern));
            } catch (Exception ignored) {
            }
        return null;
    }

    private String digits(String v) {
        return v == null ? "" : v.replaceAll("\\D", "");
    }

    private String safe(String n) {
        return n == null ? "planilha" : n.replace('\\', '/').substring(n.replace('\\', '/').lastIndexOf('/') + 1);
    }

    private UUID city(Employee e) {
        if (e.getCityHallId() == null) throw new BusinessException("Usuario sem prefeitura");
        return e.getCityHallId().getId();
    }

    private Employee admin(Employee e) {
        if (e == null) throw new UnauthorizedException("E necessario estar autenticado");
        if (!Roles.ADMIN.equals(e.getRole()))
            throw new UnauthorizedException("Somente administradores podem importar dados");
        city(e);
        return e;
    }

    private ImportBatch scoped(UUID id, Employee e) {
        return batchRepository.findByIdAndCityHall_Id(id, city(e)).orElseThrow(() -> new ResourceNotFoundException("Lote nao encontrado"));
    }

    private BatchResponse response(ImportBatch b) {
        return new BatchResponse(b.getId(), b.getOriginalFileName(), b.getTargetModule(), b.getImportMode(), b.getStatus(), b.getTotalRows(), b.getSuccessfulRows(), b.getFailedRows(), b.getCreatedRecords(), b.getUpdatedRecords(), b.getIgnoredRows(), b.getCreatedAt(), b.getUpdatedAt());
    }

    private record Parsed(List<String> headers, List<Row> rows, int ignored) {
    }

    private record Field(String name, boolean required, List<String> aliases) {
    }
}
