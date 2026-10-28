# Lab 01 — Phân công công việc nhóm

## 1. Mục tiêu chung

Nhóm gồm 5 thành viên, cùng xây dựng một chương trình mô phỏng blockchain bằng Java.

Hãy hình dung hệ thống có 8 máy tính đang trao đổi dữ liệu với nhau. Mỗi máy giữ bản sao dữ liệu riêng.

Khi có dữ liệu mới, các máy cần trao đổi và thống nhất trước khi chính thức ghi dữ liệu đó vào blockchain.

Vấn đề là:

- Tin nhắn có thể đến chậm hoặc bị mất.
- Một tin nhắn có thể được gửi nhiều lần.
- Một số máy có thể ngừng hoạt động.
- Một số máy có thể cố tình gửi thông tin sai.

Nhiệm vụ của nhóm là xây dựng chương trình vẫn hoạt động đúng trong những điều kiện được đề bài quy định.

## 2. Phân công tổng quan

| Thành viên | Phần phụ trách                | Giải thích đơn giản                                       |
| ---------- | ----------------------------- | --------------------------------------------------------- |
| TV1        | Mật mã và dữ liệu             | Kiểm tra thông tin có thực sự do đúng người gửi hay không |
| TV2        | Blockchain và xử lý giao dịch | Quản lý dữ liệu, transaction và các block                 |
| TV3        | Mạng mô phỏng                 | Mô phỏng cách các node gửi và nhận tin nhắn               |
| TV4        | Thuật toán đồng thuận         | Xây dựng quy tắc bỏ phiếu để các node thống nhất          |
| TV5        | Tích hợp và kiểm thử          | Kết nối các phần và kiểm tra toàn hệ thống                |

Đây là phân công dự kiến. Các thành viên sẽ được giao công việc nhỏ và cụ thể thông qua GitHub Issues.

## 3. Công việc từng thành viên

### TV1 — Mật mã và cấu trúc dữ liệu

**Mục đích:** Giúp hệ thống kiểm tra nguồn gốc và tính toàn vẹn của thông điệp.

Ví dụ: Alice gửi yêu cầu cập nhật dữ liệu. Chương trình cần kiểm tra yêu cầu đó có chữ ký hợp lệ của Alice hay không.

Cần tìm hiểu:

- Public key và private key.
- Chữ ký số Ed25519.
- Hàm băm SHA-256.
- Cách chuyển dữ liệu thành chuỗi byte thống nhất.

Công việc dự kiến:

- Xây dựng chức năng ký thông điệp.
- Xây dựng chức năng xác minh chữ ký.
- Xây dựng chức năng tính hash.
- Định nghĩa các cấu trúc dữ liệu chung theo thiết kế nhóm đã thống nhất.
- Viết test kiểm tra chữ ký hợp lệ và không hợp lệ.

### TV2 — Blockchain và xử lý giao dịch

**Mục đích:** Xây dựng nơi lưu trữ và xử lý dữ liệu blockchain.

Ví dụ: Alice gửi transaction yêu cầu lưu `Alice/message = Hello`.

Khi transaction hợp lệ, chương trình cập nhật dữ liệu của Alice.

Cần tìm hiểu:

- Transaction là gì?
- Block chứa những gì?
- Blockchain liên kết các block như thế nào?
- Hash được dùng để phát hiện thay đổi dữ liệu ra sao?

Công việc dự kiến:

- Xây dựng cấu trúc block.
- Quản lý danh sách block đã được chốt.
- Thực hiện cập nhật dữ liệu theo transaction.
- Kiểm tra tính hợp lệ của block.
- Ngăn một transaction được áp dụng nhiều lần.
- Viết test kiểm tra kết quả xử lý.

### TV3 — Mạng mô phỏng

**Mục đích:** Xây dựng môi trường để các node giao tiếp với nhau.

Ví dụ: Node A gửi thông điệp cho Node B. Thông điệp có thể đến sau 100 ms, đến hai lần hoặc bị mất.

Cần tìm hiểu:

