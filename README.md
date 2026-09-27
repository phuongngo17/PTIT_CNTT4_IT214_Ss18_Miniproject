# RikkeiBank Microservices

Backend ngân hàng mẫu theo mô hình database-per-service, Java 21, Spring Boot 3.5.16 và Spring Cloud 2025.0.3. Mọi API bên ngoài đi qua Gateway tại `http://localhost:8080`; các service nghiệp vụ không cần public port khi chạy bằng Compose.

## Kiến trúc

```text
Mobile/Web/Postman
        |
        v
 API Gateway :8080 -- JWT + Redis revocation
        |
        +--> identity-service      -> identity_db
        +--> customer-service      -> customer_db + Redis cache
        +--> account-service       -> account_db  + Redis cache
        +--> transaction-service   -> transaction_db
                 |       |
                 |Feign  +--> Kafka topic bank.transfer.events
                 v                   |
          account-service            v
                            notification-service -> notification_db

Config Server :8888      Eureka :8761      Redis      Kafka (KRaft)
```

Mỗi service chỉ truy cập database của chính nó. Trao đổi dữ liệu liên service đi qua REST/Feign hoặc Kafka; không có join hay foreign key xuyên database.

| Module | Trách nhiệm | Dữ liệu sở hữu |
|---|---|---|
| `identity-service` | JWT access/refresh, user/staff, role, force logout | user, refresh token |
| `customer-service` | CRUD hồ sơ khách hàng | customer |
| `account-service` | CRUD account type/account, thay đổi số dư idempotent | account, account type, ledger operation |
| `transaction-service` | Saga orchestrator, lịch sử giao dịch, phạm vi teller | transaction |
| `notification-service` | Kafka consumer và thông báo biến động | notification |
| `api-gateway` | route `lb://`, JWT, kiểm tra revoke Redis | không có DB |
| `config-server` | cấu hình dùng chung dạng native repository | không có DB |
| `discovery-server` | Eureka registry | registry tạm thời |

### Vì sao dùng Microservice

- Account/Transaction cần scale và kiểm soát lỗi độc lập với hồ sơ Customer.
- Database-per-service loại bỏ coupling ở tầng schema và ngăn một service đọc trộm dữ liệu của service khác.
- Kafka tách Notification khỏi đường giao dịch chính; notification lỗi không làm mất giao dịch tài chính.
- Gateway, Eureka và LoadBalancer giữ client không phụ thuộc địa chỉ instance cụ thể.
- Đổi lại hệ thống chấp nhận consistency cuối cùng, vận hành phức tạp hơn và phải dùng Saga/idempotency thay cho ACID xuyên service.

## Bảo mật

- Access token HMAC-SHA256 sống 15 phút; refresh token ngẫu nhiên sống 30 ngày, chỉ lưu SHA-256 hash và bị rotate sau mỗi lần refresh.
- ADMIN gọi `DELETE /api/auth/sessions/{userId}` để xóa refresh token và đặt revoke key trong Redis. Gateway từ chối access token hiện tại ngay lập tức.
- `@PreAuthorize` bảo vệ role ở service đích, không chỉ ở Gateway.
- CUSTOMER chỉ lấy hồ sơ, account, transaction và notification có `customerId` trong JWT của chính họ.
- TELLER chỉ xem transaction hôm nay có `assignedTellerId` bằng subject trong JWT.
- Các endpoint ghi nợ/ghi có nội bộ không được route qua Gateway và yêu cầu `X-Internal-Key`. Production cần thay cả `JWT_SECRET` và `INTERNAL_KEY`, dùng TLS/mTLS và secret manager.
- Error body chung gồm `timestamp`, `status`, `error`, `code`, `message`, `path`, `validationErrors`; stack trace chỉ ghi server log.

## Saga chuyển khoản

`transaction-service` là orchestrator:

1. Tạo transaction `PENDING`.
2. Gọi account-service debit tài khoản nguồn với `operationId = transactionId`.
3. Chuyển trạng thái `DEBITED`, phát Kafka event.
4. Gọi credit tài khoản đích.
5. Thành công: `COMPLETED`, phát event; notification-service lưu thông báo.
6. Credit lỗi: gọi `REFUND` nguồn. Thành công thì transaction là `COMPENSATED` và phát compensating event.

Account-service khóa bi quan dòng account khi cập nhật số dư và lưu unique `(operationId, kind)`, nên retry không trừ/cộng hai lần. Circuit Breaker `accountService` chuyển qua `CLOSED -> OPEN -> HALF_OPEN`; endpoint quan sát là `/actuator/circuitbreakers`.

> Bản mẫu này minh họa orchestration rõ ràng. Với production, nên bổ sung transactional outbox/CDC để đảm bảo DB commit và Kafka publish là atomic, thay cho publish bất đồng bộ trực tiếp sau khi đổi trạng thái.

## Chạy toàn bộ bằng Docker Compose

