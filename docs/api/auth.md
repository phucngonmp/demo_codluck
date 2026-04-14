# Auth API

Controller: `AuthController`  
Base path: `/auth`

## POST `/auth/login`

Authenticate by username or email and return an access token.

### Request Body

```json
{
  "identifier": "admin@example.com",
  "password": "Password@123"
}
```

### Validation Rules

- `identifier`: required
- `password`: required

### Success Response

HTTP `200`

```json
{
  "success": true,
  "code": 200,
  "data": {
    "accessToken": "jwt-token",
    "user": {
      "username": "admin",
      "email": "admin@example.com",
      "roles": ["ADMIN"]
    }
  },
  "errors": null,
  "message": null
}
```

### Error Cases

#### Invalid credentials

HTTP `400`

```json
{
  "success": false,
  "code": 401,
  "data": null,
  "errors": null,
  "message": "Incorrect username, email, or password."
}
```

#### Validation failed

HTTP `400`

```json
{
  "success": false,
  "code": 8,
  "errors": {
    "identifier": "Identifier is required."
  },
  "message": "Validation failed."
}
```

## POST `/auth/register`

Create a new account and immediately return an access token.

### Request Body

```json
{
  "username": "newuser",
  "email": "newuser@example.com",
  "password": "Password@123",
  "confirmPassword": "Password@123"
}
```

### Validation Rules

- `username`: required
- `email`: required and must be valid email format
- `password`: required and must contain at least 1 digit and 1 special character
- `confirmPassword`: required

### Success Response

HTTP `200`

```json
{
  "success": true,
  "code": 200,
  "data": {
    "accessToken": "jwt-token",
    "user": {
      "username": "newuser",
      "email": "newuser@example.com",
      "roles": ["USER"]
    }
  },
  "errors": null,
  "message": null
}
```

### Error Cases

#### Username already exists

HTTP `400`

```json
{
  "success": false,
  "code": 5,
  "message": "Username already exists."
}
```

#### Email already exists

HTTP `400`

```json
{
  "success": false,
  "code": 4,
  "message": "Email already exists."
}
```

#### Password confirmation mismatch

HTTP `400`

```json
{
  "success": false,
  "code": 7,
  "message": "Password confirmation does not match."
}
```

#### Validation failed

HTTP `400`

```json
{
  "success": false,
  "code": 8,
  "errors": {
    "email": "Email is not valid."
  },
  "message": "Validation failed."
}
```