- Node giao tiếp với nhau như thế nào?
- Message là gì?
- Delay, drop, duplicate và reorder là gì?
- Event queue hoạt động như thế nào?

Công việc dự kiến:

- Xây dựng cơ chế gửi và nhận message.
- Mô phỏng độ trễ mạng.
- Mô phỏng thông điệp bị mất hoặc trùng.
- Giới hạn tốc độ gửi thông điệp.
- Mô phỏng chặn và mở chặn node gửi quá nhiều.
- Viết test kiểm tra hoạt động mạng.

### TV4 — Thuật toán đồng thuận

**Mục đích:** Giúp các validator thống nhất block nào được chấp nhận.

Ví dụ: Có 4 validator và tối đa 1 validator Byzantine. Hệ thống cần đủ số phiếu hợp lệ theo quy tắc đồng thuận trước khi chốt block.

Cần tìm hiểu:

- Byzantine Fault Tolerance là gì?
- Validator khác node thông thường thế nào?
- PREVOTE và PRECOMMIT là gì?
- Quorum là gì?
- Locking dùng để làm gì?

Công việc dự kiến:

- Chọn node đề xuất block.
- Kiểm tra đề xuất trước khi bỏ phiếu.
- Triển khai PREVOTE.
- Triển khai PRECOMMIT.
- Kiểm tra số phiếu hợp lệ.
- Xử lý timeout và chuyển round.
- Xử lý locking/unlocking.
- Quyết định khi nào block được chốt.

Đây là module phức tạp nhất và cần sự hỗ trợ của các thành viên khác.

### TV5 — Tích hợp và kiểm thử

**Mục đích:** Kết nối các module thành một chương trình hoàn chỉnh.

Ví dụ: Khởi chạy 8 node, cho các node trao đổi thông điệp, thực hiện đồng thuận và kiểm tra dữ liệu cuối cùng có thống nhất hay không.

Cần tìm hiểu:

- Unit test và integration test.
- JUnit 5.
- Cách chạy chương trình mô phỏng.
- Safety và liveness.
- Cách tạo kết quả có thể tái lập.

Công việc dự kiến:

- Xây dựng chương trình khởi chạy mô phỏng.
- Kết nối các module.
- Đọc cấu hình số node và điều kiện mạng.
- Xây dựng các kịch bản kiểm thử T1–T8.
- Thu thập và đối chiếu kết quả.
- Kiểm tra hai lần chạy cùng cấu hình có log giống nhau.

## 4. Nguyên tắc phối hợp

Các thành viên không cần hiểu toàn bộ thuật toán ngay từ đầu.

Tuy nhiên, trước khi hoàn thành phần việc, mỗi người cần:

- Hiểu mục đích module mình phụ trách.
- Biết dữ liệu đầu vào và đầu ra.
- Chạy được các bài test liên quan.
- Giải thích được cách triển khai.
- Ghi lại vấn đề gặp phải và cách giải quyết.

Các cấu trúc dữ liệu chung, interface và quy tắc giao tiếp giữa module sẽ do nhóm thống nhất trước khi bắt đầu triển khai.

## 5. Trạng thái công việc

| Hạng mục                 | Trạng thái                      |
| ------------------------ | ------------------------------- |
| Chuẩn bị môi trường Java | Hoàn thành trên máy trưởng nhóm |
| Thiết lập GitHub         | Chưa thực hiện                  |
| Thống nhất kiến trúc     | Chưa thực hiện                  |
| Phân công chính thức     | Chưa thực hiện                  |
| Lập trình các module     | Chưa thực hiện                  |
| Kiểm thử toàn hệ thống   | Chưa thực hiện                  |

## 6. Lưu ý

Tài liệu này chỉ mô tả trách nhiệm tổng quan, chưa phải danh sách nhiệm vụ lập trình cụ thể.

Sau khi thống nhất kiến trúc, trưởng nhóm sẽ tạo GitHub Issues với hướng dẫn, file cần chỉnh sửa và tiêu chí kiểm thử cho từng nhiệm vụ.
