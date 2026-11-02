# CSIS 231 Midterm Audit

**Sources reviewed:** repository source, the official requirements in the review brief, `docs/Sports_Tournament_Management_System_Submission/CSIS231_Proposal_AntonioMaroun.pdf`, all 19 expected-view images, and the supplied class-project summary. The proposal PDF is present but image-based/scanned; local inspection did not expose selectable text. TOTP evidence is therefore also grounded in `02_Two_Factor_Verification.png` and `03_Security_Settings_View.png`.

## 1. Overall Midterm Assessment

The midterm core is implemented and connected: users/authentication, email 2FA, teams, players, tournaments, registrations, and JavaFX screens. The API has a coherent controller → service → repository → entity → mapper/DTO flow, and all five API tests passed.

The main limitation is security: there is no JWT, session, authentication filter, or server-side authorization. Every endpoint is public and the client-side role checks are UI controls, not security. The code explicitly leaves JWT for a later step. The proposal/mockups also show TOTP/authenticator-app behavior, while the implementation intentionally uses email codes. These facts must be disclosed.

The venue, availability, match, and standings entities are prepared final-phase entities with no API or screens. They are not missing midterm requirements under the supplied scope. Overall verdict: **READY AFTER MINOR FIXES**—the core is test-verified, but the live demo must use working local credentials and honestly explain the authorization limitation and TOTP-to-email deviation.

## 2. Requirements Checklist

| Requirement | Status | Evidence (file paths) | Problems found | Recommended fix |
|---|---|---|---|---|
| Developed from approved proposal | PARTIAL | `docs/.../CSIS231_Proposal_AntonioMaroun.pdf`; `docs/.../Expected_Views/` | Core scope is present; 2FA differs from TOTP mockups and final modules are not implemented. | Document the intentional deviation and final-scope boundary. |
| Spring Boot backend | PASS | `api/pom.xml`; `SportstournamentApplication.java` | Compile and tests pass. | None for midterm. |
| Validator, JPA, entities, DTOs | PASS | DTO records; `@Valid` controller parameters; JPA entities | Validation advice reports only first field error. | Optionally return all field errors later. |
| Routers/controllers/services | PASS | `auth`, `user`, `team`, `player`, `tournament`, `registration` packages | No server authorization yet; classified final-phase. | Add JWT/principal authorization in final phase. |
| JavaFX views | PASS for midterm / FINAL-SCOPE for 11–19 | `javafx/src/main/resources`; JavaFX controllers | Screens 01–10 exist; 11–19 intentionally do not. | Do not present final mockups as implemented. |
| PostgreSQL/Hibernate | PASS | `application.properties`; JPA entities | Startup with placeholders failed DB authentication; `ddl-auto=update` is not migration control. | Use working local DB values for demo. |
| Proposal functionality | PARTIAL | Team/player/tournament/registration APIs and views | Email 2FA is not TOTP; final modules are entities only. | Explain this in README and presentation. |
| Proper error handling | PASS with limitations | `exception/GlobalExceptionHandler.java` | First validation error only; unexpected failures lack a uniform handler; integrity errors are generic. | Add targeted integration tests and safe fallback later. |
| Setup/API/frontend/schema/library documentation | PASS | `README.md` | Documentation now matches the verified midterm implementation. | Keep it synchronized with future changes. |
| Direct code documentation | PARTIAL | New auth/email classes have Javadocs | Existing public classes are inconsistently documented. | Add concise Javadocs to important public classes. |

## 3. Critical Issues Before Submission

