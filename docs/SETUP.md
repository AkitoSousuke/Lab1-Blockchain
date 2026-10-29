# Thiết lập môi trường Python

Khung dự án dùng Python >= 3.11. Chưa có chức năng bài tập để chạy.

## Windows / PowerShell

Cài Python, Git; có thể dùng VS Code với extension Python của Microsoft. Mở đúng thư mục có `pyproject.toml`, rồi kiểm tra:

```powershell
py --list
py -3.11 --version
git --version
py -3.11 -m venv .venv
.\.venv\Scripts\python.exe -m pip install -e ".[dev]"
.\.venv\Scripts\python.exe -c "import blockchain; print(blockchain.__file__)"
```

Có thể thay `-3.11` bằng phiên bản Python mới hơn đã cài. Nếu không có launcher `py`, dùng `python --version`, xác nhận >= 3.11 rồi dùng `python -m venv .venv`. Gọi trực tiếp Python trong `.venv` không cần activate và không cần đổi Execution Policy của PowerShell.

## macOS / Linux

```bash
python3 --version
python3 -m venv .venv
.venv/bin/python -m pip install -e '.[dev]'
.venv/bin/python -c 'import blockchain; print(blockchain.__file__)'
```

Xác nhận `python3` >= 3.11 trước khi tạo môi trường. Lần cài package có thể cần Internet. Editable install giúp import package ngay từ `src/blockchain/`.

## VS Code

Mở thư mục gốc, dùng `Python: Select Interpreter` và chọn Python trong `.venv`. Dùng cùng interpreter này để cài dependency, import package và chạy pytest.

## Kiểm thử sau khi triển khai

Windows: `.\.venv\Scripts\python.exe -m pytest`.

macOS/Linux: `.venv/bin/python -m pytest`.

Pytest đã được cấu hình tìm test trong `tests/`; tên file sẽ là `test_*.py`. Hiện các thư mục test chỉ có `.gitkeep`, nên `no tests ran` với exit code 5 là kết quả dự kiến, không phải kiểm chứng thuật toán. Không thêm test luôn PASS để che việc thiếu test.

## Xử lý lỗi thường gặp

- Không tìm được Python: kiểm tra cài đặt/PATH hoặc `py --list`.
- `No module named blockchain`: đứng tại thư mục gốc và chạy editable install bằng đúng interpreter `.venv`.
- `No module named pytest`: cài extra `.[dev]` bằng đúng interpreter `.venv`.
- Không chạy được simulator: hiện chưa có CLI hoặc implementation; không có lệnh chạy bài lab ở giai đoạn này.

Dependency runtime hiện rỗng. Nhóm sẽ chọn thư viện mật mã và cơ chế khóa phiên bản trước giai đoạn triển khai/tái lập kết quả; bản cấu trúc này không cài dependency thay cho từng thành viên.
