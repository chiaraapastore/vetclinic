#  VetClinic – Backend

Modular management platform for veterinary clinics, based on a microservices architecture.  
This module implements the backend using Spring Boot, with Keycloak for security and PostgreSQL for data persistence.

---

##  Technologies Used

-  Spring Boot (REST API)
-  Keycloak (JWT, SSO, role-based access)
-  PostgreSQL + Hibernate
-  PDF reporting
-  Docker & Docker Compose

---

##  Main Modules

- **Authentication and Authorization** (Keycloak integration)
- **User and Role Management** (admin, head of department, vet, assistant, client)
- **Clinic Management**: patients, appointments, administrations, shifts, departments
- **Reporting**: PDF generation and clinical data visualization

---

##  How to Run the Backend

1. Clone the repository:
   ```bash
   git clone https://github.com/chiaraapastore/vetclinic.git
