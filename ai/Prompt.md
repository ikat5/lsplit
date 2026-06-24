# L-Split — Master Build Prompt

## 0. Project Overview

Build **L-Split**, a full-stack Splitwise-style bill-splitting app. Users form
groups, log expenses under named events, and track who owes whom. The app
calculates net balances per user and suggests the minimum set of payments to
clear all debts.

Core user flows:
1. Register / Login
2. Create a group → add members by email
3. Create an event inside a group (e.g. "Weekend trip to Berlin")
4. Add expense items to the event — who paid, total, how it's split
5. View live group balances (net per person + suggested payments)
6. Record settlements (B paid A back $40)

---

## 1. Tech Stack & Pinned Versions

| Layer        | Technology                                               | Version                          |
|--------------|----------------------------------------------------------|----------------------------------|
| Frontend     | React + Vite                                             | React 18.3.x, Vite 5.x          |
| UI           | Bootstrap                                                | 5.3.x                            |
| HTTP client  | Axios                                                    | 1.x                              |
| Routing      | React Router                                             | 6.x                              |
| Forms        | React Hook Form                                          | latest                           |
| Backend      | Java + Spring Boot                                       | Java 21 LTS, Spring Boot 3.4.x   |
| ORM          | Spring Data JPA + Hibernate                              | (managed by Boot)                |
| Security     | Spring Security + JWT via `jjwt`                         | jjwt 0.12.6                      |
| Database     | PostgreSQL                                               | 16.x on Aiven                    |
| Migrations   | Flyway (SQL-based only)                                  | (managed by Boot)                |
| Build        | Maven                                                    | 3.9.x                            |
| Backend test | JUnit 5 + Mockito                                        | —                                |
| Frontend test| Vitest                                                   | —                                |
| CI/CD        | GitHub Actions                                           | Node 22 LTS, Java 21 Temurin     |
| Deployment   | Vercel (FE), Render (BE), Aiven (DB)                     | —                                |

---

## 2. Directory Structure

```
l-split/
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/lsplit/
│       │   │   ├── LSplitApplication.java
│       │   │   ├── config/
│       │   │   │   ├── SecurityConfig.java
│       │   │   │   ├── CorsConfig.java
│       │   │   │   ├── JwtUtil.java
│       │   │   │   └── UserDetailsServiceImpl.java
│       │   │   ├── filter/
│       │   │   │   └── JwtAuthFilter.java
│       │   │   ├── controller/
│       │   │   │   ├── AuthController.java
│       │   │   │   ├── UserController.java
│       │   │   │   ├── GroupController.java
│       │   │   │   ├── EventController.java
│       │   │   │   ├── ExpenseController.java
│       │   │   │   └── SettlementController.java
│       │   │   ├── dto/
│       │   │   │   ├── request/
│       │   │   │   └── response/
│       │   │   ├── entity/
│       │   │   │   ├── User.java
│       │   │   │   ├── Group.java
│       │   │   │   ├── GroupMember.java
│       │   │   │   ├── Event.java
│       │   │   │   ├── ExpenseItem.java
│       │   │   │   ├── ExpenseShare.java
│       │   │   │   └── Settlement.java
│       │   │   ├── model/
│       │   │   │   ├── SplitType.java        # EQUAL | EXACT | PERCENTAGE
│       │   │   │   └── SettlementStatus.java # PENDING | COMPLETED
│       │   │   ├── repository/
│       │   │   │   ├── UserRepository.java
│       │   │   │   ├── GroupRepository.java
│       │   │   │   ├── GroupMemberRepository.java
│       │   │   │   ├── EventRepository.java
│       │   │   │   ├── ExpenseItemRepository.java
│       │   │   │   ├── ExpenseShareRepository.java
│       │   │   │   └── SettlementRepository.java
│       │   │   ├── service/
│       │   │   │   ├── AuthService.java
│       │   │   │   ├── UserService.java
│       │   │   │   ├── GroupService.java
│       │   │   │   ├── EventService.java
│       │   │   │   ├── ExpenseService.java
│       │   │   │   ├── SettlementService.java
│       │   │   │   └── BalanceService.java
│       │   │   ├── mapper/
│       │   │   │   ├── UserMapper.java
│       │   │   │   ├── GroupMapper.java
│       │   │   │   ├── EventMapper.java
│       │   │   │   ├── ExpenseMapper.java
│       │   │   │   └── SettlementMapper.java
│       │   │   └── exception/
│       │   │       ├── GlobalExceptionHandler.java
│       │   │       ├── ResourceNotFoundException.java
│       │   │       ├── UnauthorizedException.java
│       │   │       └── BadRequestException.java
│       │   └── resources/
│       │       ├── application.yml
│       │       └── db/migration/
│       │           ├── V1__create_users_and_groups.sql
│       │           ├── V2__create_events_and_expenses.sql
│       │           └── V3__create_settlements.sql
│       └── test/java/com/lsplit/
│           ├── service/
│           │   ├── BalanceServiceTest.java
│           │   ├── ExpenseServiceTest.java
│           │   └── AuthServiceTest.java
│           └── controller/
│               └── AuthControllerTest.java
├── frontend/
│   ├── package.json
│   ├── vite.config.js
│   └── src/
│       ├── main.jsx
│       ├── App.jsx
│       ├── context/
│       │   └── AuthContext.jsx
│       ├── services/
│       │   ├── api.js             # Axios instance + interceptors
│       │   ├── authService.js
│       │   ├── groupService.js
│       │   ├── eventService.js
│       │   ├── expenseService.js
│       │   └── settlementService.js
│       ├── pages/
│       │   ├── Login.jsx
│       │   ├── Register.jsx
│       │   ├── Dashboard.jsx
│       │   ├── GroupView.jsx
│       │   └── EventView.jsx
│       ├── components/
│       │   ├── Navbar.jsx
│       │   ├── ProtectedRoute.jsx
│       │   ├── ExpenseCard.jsx
│       │   ├── BalanceSummary.jsx
│       │   ├── SuggestedPayments.jsx
│       │   ├── MemberList.jsx
│       │   ├── AddExpenseModal.jsx
│       │   └── AddMemberModal.jsx
│       └── utils/
│           ├── dateFormatter.js
│           ├── currencyFormatter.js
│           └── validators.js
├── .github/
│   └── workflows/
│       └── ci-cd.yml
├── .gitignore
└── README.md
```

