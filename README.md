# Sports Tournament Management System

The Sports Tournament Management System is a CSIS 231 desktop application for managing users, teams, players, tournaments, and tournament registrations. The JavaFX client communicates with a Spring Boot REST API backed by PostgreSQL.

## Midterm Scope

This repository contains the midterm implementation. The approved proposal is the complete system vision; the remaining approved-proposal modules will be implemented during the final development phase. The midterm submission focuses on the core tournament-management modules.

## Implemented Features

- User registration and BCrypt password hashing.
- Email/password login and email-based six-digit 2FA for administrators and organizers.
- Optional email 2FA for team managers, including expiry, single use, resend cooldown, attempt limit, and development fallback.
- JWT authentication (HS256, stateless) with a Spring Security filter chain, server-side role rules, and ownership checks in the services.
- Team CRUD, player roster CRUD, tournament CRUD/status progression, and tournament team registration/withdrawal/approval/rejection.
- JavaFX authentication, dashboard, teams, players, tournaments, registrations, users, and security-settings views.
- Bean Validation, DTO mapping, transactional services, and ProblemDetail error responses.

## Planned for Final Project

- Venue management (`venues`)
- Venue availability (`venue_availability`)
- Match scheduling and results (`matches`)
- Tournament standings (`standings`)

The entity classes for the four domain tables are already prepared in `api/src/main/java/com/antonio/sportstournament/model/`, but they do not have API endpoints or JavaFX screens yet.

## Technology Stack

Java 21; Spring Boot 4.1.1; Spring Web MVC; Spring Security 7.1.1 (filter chain + BCrypt); jjwt 0.12.6 (`jjwt-api`, `jjwt-impl`, `jjwt-jackson`); Spring Data JPA/Hibernate 7; Bean Validation; Spring Boot Mail; PostgreSQL; H2 for tests; Lombok; JavaFX 21.0.6/FXML; Jackson 2.18.2 (client); ControlsFX 11.2.1; BootstrapFX 0.4.0; Maven. The server uses Jackson 3 for its REST JSON; `jjwt-jackson` brings its own Jackson 2 (2.21.5) only to read/write token claims, and both versions coexist without problems.

## Project Structure

