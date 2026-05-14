# Task Manager API

## Overview

This project is a Java 17 Spring Boot task manager API built for the Eulerity backend take-home assessment.

It includes:

- CRUD endpoints at `/tasks`
- An AI-assisted suggestion endpoint at `POST /tasks/suggest`
- An H2 in-memory database
- A minimal static UI at `http://localhost:8080/`

## Prerequisites

- Java 17

## Setup

Clone the repository and change into the project directory:

```bash
git clone https://github.com/Thesclhy/eulerity-task-manager.git
cd eulerity-task-manager
```

## Run Locally

Start the API locally with:

```bash
./mvnw spring-boot:run
```

Once the app is running:

- UI: `http://localhost:8080/`
- API base URL: `http://localhost:8080`

## Run Tests

```bash
./mvnw test
```

## Database

No external database is required. The application uses an H2 in-memory database.

## API Endpoints

- `POST /tasks`
- `GET /tasks`
- `GET /tasks/{id}`
- `PUT /tasks/{id}`
- `DELETE /tasks/{id}`
- `POST /tasks/suggest`

## Task Urgency Notifications

When a user creates a task with `POST /tasks`, the backend evaluates the current active workload and may
return an urgency notification.

Scoring rules:

- Active `HIGH` priority task: `+2`
- Active overdue task: `+3`
- Active task due today: `+2`
- Active task due within 7 days: `+1`
- `DONE` task: does not count

The notification threshold is configurable through application properties:

```properties
task.urgency.notification-threshold=6
```

The default threshold is `6`. It can be overridden with environment-specific Spring configuration while
keeping the scoring rules unchanged.

### `POST /tasks` Response Shape

If no warning is needed:

```json
{
  "task": {
    "id": 1,
    "title": "Prepare interview packet",
    "description": "Draft examples and print copies",
    "status": "TODO",
    "priority": "HIGH",
    "dueDate": "2026-05-01",
    "createdAt": "2026-04-27T10:30:00",
    "updatedAt": "2026-04-27T10:30:00"
  },
  "notification": null
}
```

If the urgency threshold is reached:

```json
{
  "task": {
    "id": 2,
    "title": "New task during busy week",
    "description": "This create request should surface an urgency notification",
    "status": "TODO",
    "priority": "LOW",
    "dueDate": "2026-06-15",
    "createdAt": "2026-04-27T10:31:00",
    "updatedAt": "2026-04-27T10:31:00"
  },
  "notification": {
    "score": 6,
    "urgentTaskCount": 2,
    "message": "You have several urgent tasks due soon. Consider prioritizing or rescheduling some of them."
  }
}
```

`GET /tasks`, `GET /tasks/{id}`, `PUT /tasks/{id}`, and `DELETE /tasks/{id}` keep their original response
shapes and are not wrapped in `{ "task": ... }`.

### Manual Verification

1. Run `./mvnw spring-boot:run`
2. Open `http://localhost:8080/`
3. Create a low-urgency task and confirm the form shows normal success with no warning box.
4. Create enough active `HIGH`, overdue, or near-due tasks to reach score `6`.
5. Create one more task and confirm the UI shows the urgency warning message.

## AI Suggest Endpoint

`POST /tasks/suggest` accepts a natural-language prompt and returns a structured task suggestion. The suggestion is not persisted to the database.

### Behavior Without AI Credentials

The application works without any AI credentials. If `OPENAI_API_KEY` is not configured, the endpoint uses a deterministic fallback parser to generate a suggestion.

### Optional Real AI Behavior

If `OPENAI_API_KEY` is configured, the app will attempt a real OpenAI call through the dedicated AI client path.

Optional environment variables:

- `OPENAI_API_KEY`
- `OPENAI_MODEL` (defaults to `gpt-4o-mini`)

`.env.example` is only a template. This project does not automatically load `.env`.

To use real AI behavior, export the variables before starting the app:

```bash
export OPENAI_API_KEY=your_key_here
export OPENAI_MODEL=gpt-4o-mini
./mvnw spring-boot:run
```

You can also create a local `.env` file from `.env.example` and source it manually:

```bash
set -a
source .env
set +a
./mvnw spring-boot:run
```

Without `OPENAI_API_KEY`, the app uses the deterministic fallback path.

Do not commit `.env` or real API keys.

### Example Request

```bash
curl -X POST http://localhost:8080/tasks/suggest \
  -H "Content-Type: application/json" \
  -d '{
    "prompt": "I need to prepare for an interview tomorrow"
  }'
```

### Example Response

```json
{
  "title": "I Need To Prepare",
  "description": "Suggested from prompt: I need to prepare for an interview tomorrow",
  "dueDate": "2026-04-28",
  "priority": "HIGH",
  "status": "TODO"
}
```

The exact response may differ when real AI behavior is enabled, but it will follow the same response shape:

- `title`
- `description`
- `dueDate`
- `priority`
- `status`