---

## 3. Database Schema (Flyway — do not modify SQL at runtime)

```sql
-- V1__create_users_and_groups.sql
CREATE TABLE users (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name          VARCHAR(100)  NOT NULL,
    email         VARCHAR(255)  UNIQUE NOT NULL,
    phone         VARCHAR(20),
    password_hash VARCHAR(255)  NOT NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE groups (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    created_by  UUID REFERENCES users(id),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE group_members (
    group_id  UUID REFERENCES groups(id) ON DELETE CASCADE,
    user_id   UUID REFERENCES users(id)  ON DELETE CASCADE,
    role      VARCHAR(20) DEFAULT 'MEMBER',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (group_id, user_id)
);

CREATE INDEX idx_gm_group ON group_members(group_id);
CREATE INDEX idx_gm_user  ON group_members(user_id);

-- V2__create_events_and_expenses.sql
CREATE TABLE events (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    group_id    UUID REFERENCES groups(id) ON DELETE CASCADE,
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    event_date  DATE,
    created_by  UUID REFERENCES users(id),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_events_group ON events(group_id);

CREATE TABLE expense_items (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id    UUID REFERENCES events(id) ON DELETE CASCADE,
    description VARCHAR(255)    NOT NULL,
    amount      DECIMAL(10, 2)  NOT NULL,
    split_type  VARCHAR(20)     DEFAULT 'EQUAL',
    paid_by     UUID REFERENCES users(id),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_expenses_event ON expense_items(event_id);

CREATE TABLE expense_shares (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    expense_item_id UUID REFERENCES expense_items(id) ON DELETE CASCADE,
    user_id         UUID REFERENCES users(id),
    share_amount    DECIMAL(10, 2) NOT NULL,
    is_settled      BOOLEAN DEFAULT FALSE,
    settled_at      TIMESTAMP
);

CREATE INDEX idx_shares_expense ON expense_shares(expense_item_id);
CREATE INDEX idx_shares_user    ON expense_shares(user_id);

-- V3__create_settlements.sql
CREATE TABLE settlements (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    group_id   UUID REFERENCES groups(id) ON DELETE CASCADE,
    payer_id   UUID REFERENCES users(id),
    payee_id   UUID REFERENCES users(id),
    amount     DECIMAL(10, 2) NOT NULL,
    status     VARCHAR(20) DEFAULT 'COMPLETED',
    settled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_settlements_group ON settlements(group_id);
CREATE INDEX idx_settlements_payer ON settlements(payer_id);
CREATE INDEX idx_settlements_payee ON settlements(payee_id);
```

---

## 4. Backend: pom.xml Dependencies

Parent: `spring-boot-starter-parent 3.4.x`

```xml
<dependencies>
  <dependency>spring-boot-starter-web</dependency>
  <dependency>spring-boot-starter-data-jpa</dependency>
  <dependency>spring-boot-starter-security</dependency>
  <dependency>spring-boot-starter-validation</dependency>
  <dependency>postgresql (runtime)</dependency>
  <dependency>flyway-core</dependency>
  <dependency>flyway-database-postgresql</dependency>  <!-- required for Flyway 10.x / Spring Boot 3.4.x -->

  <!-- JWT: jjwt-api, jjwt-impl (runtime), jjwt-jackson (runtime), all 0.12.6 -->

  <dependency>lombok (optional)</dependency>
  <dependency>spring-boot-starter-test (test)</dependency>
</dependencies>
```