1. **RESOLVED — No server-side authorization.** Spring Security and a JWT layer were added. `auth/TwoFactorService` (after the emailed code) and `auth/AuthService` (login without 2FA and register) now issue an HS256 JWT whose subject is the user's email. A new `security/` package holds `JwtService`, a `JwtAuthenticationFilter` (loads the user on every request and never rejects by itself), `JpaUserDetailsService`, `SecurityConfig` (stateless chain, CSRF disabled, coarse URL role rules, ProblemDetail 401/403) and `CurrentUserService`. Ownership rules live in the services and throw `ForbiddenException` (403): only a team's manager changes the team, its players, and its registrations; only a tournament's organizer edits, advances, or approves/rejects; the temporary `managerId`/`organizerId` request fields were removed (the caller is the owner); `/api/users/**` is ADMIN-only except `GET /api/users/me` and a user's own 2FA setting. `AdminBootstrap` creates the first ADMIN from `ADMIN_*` variables, and the JavaFX client sends the token on every request and returns to Login when it expires. `SecurityFlowTest` (12 tests) covers it.
2. **TOTP is not the implemented 2FA.** The expected images show authenticator-app code refresh, QR, and backup-code behavior; `auth/TwoFactorService.java` and `email/EmailService.java` implement email codes. State explicitly that email 2FA is the intentional midterm deviation.
3. **Do not demo with placeholders.** The verified API startup reached PostgreSQL but failed with `password authentication failed for user "postgres"` because the local `.env` uses placeholders. Fill the ignored file locally or use IntelliJ environment variables; never commit them.

## 4. Recommended Improvements

- Add tests for resend cooldown, expiry, email failure (503), validation, duplicates, status transitions, and delete conflicts.
- Review `RegistrationService`: decisions are blocked only for `COMPLETED`, so approval/rejection may be accepted in `DRAFT` or `IN_PROGRESS`; if only `OPEN` is valid, change that condition.
- Clarify `TournamentService` because update accepts `organizerId` but does not change the organizer.
- Fix the missing space in `User Not Found` messages.
- Add explicit deletion rules and PostgreSQL foreign-key tests.
- Consider migrations instead of `ddl-auto=update`, and disable SQL logging outside development.
- Make the JavaFX base URL configurable instead of hard-coding it in `core/ApiClient.java:26`.
- Cancel or ignore stale JavaFX tasks after scene changes.
- Add consistent class-level Javadocs without rewriting working code.

## 5. Professor Class-Flow Comparison

| Area | Finding | Classification |
|---|---|---|
| Packages | Feature-based (`auth`, `team`, `player`, etc.) rather than class layer folders. | Acceptable difference; do not reorganize solely for naming. |
| Controller/service/repository | Controllers delegate; services apply rules/transactions; repositories persist entities. | Acceptable; follows class flow. |
| DTOs/mappers/entities | Records, mappers, JPA entities, and response DTOs are used consistently. | Acceptable difference. |
| Validation/exceptions | Jakarta validation and centralized ProblemDetail advice are present. | Acceptable; first-error output can improve. |
| Authentication | BCrypt crypto dependency only, matching class project; no filter chain/JWT. | Acceptable for planned scope, but a security limitation. |
| JavaFX | Launcher/application, SceneManager, Session, ApiClient, per-feature APIs, FXML, and Tasks are present. | Acceptable; closely follows class flow. |
| Client communication | Blocking HttpClient calls are moved to Tasks and API errors preserve `ProblemDetail.detail`. | Acceptable; base URL/token handling needs final-phase work. |
| Dependencies/configuration | PostgreSQL runtime, H2 test scope, JavaFX/Jackson/ControlsFX/BootstrapFX. | PASS. |

The project follows the class flow closely enough for the midterm without copying the class folder layout.

## 6. Backend/API Audit

### Implementation review

