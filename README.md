# JihunPage

A personal introduction website built with React and Spring Boot.

JihunPage includes a personal profile page, session-based authentication, and a public gallery for each member.

## Features

### Home

- Personal introduction
- Experience
- Skills
- Projects
- Contact information
- Component-based page structure

### Authentication

- Member signup
- Session-based login and logout
- BCrypt password hashing
- Current member state management with React Context

### Gallery

- Public gallery for each member
- Image upload and deletion
- Image detail modal
- Owner-only upload and delete actions
- Uploaded image file storage

## Tech Stack

### Frontend

- React 19
- JavaScript
- Vite
- Bootstrap 5
- ESLint

### Backend

- Java 21
- Spring Boot 3.5.16
- Spring Web
- Spring Data JPA
- Bean Validation
- Spring Security Crypto
- H2 Database
- Gradle

### Development Environment

- Docker
- Docker Compose

## Project Structure

```text
JihunPage/
├── compose.yaml
├── backend/
│   ├── gradle/
│   │   └── wrapper/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/sisa/jihunpage/
│   │   │   └── resources/
│   │   └── test/
│   ├── uploads/
│   ├── Dockerfile.dev
│   ├── build.gradle
│   ├── gradlew
│   ├── gradlew.bat
│   └── settings.gradle
├── frontend/
│   ├── public/
│   ├── src/
│   │   ├── api/
│   │   ├── components/
│   │   │   └── home/
│   │   ├── context/
│   │   ├── hooks/
│   │   ├── pages/
│   │   ├── App.jsx
│   │   ├── index.css
│   │   └── main.jsx
│   ├── .dockerignore
│   ├── Dockerfile.dev
│   ├── package.json
│   └── vite.config.js
└── README.md
```

The project structure above shows the main directories and files only.

## Run with Docker

Create a `.env` file based on `.env.example`, and then run:

```bash
docker compose up --build
```

- Frontend: http://localhost:5173
- Backend: http://localhost:8080

## Documentation

- [Docker Development Environment](docs/docker-development.md)

## Run Manually

The frontend and backend can also be started without Docker.

### Run the Backend

Move to the backend directory:

```bash
cd backend
```

Run the tests:

```bash
./gradlew test
```

Start the Spring Boot application:

```bash
./gradlew bootRun
```

The backend server runs at:

```text
http://localhost:8080
```

### Run the Frontend

Open another terminal and move to the frontend directory:

```bash
cd frontend
```

Install the dependencies:

```bash
npm install
```

Start the Vite development server:

```bash
npm run dev
```

The frontend development server runs at:

```text
http://localhost:5173
```

## Health Check

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

## H2 Console

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

## Current Limitations

The project currently uses an H2 in-memory database.

Member and gallery data are deleted when the backend application or container restarts.

Uploaded image files remain in the local `backend/uploads` directory, but their database records are deleted when the H2 database is reset.

## Planned Improvements

- Replace H2 with MySQL
- Add persistent database storage
- Add Nginx
- Create production Docker images
- Deploy the application to AWS
- Add gallery pagination
- Add a guestbook feature
- Refactor the frontend styling and backend package structure
