# API Documentation

This folder contains API documents grouped by controller.

## Files

- [auth.md](C:/workspace/sample/demo/docs/api/auth.md): Authentication APIs
- [admin.md](C:/workspace/sample/demo/docs/api/admin.md): Admin APIs
- [products.md](C:/workspace/sample/demo/docs/api/products.md): Product query and seed APIs
- [exports.md](C:/workspace/sample/demo/docs/api/exports.md): Product export APIs for CSV and PDF

## Common Response Format

Most APIs return the same response envelope:

```json
{
  "success": true,
  "code": 200,
  "data": {},
  "errors": null,
  "message": null
}
```

Notes:

- `success`: whether the request was handled successfully
- `code`: application-level code, not always the same as HTTP status
- `data`: successful payload
- `errors`: validation or field-level errors
- `message`: summary message, commonly used for async/export or failures

## Common Error Shape

Validation and business errors usually look like:

```json
{
  "success": false,
  "code": 8,
  "errors": {
    "fieldName": "Validation message"
  },
  "message": "Validation failed."
}
```

## Authentication

Protected endpoints require a Bearer token:

```http
Authorization: Bearer <access-token>
```
