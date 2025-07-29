## VetClinic – Backend

Piattaforma gestionale modulare per cliniche veterinarie, sviluppata in architettura a microservizi. Questo modulo rappresenta il backend, costruito con Spring Boot, Keycloak per la sicurezza e PostgreSQL per la persistenza dei dati.

---
## Tecnologie utilizzate

-  Spring Boot (REST API)
-  Keycloak (JWT, SSO, gestione ruoli)
-  PostgreSQL + Hibernate
-  Report PDF
-  Docker & Docker Compose

---

## Moduli principali

- **Autenticazione e autorizzazione** (Keycloak integrato)
- **Gestione utenti e ruoli** (admin, capo reparto, veterinario, assistente, cliente)
- **Gestione clinica**: pazienti, appuntamenti, somministrazioni, turni, reparti
- **Reportistica**: generazione PDF e visualizzazione dati clinici

---

## Come avviare il backend

1. Clona il repository:
   ```bash
   git clone https://github.com/chiaraapastore/vetclinic.git
