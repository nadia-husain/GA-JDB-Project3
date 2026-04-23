# Pet Adoption Platform

A platform for managing pet adoptions, volunteer events, and user accounts. It connects shelter administrators with prospective adopters and volunteers through a secure, role-based backend system.

---

## 🛠️ Technologies Used

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3 |
| Security | Spring Security + JWT (JSON Web Tokens) |
| Database | PostgreSQL |
| ORM | Spring Data JPA / Hibernate |
| Build Tool | Maven |
| Email | JavaMailSender (SMTP) |
| File Handling | Multipart file upload (Base64 image encoding) |
| Concurrency | `ReentrantLock` + JPA `@Version` (Optimistic Locking) |
| Version Control | Git / GitHub |

---

## 📖 General Approach

The application was designed around a **role-based access control (RBAC)** model with two distinct user roles: `ADMIN` and `CUSTOMER`. Every endpoint is protected by Spring Security, with the currently authenticated user resolved from the JWT token on each request. This allowed me to enforce fine-grained permissions at the service layer — for example, only admins can create or delete pets and volunteer events, while customers can only view and request adoption for themselves.

I modelled the domain around five core entities: `User`, `UserProfile`, `Pet`, `AdoptionRequest`, `VolunteerEvent`, and `Volunteer`. Relationships between them (e.g. a user submitting an adoption request for a pet, or signing up to volunteer at an event) are managed through JPA associations with appropriate cascading and fetch strategies. A key design decision was to implement **per-pet locking** using `ReentrantLock` on adoption requests, ensuring that two concurrent users cannot both adopt the same pet — the first request marks the pet as `UNAVAILABLE` before the second can proceed.

The user onboarding flow includes **email verification** before login is permitted, and a **password reset** flow using expiring UUID tokens stored in the database. Both flows send transactional emails via SMTP. Account deactivation was implemented as a soft delete — setting a user's status to `INACTIVE` rather than removing their record — preserving referential integrity across adoption and volunteer data.

---

## 🚧 Unsolved Problems & Hurdles

**Concurrency on adoption requests** was one of the most significant technical challenges. Without locking, a race condition could allow two users to simultaneously adopt the same pet. I solved this using a `ConcurrentHashMap` of `ReentrantLock` objects keyed by pet ID, ensuring that only one thread at a time can check availability and mark a pet as taken. For volunteer event capacity, I used JPA's `@Version` annotation to apply optimistic locking, throwing an `OptimisticLockException` if two threads attempt to decrement capacity simultaneously.

**Image handling** presented a trade-off: storing pet photos as Base64-encoded strings in the database is simple and avoids the need for a separate file storage service, but it is not ideal at scale. This remains an area for improvement — a future iteration would offload images to an object store such as AWS S3.

---

## 📋 User Stories

> 📝 [View User Stories →](https://github.com/nadia-husain/GA-JDB-Project3/issues)

---

## 🗂️ ERD Diagram

```mermaid
erDiagram
  USER {
    Long id PK
    string emailAddress
    string password
    enum role
    boolean verified
    enum userStatus
    Long profile_id FK
  }

  USER_PROFILE {
    Long id PK
    string firstName
    string lastName
    Long phoneNumber
    string profilePic
  }

  PET {
    Long id PK
    string name
    string type
    Integer age
    string photo
    enum petStatus
    LocalDateTime createdAt
    LocalDateTime updatedAt
  }

  ADOPTION_REQUEST {
    Long id PK
    Long pet_id FK
    Long user_id FK
    enum status
    LocalDateTime createdAt
    LocalDateTime updatedAt
  }

  VOLUNTEER_EVENT {
    Long id PK
    string task
    string location
    LocalDateTime date
    Integer capacity
    boolean isFull
    Long created_by FK
    Long version
    LocalDateTime createdAt
    LocalDateTime updatedAt
  }

  VOLUNTEER {
    Long id PK
    Long user_id FK
    Long volunteer_event_id FK
    boolean hasAttended
    LocalDateTime createdAt
    LocalDateTime updatedAt
  }

  PASSWORD_RESET_TOKEN {
    Long id PK
    string token
    Long user_id FK
    LocalDateTime expiryDate
  }

  EMAIL_VERIFICATION_TOKEN {
    Long id PK
    string token
    Long user_id FK
    LocalDateTime expiryDate
  }

  USER ||--|| USER_PROFILE : "has one"
  USER ||--o{ ADOPTION_REQUEST : "submits"
  PET ||--o{ ADOPTION_REQUEST : "received by"
  USER ||--o{ VOLUNTEER : "signs up as"
  VOLUNTEER_EVENT ||--o{ VOLUNTEER : "has"
  USER ||--o{ VOLUNTEER_EVENT : "created by"
  USER ||--o{ PASSWORD_RESET_TOKEN : "requests"
  USER ||--o{ EMAIL_VERIFICATION_TOKEN : "verifies via"
```


---

## 📅 Planning Documentation

> 🗓️ [View GitHub Project Board →](https://github.com/nadia-husain/GA-JDB-Project3/projects)

---

## ⚙️ Installation & Setup

### Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL 14+
- An SMTP-capable email account (e.g. Gmail with App Password)

### 1. Clone the Repository

```bash
git clone https://github.com/nadia-husain/GA-JDB-Project3.git
cd GA-JDB-Project3
```

### 2. Configure the Database

Create a PostgreSQL database:

```sql
CREATE DATABASE petadoption;
```

### 3. Set Environment Variables

Create an `application.properties` or `application.yml` in `src/main/resources/` (or set environment variables) with the following:

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/petadoption
spring.datasource.username=YOUR_DB_USERNAME
spring.datasource.password=YOUR_DB_PASSWORD
spring.jpa.hibernate.ddl-auto=update

# JWT
jwt.secret=YOUR_JWT_SECRET_KEY
jwt.expiration=86400000

# Email (SMTP)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=YOUR_EMAIL@gmail.com
spring.mail.password=YOUR_APP_PASSWORD
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

### 4. Install Dependencies & Run

```bash
mvn clean install
mvn spring-boot:run
```

The API will start on `http://localhost:8080` by default.

### 5. Upload Directory

The application stores user profile images under `uploads/profilePic/`. Ensure this directory exists and is writable:

```bash
mkdir -p uploads/profilePic
```

