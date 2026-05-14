# Implementation Plan

## Current Plan

### Goal

### Tasks
---

## Completed Plan

### Goal

Build a Java 17 Spring Boot Maven task manager API for the Eulerity backend take-home assessment.

### Completed Tasks

- [x] Inspect the existing Spring Boot project structure.
- [x] Define the API contract and request/response shapes.
- [x] Create test skeletons first:
  - TaskService unit tests
  - CRUD integration tests
  - AI endpoint test with mocked AI service
- [x] Implement the Task domain model:
  - Task entity
  - Priority enum
  - Status enum
- [x] Implement repository and service layer.
- [x] Implement CRUD controller at `/tasks`.
- [x] Implement AI suggest endpoint at `POST /tasks/suggest`.
- [x] Add minimal static UI at `/`.
- [x] Add urgency scoring service for active task workload evaluation.
- [x] Update `POST /tasks` to return a wrapper response with `task` and optional `notification`.
- [x] Make urgency notification threshold configurable through `application.properties`.
- [x] Add test coverage for custom threshold behavior.
- [x] Update the static UI create-task flow to display urgency warnings from `POST /tasks`.
- [x] Run `./mvnw test` and fix failures.
- [x] Run `./mvnw test` after urgency notification changes and confirm all 28 tests pass.
- [x] Run `./mvnw test` after configurable-threshold changes and confirm all 29 tests pass.
- [x] Run `./mvnw spring-boot:run` and manually verify:
  - `/`
  - `/tasks`
  - `/tasks/suggest`
- [x] Update README with setup, run, test, and example requests.
- [x] Save AI transcript as `AI_TRANSCRIPT.md`.

### Notes

- The app should run without an OpenAI API key by using deterministic fallback parsing.
- The OpenAI path should be optional and enabled only when `OPENAI_API_KEY` is configured.
- Tests should not make real external API calls.
- `POST /tasks` is the only task endpoint that returns a wrapper response.
- Urgency notification threshold defaults to `6` and is configurable with `task.urgency.notification-threshold`.
