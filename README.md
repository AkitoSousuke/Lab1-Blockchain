# Lab 01 - Mô phỏng Blockchain Layer 1 bằng Python

Dự án nhóm cho bài Lab 01 môn Blockchain: xây dựng mạng mô phỏng mà các node trung thực thống nhất chuỗi block finalized và trạng thái, kể cả khi có lỗi mạng hoặc validator Byzantine.

**Trạng thái hiện tại: chỉ có khung thư mục Python và tài liệu.** Các file `.py` chỉ chứa mô tả trách nhiệm; chưa có thuật toán, CLI, test hay simulator chạy được.

## Đọc tài liệu theo thứ tự

1. [SETUP](docs/SETUP.md): chuẩn bị môi trường Python.
2. [PROJECT_STRUCTURE](docs/PROJECT_STRUCTURE.md): mục đích từng thư mục và từng file.
3. [INTERFACES](docs/INTERFACES.md): ranh giới các module và những quyết định còn cần thống nhất.
4. [TASKS](docs/TASKS.md): gợi ý phân công 5 thành viên và checklist nghiệm thu.

## Công nghệ và thiết lập

Chọn Python 3.11 trở lên cho khung này; dùng `venv`, `pip` và `pytest`. `pyproject.toml` chứa metadata, cấu hình package và cấu hình test. Chưa chọn/cài thư viện mật mã và chưa khóa phiên bản dependency.

PowerShell, chạy tại thư mục chứa `pyproject.toml`:

```powershell
py -3.11 -m venv .venv
.\.venv\Scripts\python.exe -m pip install -e ".[dev]"
.\.venv\Scripts\python.exe -c "import blockchain; print(blockchain.__file__)"
```

Nếu dùng Python mới hơn, thay `-3.11` bằng phiên bản đã cài. Lệnh import chỉ kiểm tra package, không chạy blockchain. Hướng dẫn macOS/Linux ở [SETUP](docs/SETUP.md).

Lệnh kiểm thử chung sau khi nhóm viết test:

```powershell
.\.venv\Scripts\python.exe -m pytest
```

Hiện chưa có test; pytest sẽ báo `no tests ran` và trả mã thoát 5. Đây không phải kết quả PASS bài lab. Chưa có lệnh chạy simulator hoặc script so sánh hai lần chạy.

## Cấu trúc chính

```text
src/blockchain/  # Python: model, crypto, execution, ledger, network, consensus, simulator
tests/          # Unit test, integration test và fixture (hiện chỉ giữ thư mục)
config/         # Cấu hình và kịch bản mô phỏng sẽ được thêm sau
logs/           # Bằng chứng chạy và log tái lập sẽ được thêm sau
scripts/        # Script hỗ trợ, gồm kiểm tra determinism trong giai đoạn sau
docs/           # Hướng dẫn hiện hành cho dự án Python
```

## Phạm vi bài lab cần đạt khi triển khai

- Transaction/header/vote có chữ ký và domain riêng; dữ liệu ký/hash có encoding xác định.
- State key–value, kiểm tra quyền sở hữu và chống replay; cùng đầu vào cho cùng kết quả.
- Mạng ít nhất 8 node, có delay/drop/duplicate/reorder, hạn mức gửi và block/unblock peer; header được chấp nhận trước khi gửi body.
- Validator cố định, `n = 3f + 1`; hai pha Prevote/Precommit, quorum, lock và timeout. Safety luôn đúng, liveness sau khi mạng ổn định theo giả thiết đề.
- Unit test và T1–T8; cùng input chạy hai lần cho log giống từng byte và cùng final state hash.

Số node và số validator là hai tham số riêng. Ví dụ 8 node có thể gồm 4 validator và 4 observer; nếu tất cả node là validator thì tối thiểu 10 để đồng thời thỏa `>= 8` và `n = 3f + 1`. Chưa đặt default chạy trong khung này.

## Làm việc nhóm và bài nộp

Mỗi thành viên làm trên nhánh riêng và mở Pull Request; thống nhất kiểu dữ liệu/API trước khi triển khai các module phụ thuộc. Phân công chi tiết trong [TASKS](docs/TASKS.md).

Khi hoàn thành, chuẩn bị thư mục `Lab01_ID1_ID2_ID3_ID4_ID5/` có `src/`, `tests/`, `logs/`, `config/`, `README.md`, `REPORT.pdf`, cùng file cấu hình Python cần thiết để chạy. Nén thành ZIP cùng tên. Báo cáo tối đa 10 trang, tiếng Việt hoặc Anh, có thiết kế, kết quả test, nguồn tham khảo và không chèn source code. Đối chiếu lại quy cách với đề gốc trước khi nộp; hiện chưa tạo báo cáo hoặc ZIP.