```text
api/src/main/java/com/antonio/sportstournament/
  auth/          authentication, 2FA, auth DTOs
  email/         email delivery
  exception/     exceptions and ProblemDetail handler
  security/      JWT, Spring Security configuration, current-user lookup, admin bootstrap
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

Every endpoint except the four public auth endpoints needs `Authorization: Bearer <token>`. "Auth / Role" shows the URL rule; ownership rules (see Security) are checked in the services and answer 403. Admins may do everything.

### Authentication

| Method | Path | Body | Response | Auth / Role | Status |
|---|---|---|---|---|---|
| POST | `/api/auth/register` | `RegisterRequest` | `LoginResponse` (with `token`) | Public | 201, 400, 409 |
| POST | `/api/auth/login` | `LoginRequest` | `AuthResult` (token in `user.token` only when no 2FA) | Public | 200, 401 |
| POST | `/api/auth/verify-2fa` | `VerifyTwoFactorRequest` | `LoginResponse` (with `token`) | Public | 200, 400, 401 |
| POST | `/api/auth/resend-2fa` | `ResendCodeRequest` | `AuthResult` | Public | 200, 401, 409 |

### Users

| Method | Path | Body | Response | Auth / Role | Status |
|---|---|---|---|---|---|
| GET | `/api/users/me` | none | `UserResponse` | Any logged-in user | 200, 401 |
| POST | `/api/users` | `UserCreateRequest` | `UserResponse` | ADMIN | 201, 403 |
| GET | `/api/users` | none | `UserResponse[]` | ADMIN | 200, 403 |
| GET | `/api/users/{id}` | none | `UserResponse` | ADMIN | 200, 403, 404 |
| DELETE | `/api/users/{id}` | none | empty | ADMIN (not yourself) | 204, 403, 404, 409 |
| PUT | `/api/users/{id}/two-factor` | `TwoFactorSettingRequest` | `UserResponse` | Logged-in, only for yourself | 200, 400, 403, 404, 409 |

### Teams, Players, Tournaments, and Registrations

| Method | Path | Body / params | Response | Auth / Role | Status |
|---|---|---|---|---|---|
| POST | `/api/teams` | `TeamRequest` (`teamName`; the manager is the caller) | DTO | TEAM_MANAGER, ADMIN | 201, 400, 403 |
| GET | `/api/teams` | optional `managerId` | list | Any logged-in user | 200 |
| GET | `/api/teams/{id}` | none | DTO | Any logged-in user | 200, 404 |
| PUT/DELETE | `/api/teams/{id}` | `TeamRequest` for PUT | DTO / empty | The team's manager, ADMIN | 200, 204, 403, 404, 409 |
| GET | `/api/teams/{teamId}/players` | none | list | Any logged-in user | 200, 404 |
| POST | `/api/teams/{teamId}/players` | `PlayerRequest` | DTO | The team's manager, ADMIN | 201, 403, 404, 409 |
| GET | `/api/players/{id}` | none | DTO | Any logged-in user | 200, 404 |
| PUT/DELETE | `/api/players/{id}` | `PlayerRequest` for PUT | DTO / empty | The team's manager, ADMIN | 200, 204, 403, 404, 409 |
| POST | `/api/tournaments` | `TournamentRequest` (`name`, `startDate`, `endDate`; the organizer is the caller) | DTO | ORGANIZER, ADMIN | 201, 400, 403 |
| GET | `/api/tournaments` | optional filters `status`, `organizerId` | list | Any logged-in user | 200 |
| GET | `/api/tournaments/{id}` | none | DTO | Any logged-in user | 200, 404 |
| PUT/DELETE | `/api/tournaments/{id}` | `TournamentRequest` for PUT | DTO / empty | The tournament's organizer, ADMIN | 200, 204, 403, 404, 409 |
| PUT | `/api/tournaments/{id}/status` | `TournamentStatusRequest` | `TournamentResponse` | The tournament's organizer, ADMIN | 200, 400, 403, 404, 409 |
| GET | `/api/tournaments/{tournamentId}/registrations` | none | list | Any logged-in user | 200, 404 |
| POST | `/api/tournaments/{tournamentId}/registrations` | `RegistrationRequest` | DTO | TEAM_MANAGER who manages the team, ADMIN | 201, 403, 404, 409 |
| GET | `/api/teams/{teamId}/registrations` | none | registration list | Any logged-in user | 200, 404 |
| PUT | `/api/registrations/{id}/approve` | none | `RegistrationResponse` | The tournament's organizer, ADMIN | 200, 403, 404, 409 |
| PUT | `/api/registrations/{id}/reject` | none | `RegistrationResponse` | The tournament's organizer, ADMIN | 200, 403, 404, 409 |
| DELETE | `/api/registrations/{id}` | none | empty | The team's manager, ADMIN | 204, 403, 404, 409 |

## Frontend Usage

Implemented views are login, registration, email 2FA verification, dashboard, teams, team details, players, tournaments, tournament details, tournament registration, users, and security settings. Administrators see user management; team managers see team/player and registration operations; organizers see tournament and registration-management actions. The client hides buttons the user may not use, but the server enforces the same rules. Mockups 11–19 are final-scope views and are not implemented.

The client keeps the JWT in memory only (never on disk or in Preferences) and sends it with every request. If the server answers 401 while a user is logged in, the client clears the session and returns to the Login view with "Your session has expired. Please log in again."

## Setup Instructions

1. Clone the repository and install PostgreSQL.
2. Create the database `sports_tournament_db`.
3. Copy `api/.env.example` to `api/.env`.
4. Fill in `DB_URL` (optional), `DB_USERNAME`, `DB_PASSWORD`, `MAIL_USERNAME`, `MAIL_PASSWORD`, and `JWT_SECRET`. `api/.env` is Git-ignored and must never be committed. The same names can be set in an IntelliJ Run Configuration.
   - `JWT_SECRET` must be a random string of **at least 32 characters** (the API refuses to start otherwise). Generate one in PowerShell: `[Convert]::ToBase64String((1..48 | ForEach-Object { [byte](Get-Random -Maximum 256) }))`
   - `JWT_EXPIRATION_MS` is optional (default `7200000` = 2 hours).
   - `ADMIN_NAME`, `ADMIN_EMAIL`, and `ADMIN_PASSWORD` are optional: if no ADMIN exists at startup and the email and password are set, the first administrator is created (see Security).
5. For Gmail, enable Google 2-Step Verification and create an App Password for the API. Put the generated app password without spaces in `MAIL_PASSWORD` and the sending address in `MAIL_USERNAME`.
6. Without mail settings, the code appears in the server console as a development fallback.
7. Start the API from `api/`: `.\mvnw.cmd spring-boot:run` (port 8081).
8. Start the JavaFX client from `javafx/`: `.\mvnw.cmd javafx:run`.
9. Test the API and 2FA flow with Postman (see below).

### Testing with Postman

1. `POST http://localhost:8081/api/auth/login` with `{"email":"...","password":"..."}`.
   - Team Manager without 2FA: the answer contains `user.token`.
   - Administrator, Organizer, or 2FA-enabled user: the answer contains `challengeToken`. Send `POST /api/auth/verify-2fa` with `{"challengeToken":"...","code":"123456"}` (the code arrives by email, or in the server console without mail settings); the answer contains `token`.
