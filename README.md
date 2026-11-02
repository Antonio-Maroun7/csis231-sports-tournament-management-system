# Sports Tournament Management System

The Sports Tournament Management System is a CSIS 231 desktop application for managing users, teams, players, tournaments, and tournament registrations. The JavaFX client communicates with a Spring Boot REST API backed by PostgreSQL.

## Midterm Scope

This repository contains the midterm implementation. The approved proposal is the complete system vision; the remaining approved-proposal modules will be implemented during the final development phase. The midterm submission focuses on the core tournament-management modules.

## Implemented Features

- User registration and BCrypt password hashing.
- Email/password login and email-based six-digit 2FA for administrators and organizers.
- Optional email 2FA for team managers, including expiry, single use, resend cooldown, attempt limit, and development fallback.
- Team CRUD, player roster CRUD, tournament CRUD/status progression, and tournament team registration/withdrawal/approval/rejection.
- JavaFX authentication, dashboard, teams, players, tournaments, registrations, users, and security-settings views.
- Bean Validation, DTO mapping, transactional services, and ProblemDetail error responses.

## Planned for Final Project

- Venue management (`venues`)
- Venue availability (`venue_availability`)
- Match scheduling and results (`matches`)
- Tournament standings (`standings`)
- JWT authentication with server-side role checks

The entity classes for the four domain tables are already prepared in `api/src/main/java/com/antonio/sportstournament/model/`, but they do not have API endpoints or JavaFX screens yet.

## Technology Stack

Java 21; Spring Boot 4.1.1; Spring Web MVC; Spring Data JPA/Hibernate 7; Bean Validation; `spring-security-crypto` for BCrypt only; Spring Boot Mail; PostgreSQL; H2 for tests; Lombok; JavaFX 21.0.6/FXML; Jackson 2.18.2; ControlsFX 11.2.1; BootstrapFX 0.4.0; Maven. JWT and the full Spring Security framework are not currently dependencies.

## Project Structure

```text
api/src/main/java/com/antonio/sportstournament/
  auth/          authentication, 2FA, auth DTOs
  email/         email delivery
  exception/     exceptions and ProblemDetail handler
  player/ team/ tournament/ registration/ user/
  model/         entities prepared for final-phase modules
javafx/src/main/java/com/antonio/sportstournamentclient/
  auth/ core/ dashboard/ player/ registration/ security/ team/ tournament/ user/
javafx/src/main/resources/
  feature-grouped FXML views and styles
```

The project uses feature-based packages by choice. Each feature keeps the class layers used in the course: controller, service/API, repository, entity/model, DTO, and mapper where applicable.

## Architecture / Application Flow

```text
JavaFX Client → JSON/HTTP → Spring Boot REST API (Controller)
             → Service → Spring Data JPA Repository → PostgreSQL
```

The JavaFX client uses `ApiClient`, per-feature API classes, and JavaFX `Task`/`BackgroundTask` for network work.

## Database Schema (Midterm)

| Table | Important columns and constraints | Relationships |
|---|---|---|
| `users` | `user_id` identity PK, required name/email/password hash/role, unique email, 2FA flag, unused legacy secret, creation timestamp | Referenced by teams, tournaments, and challenges |
| `teams` | identity PK, required name, manager reference | Belongs to a user manager |
| `players` | identity PK, required name/position/jersey number, team reference, unique jersey number within team | Many players belong to one team |
| `tournaments` | identity PK, required name/dates/status, organizer reference | Belongs to an organizer |
| `tournament_teams` | identity PK, tournament/team references, status, registration timestamp, unique tournament/team pair | Join table |
| `two_factor_challenges` | identity PK, unique token, BCrypt code hash, expiry, attempts, used flag, creation timestamp | Many challenges belong to one user |

`ddl-auto=update` may create/update tables from all scanned entities. `Venue`, `VenueAvailability`, `Match`, and `Standing` are final-phase entity classes only; they have no API or screens.

## API Endpoints

All endpoints are currently public because JWT/server-side authorization is planned for the final phase.

### Authentication

| Method | Path | Body | Response | Status |
|---|---|---|---|---|
| POST | `/api/auth/register` | `RegisterRequest` | `UserResponse` | 201 |
| POST | `/api/auth/login` | `LoginRequest` | `AuthResult` | 200, 401 |
| POST | `/api/auth/verify-2fa` | `VerifyTwoFactorRequest` | `LoginResponse` | 200, 400, 401 |
| POST | `/api/auth/resend-2fa` | `ResendCodeRequest` | `AuthResult` | 200, 401, 409 |

### Users

