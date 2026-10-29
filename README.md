# Lab 01 — Mô phỏng Blockchain Layer 1

## 1. Giới thiệu

Đây là đồ án thực hành số 1 của môn Blockchain.

Nhóm sẽ sử dụng Java để xây dựng một chương trình mô phỏng mạng blockchain đơn giản.

**Chúng ta không xây dựng một đồng tiền mã hóa thực tế, không sử dụng Ethereum và không cần viết giao diện web.**

Mục tiêu chính là tìm hiểu cách nhiều máy tính (gọi là node) giao tiếp và thống nhất với nhau về dữ liệu, ngay cả khi mạng có lỗi hoặc một số node cố tình hoạt động sai.

## 2. Một số khái niệm cơ bản

- **Node (nút mạng):** Một máy tính tham gia mạng blockchain. Trong bài này, mỗi node được mô phỏng bằng chương trình Java.
- **Transaction (giao dịch):** Một yêu cầu thay đổi dữ liệu, chẳng hạn Alice muốn lưu thông điệp "Hello".
- **Block (khối):** Một tập hợp các transaction được sắp xếp theo thứ tự.
- **Blockchain (chuỗi khối):** Danh sách các block liên kết với nhau bằng mã băm.
- **Validator (nút xác thực):** Node có quyền tham gia bỏ phiếu để quyết định block nào được chấp nhận.
- **Consensus (đồng thuận):** Quá trình các validator thống nhất về một block.
- **Byzantine node:** Node có thể hoạt động sai hoặc cố tình gửi thông tin mâu thuẫn.
- **Finalization (chốt khối):** Khi một block được xác nhận cuối cùng, các node trung thực không được chốt một block khác tại cùng vị trí.

## 3. Công nghệ sử dụng

| Công nghệ | Mục đích                          |
| --------- | --------------------------------- |
| Java 21   | Ngôn ngữ lập trình chính          |
| Maven     | Quản lý thư viện và biên dịch     |
| JUnit 5   | Kiểm thử chương trình             |
| Git       | Quản lý các phiên bản code        |
| GitHub    | Làm việc nhóm và lưu trữ mã nguồn |
| VS Code   | Môi trường lập trình              |

Nhóm sử dụng Maven Wrapper nên không bắt buộc phải cài Maven riêng trên từng máy.

## 4. Hướng dẫn chạy project

### Chuẩn bị

Cài đặt:

1. Java JDK 21.
2. Git.
3. VS Code.
4. Extension Pack for Java trong VS Code.

### Tải project

Sau khi được thêm vào GitHub repository, thành viên tải project về máy:

```bash
git clone <URL_REPOSITORY>
```

Thay `<URL_REPOSITORY>` bằng đường dẫn GitHub do trưởng nhóm cung cấp.

### Kiểm tra môi trường

Mở Terminal tại thư mục project:

```bash
java -version
git --version
```

### Chạy kiểm thử

Trên Windows với Git Bash:

```bash
./mvnw clean test
```

Nếu xuất hiện `BUILD SUCCESS`, quá trình biên dịch và kiểm thử đã hoàn tất thành công.

Lưu ý: Ở giai đoạn khởi tạo, chưa có chức năng blockchain hoàn chỉnh. Lệnh trên chủ yếu dùng để kiểm tra môi trường.

## 5. Cấu trúc thư mục

- `src/main/java/`: Chứa mã nguồn Java của chương trình.
- `src/test/java/`: Chứa các bài kiểm thử.
- `config/`: Chứa cấu hình mô phỏng mạng.
- `logs/`: Chứa nhật ký hoạt động của chương trình.
- `docs/`: Chứa tài liệu hướng dẫn và phân công.

## 6. Quy tắc làm việc nhóm

1. Mỗi thành viên phụ trách một phần của chương trình.
2. Không trực tiếp sửa code trên nhánh `main` sau khi thiết lập quy trình bảo vệ nhánh.
3. Mỗi người tạo nhánh riêng để làm việc.
4. Khi hoàn thành, tạo Pull Request để nhóm kiểm tra.
5. Chưa hiểu yêu cầu hoặc cách triển khai thì phải trao đổi trước khi viết code.
6. Mỗi thành viên cần hiểu và giải thích được phần code mình phụ trách.

## 7. Tiến độ hiện tại

**Giai đoạn 1 — Chuẩn bị môi trường.**

Đã khởi tạo project Java và cấu hình Maven.

Các thành phần blockchain chưa được triển khai.
