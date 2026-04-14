# Product API

Controller: `ProductController`  
Base path: `/products`

## GET `/products`

Query products with pagination, sorting, and search.

### Query Parameters

- `page`: optional, default `0`
- `size`: optional, default `10`
- `sort`: optional, default `id,desc`
- `search`: optional, search by product name

### Validation Rules

- `page >= 0`
- `size >= 1`
- `size <= 1000`
- `sort` format must be one of:
  - `id,asc|desc`
  - `name,asc|desc`
  - `price,asc|desc`
  - `quantity,asc|desc`
  - `createdAt,asc|desc`
  - `updatedAt,asc|desc`

### Example Request

```http
GET /products?page=0&size=20&sort=name,asc&search=phone
```

### Success Response

HTTP `200`

```json
{
  "success": true,
  "code": 200,
  "data": {
    "content": [
      {
        "id": 1,
        "name": "Product 1",
        "price": 1000,
        "description": "Random description",
        "quantity": 10,
        "createdAt": "2026-04-14T09:00:00",
        "updatedAt": "2026-04-14T09:00:00"
      }
    ],
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 100000,
    "totalPages": 5000
  },
  "errors": null,
  "message": null
}
```

### Error Cases

#### Validation failed

HTTP `400`

```json
{
  "success": false,
  "code": 8,
  "errors": {
    "size": "Size must be less than or equal to 1000."
  },
  "message": "Validation failed."
}
```

#### Binding/type mismatch

Example: `page=1.6`

HTTP `400`

```json
{
  "success": false,
  "code": 8,
  "errors": {
    "page": "Page must be an integer."
  },
  "message": "Validation failed."
}
```

## POST `/products`

Generate 50 sample products.

## Security

Requires role `USER` or `ADMIN`.

### Success Response

HTTP `200`

```json
{
  "success": true,
  "code": 200,
  "data": "Sample products created successfully.",
  "errors": null,
  "message": null
}
```

### Error Cases

#### Unauthorized or forbidden

Spring Security may return `401` or `403` before the request reaches controller logic.
