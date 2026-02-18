TaskMan: A Team Task & Workflow Management System

A Backend project using SpringBoot that lets teams handle task management, user assigning and workflow status tracking.
This project is built from scratch to understand core backend features and implementation techniques.

The following base model is being followed:
![model-relations](relational-diagram.png)


## Planned Role Based Access
Admin -
- Create teams
- Add/remove users from teams
- Assign roles
- Delete teams
- View everything

Manager-
- Create tasks
- Assign tasks to users
- Update any task in their team
- View team tasks
- Add comments

Member-
- View tasks assigned to them
- Update status of their own tasks
- Add comments
- Cannot assign tasks
- Cannot delete teams


## Planned Features

- Task CRUD operations
- User registration and login
- JWT Authentication
- Role-based authorization (ADMIN, MANAGER, MEMBER)
- Team management
- Comments on tasks
- Filtering and pagination
- Docker deployment


## Current Progress

- Spring Boot project initialized
- GitHub repository connected
- Entities created:
  - User
  - Task
  - Team
  - Comment
- Enums added:
  - TaskStatus
  - TaskPriority
  - UserRole
- Basic database structure prepared
