# JihunPage

A personal introduction page built with React.

## Current Scope

- Build the frontend first
- Add a Spring Boot backend later
- Implement signup and session-based login as a future feature

## Tech Stack

- React
- Vite
- JavaScript
- Bootstrap
- ESLint

## Project Structure

```text
JihunPage/
├── backend/
│   ├── gradle/
│   ├── src/
│   ├── build.gradle
│   ├── gradlew
│   ├── gradlew.bat
│   └── settings.gradle
├── frontend/
│   ├── public/
│   ├── src/
│   │   ├── assets/
│   │   ├── App.jsx
│   │   ├── index.css
│   │   └── main.jsx
│   ├── package.json
│   └── vite.config.js
└── README.md
```

## Run Locally

```bash
cd frontend
npm install
npm run dev
```

The development server runs at:
```text
http://localhost:5173
```

## Backend

The backend API is built with Spring Boot and uses an in-memory H2 database during development.

### Tech Stack

- Java 21
- Spring Boot 3.5.16
- Spring Web
- Spring Data JPA
- Bean Validation
- H2 Database
- Gradle

### Run the Backend

Move to the backend directory.

```bash
cd backend
```

Run the tests.

```bash
./gradlew test
```

Start the Spring Boot application.

```bash
./gradlew bootRun
```

The backend server runs at:

```text
http://localhost:8080
```

### Health Check

Send a request to the following endpoint:

```text
GET http://localhost:8080/api/health
```

Expected response:

```json
{
  "status": "UP"
}
```

### H2 Console

The H2 Console is available at:

```text
http://localhost:8080/h2-console
```

Connection information:

```text
JDBC URL: jdbc:h2:mem:jihunpage
User Name: sa
Password: leave empty
```

The current H2 database runs in memory, so stored data is deleted when the backend application stops.