- Entities use `@Entity`, identity IDs, required columns, relationships, and string enums. Unique constraints cover email, jersey number per team, tournament/team registration, and challenge token.
- Lazy challenge-to-user loading is used within transactional service methods, compatible with `spring.jpa.open-in-view=false`.
- Repository names were checked against entity fields, including email lookup, manager/team/organizer filters, roster ordering, jersey checks, registration checks, and the JPQL challenge invalidation update. Application startup parsed the repositories successfully.
- Request DTOs are records with Bean Validation; controllers use `@Valid`; controllers return DTOs rather than entities.
- Write services are transactional and read services use `readOnly=true`. 2FA verification uses `noRollbackFor=UnauthorizedException` so failed attempts persist; challenge save precedes email send so delivery failure rolls back.
- BCrypt hashes passwords and 2FA codes; plain secrets are not returned.

### Endpoint table

All endpoints are **Public (no JWT yet)**.

| Method | Endpoint | Purpose | Intended role | Request body/params | Response | Status |
|---|---|---|---|---|---|---|
| POST | `/api/auth/register` | Register manager | Public | `RegisterRequest` | `UserResponse` | 201, 400, 409 |
| POST | `/api/auth/login` | Login/start 2FA | Public | `LoginRequest` | `AuthResult` | 200, 400, 401, 503 |
| POST | `/api/auth/verify-2fa` | Verify challenge | Challenge holder | `VerifyTwoFactorRequest` | `LoginResponse` | 200, 400, 401 |
| POST | `/api/auth/resend-2fa` | Replace challenge | Challenge holder | `ResendCodeRequest` | `AuthResult` | 200, 400, 401, 409, 503 |
| POST | `/api/users` | Create user | Admin | `UserCreateRequest` | `UserResponse` | 201, 400, 409 |
| GET | `/api/users` | List users | Admin | none | `UserResponse[]` | 200 |
| GET | `/api/users/{id}` | Find user | Authenticated | path id | `UserResponse` | 200, 404 |
| DELETE | `/api/users/{id}` | Delete user | Admin | path id | empty | 204, 404, 409 |
| PUT | `/api/users/{id}/two-factor` | Change 2FA | Self | `TwoFactorSettingRequest` | `UserResponse` | 200, 400, 404, 409 |
| POST | `/api/teams` | Create team | Manager/admin | `TeamRequest` | `TeamResponse` | 201, 400, 404, 409 |
| GET | `/api/teams` | List teams | Authenticated | optional `managerId` | list | 200 |
| GET/PUT/DELETE | `/api/teams/{id}` | Team CRUD | Owner/admin | `TeamRequest` for PUT | DTO/empty | 200, 204, 404, 409 |
| GET/POST | `/api/teams/{teamId}/players` | Roster/list/add | Owner/admin | `PlayerRequest` for POST | list/DTO | 200, 201, 400, 404, 409 |
| GET/PUT/DELETE | `/api/players/{id}` | Player CRUD | Owner/admin | `PlayerRequest` for PUT | DTO/empty | 200, 204, 404, 409 |
| POST | `/api/tournaments` | Create tournament | Organizer/admin | `TournamentRequest` | `TournamentResponse` | 201, 400, 404, 409 |
| GET | `/api/tournaments` | List/filter | Authenticated | `status`, `organizerId` | list | 200, 400 |
| GET/PUT/DELETE | `/api/tournaments/{id}` | Tournament CRUD | Organizer/admin | `TournamentRequest` for PUT | DTO/empty | 200, 204, 404, 409 |
| PUT | `/api/tournaments/{id}/status` | Advance status | Organizer/admin | `TournamentStatusRequest` | DTO | 200, 400, 404 |
| GET/POST | `/api/tournaments/{tournamentId}/registrations` | List/register | Manager/organizer | `RegistrationRequest` for POST | list/DTO | 200, 201, 409 |
| GET | `/api/teams/{teamId}/registrations` | Team registrations | Owner/admin | path id | list | 200, 404 |
| PUT | `/api/registrations/{id}/approve` | Approve | Organizer/admin | none | `RegistrationResponse` | 200, 404, 409 |
| PUT | `/api/registrations/{id}/reject` | Reject | Organizer/admin | none | `RegistrationResponse` | 200, 404, 409 |
| DELETE | `/api/registrations/{id}` | Withdraw | Owner/admin | path id | empty | 204, 404, 409 |

