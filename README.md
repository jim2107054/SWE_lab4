# Student-Teacher Course Management System

A production-ready Spring Boot REST API with JWT authentication, role-based authorization, and comprehensive testing.

## Tech Stack

- **Java 21**
- **Spring Boot 3.4.2**
- **Spring Security** with JWT
- **Spring Data JPA**
- **PostgreSQL** (production) / **H2** (testing)
- **Maven**
- **Docker & Docker Compose**
- **JUnit 5 & Mockito** for testing

## Features

### Authentication & Authorization
- JWT-based authentication
- BCrypt password hashing
- Role-based access control (RBAC)

### Roles & Permissions

| Role | Permissions |
|------|-------------|
| **TEACHER** | Login, Create/Update/Delete courses, View all resources |
| **STUDENT** | Login, View courses/teachers/departments (read-only) |

### Domain Model

```
Department ─┬─► Teacher ─► Course
            └─► Course ◄─── Student (enrollment)
```

## Project Structure

```
src/main/java/com/example/webapp/
├── config/          # Configuration classes
├── controller/      # REST API controllers
├── dto/             # Data Transfer Objects
├── entity/          # JPA entities
├── exception/       # Custom exceptions & handlers
├── repository/      # Data access layer
├── security/        # JWT & Spring Security config
└── service/         # Business logic layer
```

## Getting Started

### Prerequisites

- Java 21
- Maven 3.9+
- Docker & Docker Compose (for containerized deployment)
- PostgreSQL 16 (for local development without Docker)

### Local Development

1. **Clone the repository**
   ```bash
   git clone https://github.com/jim2107054/SWE_lab4.git
   cd SWE_lab4
   ```

2. **Configure environment variables** (optional - defaults provided)
   ```bash
   cp .env.example .env
   # Edit .env with your settings
   ```

3. **Run with Maven (H2 in-memory database)**
   ```bash
   mvn spring-boot:run -Dspring.profiles.active=test
   ```

4. **Run with PostgreSQL**
   ```bash
   # Start PostgreSQL (ensure it's running on localhost:5432)
   mvn spring-boot:run
   ```

### Docker Deployment

```bash
# Build and start all services
docker-compose up --build

# Run in detached mode
docker-compose up -d --build

# Stop services
docker-compose down

# Stop and remove volumes
docker-compose down -v
```

## API Endpoints

### Authentication

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/api/auth/register` | Register new user | Public |
| POST | `/api/auth/login` | Login and get JWT | Public |

### Courses

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/courses` | Get all courses | TEACHER, STUDENT |
| GET | `/api/courses/{id}` | Get course by ID | TEACHER, STUDENT |
| GET | `/api/courses/code/{courseCode}` | Get course by code | TEACHER, STUDENT |
| POST | `/api/courses` | Create course | TEACHER |
| PUT | `/api/courses/{id}` | Update course | TEACHER |
| DELETE | `/api/courses/{id}` | Delete course | TEACHER |

### Departments

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/departments` | Get all departments | TEACHER, STUDENT |
| GET | `/api/departments/{id}` | Get department by ID | TEACHER, STUDENT |
| POST | `/api/departments` | Create department | TEACHER |
| PUT | `/api/departments/{id}` | Update department | TEACHER |
| DELETE | `/api/departments/{id}` | Delete department | TEACHER |

### Teachers

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/teachers` | Get all teachers | TEACHER, STUDENT |
| GET | `/api/teachers/{id}` | Get teacher by ID | TEACHER, STUDENT |

### Students

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/students` | Get all students | TEACHER, STUDENT |
| GET | `/api/students/{id}` | Get student by ID | TEACHER, STUDENT |

## API Usage Examples

### Register a Teacher

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "teacher1",
    "password": "password123",
    "email": "teacher1@example.com",
    "name": "Dr. John Smith",
    "role": "ROLE_TEACHER",
    "employeeId": "EMP001",
    "designation": "Professor",
    "specialization": "Machine Learning"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "teacher1",
  "email": "teacher1@example.com",
  "name": "Dr. John Smith",
  "role": "ROLE_TEACHER",
  "message": "Registration successful"
}
```

