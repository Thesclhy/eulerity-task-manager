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
cd task_manager
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
