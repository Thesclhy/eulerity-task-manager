# API Contract

This document confirms the current API contract for the take-home task manager.

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

If the active workload urgency score reaches the notification threshold, the response includes a warning payload:

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

Urgency scoring rules used by `POST /tasks`:

- Active `HIGH` priority task: `+2`
- Active overdue task: `+3`
- Active task due today: `+2`
- Active task due within 7 days: `+1`
- `DONE` task: does not count

The notification is returned only when the total score is `>= 6`.

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

Returns a single `Task` object. This endpoint is not wrapped in `{ "task": ... }`.

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

Returns a single updated `Task` object. This endpoint is not wrapped in `{ "task": ... }`.

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
  "dueDate": "2026-05-04",
  "priority": "HIGH",
  "status": "TODO"
}
```