2. Copy the token. In the request's **Authorization** tab choose **Bearer Token** and paste it (or add the header `Authorization: Bearer <token>`).
3. Expected errors (always ProblemDetail JSON):
   - No token, a tampered token, or an expired token on a protected URL: `401` with `"detail": "Your session has expired or you are not logged in. Please log in again."`
   - A Team Manager calling `POST /api/tournaments`: `403` with `"detail": "You do not have permission to do this."`
   - A Team Manager calling `PUT /api/teams/{teamOfAnotherManager}`: `403` with `"detail": "Only the team's manager can change this team."`

## Testing

From `api/`, run `.\mvnw.cmd test`. The classes are `SecurityFlowTest` (12 tests: 401/403 handling, roles, ownership, tokens), `TwoFactorFlowTest` (4 tests), and `SportstournamentApplicationTests` (1 context test). Tests use H2 and do not require PostgreSQL or mail.

## Error Handling and Validation

`@Valid` DTOs and `GlobalExceptionHandler` return ProblemDetail responses for validation/malformed JSON (400), invalid credentials, 2FA failures, and missing/expired tokens (401), missing permission (403), missing resources (404), conflicts/integrity errors (409), and mail delivery failures (503). The security filter chain writes its own 401/403 responses in the same ProblemDetail format, so the JavaFX client can always show the `detail` text.

## Security

- **BCrypt** hashes passwords and 2FA codes; they are never returned. One shared `PasswordEncoder` bean is used everywhere.
- **Email 2FA** uses a random six-digit code, five-minute expiry, single use, five failed-attempt lockout, and 60-second resend cooldown. It is required for Administrators and Organizers. It is an intentional deviation from the authenticator-app/TOTP flow shown in the proposal mockups. A JWT is issued only after the password (and, when required, the emailed code) is accepted.
- **JWT**: HS256, signed with `JWT_SECRET`, valid 2 hours by default, stateless (no server session, no cookie, CSRF disabled), sent as `Authorization: Bearer <token>`. The subject is the user's email.
- **Spring Security filter chain** (`security/` package):
  - `JwtService`: creates and validates tokens; refuses to start if the secret is shorter than 32 bytes.
  - `JwtAuthenticationFilter`: reads the Bearer token and loads the user from the database on every request (a deleted user loses access immediately). It never rejects a request itself; an invalid token leaves the request anonymous.
  - `JpaUserDetailsService`: maps a `User` to Spring Security (`ROLE_ADMIN`, `ROLE_ORGANIZER`, `ROLE_TEAM_MANAGER`).
  - `SecurityConfig`: `PasswordEncoder` bean, stateless chain, URL rules, and ProblemDetail 401/403 responses.
  - `CurrentUserService`: gives controllers the `User` of the caller so services can apply ownership rules.
  - `AdminBootstrap`: optional first-administrator creation at startup.
- **URL rules** (first match wins): the four public auth endpoints and `/error` are open; `GET /api/users/me` and `PUT /api/users/{id}/two-factor` need a login; every other `/api/users/**` is ADMIN only; `POST /api/teams` and `POST /api/tournaments/{id}/registrations` need TEAM_MANAGER or ADMIN; `POST /api/tournaments` and approve/reject need ORGANIZER or ADMIN; everything else needs a login (all GETs are readable by any logged-in user).
- **Ownership rules** (in the services; Admins may do everything):

| Resource | Who may change it |
|---|---|
| User 2FA setting | Only the user themself; admins and organizers cannot disable it (409) |
| User deletion | ADMIN, but not their own account (409) |
| Team (update/delete) and its players | The team's manager |
| Tournament (update/status/delete) | The tournament's organizer |
| Registration (create) | The manager of the team being registered |
| Registration (approve/reject) | The tournament's organizer |
| Registration (withdraw) | The team's manager |

- **401 vs 403**: 401 means "not authenticated" (no, invalid, or expired token; the client returns to the Login view). 403 means "authenticated but not allowed" (the client shows the message).
- **Bootstrap admin**: `POST /api/users` is ADMIN only, so set `ADMIN_EMAIL` and `ADMIN_PASSWORD` (and optionally `ADMIN_NAME`) in `api/.env`; at startup, if no ADMIN exists, one is created with 2FA enabled and only `Bootstrap admin created: <email>` is logged. Remove the password from `.env` afterwards.
- **Client**: the token is kept in memory only and is forgotten on logout.
- Credentials belong in ignored `api/.env` or environment variables, with `api/.env.example` as the template. There is no HTTP/IP rate limiting on the login endpoints.

## Code Documentation

- Every class and public method in `api/` and `javafx/` is documented with Javadoc.
- The business rules are described in the service classes, and each controller lists its endpoints, allowed roles and status codes.
- To generate the HTML documentation, run `.\mvnw.cmd javadoc:javadoc` in `api/`, then open `api/target/reports/apidocs/index.html`.

## Future Development

The final phase will add venues, venue availability, match scheduling, match results, standings, and their REST APIs and JavaFX views. These correspond to expected views 11–19.

## Author

Antonio Maroun, CSIS 231 – Java Technologies, University of Balamand
