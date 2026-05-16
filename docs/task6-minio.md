# Task 6: MinIO setup

1. Start MinIO:
   `docker compose -f docker-compose.minio.yml up -d`
2. Open the MinIO console:
   [http://localhost:9001](http://localhost:9001)
3. Root credentials for the console:
   - login: `minioadmin`
   - password: `minioadmin123`
4. Application credentials used by Spring Boot:
   - access key: `app-signatures`
   - secret key: `change-me-minio-secret`
5. The bucket `signature-files` is created as private by `mc-init`.

`.env` example for the application:

```properties
MINIO_ENABLED=true
MINIO_ENDPOINT=http://localhost:9000
MINIO_ACCESS_KEY=app-signatures
MINIO_SECRET_KEY=change-me-minio-secret
MINIO_BUCKET=signature-files
MINIO_PRESIGNED_URL_TTL_MINUTES=30
SIGNATURE_FIRST_BYTES_LENGTH=16
```

New admin-only API:

1. `POST /api/signatures/upload`
   - `multipart/form-data`
   - part `file`
   - optional query parameter `threatName`
2. `POST /api/signatures/files/presigned-urls/by-ids`
   - JSON body:

```json
{
  "ids": [
    "00000000-0000-0000-0000-000000000000"
  ]
}
```
