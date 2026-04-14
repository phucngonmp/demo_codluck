# Hướng Dẫn Test API Import/Export DataTable

Tài liệu này hướng dẫn test flow import dữ liệu lớn và export file CSV/PDF cho bảng `DataTable`.

## 1. Chuẩn Bị

Trước khi test, cần đảm bảo:

- Backend đang chạy ở `http://localhost:8080`.
- Database đã kết nối thành công.
- Có tài khoản `ADMIN` để gọi các API `/api/admin/**`.
- Nếu dùng Postman/Insomnia, thêm header:

```http
Authorization: Bearer <ADMIN_JWT_TOKEN>
Content-Type: application/json
```

Lưu ý: các API export nằm dưới `/api/admin/**`, nên user thường không gọi được. Nếu token không phải admin, API sẽ trả `403 Forbidden`.

## 2. Flow Test Nhanh

Thứ tự test đề xuất:

1. Login bằng tài khoản admin để lấy JWT token.
2. Kiểm tra dữ liệu DataTable hiện có.
3. Nếu cần dữ liệu lớn, chạy bulk import.
4. Gọi API tạo export job CSV hoặc PDF.
5. Poll API status đến khi job `COMPLETED`.
6. Gọi API download file.

## 3. Login Admin Lấy Token

```http
POST /auth/login
```

Body ví dụ:

```json
{
  "email": "admin@example.com",
  "password": "your-password"
}
```

Sau khi login thành công, copy JWT token từ response và dùng cho các API admin bên dưới.

## 4. Kiểm Tra Dữ Liệu DataTable

```http
GET /api/data-tables?page=0&size=10&keyword=firefox&sort=browser,asc
```

Ví dụ không search:

```http
GET /api/data-tables?page=0&size=10&sort=createdAt,desc
```

Các field có thể sort:

- `id`
- `renderingEngine`
- `browser`
- `platforms`
- `engineVersion`
- `cssGrade`
- `createdAt`

Ví dụ sort tăng dần:

```http
GET /api/data-tables?page=0&size=10&sort=browser,asc
```

Ví dụ sort giảm dần:

```http
GET /api/data-tables?page=0&size=10&sort=browser,desc
```

## 5. Bulk Import Dữ Liệu Lớn

Chức năng import hiện tại chạy bằng command runner để tạo dữ liệu random phục vụ test dữ liệu lớn.

Chạy lệnh:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--app.data-table.bulk-import.enabled=true --app.data-table.bulk-import.count=100000 --app.data-table.bulk-import.batch-size=1000"
```

Ý nghĩa:

- `app.data-table.bulk-import.enabled=true`: bật import.
- `app.data-table.bulk-import.count=100000`: tạo 100.000 bản ghi.
- `app.data-table.bulk-import.batch-size=1000`: lưu theo batch 1.000 bản ghi/lần.

Sau khi chạy xong, gọi lại API list:

```http
GET /api/data-tables?page=0&size=10
```

Nếu muốn test nhẹ trước, có thể giảm count:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--app.data-table.bulk-import.enabled=true --app.data-table.bulk-import.count=1000 --app.data-table.bulk-import.batch-size=200"
```

## 6. Tạo Export Job CSV

```http
POST /api/admin/data-tables/exports?lang=vi
```

Headers:

```http
Authorization: Bearer <ADMIN_JWT_TOKEN>
Content-Type: application/json
```

Body:

```json
{
  "format": "CSV",
  "sort": "browser,asc"
}
```

Response mong đợi:

```json
{
  "id": 1,
  "format": "CSV",
  "status": "PENDING",
  "fileName": null,
  "downloadUrl": null,
  "message": "Đang xuất file, vui lòng chờ",
  "createdAt": "...",
  "completedAt": null
}
```

API này trả về ngay, không đợi export xong. Backend sẽ xử lý export bất đồng bộ ở background để tránh timeout.

## 7. Tạo Export Job PDF

```http
POST /api/admin/data-tables/exports?lang=vi
```

Body:

```json
{
  "format": "PDF",
  "keyword": "",
  "sort": "createdAt,desc"
}
```

