# ☀️ Sun King PAYG Solar System Backend

A production-ready **Pay-As-You-Go (PAYG) solar system backend** built with **Java 17 + Spring Boot 3.2**, featuring JWT-based RBAC, automated device locking, mobile money gateway integration, and full payment lifecycle management.

---

## 📋 Table of Contents
- [Architecture Overview](#-architecture-overview)
- [Tech Stack](#-tech-stack)
- [Setup Instructions](#-setup-instructions)
- [API Documentation](#-api-documentation)
- [Database Schema](#-database-schema)
- [Design Decisions](#-design-decisions)
- [Trade-offs](#-trade-offs)
- [Scalability](#-scalability)

---

## 🏗 Architecture Overview

```
┌─────────────────────────────────────────────┐
│              REST Clients / Postman          │
└─────────────────┬───────────────────────────┘
                  │ HTTPS
┌─────────────────▼───────────────────────────┐
│        Spring Boot 3.2 Application           │
│  ┌──────────────────────────────────────┐   │
│  │  Security Layer (JWT + Spring Sec)   │   │
│  └──────────────┬───────────────────────┘   │
│  ┌──────────────▼───────────────────────┐   │
│  │   Controllers (REST API Layer)       │   │
│  │   Auth / Customer / Device / Payment │   │
│  └──────────────┬───────────────────────┘   │
│  ┌──────────────▼───────────────────────┐   │
│  │   Service Layer (Business Logic)     │   │
│  │   + DeviceLockingScheduler (cron)    │   │
│  └──────────────┬───────────────────────┘   │
│  ┌──────────────▼───────────────────────┐   │
│  │   Repository Layer (Spring Data JPA) │   │
│  └──────────────┬───────────────────────┘   │
│  ┌──────────────▼───────────────────────┐   │
│  │   External Integration               │   │
│  │   MobileMoneyGatewayClient (Retry)   │   │
│  └──────────────────────────────────────┘   │
└─────────────────┬───────────────────────────┘
                  │
┌─────────────────▼───────────────────────────┐
│     PostgreSQL Database (Flyway Managed)     │
│  customers | devices | device_assignments    │
│  payments  | app_users                       │
└─────────────────────────────────────────────┘
```

---

## 🛠 Tech Stack

| Component | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2 |
| Database | PostgreSQL 16 (H2 for local/test) |
| Migrations | Flyway |
| Security | Spring Security + JWT (JJWT 0.12) |
| API Docs | SpringDoc OpenAPI 3 (Swagger UI) |
| Retries | Spring Retry (exponential backoff) |
| Caching | Caffeine |
| Scheduling | Spring `@Scheduled` |
| Container | Docker + Docker Compose |
| Build | Maven |
| Testing | JUnit 5 + MockMvc |

---

## 🚀 Setup Instructions

### Prerequisites
- Java 17+
- Maven 3.9+
- (Optional) Docker & Docker Compose for full stack

---

### Option 1: Run Locally with H2 (No DB needed)

```bash
# Clone the repository
git clone <repo-url>
cd payg-solar-backend

# Run with local profile (H2 in-memory database)
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

**Default credentials:**
- Admin: `admin` / `Admin@123`
- Agent: `agent` / `Admin@123`

**Swagger UI:** http://localhost:8080/swagger-ui.html  
**H2 Console:** http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:paygdb`)

---

### Option 2: Run with Docker Compose (Full Stack)

```bash
# Start PostgreSQL + Spring Boot
docker-compose up --build

# Application: http://localhost:8080/swagger-ui.html
```

---

### Option 3: Run with Real PostgreSQL

```bash
# Set environment variables
export DB_URL=jdbc:postgresql://localhost:5432/paygdb
export DB_USERNAME=payguser
export DB_PASSWORD=yourpassword
export JWT_SECRET=<base64-encoded-256-bit-secret>

# Run with prod profile
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### Running Tests

```bash
mvn test
```

---

## 📡 API Documentation

**Swagger UI (interactive):** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

### Endpoints Summary

| Method | Path | Description | Role |
|---|---|---|---|
| `POST` | `/api/v1/auth/login` | Authenticate and get JWT | Public |
| `POST` | `/api/v1/customers` | Create a new customer | ADMIN, AGENT |
| `GET` | `/api/v1/customers/{id}` | Get customer by ID | Any authenticated |
| `GET` | `/api/v1/customers` | List customers (paginated) | ADMIN, AGENT |
| `POST` | `/api/v1/devices` | Register a solar device | ADMIN |
| `POST` | `/api/v1/devices/{id}/assign` | Assign device to customer | ADMIN, AGENT |
| `GET` | `/api/v1/devices/{id}` | Get device details | Any authenticated |
| `GET` | `/api/v1/devices/{id}/status` | Get device PAYG status | Any authenticated |
| `GET` | `/api/v1/devices` | List devices (paginated) | Any authenticated |
| `POST` | `/api/v1/payments` | Record a payment | Any authenticated |
| `GET` | `/api/v1/payments/{customerId}` | Get customer payment history | Any authenticated |
| `GET` | `/api/v1/payments/assignment/{id}` | Get assignment payment history | Any authenticated |

### Example: Login Flow
```bash
# 1. Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin@123"}'

# 2. Use token in subsequent requests
TOKEN="<jwt-token-from-response>"
curl http://localhost:8080/api/v1/customers \
  -H "Authorization: Bearer $TOKEN"
```

### Sample Payment Flow
```bash
# 1. Create customer
curl -X POST http://localhost:8080/api/v1/customers \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"firstName":"John","lastName":"Kamau","phoneNumber":"+254712345678","region":"Nairobi"}'

# 2. Register device (Admin only)
curl -X POST http://localhost:8080/api/v1/devices \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"serialNumber":"SK-001","model":"SunKing Pro 200","totalCost":15000,"dailyRate":100,"gracePeriodDays":3}'

# 3. Assign device
curl -X POST http://localhost:8080/api/v1/devices/<deviceId>/assign \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"customerId":"<customerId>"}'

# 4. Make payment
curl -X POST http://localhost:8080/api/v1/payments \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"customerId":"<customerId>","assignmentId":"<assignmentId>","amount":500,"paymentMethod":"MOBILE_MONEY","mobileNumber":"+254712345678","transactionReference":"TXN-001"}'

# 5. Check status
curl http://localhost:8080/api/v1/devices/<deviceId>/status \
  -H "Authorization: Bearer $TOKEN"
```

---

## 🗃 Database Schema

```
customers
├── id (UUID PK)
├── first_name, last_name
├── phone_number (UNIQUE, indexed)
├── email (UNIQUE, indexed)
├── national_id (UNIQUE, indexed)
├── region, address
├── status (ACTIVE | INACTIVE | BLACKLISTED)
└── created_at, updated_at

devices
├── id (UUID PK)
├── serial_number (UNIQUE, indexed)
├── model (indexed)
├── total_cost, daily_rate
├── grace_period_days
├── status (ACTIVE | INACTIVE | LOCKED)
└── created_at, updated_at

device_assignments
├── id (UUID PK)
├── customer_id (FK → customers, indexed)
├── device_id (FK → devices, indexed)
├── total_cost_at_assignment (snapshot)
├── daily_rate_at_assignment (snapshot)
├── amount_paid
├── next_payment_due_date (indexed)
├── last_payment_date
├── days_overdue
├── is_fully_paid
└── is_active

payments
├── id (UUID PK)
├── customer_id (FK, indexed)
├── assignment_id (FK, indexed)
├── amount
├── status (PENDING | SUCCESS | FAILED | REFUNDED)
├── payment_method (MOBILE_MONEY | CASH | BANK_TRANSFER | CARD)
├── transaction_reference (UNIQUE — idempotency)
├── external_transaction_id
├── retry_count
└── created_at (indexed)

app_users
├── id (UUID PK)
├── username (UNIQUE, indexed)
├── password_hash (BCrypt)
├── role (ADMIN | AGENT)
└── is_active
```

---

## 🧠 Design Decisions

### 1. UUID Primary Keys
Chosen over auto-increment integers to support horizontal scaling and eventual sharding. UUIDs prevent ID enumeration attacks and work with distributed systems.

### 2. Snapshot Pricing in DeviceAssignment
`total_cost_at_assignment` and `daily_rate_at_assignment` capture the device's pricing **at the time of assignment**. This ensures payment history remains accurate even if device pricing changes later.

### 3. Device Locking Scheduler
A `@Scheduled` cron job runs nightly at 01:00 AM to lock overdue devices. The query uses a **partial index** on `(is_active, is_fully_paid, next_payment_due_date)` for maximum efficiency at 1M+ scale.

### 4. Payment Idempotency
The `transaction_reference` field has a `UNIQUE` database constraint. If an API call is retried with the same reference, a `409 Conflict` is returned instead of creating a duplicate payment.

### 5. External Gateway with Spring Retry
The `MobileMoneyGatewayClient` uses `@Retryable` with exponential backoff (500ms → 5s). The `@Recover` fallback marks the payment as FAILED after all retries are exhausted.

### 6. H2 for Local Development
H2 in PostgreSQL compatibility mode allows developers to run the entire application without installing PostgreSQL. Separate Flyway migration scripts exist for H2 vs PostgreSQL.

### 7. ReadOnly Transactions
Service read methods use `@Transactional(readOnly = true)` to reduce lock contention and allow the database to optimize reads.

### 8. Role-Based Access Control
Two roles:
- **ADMIN**: Can register devices, view all data
- **AGENT**: Can create customers, assign devices, record payments

---

## ⚖️ Trade-offs

| Decision | Pro | Con |
|---|---|---|
| H2 for local dev | Zero setup friction | H2 migration scripts needed in parallel |
| Caffeine cache | Fast in-process cache | Invalidation not shared across instances |
| Mock gateway | Easy to demo and test | Must swap with real client for prod |
| Scheduler-based locking | Simple and reliable | Not real-time; max 24h delay |
| Flyway migrations | Schema version control | More migration files to manage |
| UUID keys | Distributed safe | Slightly larger index size than int |

### What Would Change in Production
- Replace mock gateway with real M-Pesa Daraja API or similar
- Add Kafka/RabbitMQ for async payment event processing
- Use Redis instead of Caffeine for a distributed cache
- Add rate limiting (via Spring Cloud Gateway or nginx)
- Deploy on AWS ECS/EKS with RDS PostgreSQL
- Add CloudWatch/Datadog for monitoring

---

## 📈 Scalability

### Handling 1M+ Customers & High Payment Volume

1. **Database indexes** on all high-cardinality lookup fields (phone, email, status, payment date)
2. **Partial indexes** for the locking scheduler query — only considers active, unpaid assignments
3. **Pagination** enforced on all list endpoints (max page size 100)
4. **Batch JDBC** settings (`hibernate.jdbc.batch_size=50`) for bulk inserts
5. **HikariCP** connection pool tuned for 20 max connections in production
6. **Caffeine cache** reduces repeat customer/device reads
7. **ReadOnly transactions** reduce DB lock pressure
8. **Idempotency** prevents duplicate payment records under retry storms