---

## 5. Backend: Security & JWT

### JwtUtil.java
- `generateToken(String email, UUID userId)` → HS256 signed JWT
  - subject = email
  - claim `"userId"` = userId.toString()
  - expiration = `app.jwt.expiration-ms` from config (86400000 = 24 h)
  - secret = HMAC key from `app.jwt.secret`
- `validateToken(String token)` → boolean (catch all exceptions → false)
- `getEmailFromToken(String token)` → String
- `getUserIdFromToken(String token)` → UUID

### JwtAuthFilter.java (extends OncePerRequestFilter)
1. Read `Authorization: Bearer <token>` header; skip if absent.
2. Call `jwtUtil.validateToken(token)`.
3. Load `UserDetails` by email via `UserDetailsService`.
4. Set `UsernamePasswordAuthenticationToken` in `SecurityContextHolder`.

### SecurityConfig.java
```
- SessionCreationPolicy.STATELESS
- CSRF disabled
- Public (permitAll): POST /api/auth/register, POST /api/auth/login
- Authenticated: all /api/** routes
- Insert JwtAuthFilter before UsernamePasswordAuthenticationFilter
- PasswordEncoder bean: BCryptPasswordEncoder
- CORS origins: https://<app>.vercel.app, http://localhost:5173
- CORS methods: GET, POST, PUT, DELETE, OPTIONS
- CORS headers: *
- CORS allowCredentials: false
```

### UserDetailsServiceImpl.java
- Implement `UserDetailsService`
- `loadUserByUsername(String email)` → look up `User` by email → wrap in Spring's `User` 
  (org.springframework.security.core.userdetails.User) with empty authorities

---

## 6. Backend: Entities

Use Lombok (`@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`).
All UUIDs are `@GeneratedValue(strategy = GenerationType.AUTO)` with  
`@Column(columnDefinition = "uuid", updatable = false)`.

**User** — maps to `users`  
Fields: id, name, email, phone, passwordHash, createdAt

**Group** — maps to `groups`  
Fields: id, name, description, createdBy (ManyToOne User), createdAt  
`@Table(name = "\"groups\"")`  (groups is a reserved word in some dialects)

**GroupMember** — maps to `group_members`  
Composite PK via `@Embeddable GroupMemberId { UUID groupId; UUID userId; }`.  
Fields: id (EmbeddedId), group (ManyToOne), user (ManyToOne), role (String default MEMBER), joinedAt  
**JPA column conflict fix**: the `@EmbeddedId` fields (`groupId`, `userId`) and the `@ManyToOne` join columns map to the same DB columns. Annotate the `@ManyToOne` sides with `insertable = false, updatable = false` to avoid a Hibernate `MappingException`:
```java
@ManyToOne
@JoinColumn(name = "group_id", insertable = false, updatable = false)
private Group group;

@ManyToOne
@JoinColumn(name = "user_id", insertable = false, updatable = false)
private User user;
```

**Event** — maps to `events`  
Fields: id, group (ManyToOne), title, description, eventDate (LocalDate), createdBy (ManyToOne User), createdAt
Deletion permissions: creator OR group ADMIN

**ExpenseItem** — maps to `expense_items`  
Fields: id, event (ManyToOne), description, amount (BigDecimal), splitType (Enum SplitType), paidBy (ManyToOne User), createdAt  
`@OneToMany(mappedBy="expenseItem", cascade=ALL, orphanRemoval=true) List<ExpenseShare> shares`

**ExpenseShare** — maps to `expense_shares`  
Fields: id, expenseItem (ManyToOne), user (ManyToOne), shareAmount (BigDecimal), isSettled (boolean default false), settledAt

**Settlement** — maps to `settlements`  
Fields: id, group (ManyToOne), payer (ManyToOne User), payee (ManyToOne User), amount (BigDecimal), status (Enum SettlementStatus default COMPLETED), settledAt

---

## 7. Backend: DTOs

### Request DTOs (use Bean Validation annotations)

```
RegisterRequest:
  @NotBlank name (max 100)
  @Email @NotBlank email
  phone (optional, max 20)
  @NotBlank @Size(min=8) password

LoginRequest:
  @Email @NotBlank email
  @NotBlank password

UpdateProfileRequest:
  @NotBlank name (max 100)
  phone (optional)

CreateGroupRequest:
  @NotBlank name (max 255)
  description (optional)

AddMemberRequest:
  @Email @NotBlank email   ← look up user by email server-side

CreateEventRequest:
  @NotBlank title (max 255)
  description (optional)
  eventDate (optional, ISO date string)

CreateExpenseRequest:
  @NotBlank description (max 255)
  @NotNull @Positive amount (BigDecimal)
  @NotNull splitType (EQUAL | EXACT | PERCENTAGE)
  @NotNull paidByUserId (UUID)
  @NotEmpty participantIds (List<UUID>)        ← for EQUAL split
  shares (List<ShareInput>)                    ← for EXACT / PERCENTAGE
    ShareInput: userId (UUID), value (BigDecimal)
    For EXACT: value = exact dollar amount per user
    For PERCENTAGE: value = percentage (must sum to 100)

CreateSettlementRequest:
  @NotNull payeeId (UUID)
  @NotNull @Positive amount (BigDecimal)
```