Yêu cầu Docker Engine/Compose và khoảng 6 GB RAM.

```bash
cp .env.example .env
# thay JWT_SECRET và INTERNAL_KEY trong .env
docker compose -f compose.yml up --build
```

Chờ health check hoàn tất rồi mở:

- Gateway: `http://localhost:8080/actuator/health`
- Config Server: `http://localhost:8888/actuator/health`
- Eureka dashboard: `http://localhost:8761`

### Chạy infrastructure bằng Docker, service bằng IntelliJ

Chọn **JDK 21** cho Project SDK và Run Configuration. Không dùng JDK 25 cho baseline Spring Boot 3.5; cảnh báo Netty `sun.misc.Unsafe` trên JDK 25 không làm crash nhưng cho biết runtime nằm ngoài baseline của dự án.

```bash
docker compose -f compose.yml up -d postgres redis kafka config-server discovery-server
```

Sau đó chạy service trong IDE theo thứ tự: `identity-service`, `customer-service`, `account-service`, `transaction-service`, `notification-service`, cuối cùng `api-gateway`. Hạ tầng được bind vào loopback để IDE dùng các giá trị mặc định an toàn:

- Config Server: `localhost:8888`
- Eureka: `localhost:8761`
- PostgreSQL: `localhost:5432`
- Redis: `localhost:6379`
- Kafka external listener: `localhost:9092`

Nếu Config Server chưa chạy, import đang để `optional` nên service dùng cấu hình local và chỉ ghi warning. Redis là bắt buộc cho cache/revocation; Kafka là bắt buộc để nhận notification. Gateway cần Eureka có các service đã đăng ký thì route `lb://...` mới hoạt động.

Tài khoản bootstrap chỉ dành cho local demo: `admin / Admin@123`. PostgreSQL chứa năm database logic độc lập; xóa volume nếu cần seed lại từ đầu: `docker compose -f compose.yml down -v` (lệnh này xóa toàn bộ dữ liệu local).

## Chạy/test bằng Maven

```bash
mvn clean verify
```

JaCoCo report của từng service nằm tại `<service>/target/site/jacoco/index.html`. Máy không có Docker vẫn có thể compile/test với H2; Kafka/Redis/Eureka cần chạy riêng nếu muốn khởi động đầy đủ các service.

## Postman

Import theo thứ tự:

1. `postman/RikkeiBank.postman_environment.json`
2. `postman/RikkeiBank.postman_collection.json`

Chọn environment **RikkeiBank Local**, sau đó chạy folder từ `01` đến `09`. Script tự lưu token và các UUID. Folder `07` có cả luồng thành công và rollback có chủ đích. Failure injection chỉ bật trong Compose bằng `DEMO_FAILURE_INJECTION=true`; mặc định source code tắt tính năng này.

## REST và status code

| Nghiệp vụ | Method/path | Thành công | Quyền |
|---|---|---:|---|
| Login/refresh | `POST /api/auth/login`, `/refresh` | 200 | public |
| Thu hồi phiên | `DELETE /api/auth/sessions/{userId}` | 204 | ADMIN |
| CRUD staff | `/api/staff` | 200/201/204 | ADMIN |
| CRUD customer | `/api/customers` | 200/201/204 | ADMIN; self-read CUSTOMER |
| CRUD account type | `/api/account-types` | 200/201/204 | ADMIN |
| CRUD account | `/api/accounts` | 200/201/204 | ADMIN; self-read CUSTOMER |
| Chuyển khoản | `POST /api/transfers` | 201 | CUSTOMER |
| Lịch sử của tôi | `GET /api/transactions/mine` | 200 | CUSTOMER |
| Giao dịch được giao hôm nay | `GET /api/transactions/today` | 200 | TELLER |
| Giao transaction | `PATCH /api/transactions/{id}/assign/{tellerId}` | 200 | ADMIN |
| Notification của tôi | `GET /api/notifications/me` | 200 | CUSTOMER |

Response lỗi sử dụng 400 (validation/nghiệp vụ), 401 (token thiếu/sai/hết hạn/revoked), 403 (đúng danh tính nhưng sai quyền), 404, 409 và 503 phù hợp.

## Cache-aside

`CustomerService.get` và `AccountTypeService.get` dùng `@Cacheable`; create/update dùng `@CachePut`, delete dùng `@CacheEvict`. Cache TTL mặc định 10 phút. Gọi request Postman “Get account type twice” và xem Redis/DB log để minh chứng cache hit.

## Cấu trúc repository

```text
common/                shared error contract, JWT decoder, Kafka event schema
config-server/         centralized native config repository
discovery-server/      Eureka
api-gateway/           WebFlux Gateway, load balancing, token revocation
identity-service/
customer-service/
account-service/
transaction-service/
notification-service/
postman/               collection + environment
docker/                database initialization
docs/                   presentation runbook
```
