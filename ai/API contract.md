27. API Contract (Source of Truth)

The following request and response payloads are authoritative.

Frontend, backend, tests, DTOs, services, and controllers must strictly follow these JSON structures.

No endpoint may return additional fields unless explicitly specified.

All UUID values are serialized as strings.

All monetary values use BigDecimal and are serialized as JSON numbers with 2 decimal places.

Dates:

- LocalDate → YYYY-MM-DD
- Timestamp → ISO-8601 UTC format

---

POST /api/auth/register

Request

{
  "name": "John Doe",
  "email": "john@example.com",
  "phone": "+8801712345678",
  "password": "password123"
}

Response (201)

{
  "token": "jwt-token",
  "userId": "uuid",
  "name": "John Doe",
  "email": "john@example.com"
}

---

POST /api/auth/login

Request

{
  "email": "john@example.com",
  "password": "password123"
}

Response (200)

{
  "token": "jwt-token",
  "userId": "uuid",
  "name": "John Doe",
  "email": "john@example.com"
}

---

GET /api/users/me

Response (200)

{
  "id": "uuid",
  "name": "John Doe",
  "email": "john@example.com"
}

---

PUT /api/users/me

Request

{
  "name": "John Doe",
  "phone": "+8801712345678"
}

Response (200)

{
  "id": "uuid",
  "name": "John Doe",
  "email": "john@example.com"
}

---

POST /api/groups

Request

{
  "name": "Berlin Trip",
  "description": "Weekend trip"
}

Response (201)

{
  "id": "uuid",
  "name": "Berlin Trip",
  "description": "Weekend trip",
  "createdBy": {
    "id": "uuid",
    "name": "John Doe",
    "email": "john@example.com"
  },
  "members": [
    {
      "userId": "uuid",
      "name": "John Doe",
      "email": "john@example.com",
      "role": "ADMIN",
      "joinedAt": "2026-06-24T12:00:00Z"
    }
  ],
  "createdAt": "2026-06-24T12:00:00Z"
}

---

GET /api/groups

Response (200)

[
  {
    "id": "uuid",
    "name": "Berlin Trip",
    "description": "Weekend trip",
    "memberCount": 5,
    "myNetBalance": 25.50
  }
]

---

GET /api/groups/{groupId}

Response (200)

{
  "id": "uuid",
  "name": "Berlin Trip",
  "description": "Weekend trip",
  "createdBy": {
    "id": "uuid",
    "name": "John Doe",
    "email": "john@example.com"
  },
  "members": [
    {
      "userId": "uuid",
      "name": "Alice",
      "email": "alice@example.com",
      "role": "ADMIN",
      "joinedAt": "2026-06-24T12:00:00Z"
    }
  ],
  "createdAt": "2026-06-24T12:00:00Z"
}

---

POST /api/groups/{groupId}/members

Request

{
  "email": "alice@example.com"
}

Response (201)

{
  "userId": "uuid",
  "name": "Alice",
  "email": "alice@example.com",
  "role": "MEMBER",
  "joinedAt": "2026-06-24T12:00:00Z"
}

---

POST /api/groups/{groupId}/events

Request

{
  "title": "Dinner",
  "description": "Italian restaurant",
  "eventDate": "2026-06-24"
}

Response (201)

{
  "id": "uuid",
  "title": "Dinner",
  "description": "Italian restaurant",
  "eventDate": "2026-06-24",
  "expenseCount": 0,
  "totalAmount": 0.00,
  "createdAt": "2026-06-24T12:00:00Z"
}

---

GET /api/groups/{groupId}/events

Response (200)

[
  {
    "id": "uuid",
    "title": "Dinner",
    "description": "Italian restaurant",
    "eventDate": "2026-06-24",
    "expenseCount": 3,
    "totalAmount": 150.00,
    "createdAt": "2026-06-24T12:00:00Z"
  }
]

---

GET /api/events/{eventId}

Response (200)

{
  "id": "uuid",
  "title": "Dinner",
  "description": "Italian restaurant",
  "eventDate": "2026-06-24",
  "createdBy": {
    "id": "uuid",
    "name": "John Doe",
    "email": "john@example.com"
  },
  "expenseCount": 3,
  "totalAmount": 150.00,
  "expenses": [
    {
      "id": "uuid",
      "description": "Dinner bill",
      "amount": 120.00,
      "splitType": "EQUAL",
      "paidBy": {
        "id": "uuid",
        "name": "John Doe",
        "email": "john@example.com"
      },
      "shares": [],
      "createdAt": "2026-06-24T12:00:00Z"
    }
  ],
  "createdAt": "2026-06-24T12:00:00Z"
}