### Response DTOs

```
AuthResponse:
  token, userId, name, email

UserSummaryResponse:
  id, name, email

MemberResponse:
  userId, name, email, role, joinedAt

GroupSummaryResponse:                ← for Dashboard list
  id, name, description, memberCount, myNetBalance

GroupDetailResponse:
  id, name, description,
  createdBy (UserSummaryResponse),
  members (List<MemberResponse>),
  createdAt

EventDetailResponse:
  id, title, description, eventDate, 
  createdBy (UserSummaryResponse),
  expenses (List<ExpenseItemResponse>),
  totalAmount, expenseCount, createdAt

EventSummaryResponse:                ← used for list endpoints (no createdBy, no expenses)
  id, title, description, eventDate,
  expenseCount, totalAmount, createdAt

ExpenseShareResponse:
  userId, name, shareAmount, isSettled

ExpenseItemResponse:
  id, description, amount, splitType,
  paidBy (UserSummaryResponse),
  shares (List<ExpenseShareResponse>),
  createdAt

ValidationErrorResponse:
  timestamp, status, error, message,
  path, validationErrors (Map<String,String>)

GroupBalanceResponse:
  groupId,
  userBalances (List<UserBalanceEntry>),
    UserBalanceEntry: userId, name, netBalance (BigDecimal, positive = owed money, negative = owes money)
  suggestedPayments (List<SuggestedPayment>)
    SuggestedPayment: fromUserId, fromName, toUserId, toName, amount

SettlementResponse:
  id, groupId,
  payer (UserSummaryResponse),
  payee (UserSummaryResponse),
  amount, status, settledAt

ErrorResponse:
  timestamp, status, error, message, path
```

---

## 8. Backend: Repositories

All extend `JpaRepository<Entity, UUID>`.

```
UserRepository:
  Optional<User> findByEmail(String email)
  boolean existsByEmail(String email)

GroupRepository:
  (standard only)

GroupMemberRepository:
  // Navigate via @ManyToOne relationships (not embedded ID fields directly)
  List<GroupMember> findByGroup_Id(UUID groupId)
  List<GroupMember> findByUser_Id(UUID userId)
  Optional<GroupMember> findByGroup_IdAndUser_Id(UUID groupId, UUID userId)
  boolean existsByGroup_IdAndUser_Id(UUID groupId, UUID userId)

  @Modifying
  @Query("DELETE FROM GroupMember gm WHERE gm.group.id = :groupId AND gm.user.id = :userId")
  void deleteByGroupIdAndUserId(UUID groupId, UUID userId)

EventRepository:
  List<Event> findByGroupId(UUID groupId)

ExpenseItemRepository:
  List<ExpenseItem> findByEventId(UUID eventId)
  
  @Query("SELECT e FROM ExpenseItem e WHERE e.event.group.id = :groupId")
  List<ExpenseItem> findByGroupId(UUID groupId)

ExpenseShareRepository:
  List<ExpenseShare> findByExpenseItemId(UUID expenseItemId)
  
  @Query("""
    SELECT es FROM ExpenseShare es
    WHERE es.expenseItem.event.group.id = :groupId
  """)
  List<ExpenseShare> findByGroupId(UUID groupId)

SettlementRepository:
  List<Settlement> findByGroupId(UUID groupId)
```

---

## 9. Backend: Services

### AuthService
- `register(RegisterRequest)` → check email not taken → hash password (BCrypt) → save User → generate + return AuthResponse
- `login(LoginRequest)` → find user by email → verify password → generate + return AuthResponse

### UserService
- `getProfile(UUID userId)` → UserSummaryResponse
- `updateProfile(UUID userId, UpdateProfileRequest)` → UserSummaryResponse

### GroupService
- `createGroup(UUID creatorId, CreateGroupRequest)` → save group → add creator as ADMIN member → GroupDetailResponse
- `getMyGroups(UUID userId)` → find all groups where userId is a member → List<GroupSummaryResponse> (include myNetBalance from BalanceService)
- `getGroupDetail(UUID groupId, UUID requesterId)` → verify requester is member → GroupDetailResponse
- `addMember(UUID groupId, UUID requesterId, AddMemberRequest)` → requester must be ADMIN → find user by email → add as MEMBER → MemberResponse
- `removeMember(UUID groupId, UUID requesterId, UUID targetUserId)` → requester must be ADMIN → delete GroupMember
- `deleteGroup(UUID groupId, UUID requesterId)` → requester must be ADMIN → delete group (cascade handles rest)

