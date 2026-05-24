import { useEffect, useMemo, useState } from "react";
import { DashboardLayout } from "../components/DashboardLayout.jsx";
import { EmptyState, FieldLabel, Modal, PageHeader, ProfileBadge, StatusBadge } from "../components/DashboardShared.jsx";
import { api, getSelectedCityHall, getStoredUser, getUserType } from "../services/api.js";

function pageItems(payload) {
  if (Array.isArray(payload)) return payload;
  return payload?.content || payload?.items || [];
}

function resolveCityHall() {
  const selected = getSelectedCityHall();
  if (selected?.id) return selected;

  const user = getStoredUser() || {};
  const cityHall = user?.cityHall;
  if (cityHall?.id) return cityHall;
  if (user?.cityHallId) return { id: user.cityHallId, name: user.prefeitura || "Prefeitura vinculada" };
  return null;
}

function resolveCityHallName() {
  const cityHall = resolveCityHall();
  return cityHall?.name || "Prefeitura vinculada";
}

function employeeName(employee) {
  return [employee?.firstName, employee?.lastName].filter(Boolean).join(" ") || employee?.email || "Funcionario";
}

function initials(name) {
  return String(name || "")
    .split(" ")
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0])
    .join("")
    .toUpperCase();
}

function roleLabel(role) {
  return String(role || "").includes("ADMIN") ? "Admin cidade" : "Servidor";
}

function normalizeEmployee(item) {
  return {
    id: item.id,
    name: employeeName(item),
    email: item.email || "",
    sector: item.sectorName || "-",
    job: item.occupationName || "-",
    profile: roleLabel(item.role),
    active: item.status ?? true,
    cpf: item.cpf || "",
    phone: item.phone || "",
    registration: item.registrationNumber || "",
  };
}

const emptyForm = {
  firstName: "",
  lastName: "",
  email: "",
  cpf: "",
  phone: "",
  password: "",
  registrationNumber: "",
  sectorId: "",
  occupationId: "",
  salary: "",
  hoursWorked: "",
  admissionDate: "",
};

function isValidEmail(value) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(String(value || "").trim());
}

function isValidCpf(value) {
  const digits = String(value || "").replace(/\D/g, "");
  if (digits.length !== 11 || /^(\d)\1{10}$/.test(digits)) return false;

  let sum = 0;
  for (let index = 0; index < 9; index += 1) {
    sum += Number(digits[index]) * (10 - index);
  }
  let remainder = (sum * 10) % 11;
  if (remainder === 10) remainder = 0;
  if (remainder !== Number(digits[9])) return false;

  sum = 0;
  for (let index = 0; index < 10; index += 1) {
    sum += Number(digits[index]) * (11 - index);
  }
  remainder = (sum * 10) % 11;
  if (remainder === 10) remainder = 0;
  return remainder === Number(digits[10]);
}

function isValidPhone(value) {
  const digits = String(value || "").replace(/\D/g, "");
  return digits.length >= 10 && digits.length <= 11;
}

function validateForm(form) {
  const errors = {};

  if (!form.firstName.trim()) errors.firstName = "Informe o nome.";
  if (!form.lastName.trim()) errors.lastName = "Informe o sobrenome.";
  if (!form.email.trim()) {
    errors.email = "Informe o email.";
  } else if (!isValidEmail(form.email)) {
    errors.email = "Informe um email valido.";
  }
  if (!form.password.trim()) errors.password = "Informe a senha inicial.";
  if (!form.cpf.trim()) {
    errors.cpf = "Informe o CPF.";
  } else if (!isValidCpf(form.cpf)) {
    errors.cpf = "Informe um CPF valido.";
  }
  if (!form.phone.trim()) {
    errors.phone = "Informe o celular.";
  } else if (!isValidPhone(form.phone)) {
    errors.phone = "Informe um celular valido com DDD.";
  }
  if (!/^\d{7}$/.test(form.registrationNumber.trim())) errors.registrationNumber = "O registro deve conter 7 digitos.";
  if (!form.salary || Number(form.salary) < 0) errors.salary = "Informe um salario valido.";
  if (!form.hoursWorked || Number(form.hoursWorked) < 0) errors.hoursWorked = "Informe uma carga horaria valida.";
  if (!form.admissionDate) errors.admissionDate = "Informe a data de admissao.";
  if (!form.sectorId) errors.sectorId = "Selecione um setor.";
  if (!form.occupationId) errors.occupationId = "Selecione um cargo.";

  return errors;
}

function validateField(field, form) {
  return validateForm(form)[field] || "";
}

