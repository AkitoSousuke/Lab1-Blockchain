# Tài liệu kỹ thuật — Các kiểu dữ liệu dùng chung

## 1. Mục đích

Tài liệu giải thích các kiểu dữ liệu cơ bản của hệ thống blockchain.

Các thành viên cần đọc tài liệu này trước khi triển khai module của mình.

Hiện tại, chúng ta mới định nghĩa cấu trúc dữ liệu. Các quy tắc xác thực và xử lý sẽ được bổ sung sau.

## 2. Transaction.java

**Transaction là một yêu cầu thay đổi dữ liệu.**

Ví dụ: Alice muốn lưu một thông điệp.

| Thuộc tính | Giá trị ví dụ      |
| ---------- | ------------------ |
| chainId    | lab01              |
| senderId   | Alice              |
| key        | message            |
| value      | Hello              |
| nonce      | 1                  |
| signature  | Chữ ký dạng byte[] |

Khi xử lý thành công, dữ liệu có thể được lưu dưới dạng:

`Alice/message = Hello`

Chúng ta dự kiến quy định mỗi người chỉ được thay đổi dữ liệu thuộc về mình.

**Thành viên sử dụng:**

- TV1: Ký và xác minh transaction.
- TV2: Kiểm tra và thực thi transaction.
- TV5: Tạo transaction cho các kịch bản mô phỏng.

## 3. BlockHeader.java

**BlockHeader chứa thông tin nhận diện và xác thực một block.**

Các trường quan trọng:

- `height`: Vị trí block.
- `round`: Vòng đồng thuận.
- `parentHash`: Mã băm liên kết với block cha.
- `stateHash`: Mã băm trạng thái sau khi thực thi.
- `proposerId`: Validator đề xuất block.
- `signature`: Chữ ký của proposer.

Ví dụ: Một block có `height = 2` sẽ tham chiếu hash của block ở `height = 1` trong chuỗi hợp lệ.

**Thành viên sử dụng:**

- TV1: Ký và xác minh header.
- TV2: Kiểm tra block và liên kết giữa các block.
- TV4: Xác thực proposal trong quá trình đồng thuận.

## 4. Vote.java

**Vote là phiếu bầu của một validator.**

Có hai loại phiếu:

- `PREVOTE`: Phiếu bầu trong giai đoạn đầu.
- `PRECOMMIT`: Phiếu bầu trong giai đoạn tiếp theo.

Ví dụ:

Validator 1 gửi PREVOTE cho block X ở height 1, round 0.

Một phiếu phải được xác minh chữ ký trước khi tính vào kết quả đồng thuận.

**Thành viên sử dụng:**

- TV1: Xác minh chữ ký.
- TV3: Truyền phiếu qua mạng mô phỏng.
- TV4: Kiểm tra và đếm phiếu.

## 5. Quy tắc chung

1. Không tự ý đổi tên trường dữ liệu.
2. Không tự ý thay đổi kiểu dữ liệu.
3. Không tự tạo phiên bản Transaction, BlockHeader hoặc Vote riêng trong module.
4. Nếu cần sửa thiết kế chung, phải trao đổi trước khi sửa.
5. Mọi thay đổi phải được kiểm tra bằng unit test.

## 6. Những vấn đề chưa hoàn thiện

Các cấu trúc hiện tại chưa phải phiên bản cuối cùng.

Nhóm cần tiếp tục quyết định:

- Cách ánh xạ `senderId` với public key.
- Cách ký và xác minh từng loại thông điệp.
- Định dạng byte dùng chung cho hashing và signing.
- Quy tắc biểu diễn phiếu NIL.
- Cách chống replay transaction.
- Cách bảo vệ dữ liệu `byte[]` khỏi bị chỉnh sửa ngoài ý muốn.

Những vấn đề này phải được giải quyết trước khi tích hợp các module tương ứng.

## 7. Trạng thái

- Transaction: Đã tạo cấu trúc ban đầu.
- BlockHeader: Đã tạo cấu trúc ban đầu.
- Vote: Đã tạo cấu trúc ban đầu.
- Xác minh chữ ký: Chưa triển khai.
- Kiểm tra dữ liệu blockchain: Chưa triển khai.
