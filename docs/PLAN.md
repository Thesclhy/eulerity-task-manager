# Implementation Plan

1. Inspect the existing Spring Boot project structure.
2. Define the API contract and request/response shapes.
3. Create test skeletons first:
   - TaskService unit tests
   - CRUD integration tests
   - AI endpoint test with mocked AI service
4. Implement the Task domain model:
   - Task entity
   - Priority enum
   - Status enum
5. Implement repository and service layer.
6. Implement CRUD controller at `/tasks`.
7. Implement AI suggest endpoint at `POST /tasks/suggest`.
8. Add minimal static UI at `/`.
9. Run `./mvnw test` and fix failures.
10. Run `./mvnw spring-boot:run` and manually verify:
    - `/`
    - `/tasks`
    - `/tasks/suggest`
11. Update README with setup, run, test, and example requests.
12. Save AI transcript as `AI_TRANSCRIPT.md`.
