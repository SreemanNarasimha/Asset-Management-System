# Military Asset Management System (MAMS)

MAMS is a secure, full-stack application designed to manage military assets, tracking allocations, condition, and status of various equipments. 

## Architecture
- **Frontend**: React.js with Vite
- **Backend**: Java Spring Boot
- **Database**: MySQL 8.0 (Containerized via Docker)

## Features
- Secure Authentication and Role-Based Access Control (RBAC) via JWT
- Dashboard for analytics and tracking
- Entity management for personnel and military equipment

## Getting Started (Local Development)

### Prerequisites
- Docker and Docker Compose
- Java 17+ (Maven is included via wrapper)
- Node.js 18+

### Setup

1. **Environment Variables**:
   Copy the example environment file and fill in your secrets.
   ```bash
   cp .env.example .env
   ```

2. **Start the Database**:
   ```bash
   docker-compose up -d
   ```

3. **Run the Backend**:
   Load your environment variables, or pass them inline:
   ```bash
   cd backend
   ./mvnw spring-boot:run -Dspring-boot.run.arguments="--DB_PASSWORD=your_password --JWT_SECRET=your_jwt_secret"
   ```
   *(Ensure you use actual `.env` file reading mechanism or pass args manually depending on your OS)*

4. **Run the Frontend**:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

## Production Deployment

1. Make sure to generate strong passwords and a 256-bit secure `JWT_SECRET`.
2. Do NOT commit the `.env` file to version control.
3. Build the frontend (`npm run build`) and serve via Nginx or equivalent.
4. Package the backend (`./mvnw clean package`) and run the `.jar` file with appropriate environment variables securely injected.

## Security Considerations
- CORS is configured to allow `localhost:5173` for development. In production, adjust `CORS_ORIGINS` to match your domain.
- The `mams` database user should not be root.