### Group Administration Rules

Roles:
ADMIN
MEMBER

Creator becomes ADMIN automatically.

Rules:

- ADMIN can add members.
- ADMIN can remove members.
- MEMBER cannot manage membership.

Restrictions:

- ADMIN cannot remove themselves.
- Group must always have at least one ADMIN.
- Last ADMIN cannot be removed.
- Group creator cannot lose ADMIN role.

### EventService
- `createEvent(UUID groupId, UUID creatorId, CreateEventRequest)` → verify creator is group member → save → EventSummaryResponse
- `getGroupEvents(UUID groupId, UUID requesterId)` → verify requester is member → List<EventSummaryResponse>
- `getEventDetail(UUID eventId, UUID requesterId)` → verify requester is in same group → EventDetailResponse
- `deleteEvent(UUID eventId, UUID requesterId)` → creator or group ADMIN only

### ExpenseService
- `addExpense(UUID eventId, UUID creatorId, CreateExpenseRequest)`:
  1. Verify creator is group member.
  2. Verify paidByUserId is group member.
  3. Calculate shares:
     - EQUAL: `shareAmount = totalAmount / participantIds.size()` for each participant.
       Round using HALF_UP; adjust last share if rounding error.
     - EXACT: use provided values; validate sum == totalAmount.
     - PERCENTAGE: validate percentages sum to 100; compute `amount * pct / 100`; adjust last share for rounding.
  4. Save ExpenseItem + ExpenseShare rows.
  5. Return ExpenseItemResponse.
- `getEventExpenses(UUID eventId, UUID requesterId)` → List<ExpenseItemResponse>
- `deleteExpense(UUID expenseId, UUID requesterId)` → creator or group ADMIN only

### Monetary Validation Rules

Use BigDecimal everywhere.

Never compare BigDecimal using equals().

Use compareTo().

For EXACT split:
- sum(shares) must equal amount

Validation:
sharesTotal.compareTo(amount) == 0

For PERCENTAGE split:
- percentages must sum exactly to 100

Validation:
percentageTotal.compareTo(BigDecimal.valueOf(100)) == 0

Scale:
2 decimal places

Rounding:
RoundingMode.HALF_UP

### BalanceService
- `getGroupBalances(UUID groupId)` → GroupBalanceResponse

  **Net balance algorithm:**
  ```
  For each member U in group:
    totalPaid(U)      = SUM(expense_items.amount WHERE paid_by = U
                            AND expense is in an event of this group)

    totalOwed(U)      = SUM(expense_shares.share_amount WHERE user_id = U
                            AND expense is in this group)

    settlementsPaid(U) = SUM(settlements.amount WHERE payer_id = U AND group_id = group)
    settlementsReceived(U) = SUM(settlements.amount WHERE payee_id = U AND group_id = group)

    netBalance(U) = totalPaid(U) - totalOwed(U)
                   + settlementsPaid(U)    ← money user has already paid toward settling debt
                   - settlementsReceived(U) ← money user has already received from others
  ```
  
  Positive netBalance → others owe this user.  
  Negative netBalance → this user owes others.

  **Debt simplification algorithm (minimise transaction count):**
  ```
  debtors   = [u for u in members if netBalance(u) < 0], sorted ascending by balance
  creditors = [u for u in members if netBalance(u) > 0], sorted descending by balance
  payments  = []

  while debtors and creditors not empty:
    debtor   = debtors[0]
    creditor = creditors[0]
    amount   = min(abs(debtor.balance), creditor.balance)
    payments.append(SuggestedPayment(debtor → creditor, amount))
    debtor.balance   += amount
    creditor.balance -= amount
    if debtor.balance   == 0: debtors.remove(debtor)
    if creditor.balance == 0: creditors.remove(creditor)

  return payments
  ```

- `getUserNetBalance(UUID groupId, UUID userId)` → BigDecimal (used by GroupService for dashboard summaries)

### SettlementService
- `createSettlement(UUID groupId, UUID payerId, CreateSettlementRequest)`:
  1. Verify payer and payee are both group members.
  2. Save Settlement with status = COMPLETED.
  3. Return SettlementResponse.
- `getGroupSettlements(UUID groupId, UUID requesterId)` → List<SettlementResponse>

---

## 10. Backend: API Endpoints

Base path: `/api`