export default function EmployeesPage() {
  const cityHall = resolveCityHall();
  const cityHallName = resolveCityHallName();
  const canManage = ["admin_cidade", "admin_equipe"].includes(getUserType(getStoredUser()));
  const [sectors, setSectors] = useState([]);
  const [occupations, setOccupations] = useState([]);
  const [employees, setEmployees] = useState([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [saving, setSaving] = useState(false);
  const [errors, setErrors] = useState({});
  const [search, setSearch] = useState("");
  const [sector, setSector] = useState("");
  const [form, setForm] = useState(emptyForm);

  useEffect(() => {
    let mounted = true;

    async function loadEmployeesPage() {
      setLoading(true);
      setMessage(null);

      const [employeesResult, sectorsResult, occupationsResult] = await Promise.allSettled([
        api.getEmployees(),
        api.getSectors(),
        api.getOccupations(),
      ]);

      if (!mounted) return;

      const nextEmployees =
        employeesResult.status === "fulfilled" ? pageItems(employeesResult.value).map(normalizeEmployee) : [];
      const nextSectors = sectorsResult.status === "fulfilled" ? pageItems(sectorsResult.value) : [];
      const nextOccupations = occupationsResult.status === "fulfilled" ? pageItems(occupationsResult.value) : [];
      const failures = [];

      if (employeesResult.status === "rejected") failures.push("funcionarios");
      if (sectorsResult.status === "rejected") failures.push("setores");
      if (occupationsResult.status === "rejected") failures.push("cargos");

      setEmployees(nextEmployees);
      setSectors(nextSectors);
      setOccupations(nextOccupations);
      setLoading(false);

      if (failures.length > 0) {
        setMessage({
          type: "warning",
          text: `Nao foi possivel carregar ${failures.join(", ")} no backend. A pagina exibira apenas os dados disponiveis.`,
        });
      }
    }

    loadEmployeesPage();
    return () => {
      mounted = false;
    };
  }, []);

  const filteredEmployees = useMemo(() => {
    const query = search.trim().toLowerCase();
    return employees.filter((employee) => {
      const matchesSearch = !query || [employee.name, employee.email].join(" ").toLowerCase().includes(query);
      const matchesSector = !sector || employee.sector === sector;
      return matchesSearch && matchesSector;
    });
  }, [employees, search, sector]);

  const availableOccupations = useMemo(() => {
    if (!form.sectorId) return occupations;
    return occupations.filter((occupation) => String(occupation.sectorId) === String(form.sectorId));
  }, [occupations, form.sectorId]);

  function openCreate() {
    setErrors({});
    setForm({
      ...emptyForm,
      sectorId: sectors[0]?.id || "",
      occupationId: "",
      admissionDate: new Date().toISOString().slice(0, 16),
    });
    setModalOpen(true);
  }

  function closeModal() {
    setModalOpen(false);
    setForm(emptyForm);
    setErrors({});
  }

  function updateForm(field, value) {
    setForm((current) => {
      const next = { ...current, [field]: value };
      setErrors((currentErrors) => ({
        ...currentErrors,
        [field]: validateField(field, next),
        form: "",
      }));
      return next;
    });
  }

  function touchField(field) {
    setErrors((current) => ({
      ...current,
      [field]: validateField(field, form),
    }));
  }

  async function saveEmployee(event) {
    event.preventDefault();
    const nextErrors = validateForm(form);
    setErrors(nextErrors);

    if (Object.keys(nextErrors).length > 0) {
      return;
    }

    if (!cityHall?.id) {
      setMessage({ type: "error", text: "Nao foi possivel identificar a prefeitura ativa para cadastrar o funcionario." });
      return;
    }

    setSaving(true);

    try {
      await api.createEmployee({
        cityHallId: cityHall.id,
        sectorId: form.sectorId,
        occupationId: form.occupationId,
        salary: Number(form.salary),
        admissionDate: `${form.admissionDate}:00`,
        registrationNumber: form.registrationNumber,
        hoursWorked: Number(form.hoursWorked),
        firstName: form.firstName,
        lastName: form.lastName,
        cpf: form.cpf,
        email: form.email,
        password: form.password,
        phone: form.phone,
      });

      const refreshed = await api.getEmployees();
      setEmployees(pageItems(refreshed).map(normalizeEmployee));
      setMessage({ type: "success", text: "Funcionario criado com sucesso." });
      closeModal();
    } catch (error) {
      setErrors((current) => ({ ...current, form: error.message || "Nao foi possivel cadastrar o funcionario." }));
      setMessage({ type: "error", text: error.message || "Nao foi possivel cadastrar o funcionario." });
    } finally {
      setSaving(false);
    }
  }

  return (
    <DashboardLayout styles={["/css/management.css"]}>
      <main className="dashboard">
        <div className="container">
          <PageHeader
            eyebrow={cityHallName}
            title="Funcionarios"
            action={
              canManage ? (
                <button className="btn btn-primary" type="button" onClick={openCreate}>
                  <i className="bi bi-person-plus-fill"></i> Novo funcionario
                </button>
              ) : null
            }
          />

          {message && (
            <div className={`auth-message ${message.type} mb-3`}>
              <i className={`bi ${message.type === "error" ? "bi-exclamation-circle-fill" : "bi-info-circle-fill"}`}></i>
              {message.text}
            </div>
          )}

          <section className="panel">
            <div className="row g-2 align-items-end mb-3">
              <div className="col-md-5">
                <FieldLabel>Buscar</FieldLabel>
                <input
                  className="form-control"
                  placeholder="Nome ou email"
                  value={search}
                  onChange={(event) => setSearch(event.target.value)}
                />
              </div>
              <div className="col-md-4">
                <FieldLabel>Setor</FieldLabel>
                <select className="form-select" value={sector} onChange={(event) => setSector(event.target.value)}>
                  <option value="">Todos</option>
                  {sectors.map((item) => (
                    <option value={item.name} key={item.id}>
                      {item.name}
                    </option>
                  ))}
                </select>
              </div>
              <div className="col-md-3">
                <button className="btn btn-primary w-100" type="button">
                  <i className="bi bi-search"></i> Filtrar
                </button>
              </div>
            </div>

            <div className="table-responsive">
              <table className="table align-middle management-table">
                <thead>
                  <tr>
                    <th>Funcionario</th>
                    <th>Setor</th>
                    <th>Cargo</th>
                    <th>Perfil</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {loading ? (
                    <tr>
                      <td colSpan={5}>
                        <EmptyState icon="bi-arrow-repeat">Carregando funcionarios...</EmptyState>
                      </td>
                    </tr>
                  ) : filteredEmployees.map((employee) => (
                    <tr key={employee.id}>
                      <td>
                        <div className="user-cell">
                          <span className="avatar small">{initials(employee.name)}</span>
                          <div>
                            <h4 className="mb-0">{employee.name}</h4>
                            <small>{employee.email}</small>
                          </div>
                        </div>
                      </td>
                      <td>{employee.sector || "-"}</td>
                      <td>{employee.job || "-"}</td>
                      <td>
                        <ProfileBadge variant={employee.profile === "Admin cidade" ? "primary" : "light"}>
                          {employee.profile}
                        </ProfileBadge>
                      </td>
                      <td>
                        <StatusBadge active={employee.active} />
                      </td>
                    </tr>
                  ))}

                  {!loading && filteredEmployees.length === 0 && (
                    <tr>
                      <td colSpan={5}>
                        <EmptyState icon="bi-people">Nenhum funcionario encontrado.</EmptyState>
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </section>
        </div>

        <Modal
          open={modalOpen}
          title="Novo funcionario"
          onClose={closeModal}
          size="lg"
          footer={
            <button className="btn btn-primary" type="submit" form="employeeForm" disabled={saving}>
              {saving ? "Salvando..." : "Cadastrar funcionario"}
            </button>
          }
        >
          <form id="employeeForm" onSubmit={saveEmployee} noValidate>
            {errors.form && <div className="alert alert-danger py-2">{errors.form}</div>}
            <div className="row g-3">
              <div className="col-md-6">
                <label className="form-label">Nome</label>
                <input
                  className={`form-control ${errors.firstName ? "is-invalid" : ""}`}
                  value={form.firstName}
                  onBlur={() => touchField("firstName")}
                  onChange={(event) => updateForm("firstName", event.target.value)}
                />
                {errors.firstName && <div className="invalid-feedback">{errors.firstName}</div>}
              </div>
              <div className="col-md-6">
                <label className="form-label">Sobrenome</label>
                <input
                  className={`form-control ${errors.lastName ? "is-invalid" : ""}`}
                  value={form.lastName}
                  onBlur={() => touchField("lastName")}
                  onChange={(event) => updateForm("lastName", event.target.value)}
                />
                {errors.lastName && <div className="invalid-feedback">{errors.lastName}</div>}
              </div>
              <div className="col-md-6">
                <label className="form-label">Email</label>
                <input
                  className={`form-control ${errors.email ? "is-invalid" : ""}`}
                  type="email"
                  value={form.email}
                  onBlur={() => touchField("email")}
                  onChange={(event) => updateForm("email", event.target.value)}
                />
                {errors.email && <div className="invalid-feedback">{errors.email}</div>}
              </div>
              <div className="col-md-6">
                <label className="form-label">Senha inicial</label>
                <input
                  className={`form-control ${errors.password ? "is-invalid" : ""}`}
                  type="password"
                  value={form.password}
                  onBlur={() => touchField("password")}
                  onChange={(event) => updateForm("password", event.target.value)}
                />
                {errors.password && <div className="invalid-feedback">{errors.password}</div>}
              </div>
              <div className="col-md-6">
                <label className="form-label">CPF</label>
                <input
                  className={`form-control ${errors.cpf ? "is-invalid" : ""}`}
                  value={form.cpf}
                  onBlur={() => touchField("cpf")}
                  onChange={(event) => updateForm("cpf", event.target.value)}
                />
                {errors.cpf && <div className="invalid-feedback">{errors.cpf}</div>}
              </div>
              <div className="col-md-6">
                <label className="form-label">Celular</label>
                <input
                  className={`form-control ${errors.phone ? "is-invalid" : ""}`}
                  value={form.phone}
                  onBlur={() => touchField("phone")}
                  onChange={(event) => updateForm("phone", event.target.value)}
                />
                {errors.phone && <div className="invalid-feedback">{errors.phone}</div>}
              </div>
              <div className="col-md-4">
                <label className="form-label">Registro</label>
                <input
                  className={`form-control ${errors.registrationNumber ? "is-invalid" : ""}`}
                  value={form.registrationNumber}
                  onBlur={() => touchField("registrationNumber")}
                  onChange={(event) => updateForm("registrationNumber", event.target.value)}
                />
                {errors.registrationNumber && <div className="invalid-feedback">{errors.registrationNumber}</div>}
              </div>
              <div className="col-md-4">
                <label className="form-label">Salario</label>
                <input
                  className={`form-control ${errors.salary ? "is-invalid" : ""}`}
                  type="number"
                  min="0"
                  step="0.01"
                  value={form.salary}
                  onBlur={() => touchField("salary")}
                  onChange={(event) => updateForm("salary", event.target.value)}
                />
                {errors.salary && <div className="invalid-feedback">{errors.salary}</div>}
              </div>
              <div className="col-md-4">
                <label className="form-label">Horas trabalhadas</label>
                <input
                  className={`form-control ${errors.hoursWorked ? "is-invalid" : ""}`}
                  type="number"
                  min="0"
                  step="0.01"
                  value={form.hoursWorked}
                  onBlur={() => touchField("hoursWorked")}
                  onChange={(event) => updateForm("hoursWorked", event.target.value)}
                />
                {errors.hoursWorked && <div className="invalid-feedback">{errors.hoursWorked}</div>}
              </div>
              <div className="col-md-4">
                <label className="form-label">Admissao</label>
                <input
                  className={`form-control ${errors.admissionDate ? "is-invalid" : ""}`}
                  type="datetime-local"
                  value={form.admissionDate}
                  onBlur={() => touchField("admissionDate")}
                  onChange={(event) => updateForm("admissionDate", event.target.value)}
                />
                {errors.admissionDate && <div className="invalid-feedback">{errors.admissionDate}</div>}
              </div>
              <div className="col-md-4">
                <label className="form-label">Setor</label>
                <select
                  className={`form-select ${errors.sectorId ? "is-invalid" : ""}`}
                  value={form.sectorId}
                  onBlur={() => touchField("sectorId")}
                  onChange={(event) => {
                    updateForm("sectorId", event.target.value);
                    updateForm("occupationId", "");
                  }}
                >
                  <option value="">Selecionar setor...</option>
                  {sectors.map((item) => (
                    <option value={item.id} key={item.id}>
                      {item.name}
                    </option>
                  ))}
                </select>
                {errors.sectorId && <div className="invalid-feedback">{errors.sectorId}</div>}
              </div>
              <div className="col-md-4">
                <label className="form-label">Cargo</label>
                <select
                  className={`form-select ${errors.occupationId ? "is-invalid" : ""}`}
                  value={form.occupationId}
                  onBlur={() => touchField("occupationId")}
                  onChange={(event) => updateForm("occupationId", event.target.value)}
                >
                  <option value="">Selecionar cargo...</option>
                  {availableOccupations.map((item) => (
                    <option value={item.id} key={item.id}>
                      {item.name}
                    </option>
                  ))}
                </select>
                {errors.occupationId && <div className="invalid-feedback">{errors.occupationId}</div>}
              </div>
            </div>
          </form>
        </Modal>
      </main>
    </DashboardLayout>
  );
}
