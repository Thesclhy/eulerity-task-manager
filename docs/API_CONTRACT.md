# API Contract

This document confirms the first-pass API contract for the take-home task manager.

## Resource: `Task`

Fields used across requests and responses:

- `id`: `Long`
- `title`: `String`
- `description`: `String`
- `status`: `TODO | IN_PROGRESS | DONE`
- `priority`: `LOW | MEDIUM | HIGH`
- `dueDate`: `YYYY-MM-DD`
- `createdAt`: ISO-8601 date-time
- `updatedAt`: ISO-8601 date-time

## Endpoints

### `POST /tasks`

Request body:

```json
{
  "title": "Prepare interview packet",
  "description": "Draft examples and print copies",
  "status": "TODO",
  "priority": "HIGH",
  "dueDate": "2026-05-01"
}
```

Response: `201 Created`

```json
{
  "id": 1,
  "title": "Prepare interview packet",
  "description": "Draft examples and print copies",
  "status": "TODO",
  "priority": "HIGH",
  "dueDate": "2026-05-01",
  "createdAt": "2026-04-27T10:30:00",
  "updatedAt": "2026-04-27T10:30:00"
}
```

### `GET /tasks`

Response: `200 OK`

```json
[
  {
    "id": 1,
    "title": "Prepare interview packet",
    "description": "Draft examples and print copies",
    "status": "TODO",
    "priority": "HIGH",
    "dueDate": "2026-05-01",
    "createdAt": "2026-04-27T10:30:00",
    "updatedAt": "2026-04-27T10:30:00"
  }
]
```

### `GET /tasks/{id}`

Response: `200 OK`

Same body shape as `POST /tasks`.

### `PUT /tasks/{id}`

Request body:

```json
{
  "title": "Prepare updated interview packet",
  "description": "Add backend API examples",
  "status": "IN_PROGRESS",
  "priority": "HIGH",
  "dueDate": "2026-05-03"
}
```

Response: `200 OK`

Same body shape as `POST /tasks`.

### `DELETE /tasks/{id}`

Response: `204 No Content`

### `POST /tasks/suggest`

Request body:

```json
{
  "prompt": "I need to plan my week for job applications and study time"
}
```

Response: `200 OK`

```json
{
  "title": "Plan weekly applications",
  "description": "Block time for applications, follow-ups, and study sessions",
  "priority": "HIGH"
}
```
