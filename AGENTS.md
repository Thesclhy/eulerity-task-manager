# AGENTS.md

## Goal
Build a Java 17 Spring Boot Maven task manager API for the Eulerity backend take-home assessment.

## Must Have
- CRUD endpoints at `/tasks`
- AI endpoint at `POST /tasks/suggest`
- H2 in-memory database
- Static UI at `/`
- Tests pass with `./mvnw test`
- App runs with `./mvnw spring-boot:run`

## Architecture
Use:
- controller
- service
- repository
- model
- dto

## Rules
- Keep the implementation simple and readable.
- Do not add authentication.
- Do not require an external database.
- Do not commit API keys.
- Mock AI calls in tests.
- Prefer small, reviewable changes.