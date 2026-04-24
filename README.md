# TaskMan: A Team Task & Workflow Management System

TaskMan is a secure multi-user backend for managing tasks across teams — built with Spring Boot, focusing on real-world backend concerns like role-based access, token security, and business rule enforcement.

## Architecture
![model-relations](taskman.drawio.png)

``` Controller –> Service –> Repository ```

- DTO-based API (without entity exposure)
- Business rules implemented in service layer
- Security implemented through JWT with filter chain

## Core Features
- JWT-based Authentication
- Role-based Access Control
  1. ADMIN 
  2. MANAGER 
  3. MEMBER
- Task Management 
  1. Create 
  2. Update 
  3. Assign
- Team Management
- Comment System on Tasks
- Pagination & Filtering
- Token Versioning
- Global Logout
- Soft Deletion of Users
- Dockerized Application
- Flyway DB Migration

## Tech Stack
- Java 21, Spring Boot
- Spring Security (JWT)
- PostgreSQL, Hibernate/JPA
- Flyway
- Docker
- JUnit, Mockito

## How to Run
``` 
git clone https://github.com/Flicko75/taskman.git
cd TaskMan
 ```
### Run locally
``` 
mvn spring-boot:run
```
### With Docker
``` 
docker-compose up
```

## API Endpoints

### Auth
- ```POST   /api/auth/login```
- ```POST   /api/auth/logout```

### Tasks
- ```GET    /api/tasks```
- ```GET    /api/tasks/{id}```
- ```POST   /api/tasks```
- ```PUT    /api/tasks/{id}```
- ```DELETE /api/tasks/{id}```
- ```PUT    /api/tasks/{taskId}/assign/{userId}```
- ```PUT    /api/tasks/{id}/unassign```

### Teams
- ```GET    /api/teams```
- ```GET    /api/teams/{id}```
- ```POST   /api/teams```
- ```PUT    /api/teams/{id}```
- ```DELETE /api/teams/{id}```

### Users
- ```GET    /api/users```
- ```GET    /api/users/{id}```
- ```POST   /api/users```
- ```PUT    /api/users/{id}```
- ```PUT    /api/users/{id}/role```
- ```DELETE /api/users/{id}```
- ```PUT    /api/users/{userId}/assign/{teamId}```
- ```PUT    /api/users/{userId}/unassign```
- ```POST   /api/users/{id}/force-logout```

### Comments
- ```GET    /api/tasks/{taskId}/comments```
- ```GET    /api/comments/{commentId}```
- ```POST   /api/tasks/{taskId}/comments```
- ```PUT    /api/comments/{commentId}```
- ```DELETE /api/comments/{commentId}```

## Business Rules (Highlights)
- Tasks must belong to a team; assignment only allowed within the same team
- Deleting a team auto-unassigns users; blocked if active tasks exist
- Token invalidated on logout or soft deletion via token versioning
- Only ADMINs can assign ADMIN roles; a user cannot demote themselves
- Last ADMIN in the system cannot be deleted

## Testing
- 150+ unit tests covering service layer business rules and edge cases
- Integration tests for full request flow
- Run with: mvn test
