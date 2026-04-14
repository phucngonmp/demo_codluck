# Admin API

Controller: `AdminController`  
Base path: `/admin`

## Security

All endpoints in this controller require role `ADMIN`.

Header:

```http
Authorization: Bearer <access-token>
```

## GET `/admin`

Simple admin-only status endpoint.

### Success Response

HTTP `200`

```json
{
  "success": true,
  "code": 200,
  "data": "Admin API is working.",
  "errors": null,
  "message": null
}
```

### Error Cases

#### Missing token

Common Spring Security response. This endpoint may return framework-level unauthorized/forbidden responses if the request is blocked before reaching controller advice.

#### Forbidden

If authenticated user does not have role `ADMIN`, Spring Security returns `403 Forbidden`.
