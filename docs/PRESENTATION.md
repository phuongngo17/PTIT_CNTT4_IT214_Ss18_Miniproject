# Kịch bản thuyết trình RikkeiBank

## 1. Hạ tầng và discovery (2 phút)

1. Chạy `docker compose -f compose.yml up --build`.
2. Mở Eureka tại `http://localhost:8761` và chỉ ra Gateway cùng 5 business services.
3. Mở `http://localhost:8888/application/default` để minh họa centralized configuration.
4. Giải thích mọi request Postman đi qua port 8080 và route bằng service name `lb://...`.

## 2. Security và phân quyền (3 phút)

1. Chạy Postman folder 01-06.
2. Dùng CUSTOMER gọi `GET /api/accounts/me` thành công.
3. Dùng CUSTOMER gọi `GET /api/customers` và chỉ ra 403.
4. Dùng ADMIN gọi force logout, sau đó tái dùng customer access token và chỉ ra 401 tại Gateway.
5. Dùng refresh token cũ và chỉ ra 401 vì refresh token đã bị xóa/rotate.

## 3. Saga thành công (3 phút)

1. Ghi lại số dư nguồn/đích từ `GET /api/accounts` bằng ADMIN.
2. Login lại CUSTOMER nếu vừa demo revoke.
3. Chạy “Successful transfer”. Response phải là `COMPLETED`.
4. Kiểm tra số dư: nguồn giảm đúng số tiền, đích tăng đúng số tiền.
5. Mở lịch sử transaction và notification; Kafka consumer đã tạo notification bất đồng bộ.

## 4. Saga rollback (3 phút)

1. Ghi lại số dư nguồn trước test.
2. Chạy “Rollback demo (COMPENSATED)”. Compose đã bật failure injection ở đúng khoảng giữa debit và credit.
3. Response là `COMPENSATED`; số dư nguồn sau test bằng trước test, số dư đích không đổi.
4. Theo dõi log `transaction-service`/`notification-service` để thấy event `COMPENSATED` và thông báo hoàn tiền.
5. Nhấn mạnh ledger operation idempotency: `DEBIT`, `CREDIT`, `REFUND` dùng cùng transaction ID nhưng kind khác nhau.

## 5. Circuit Breaker và cache (3 phút)

1. Dừng `account-service`, gọi chuyển khoản ít nhất 3 lần.
2. Mở `http://localhost:8084/actuator/circuitbreakers`; state chuyển sang `OPEN`.
3. Sau 10 giây, state là `HALF_OPEN`; khởi động account-service và gọi lại để đóng circuit.
4. Với cache, gọi GET account type hai lần và quan sát query DB chỉ xuất hiện ở lần cache miss đầu tiên.

## 6. Teller isolation (2 phút)

1. ADMIN assign một transaction cho `teller01`.
2. Login teller và gọi `/api/transactions/today`: chỉ transaction được assign xuất hiện.
3. Teller không có quyền CRUD customer/account type/account và không thấy giao dịch của teller khác.

## Điểm cần nói rõ

- Đây là Saga orchestration, không phải distributed ACID transaction.
- WebFlux nằm tại Gateway để xử lý I/O route non-blocking; Kafka đảm nhiệm event-driven asynchronous processing.
- Production cần TLS/mTLS, Vault/KMS, migrations Flyway, transactional outbox, monitoring/tracing và Kafka multi-broker; các secret/demo credentials hiện tại chỉ phục vụ bài trình diễn local.
