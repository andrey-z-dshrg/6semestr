# Antivirus Signatures Module

This module now follows the ER diagram with three tables:

- `signatures`
- `signatures_history`
- `signatures_audit`

## Main fields

The current signature record stores:

- `id` as UUID
- `threatName`
- `firstBytes`
- `remainderHashHex`
- `remainderLength`
- `fileType`
- `offsetStart`
- `offsetEnd`
- `updatedAt`
- `status`
- `digitalSignatureBase64`

## API flow

1. `POST /api/signatures` creates a new signature and signs it.
2. `PUT /api/signatures/{id}` saves the previous version to history, updates the current row, and writes an audit event.
3. `DELETE /api/signatures/{id}` performs a logical delete by switching `status` to `DELETED`.
4. `GET /api/signatures/{id}/history` returns previous versions from `signatures_history`.
5. `GET /api/signatures/{id}/audit` returns audit entries from `signatures_audit`.
6. `POST /api/signatures/{id}/verify` checks the stored digital signature.

## Audit semantics

- `fieldsChanged` contains a comma-separated list of changed fields.
- `description` explains the operation in a human-readable way.
- `changedBy` stores the authenticated username, or `system` if there is no user.

## Public key endpoints

- `GET /api/signatures/public-key`
- `GET /api/signatures/public-key/pem`

These endpoints expose the public key used to verify `digitalSignatureBase64`.
