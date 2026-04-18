# Настройка модуля ЭЦП

## Что уже используется
- хранилище ключей: `PKCS12`
- алгоритм подписи: `SHA256withRSA`
- канонизация JSON: `RFC 8785 (JCS)`

## Как получить публичный ключ
После запуска сервиса:
- Base64: `GET /api/licenses/public-key`
- PEM: `GET /api/licenses/public-key/pem`

## Что добавить в GitHub / GitLab
### Variables
- `SIGNATURE_PUBLIC_KEY_BASE64` — значение из `GET /api/licenses/public-key`

### Secrets
- `SIGNATURE_KEYSTORE_PASSWORD`
- `SIGNATURE_KEY_PASSWORD`
- при необходимости `SERVER_SSL_KEY_STORE_PASSWORD`

## Проверка подписи тикета
1. Вызвать `POST /api/licenses/activate`, `POST /api/licenses/renew` или `POST /api/licenses/check`
2. Получить `ticket` и `signature`
3. Передать их в `POST /api/licenses/verify-ticket`
4. Убедиться, что сервис возвращает `{ "valid": true }`