| Method | Path | Body | Response | Status |
|---|---|---|---|---|
| POST | `/api/users` | `UserCreateRequest` | `UserResponse` | 201 |
| GET | `/api/users` | none | `UserResponse[]` | 200 |
| GET | `/api/users/{id}` | none | `UserResponse` | 200, 404 |
| DELETE | `/api/users/{id}` | none | empty | 204, 404, 409 |
| PUT | `/api/users/{id}/two-factor` | `TwoFactorSettingRequest` | `UserResponse` | 200, 400, 404, 409 |

### Teams, Players, Tournaments, and Registrations

| Method | Path | Body / params | Response | Status |
|---|---|---|---|---|
| POST/GET | `/api/teams` | `TeamRequest`; optional `managerId` | DTO / list | 201, 200 |
| GET/PUT/DELETE | `/api/teams/{id}` | `TeamRequest` for PUT | DTO / empty | 200, 204, 404, 409 |
| GET/POST | `/api/teams/{teamId}/players` | `PlayerRequest` for POST | list / DTO | 200, 201 |
| GET/PUT/DELETE | `/api/players/{id}` | `PlayerRequest` for PUT | DTO / empty | 200, 204, 404, 409 |
| POST/GET | `/api/tournaments` | `TournamentRequest`; optional filters | DTO / list | 201, 200 |
| GET/PUT/DELETE | `/api/tournaments/{id}` | `TournamentRequest` for PUT | DTO / empty | 200, 204, 404, 409 |
| PUT | `/api/tournaments/{id}/status` | `TournamentStatusRequest` | `TournamentResponse` | 200, 400, 404 |
| GET/POST | `/api/tournaments/{tournamentId}/registrations` | `RegistrationRequest` for POST | list / DTO | 200, 201 |
| GET | `/api/teams/{teamId}/registrations` | none | registration list | 200 |
| PUT | `/api/registrations/{id}/approve` | none | `RegistrationResponse` | 200, 404, 409 |
| PUT | `/api/registrations/{id}/reject` | none | `RegistrationResponse` | 200, 404, 409 |
| DELETE | `/api/registrations/{id}` | none | empty | 204, 404, 409 |

## Frontend Usage

Implemented views are login, registration, email 2FA verification, dashboard, teams, team details, players, tournaments, tournament details, tournament registration, users, and security settings. Administrators see user management; team managers see team/player and registration operations; organizers see tournament and registration-management actions. These are client-side UX controls only until JWT authorization is added. Mockups 11–19 are final-scope views and are not implemented.

## Setup Instructions

1. Clone the repository and install PostgreSQL.
2. Create the database `sports_tournament_db`.
3. Copy `api/.env.example` to `api/.env`.
4. Fill in `DB_URL` (optional), `DB_USERNAME`, `DB_PASSWORD`, `MAIL_USERNAME`, and `MAIL_PASSWORD`. `api/.env` is Git-ignored and must never be committed. The same names can be set in an IntelliJ Run Configuration.
5. For Gmail, enable Google 2-Step Verification and create an App Password for the API. Put the generated app password without spaces in `MAIL_PASSWORD` and the sending address in `MAIL_USERNAME`.
6. Without mail settings, the code appears in the server console as a development fallback.
7. Start the API from `api/`: `.\mvnw.cmd spring-boot:run` (port 8081).
8. Start the JavaFX client from `javafx/`: `.\mvnw.cmd javafx:run`.
9. Test the API and 2FA flow with Postman.

## Testing

From `api/`, run `.\mvnw.cmd test`. The verified classes are `TwoFactorFlowTest` (4 tests) and `SportstournamentApplicationTests` (1 context test); the complete API suite passed in this review. Tests use H2 and do not require PostgreSQL or mail.

## Error Handling and Validation

`@Valid` DTOs and `GlobalExceptionHandler` return ProblemDetail responses for validation/malformed JSON (400), invalid credentials and 2FA failures (401), missing resources (404), conflicts/integrity errors (409), and mail delivery failures (503).

## Security

Passwords and 2FA codes are BCrypt-hashed and are not returned. Email 2FA uses a random six-digit code, five-minute expiry, single use, five failed-attempt lockout, and 60-second resend cooldown. Email 2FA is an intentional deviation from the authenticator-app/TOTP flow shown in the proposal mockups. Credentials belong in ignored `api/.env` or environment variables, with `api/.env.example` as the template.

There is currently no JWT, session, authentication filter, or server-side role/ownership authorization. Any caller who can reach the API can call CRUD and administrative endpoints; JWT and authorization are planned for the final phase.

## Future Development

The final phase will add venues, venue availability, match scheduling, match results, standings, their REST APIs and JavaFX views, and JWT-based server-side authorization. These correspond to expected views 11–19.

## Author

Antonio Maroun, CSIS 231 – Java Technologies, University of Balamand
