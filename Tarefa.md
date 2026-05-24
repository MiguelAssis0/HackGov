Go to:

`frontend/src/pages/EmployeesPage.jsx`

Remove the mocked employee data and integrate the page with the backend Employee API.

Requirements:

* Replace all employee mocks with real backend data.
* Connect the create employee flow/form to the backend.
* Fetch and list employees from the API.
* Implement employee creation using real API requests.
* Use the Employee controller as the backend reference:
  `backend/src/main/java/com/fiap/hackgov/cityhall_management/internal/controllers/EmployeeController.java`
* If the frontend needs fields, endpoints, DTOs, validations, or mappings that do not exist yet, add the necessary backend changes.
* Keep the current UI and styling consistent.
* Reuse the existing frontend API/service pattern.
* Handle loading, empty, success, and error states properly.
* Remove hardcoded values, fake datasets, and temporary frontend-only logic.