Export PDF toàn bộ bảng:

```json
{
  "format": "PDF",
  "sort": "createdAt,desc"
}
```

## 8. Kiểm Tra Trạng Thái Export Job

Sau khi tạo job, lấy `id` trong response và gọi:

```http
GET /api/admin/data-tables/exports/{jobId}?lang=vi
```

Ví dụ:

```http
GET /api/admin/data-tables/exports/1?lang=vi
```

Các trạng thái có thể gặp:

- `PENDING`: job vừa được tạo, đang chờ xử lý.
- `PROCESSING`: backend đang xuất file.
- `COMPLETED`: đã xuất file xong, có thể download.
- `FAILED`: export lỗi, xem `message` để biết lý do.

Khi hoàn tất, response sẽ có `downloadUrl`, ví dụ:

```json
{
  "id": 1,
  "format": "CSV",
  "status": "COMPLETED",
  "fileName": "data-table-export-1.csv",
  "downloadUrl": "/api/admin/data-tables/exports/1/download",
  "message": "Xuất file thành công",
  "createdAt": "...",
  "completedAt": "..."
}
```

## 9. Download File Export

Chỉ download khi job đã `COMPLETED`.

```http
GET /api/admin/data-tables/exports/{jobId}/download
```

Ví dụ:

```http
GET /api/admin/data-tables/exports/1/download
```

Header:

```http
Authorization: Bearer <ADMIN_JWT_TOKEN>
```

Nếu dùng trình duyệt, có thể mở:

```text
http://localhost:8080/api/admin/data-tables/exports/1/download
```

Nếu chưa login bằng token admin trên browser, nên test bằng Postman/Insomnia sẽ dễ hơn.

## 10. Test Sort Khi Export

Export sort tăng dần theo browser:

```json
{
  "format": "CSV",
  "sort": "browser,asc"
}
```

Export sort giảm dần theo browser:

```json
{
  "format": "CSV",
  "sort": "browser,desc"
}
```

Export sort giảm dần theo thời gian tạo:

```json
{
  "format": "PDF",
  "sort": "createdAt,desc"
}
```

## 11. Lưu Ý Về Keyword Khi Export

API list vẫn hỗ trợ search bằng `keyword`:

```http
GET /api/data-tables?page=0&size=10&keyword=firefox&sort=browser,asc
```

Riêng API export được cấu hình để xuất toàn bộ dữ liệu trong bảng `data_tables`, không chỉ xuất dữ liệu của page hiện tại và không lọc theo `keyword`.

## 12. Test Các Trường Hợp Lỗi

Không gửi token:

```http
POST /api/admin/data-tables/exports?lang=vi
```

Kết quả mong đợi: `401 Unauthorized` hoặc `403 Forbidden`.

Dùng token user thường:

```http
POST /api/admin/data-tables/exports?lang=vi
```

Kết quả mong đợi: `403 Forbidden`.

Format không hợp lệ:

```json
{
  "format": "EXCEL",
  "sort": "browser,asc"
}
```

Kết quả mong đợi: API trả lỗi validation hoặc bad request.

Download job chưa hoàn tất:

```http
GET /api/admin/data-tables/exports/1/download
```

Kết quả mong đợi: API báo file chưa sẵn sàng hoặc job chưa hoàn tất.

Download job không tồn tại:

```http
GET /api/admin/data-tables/exports/999999/download
```

Kết quả mong đợi: API trả lỗi không tìm thấy job.

## 13. File Export Được Lưu Ở Đâu?

File export được lưu trên server trong thư mục:

```text
exports/
```

Thư mục này là output runtime, không nên commit lên GitHub. Chỉ commit code, config mẫu, và tài liệu hướng dẫn.

## 14. Flow Tổng Quát

```text
Admin login
  -> lấy JWT token
  -> POST /api/admin/data-tables/exports
  -> backend tạo job PENDING và trả response ngay
  -> backend xử lý export bất đồng bộ
  -> FE poll GET /api/admin/data-tables/exports/{jobId}
  -> khi status COMPLETED thì FE hiển thị nút download
  -> GET /api/admin/data-tables/exports/{jobId}/download
```