### Register a Student

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "student1",
    "password": "password123",
    "email": "student1@example.com",
    "name": "Alice Johnson",
    "role": "ROLE_STUDENT",
    "roll": "STU001",
    "program": "BSc Computer Science",
    "semester": 4
  }'
```

### Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "teacher1",
    "password": "password123"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "teacher1",
  "email": "teacher1@example.com",
  "name": "Dr. John Smith",
  "role": "ROLE_TEACHER",
  "message": "Login successful"
}
```

### Create a Department (Teacher only)

```bash
curl -X POST http://localhost:8080/api/departments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <YOUR_JWT_TOKEN>" \
  -d '{
    "name": "Computer Science",
    "code": "CS",
    "description": "Department of Computer Science and Engineering"
  }'
```

**Response:**
```json
{
  "id": 1,
  "name": "Computer Science",
  "code": "CS",
  "description": "Department of Computer Science and Engineering",
  "teacherCount": 0,
  "courseCount": 0
}
```

### Create a Course (Teacher only)

```bash
curl -X POST http://localhost:8080/api/courses \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <YOUR_JWT_TOKEN>" \
  -d '{
    "title": "Data Structures",
    "description": "Introduction to Data Structures and Algorithms",
    "courseCode": "CS201",
    "credits": 3,
    "departmentId": 1
  }'
```

**Response:**
```json
{
  "id": 1,
  "title": "Data Structures",
  "description": "Introduction to Data Structures and Algorithms",
  "courseCode": "CS201",
  "credits": 3,
  "teacherId": null,
  "teacherName": null,
  "departmentId": 1,
  "departmentName": "Computer Science"
}
```

### Get All Courses

```bash
curl -X GET http://localhost:8080/api/courses \
  -H "Authorization: Bearer <YOUR_JWT_TOKEN>"
```

## Testing

### Run All Tests

```bash
mvn test
```

### Run Unit Tests Only

```bash
mvn test -Dtest="*Test"
```

### Run Integration Tests Only

```bash
mvn verify -DskipUnitTests
```

### Test Coverage

The project includes:
- **Unit Tests**: Service layer, Security logic
- **Integration Tests**: Authentication flow, Role-based access control, CRUD operations

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_PROFILES_ACTIVE` | `dev` | Active Spring profile |
| `DB_URL` | `jdbc:postgresql://localhost:5432/admindb` | Database URL |
| `DB_USER` | `admin` | Database username |
| `DB_PASS` | `admin` | Database password |
| `JWT_SECRET` | (base64 encoded) | JWT signing key |
| `JWT_EXPIRATION` | `86400000` | Token expiration (24h) |

## GitHub Actions CI/CD

The project includes a GitHub Actions workflow that:

1. **Builds** the project with Maven
2. **Runs Unit Tests**
3. **Runs Integration Tests**
4. **Builds Docker image**

Pipeline triggers on:
- Push to `main` or `develop` branches
- Pull requests to `main`

## Error Handling

The API returns consistent error responses:

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Course not found with id: '999'",
  "path": "/api/courses/999"
}
```

### HTTP Status Codes

| Code | Description |
|------|-------------|
| 200 | Success |
| 201 | Created |
| 204 | No Content (successful deletion) |
| 400 | Bad Request (validation errors) |
| 401 | Unauthorized |
| 403 | Forbidden (insufficient permissions) |
| 404 | Not Found |
| 409 | Conflict (duplicate resource) |
| 500 | Internal Server Error |

## Security Considerations

- Passwords are hashed using BCrypt
- JWT tokens expire after 24 hours (configurable)
- Role-based access control prevents unauthorized operations
- Non-root user in Docker container
- Environment variables for sensitive configuration

## License

This project is for educational purposes.