```
# Auth (public)
POST   /api/auth/register          RegisterRequest   → 201 AuthResponse
POST   /api/auth/login             LoginRequest      → 200 AuthResponse

# Profile
GET    /api/users/me               —                 → 200 UserSummaryResponse
PUT    /api/users/me               UpdateProfileReq  → 200 UserSummaryResponse

# Groups
POST   /api/groups                 CreateGroupRequest → 201 GroupDetailResponse
GET    /api/groups                 —                 → 200 List<GroupSummaryResponse>
GET    /api/groups/{groupId}       —                 → 200 GroupDetailResponse
DELETE /api/groups/{groupId}       —                 → 204

# Group Members
POST   /api/groups/{groupId}/members   AddMemberRequest  → 201 MemberResponse
DELETE /api/groups/{groupId}/members/{userId} —       → 204

# Events
POST   /api/groups/{groupId}/events    CreateEventRequest → 201 EventSummaryResponse
GET    /api/groups/{groupId}/events    —                  → 200 List<EventSummaryResponse>
GET    /api/events/{eventId}           —                  → 200 EventDetailResponse
DELETE /api/events/{eventId}           —                  → 204

# Expenses
POST   /api/events/{eventId}/expenses  CreateExpenseRequest → 201 ExpenseItemResponse
GET    /api/events/{eventId}/expenses  —                    → 200 List<ExpenseItemResponse>
DELETE /api/expenses/{expenseId}       —                    → 204

# Balances
GET    /api/groups/{groupId}/balances  —  → 200 GroupBalanceResponse

# Settlements
POST   /api/groups/{groupId}/settlements CreateSettlementRequest → 201 SettlementResponse
GET    /api/groups/{groupId}/settlements —                       → 200 List<SettlementResponse>
```

### Authenticated User Retrieval

Controllers must never receive userId in request body.

Retrieve authenticated user from SecurityContext.

Standard pattern:

Authentication auth =
SecurityContextHolder.getContext().getAuthentication();

String email = auth.getName();

Look up current user using UserRepository.

All services requiring requester identity must use this authenticated user.

