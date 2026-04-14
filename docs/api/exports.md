# Export API

Controller: `ProductExportController`  
Base path: `/exports`

## Purpose

Exports products asynchronously to CSV or PDF.  
The create API returns immediately and the file is generated in background.

## Security

All endpoints require role `USER` or `ADMIN`.

Header:

```http
Authorization: Bearer <access-token>
```

## POST `/exports`

Create an export job.

### Request Body

```json
{
  "chunkSize": 1000,
  "fileFormat": "CSV"
}
```

### Validation Rules

- `chunkSize >= 100`
- `chunkSize <= 5000`
- default `chunkSize = 1000`
- `fileFormat` must be `CSV` or `PDF`
- default `fileFormat = CSV`

### Supported Formats

- `CSV`: recommended for large datasets
- `PDF`: supported, but heavier and less suitable for very large datasets

### Success Response

HTTP `202`

```json
{
  "success": true,
  "code": 202,
  "message": "Export is being prepared. Please wait.",
  "data": {
    "jobId": 1,
    "status": "PENDING",
    "fileFormat": "CSV",
    "totalRecords": 100000,
    "processedRecords": 0,
    "progressPercent": 0,
    "downloadUrl": null
  }
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
    "fileFormat": "File format must be either CSV or PDF."
  },
  "message": "Validation failed."
}
```

## GET `/exports/{jobId}`

Get export job status and progress.

### Success Response When Processing

HTTP `200`

```json
{
  "success": true,
  "code": 200,
  "data": {
    "jobId": 1,
    "status": "PROCESSING",
    "fileFormat": "CSV",
    "totalRecords": 100000,
    "processedRecords": 15000,
    "progressPercent": 15,
    "downloadUrl": null
  }
}
```

### Success Response When Completed

HTTP `200`

```json
{
  "success": true,
  "code": 200,
  "data": {
    "jobId": 1,
    "status": "COMPLETED",
    "fileFormat": "CSV",
    "totalRecords": 100000,
    "processedRecords": 100000,
    "progressPercent": 100,
    "downloadUrl": "http://localhost:8080/exports/1/download"
  }
}
```

### Error Cases

#### Export job not found

HTTP `400`

```json
{
  "success": false,
  "code": 9,
  "message": "Export job does not exist."
}
```

#### Access denied for this job

HTTP `400`

```json
{
  "success": false,
  "code": 11,
  "message": "You do not have permission to access this export job."
}
```

## GET `/exports/{jobId}/download`

Download generated export file when the job is completed.

### Success Response

HTTP `200`

- For CSV:
  - Content-Type: `text/csv`
  - Content-Disposition: `attachment; filename="products-export-<jobId>-<timestamp>.csv"`
- For PDF:
  - Content-Type: `application/pdf`
  - Content-Disposition: `attachment; filename="products-export-<jobId>-<timestamp>.pdf"`

### Error Cases

#### Export is not ready yet

HTTP `400`

```json
{
  "success": false,
  "code": 10,
  "message": "Export file is not ready yet."
}
```

#### Export job not found

HTTP `400`

```json
{
  "success": false,
  "code": 9,
  "message": "Export job does not exist."
}
```

#### Access denied for this job

HTTP `400`

```json
{
  "success": false,
  "code": 11,
  "message": "You do not have permission to access this export job."
}
```