No venue, availability, match, result, or standings endpoints exist; that matches the final-scope boundary.

## 7. JavaFX Audit

| Mockup | Implemented JavaFX view |
|---|---|
| 01 Login | Implemented: `auth/login-view.fxml` + `LoginController` |
| 02 2FA | Implemented: `auth/two-factor-view.fxml` + `TwoFactorController`; email rather than TOTP |
| 03 Security settings | Implemented: `security/security-view.fxml` + `SecurityController`; no QR/backup-code UI |
| 04 Dashboard | Implemented: `dashboard/dashboard-view.fxml` + `DashboardController` |
| 05 Teams | Implemented: `team/teams-view.fxml` + `TeamsController` |
| 06 Team details | Implemented: `team/team-details-view.fxml` + `TeamDetailsController` |
| 07 Players | Implemented: `player/players-view.fxml` + `PlayersController` |
| 08 Tournaments | Implemented: `tournament/tournaments-view.fxml` + `TournamentsController` |
| 09 Tournament details | Implemented: `tournament/tournament-details-view.fxml` + `TournamentDetailsController` |
| 10 Tournament registration | Implemented: `registration/tournament-registration-view.fxml` + `TournamentRegistrationController` |
| 11–19 | FINAL-SCOPE: venue, availability, match, result, and standings views have no source FXML/controller/API |

FXML controllers are wired with `fx:controller`; navigation uses `SceneManager.switchScene`; the sidebar is shared. `ApiClient` uses Jackson and propagates `ProblemDetail.detail`. Network calls use JavaFX `Task`/`BackgroundTask` and update controls on FX callbacks. `Session` stores the current login response and selected objects; role checks hide/disable controls. `module-info.java` opens controller packages to FXML and DTO packages to Jackson.

Limitations are a hard-coded API URL, no access token sent, basic client validation, and no cancellation of stale tasks after scene replacement. Source inspection verified mappings, but JavaFX runtime loading could not be executed because `JAVA_HOME` is unset.

## 8. Database Audit

### Midterm tables

`users`, `teams`, `players`, `tournaments`, `tournament_teams`, and `two_factor_challenges` have connected entities and APIs. Constraints include unique email, jersey number per team, tournament/team pair, and challenge token.

### Final-phase tables

`venues`, `venue_availability`, `matches`, and `standings` exist as entity classes in `api/src/main/java/com/antonio/sportstournament/model/` only. There are no repositories, DTOs, mappers, services, controllers, JavaFX APIs, or FXML screens. Since `ddl-auto=update` scans entities, Hibernate may create their tables despite the absence of features.

Production uses PostgreSQL, port 8081, environment-backed DB settings, `ddl-auto=update`, and SQL logging. Tests use H2 PostgreSQL mode/create-drop, and H2 is test-scoped.

## 9. Security Audit

BCrypt is used through `spring-security-crypto`, not the full Spring Security filter chain. Passwords and code hashes are not returned. Email codes use `SecureRandom`, five-minute expiry, single use, five failed-attempt lockout, and 60-second resend cooldown. Older unused challenges are invalidated. Mail credentials use environment/config values; only the unconfigured development fallback logs the code.

The successful verification path returns `LoginResponse` but does not issue a JWT; `TwoFactorService` contains `// TODO Step 11: issue the JWT here.` The token is currently a bearer challenge credential, and there is no HTTP/IP rate limiting. Most importantly, all REST endpoints are public: any reachable caller can create an admin, list/delete users, change 2FA, alter teams, approve registrations, or advance tournaments. This risk is real even though the review brief classifies JWT/server authorization as final-scope.

## 10. Demo Preparation

### Exact order