---

POST /api/events/{eventId}/expenses

Request (EQUAL)

{
  "description": "Dinner bill",
  "amount": 120.00,
  "splitType": "EQUAL",
  "paidByUserId": "uuid",
  "participantIds": [
    "uuid1",
    "uuid2",
    "uuid3"
  ]
}

Request (EXACT)

{
  "description": "Dinner bill",
  "amount": 120.00,
  "splitType": "EXACT",
  "paidByUserId": "uuid",
  "shares": [
    {
      "userId": "uuid1",
      "value": 40.00
    },
    {
      "userId": "uuid2",
      "value": 80.00
    }
  ]
}

Request (PERCENTAGE)

{
  "description": "Dinner bill",
  "amount": 120.00,
  "splitType": "PERCENTAGE",
  "paidByUserId": "uuid",
  "shares": [
    {
      "userId": "uuid1",
      "value": 25
    },
    {
      "userId": "uuid2",
      "value": 75
    }
  ]
}

Response (201)

{
  "id": "uuid",
  "description": "Dinner bill",
  "amount": 120.00,
  "splitType": "EQUAL",
  "paidBy": {
    "id": "uuid",
    "name": "John Doe",
    "email": "john@example.com"
  },
  "shares": [
    {
      "userId": "uuid",
      "name": "Alice",
      "shareAmount": 40.00,
      "isSettled": false
    }
  ],
  "createdAt": "2026-06-24T12:00:00Z"
}

---

GET /api/events/{eventId}/expenses

Response (200)

[
  {
    "id": "uuid",
    "description": "Dinner bill",
    "amount": 120.00,
    "splitType": "EQUAL",
    "paidBy": {
      "id": "uuid",
      "name": "John Doe",
      "email": "john@example.com"
    },
    "shares": [],
    "createdAt": "2026-06-24T12:00:00Z"
  }
]

---

GET /api/groups/{groupId}/balances

Response (200)

{
  "groupId": "uuid",
  "userBalances": [
    {
      "userId": "uuid",
      "name": "Alice",
      "netBalance": 35.50
    }
  ],
  "suggestedPayments": [
    {
      "fromUserId": "uuid",
      "fromName": "Bob",
      "toUserId": "uuid",
      "toName": "Alice",
      "amount": 35.50
    }
  ]
}

---

POST /api/groups/{groupId}/settlements

Request

{
  "payeeId": "uuid",
  "amount": 35.50
}

Response (201)

{
  "id": "uuid",
  "groupId": "uuid",
  "payer": {
    "id": "uuid",
    "name": "Bob",
    "email": "bob@example.com"
  },
  "payee": {
    "id": "uuid",
    "name": "Alice",
    "email": "alice@example.com"
  },
  "amount": 35.50,
  "status": "COMPLETED",
  "settledAt": "2026-06-24T12:00:00Z"
}

---

GET /api/groups/{groupId}/settlements

Response (200)

[
  {
    "id": "uuid",
    "groupId": "uuid",
    "payer": {
      "id": "uuid",
      "name": "Bob",
      "email": "bob@example.com"
    },
    "payee": {
      "id": "uuid",
      "name": "Alice",
      "email": "alice@example.com"
    },
    "amount": 35.50,
    "status": "COMPLETED",
    "settledAt": "2026-06-24T12:00:00Z"
  }
]

---

Standard Error Response

{
  "timestamp": "2026-06-24T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/groups"
}

---

Validation Error Response

{
  "timestamp": "2026-06-24T12:00:00Z",
  "status": 400,
  "error": "Validation Failed",
  "message": "Invalid request",
  "path": "/api/groups",
  "validationErrors": {
    "name": "must not be blank",
    "email": "must be a valid email address"
  }
}

---

Authorization Error Response

{
  "timestamp": "2026-06-24T12:00:00Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid or expired token",
  "path": "/api/groups"
}

---

Forbidden Error Response

{
  "timestamp": "2026-06-24T12:00:00Z",
  "status": 403,
  "error": "Forbidden",
  "message": "You do not have permission to perform this action",
  "path": "/api/groups"
}