**HTTP status conventions:**
- 200 OK, 201 Created, 204 No Content
- 400 Bad Request (validation errors → field-level map)
- 401 Unauthorized (missing/invalid token)
- 403 Forbidden (authenticated but not permitted)
- 404 Not Found (resource doesn't exist)

---

## 11. Backend: Exception Handling

```java
// GlobalExceptionHandler (@ControllerAdvice)

@ExceptionHandler(ResourceNotFoundException.class) → 404
@ExceptionHandler(UnauthorizedException.class)     → 401
@ExceptionHandler(BadRequestException.class)       → 400
@ExceptionHandler(MethodArgumentNotValidException.class) → 400
  // build map: field → defaultMessage for each FieldError
@ExceptionHandler(Exception.class)                → 500 (log, generic message)
```

All responses use `ErrorResponse` DTO.

---

## 12. Backend: application.yml

```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 5
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
  flyway:
    enabled: true
    locations: classpath:db/migration

app:
  jwt:
    secret: ${JWT_SECRET}
    expiration-ms: 86400000

server:
  port: ${PORT:8080}
```

---

## 13. Frontend: Setup

```bash
npm create vite@latest frontend -- --template react
cd frontend
npm install axios react-router-dom bootstrap react-hook-form
```

### vite.config.js
```js
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    // Proxy /api requests to the backend in development so that
    // VITE_API_BASE_URL can be set to the relative path '/api'.
    // Without this proxy the env var must be a full absolute URL,
    // making the proxy entry below unused.
    proxy: {
      '/api': 'http://localhost:8080'
    }
  }
})
```

### src/main.jsx
```jsx
import 'bootstrap/dist/css/bootstrap.min.css'
import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App'

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode><App /></React.StrictMode>
)
```

---

## 14. Frontend: Axios Instance & Interceptors

### src/services/api.js
```js
import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL  // '/api' in dev (proxied by Vite), full URL in production
})

// Request interceptor: attach JWT
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// Response interceptor: redirect to /login on 401
api.interceptors.response.use(
  res => res,
  err => {
    if (err.response?.status === 401) {
      localStorage.removeItem('token')
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

export default api
```

---

## 15. Frontend: Auth Context

### src/context/AuthContext.jsx
```
State: { user: { id, name, email } | null, token: string | null }

On mount: read token from localStorage → decode payload → restore user state
          (use jwtDecode or parse base64 manually — no extra lib needed)

Methods:
  login(authResponse)  → store token in localStorage + set user state
  logout()             → remove token from localStorage + clear user state

Wrap entire app with <AuthContext.Provider>
```

### src/components/ProtectedRoute.jsx
```
If !user → <Navigate to="/login" replace />
Else     → <Outlet />
```

### src/App.jsx routing structure
```
/login          → <Login />       (public)
/register       → <Register />    (public)

Protected (via ProtectedRoute):
  /             → <Dashboard />
  /groups/:id   → <GroupView />
  /events/:id   → <EventView />
```

---

## 16. Frontend: Service Layer

Each service file wraps `api.js` calls. Return `response.data`.

### authService.js
```js
register(data)  → POST /auth/register
login(data)     → POST /auth/login
getProfile()    → GET  /users/me
```

### groupService.js
```js
create(data)                  → POST /groups
getAll()                      → GET  /groups
getById(id)                   → GET  /groups/:id
addMember(groupId, data)      → POST /groups/:id/members
removeMember(groupId, userId) → DELETE /groups/:groupId/members/:userId
delete(id)                    → DELETE /groups/:id
getBalances(groupId)          → GET  /groups/:id/balances
```

### eventService.js
```js
create(groupId, data)  → POST /groups/:groupId/events
getAll(groupId)        → GET  /groups/:groupId/events
getById(eventId)       → GET  /events/:eventId
delete(eventId)        → DELETE /events/:eventId
```

### expenseService.js
```js
create(eventId, data)  → POST /events/:eventId/expenses
getAll(eventId)        → GET  /events/:eventId/expenses
delete(expenseId)      → DELETE /expenses/:expenseId
```

### settlementService.js
```js
create(groupId, data)  → POST  /groups/:groupId/settlements
getAll(groupId)        → GET   /groups/:groupId/settlements
```

---

## 17. Frontend: Pages

### Login.jsx
- React Hook Form: email + password fields
- On submit: call `authService.login` → call `login()` from AuthContext → navigate to `/`
- Link to `/register`

### Register.jsx
- React Hook Form: name, email, phone (optional), password, confirmPassword
- Client-side: `confirmPassword === password`
- On submit: call `authService.register` → auto-login → navigate to `/`

### Dashboard.jsx
- On mount: `groupService.getAll()` → store in state
- Display list of `GroupSummaryCards` showing group name, member count, and the current user's net balance (color: green if positive, red if negative, grey if zero)
- "New Group" button → Bootstrap modal with `CreateGroupRequest` form

### GroupView.jsx (route: /groups/:id)
- On mount: parallel fetch `groupService.getById(id)` + `groupService.getBalances(id)` + `eventService.getAll(id)`
- Tabs or sections: **Members** | **Events** | **Balances** | **Settlements**
- Members tab: `<MemberList>` + "Add Member" button (ADMIN only) → `<AddMemberModal>`
- Events tab: list of event cards with `eventDate`, `totalAmount`, `expenseCount` → click navigates to `/events/:eventId`; "New Event" button
- Balances tab: `<BalanceSummary>` showing net per user + `<SuggestedPayments>` list + "Record Settlement" button
- Settlements tab: chronological list of past settlements

### EventView.jsx (route: /events/:id)
- On mount: `eventService.getById(id)` → loads EventSummaryResponse + expense list
- Shows event title, date, total amount
- List of `<ExpenseCard>` components
- "Add Expense" button → `<AddExpenseModal>`

---

## 18. Frontend: Components

### Navbar.jsx
- Brand name "L-Split" → links to `/`
- If authenticated: show user's name + "Logout" button
- If not: "Login" / "Register" links

### ExpenseCard.jsx
Props: `expense` (ExpenseItemResponse)
- Shows description, amount, "Paid by {name}"
- Collapsible share breakdown showing each member's share and ✓ if settled
- Delete button (if current user is creator)

### BalanceSummary.jsx
Props: `balances` (List<UserBalanceEntry>)
- Table or list: Name | Net Balance
- Positive → green badge ("Gets back"), negative → red badge ("Owes"), zero → grey

### SuggestedPayments.jsx
Props: `payments` (List<SuggestedPayment>)
- "{fromName} → {toName}: {amount}" per row
- "Settle" button on each row → prefills `CreateSettlementRequest` and calls settlementService

### MemberList.jsx
Props: `members` (List<MemberResponse>), `isAdmin` (bool), `onRemove`
- Table: avatar initial, name, email, role badge
- Remove button (ADMIN only, cannot remove self)

### AddExpenseModal.jsx
- React Hook Form: description, amount, splitType selector, paidBy selector (dropdown of group members), participant multi-select
- Conditional share inputs: for EQUAL show nothing extra; for EXACT show amount per participant; for PERCENTAGE show % per participant with running sum
- Validate before submit; call `expenseService.create`

### AddMemberModal.jsx
- Single email input → `groupService.addMember`

### Frontend Error Handling

401:
- clear localStorage
- redirect to /login

403:
- show Bootstrap alert

404:
- show Not Found message

500:
- show generic error message

Forms:
- show validation errors below fields
- disable submit button while request is pending

Loading:
- show Bootstrap spinner during API requests

---

## 19. Frontend: .env files

```
# .env.development
# Relative path — requests are proxied by Vite to http://localhost:8080
VITE_API_BASE_URL=/api

# .env.production
VITE_API_BASE_URL=https://<your-render-app>.onrender.com/api
```

---

## 20. Backend: .env (never commit)

```
DB_URL=jdbc:postgresql://<host>:<port>/<dbname>?sslmode=require
DB_USERNAME=<username>
DB_PASSWORD=<password>
JWT_SECRET=<generate with: openssl rand -hex 32>
```

---

## 21. CI/CD — .github/workflows/ci-cd.yml

```yaml
name: L-Split CI/CD

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [develop]

jobs:
  backend-test:
    runs-on: ubuntu-24.04
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
      - run: cd backend && mvn test

  frontend-test:
    runs-on: ubuntu-24.04
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: '22'
      - run: cd frontend && npm ci && npm run test

  deploy-backend:
    needs: backend-test
    if: github.ref == 'refs/heads/main'
    runs-on: ubuntu-24.04
    steps:
      - run: curl -X POST ${{ secrets.RENDER_DEPLOY_HOOK }}

  deploy-frontend:
    needs: frontend-test
    if: github.ref == 'refs/heads/main'
    runs-on: ubuntu-24.04
    steps:
      - run: curl -X POST ${{ secrets.VERCEL_DEPLOY_HOOK }}
```

GitHub repository secrets required: `RENDER_DEPLOY_HOOK`, `VERCEL_DEPLOY_HOOK`

---

## 22. Build Order

Follow this sequence to avoid broken dependencies:

1. **Repo & infra** — init Git, create GitHub repo, provision Aiven DB, set up Vercel + Render projects
2. **Backend scaffold** — Spring Initializr → paste pom.xml deps → application.yml → Flyway migrations (V1, V2, V3) → verify DB connection
3. **Entities** — implement all 7 entity classes with JPA annotations
4. **Repositories** — implement all 7 repositories
5. **Security** — JwtUtil → UserDetailsServiceImpl → JwtAuthFilter → SecurityConfig
6. **Auth endpoints** — AuthService → AuthController → test with Postman/curl
7. **Group + Member endpoints** — GroupService → GroupController
8. **Event endpoints** — EventService → EventController
9. **Expense endpoints** — ExpenseService (EQUAL first, then EXACT/PERCENTAGE) → ExpenseController
10. **Balance + Settlement** — BalanceService (net calc first, then simplification) → SettlementService → endpoints
11. **Exception handling** — GlobalExceptionHandler (add after each layer to test)
12. **Backend tests** — BalanceServiceTest, ExpenseServiceTest, AuthControllerTest
13. **CI/CD pipeline** — push, verify Actions green
14. **Frontend scaffold** — Vite + install deps → api.js → AuthContext → ProtectedRoute → routing skeleton
15. **Auth pages** — Login, Register (wire to real API)
16. **Dashboard** — group list + create group modal
17. **Group view** — members, events, balances, settlements tabs
18. **Event view** — expense list + AddExpenseModal (all 3 split types)
19. **Components** — BalanceSummary, SuggestedPayments, MemberList, ExpenseCard
20. **Frontend tests** — Vitest for services and balance display logic
21. **Production deploy** — set env vars in Render + Vercel dashboards → push to main → verify

---

## 23. Key Rules

- **Never** expose JPA entities directly in REST responses — always map to DTOs.
- **Never** write raw SQL in Java — JPA repos only; SQL lives exclusively in Flyway migrations.
- **Never** hardcode secrets — all sensitive values via environment variables.
- **Never** use Redux — React Context is sufficient for auth state.
- **No** custom crypto — BCrypt via Spring Security only.
- Use `BigDecimal` (not `double`) for all monetary amounts throughout backend.
- Round monetary splits using `RoundingMode.HALF_UP`; absorb rounding remainder into the last participant's share.
- Enforce group membership on every request that touches group-scoped data.
- Return 403 (not 404) when a resource exists but the requester lacks permission.

---

# 24. Scalability & Architecture Directives

- Ensure the application remains entirely stateless using JWT, allowing horizontal scaling without session replication.
- Restrict the HikariCP connection pool to maximum-pool-size: 5 to comply with Aiven free tier limitations.
- Avoid Microservices, GraphQL, and WebSockets; maintain a RESTful modular monolith as it is optimal for this scale.
- Do not use local Docker or Kubernetes setups; rely strictly on Render's auto-containerization to avoid slowing down the development feedback loop.

---

## 25. Mapping Layer

Never map entities directly inside controllers.

Create:

mapper/
  UserMapper
  GroupMapper
  EventMapper
  ExpenseMapper
  SettlementMapper

Responsibilities:

Entity -> DTO
DTO -> Entity

Controllers call services.

Services call mappers.

Controllers never touch JPA entities.

---

## 26. Output Requirements

Generate every file listed in the directory structure.

Do not generate pseudocode.

Do not generate TODO comments.

Do not omit any class.

Do not leave any method unimplemented.

All Java code must compile.

All React code must compile.

All imports must be included.

All DTO mappings must be implemented.

All repositories must be implemented.

All controllers must be implemented.

All services must be implemented.

Output format:

=== FILE: backend/src/main/java/... ===

<file content>

=== FILE: frontend/src/... ===

<file content>

Continue generating files until the entire project is complete.

If output limit is reached:
continue automatically in the next response from the next file.

---