1. Start PostgreSQL and confirm `sports_tournament_db`.
2. Fill ignored `api/.env` or set the variable names in IntelliJ.
3. Start API on 8081 and show Hibernate startup.
4. Use Postman for manager login, organizer email 2FA, wrong code, correct code, and resend cooldown.
5. Show implemented midterm tables in pgAdmin and identify final-phase entity tables separately.
6. Launch JavaFX; manager: teams → players → tournament registration.
7. Organizer: email 2FA → create/open tournament → approve registration.
8. Admin: users → Security Settings.
9. State clearly that venues/matches/standings and JWT authorization are final-phase work, and email 2FA is the TOTP deviation.

### Likely questions

- **Why DTOs?** Validated records cross the HTTP boundary; JPA entities are not returned directly.
- **Why mappers?** Mappers isolate entity-to-response conversion.
- **What do transactions do?** Services make writes atomic and reads can be `readOnly`; 2FA uses `noRollbackFor` so failed attempts persist.
- **Authentication vs authorization?** Login/2FA authenticate; server authorization is not implemented until JWT.
- **Why BCrypt?** Salted one-way hashing prevents storing passwords/codes in plaintext.
- **How do derived queries work?** Spring Data parses names against entity fields, such as roster ordering and jersey checks.
- **Why layers?** Controllers handle HTTP, services rules/transactions, repositories persistence.
- **Why validation/status codes?** DTO annotations reject bad requests; 201 create, 204 delete, 400 invalid input, 401 credentials, 404 missing, 409 conflict, 503 mail failure.
- **What are `ddl-auto` and `open-in-view`?** `update` changes schema from entities; `open-in-view=false` requires lazy access inside transactions.
- **How does JavaFX avoid freezing?** `ApiClient` calls run in `Task`/`BackgroundTask`, with UI callbacks on the FX thread.
- **Why feature packages?** They preserve the class flow while keeping each feature's code together.

### On-the-spot features

1. Tournament-registration filtering: `RegistrationController`, service/repository, DTO/API client, JavaFX controller.
2. Team-name search: `TeamController`, service, repository, `TeamApi`, `TeamsController`.
3. Tournament date validation: `TournamentRequest`, `TournamentService`, tests.
4. Persistent 2FA resend countdown: `TwoFactorController`, `AuthApi`, `Session`.
5. User email edit: user request DTO/service/controller/mapper, `UserApi`, dialog/controller.
6. Registration-status filter: registration API/DTO/controller and FXML.
7. Configurable API URL: `ApiClient` and setup documentation.

## 11. Build & Test Results

From `api/`, `.\mvnw.cmd -q compile` passed. `.\mvnw.cmd test` passed with `TwoFactorFlowTest`: 4 tests and `SportstournamentApplicationTests`: 1 test, for 5 total, 0 failures/errors. H2 context startup succeeded.

`.\mvnw.cmd spring-boot:run` compiled and reached PostgreSQL but failed with `password authentication failed for user "postgres"` because the local placeholder password was used. Real database connectivity and Gmail delivery were not verified.

`.\mvnw.cmd javafx:run` could not run because `JAVA_HOME` is not set. JavaFX compile/FXML runtime was therefore not verified in this environment; source inspection verified the mappings.

`git ls-files` found no tracked `.env` files, and `git log --all -p -S "MAIL_PASSWORD=" -- "*.env"` found no matching history. `git check-ignore -v api/.env` confirms the ignored `**/.env` rule; `api/.env.example` is explicitly unignored.

## 12. Final Verdict

**READY AFTER MINOR FIXES**

The midterm core is implemented, connected, documented, and verified by five passing API tests. It is not a secure production system: REST endpoints are public, 2FA issues no JWT, and JavaFX could not run here because `JAVA_HOME` is missing. Those limitations are identified as final-phase work, but the presentation must state them plainly. Before submission, use working local PostgreSQL/mail configuration, verify JavaFX on the demo machine, and explain the TOTP-to-email deviation and final-scope boundary.
