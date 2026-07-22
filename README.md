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
- Redis-backed shared HTTP session storage
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
- Spring Session
- MySQL Connector/J
- Gradle

### Database

- MySQL 8.4 LTS
- Docker named volume for data persistence

### Session Storage

- Redis 7.4
- Spring Session Data Redis
- Redis-backed HTTP session persistence

### Development Environment

- Docker
- Docker Compose

## Project Structure

```text
JihunPage/
├── compose.yaml
├── .env.example
├── backend/
│   ├── gradle/
│   │   └── wrapper/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   └── resources/
│   │   │       └── application.yaml
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
├── docs/
│   └── docker-development.md
└── README.md
```

The project structure above shows the main directories and files only.

## Run with Docker

Create a local `.env` file based on `.env.example`.

```bash
cp .env.example .env
```

Update the MySQL credentials in `.env`.

```dotenv
MYSQL_DATABASE=jihunpage
MYSQL_USER=your_mysql_user
MYSQL_PASSWORD=your_mysql_password
MYSQL_ROOT_PASSWORD=your_mysql_root_password
```

Start the application from the project root:

```bash
docker compose up --build
```

To run the containers in the background:

```bash
docker compose up -d --build
```

After the containers start, open the following URLs:

| Service    | URL                              |
| ---------- | -------------------------------- |
| Frontend   | http://localhost:5173            |
| Backend    | http://localhost:8080            |
| Health API | http://localhost:8080/api/health |
| MySQL      | localhost:3306                   |
| Redis      | localhost:6379                   |

Stop the application:

```bash
docker compose down
```

MySQL data remains in the Docker named volume after running `docker compose down`.

The following command also deletes the MySQL data volume:

```bash
docker compose down -v
```

## Documentation

- [Docker Development Environment](docs/docker-development.md)

## Development Workflow

Frontend source changes are automatically reflected through Vite HMR.

After changing backend source code, restart the backend container:

```bash
docker compose restart backend
```

View all container logs:

```bash
docker compose logs -f
```

View backend logs:

```bash
docker compose logs -f backend
```

View MySQL logs:

```bash
docker compose logs -f mysql
```

## Run Manually

The frontend and backend can also be started without Docker.

A MySQL server must already be running before starting the backend manually.

### Run the Backend

Move to the backend directory:

```bash
cd backend
```

Set the database connection environment variables:

```bash
export SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/jihunpage"
export SPRING_DATASOURCE_USERNAME="your_mysql_user"
export SPRING_DATASOURCE_PASSWORD="your_mysql_password"
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

## Data and Session Persistence

MySQL data is stored in the `mysql_data` Docker named volume.

Member and gallery database records remain after restarting the backend:

```bash
docker compose restart backend
```

HTTP sessions are stored in Redis instead of the backend application's memory.

Therefore, the authenticated session also remains after restarting the backend container:

```bash
docker compose restart backend
```

The database records and Redis session data remain after stopping and recreating the containers:

```bash
docker compose down
docker compose up -d
```

Uploaded gallery image files are stored in the local `backend/uploads` directory.

The following command deletes the MySQL and Redis named volumes:

```bash
docker compose down -v
```

## Planned Improvements

- Add Nginx as a reverse proxy
- Run multiple backend instances
- Refactor authentication from Session to JWT
- Add Access Token and Refresh Token support
- Store Refresh Tokens in Redis
- Create production Docker images
- Deploy the application to AWS
- Add gallery pagination
- Add a guestbook feature
- Add database migration management with Flyway
