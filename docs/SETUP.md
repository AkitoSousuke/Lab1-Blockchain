# Hướng dẫn cài đặt môi trường — Lab 01

Tài liệu dành cho các thành viên mới tham gia dự án.

**Mục tiêu:** Sau khi hoàn thành hướng dẫn, bạn có thể mở project trong VS Code và chạy thành công các bài kiểm thử Java.

Không cần có kiến thức về blockchain để thực hiện phần này.

## 1. Cài đặt Java JDK 21

Tải JDK 21 từ:

https://adoptium.net/

Chọn Java 21 (LTS), hệ điều hành Windows và phiên bản phù hợp với máy.

Sau khi cài đặt, mở Terminal trong VS Code:

```bash
java -version
```

Tiếp tục:

```bash
javac -version
```

Cả hai lệnh phải hiển thị phiên bản 21.

Nếu lệnh không được nhận diện, kiểm tra lại quá trình cài JDK và biến môi trường `PATH`.

## 3. Cài đặt VS Code

Mở VS Code và truy cập Extensions bằng tổ hợp phím `Ctrl + Shift + X`.

Tìm và cài:

**Extension Pack for Java — Microsoft**

Extension này hỗ trợ lập trình, chạy và kiểm thử Java.

## 4. Clone repository

Sau khi trưởng nhóm cung cấp đường dẫn GitHub, mở Git Bash và chạy:

```bash
git clone <REPOSITORY_URL>
```

Thay `<REPOSITORY_URL>` bằng địa chỉ repository thực tế.

Di chuyển vào thư mục vừa clone:

```bash
cd Lab1-Blockchain
```

Tên thư mục thực tế phụ thuộc vào tên repository.

## 5. Mở project trong VS Code

Trong thư mục project, chạy:

```bash
code .
```

Hoặc mở VS Code, chọn:

`File → Open Folder`

Chọn thư mục chứa `pom.xml`.

Chờ VS Code nhận diện Java project.

## 6. Chạy kiểm thử

Trong Git Bash:

```bash
./mvnw clean test
```

Trong PowerShell:

```powershell
.\mvnw.cmd clean test
```

**Không cần cài Maven riêng.**

Maven Wrapper sẽ sử dụng phiên bản Maven đã được cấu hình trong project.

Lần đầu chạy có thể cần tải Maven và thư viện JUnit từ Internet.

Khi chạy thành công, terminal hiển thị:

```text
Tests run: 3, Failures: 0, Errors: 0
BUILD SUCCESS
```

Số lượng test có thể tăng khi dự án phát triển.

## 7. Nếu gặp lỗi

### Không nhận diện `java`

Kiểm tra JDK đã cài và biến môi trường `PATH`.

### Java không đúng phiên bản

Kiểm tra:

```bash
java -version
javac -version
```

Cả hai phải sử dụng Java 21.

### Không chạy được Maven Wrapper

Kiểm tra thư mục project có các file:

- `mvnw`
- `mvnw.cmd`
- `.mvn/wrapper/maven-wrapper.properties`
- `pom.xml`

Nếu dùng Windows PowerShell, thử `.\mvnw.cmd clean test`.

### Maven báo lỗi tải dependency

Kiểm tra kết nối Internet và chạy lại.

Nếu vẫn lỗi, gửi toàn bộ thông báo lỗi cho nhóm thay vì tự ý thay đổi `pom.xml`.

## 8. Xác nhận hoàn thành

Thành viên được xem là hoàn thành thiết lập khi:

- [ ] `java -version` hiển thị Java 21.
- [ ] `javac -version` hiển thị Java 21.
- [ ] `git --version` chạy thành công.
- [ ] VS Code nhận diện Java project.
- [ ] `./mvnw clean test` hiển thị `BUILD SUCCESS`.

Sau khi hoàn thành, thông báo cho trưởng nhóm để được giao nhiệm vụ lập trình.
