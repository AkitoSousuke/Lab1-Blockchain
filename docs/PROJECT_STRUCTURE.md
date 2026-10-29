# Bản đồ thư mục và từng file

Tài liệu này giúp đọc khung Python trước khi triển khai. Mọi chức năng mô tả dưới đây là **trách nhiệm dự kiến**, không phải chức năng đã chạy. Các file `.py` hiện chỉ chứa docstring.

## Mục đích từng thư mục

| Đường dẫn | Mục đích |
| --- | --- |
| `src/` | Gốc source theo bố cục src-layout, không phải tên package import. |
| `src/blockchain/` | Package Python chính, import bằng tên `blockchain`. |
| `src/blockchain/model/` | Các kiểu dữ liệu dùng chung giữa các module. |
| `src/blockchain/crypto/` | Mã hóa xác định, chữ ký và hash. |
| `src/blockchain/execution/` | Trạng thái ứng dụng và thực thi transaction. |
| `src/blockchain/ledger/` | Lịch sử finalized và kiểm tra block. |
| `src/blockchain/network/` | Mạng mô phỏng và sự kiện truyền tin. |
| `src/blockchain/consensus/` | Đồng thuận hai pha, khóa và chuyển round. |
| `src/blockchain/simulator/` | Ghép các module thành node và điều phối mô phỏng. |
| `tests/` | Gốc mọi kiểm thử, tách khỏi source chạy chính. |
| `tests/unit/` | Kiểm thử riêng từng thành phần. |
| `tests/integration/` | Kiểm thử phối hợp nhiều module và kịch bản T1–T8. |
| `tests/fixtures/` | Dữ liệu test cố định; chưa có danh tính/khóa hoặc bộ input. |
| `config/` | File cấu hình chạy và scenario sau khi thống nhất định dạng. |
| `logs/` | Log/bằng chứng chạy có thể chọn đưa vào Git; hiện chưa có kết quả chạy. |
| `scripts/` | Công cụ hỗ trợ chạy/so sánh determinism/đóng gói sẽ viết sau. |
| `docs/` | Tài liệu hiện hành của bản Python. |
| `.git/` | Metadata Git do Git quản lý, không sửa tay hoặc đóng gói vào bài nộp. |

Các thư mục `.venv/`, cache, build và `logs/local/` được ignore và chỉ xuất hiện khi dùng công cụ; không phải dữ liệu đầu vào của bài lab.

## File ở gốc và tài liệu

| File | Mục đích |
| --- | --- |
| `README.md` | Trang bắt đầu: mục tiêu, trạng thái thực tế, setup nhanh và liên kết tài liệu. |
| `pyproject.toml` | Metadata Python, setuptools src-layout, extra pytest và nơi pytest tìm test; không chứa thuật toán. |
| `.gitignore` | Loại môi trường ảo/cache/build/secret khỏi Git; vẫn cho phép lưu log nghiệm thu. |
| `docs/PROJECT_STRUCTURE.md` | Danh mục đang đọc, giải thích từng đường dẫn/file. |
| `docs/SETUP.md` | Thiết lập Python và cách kiểm tra package/môi trường. |
| `docs/INTERFACES.md` | Ranh giới các module và những điểm cần chốt trước triển khai API. |
| `docs/TASKS.md` | Phân công dự kiến cho 5 người và checklist T1–T8. |

## Các file package Python

`__init__.py` đánh dấu package; ở đây chỉ có mô tả, không khởi tạo simulator, không re-export API và không có side effect.

| File | Mục đích |
| --- | --- |
| `src/blockchain/__init__.py` | Đánh dấu package gốc `blockchain`. |
| `src/blockchain/model/__init__.py` | Đánh dấu subpackage `model`. Các kiểu dữ liệu dùng chung giữa các module. |
| `src/blockchain/crypto/__init__.py` | Đánh dấu subpackage `crypto`. Mã hóa xác định, chữ ký và hash. |
| `src/blockchain/execution/__init__.py` | Đánh dấu subpackage `execution`. Trạng thái ứng dụng và thực thi transaction. |
| `src/blockchain/ledger/__init__.py` | Đánh dấu subpackage `ledger`. Lịch sử finalized và kiểm tra block. |
| `src/blockchain/network/__init__.py` | Đánh dấu subpackage `network`. Mạng mô phỏng và sự kiện truyền tin. |
| `src/blockchain/consensus/__init__.py` | Đánh dấu subpackage `consensus`. Đồng thuận hai pha, khóa và chuyển round. |
| `src/blockchain/simulator/__init__.py` | Đánh dấu subpackage `simulator`. Ghép các module thành node và điều phối mô phỏng. |

### `model/`

Đường dẫn tính từ `src/blockchain/`.

| File | Trách nhiệm khi triển khai |
| --- | --- |
| `model/transaction.py` | Biểu diễn yêu cầu cập nhật dữ liệu: người gửi, key, value, nonce, chain và chữ ký. |
| `model/block_header.py` | Biểu diễn metadata block/proposal, liên kết parent, các commitment và chữ ký proposer. |
| `model/block.py` | Gom header và danh sách transaction có thứ tự thành một block. |
| `model/vote.py` | Biểu diễn phiếu PREVOTE/PRECOMMIT theo chain, height, round, block và validator. |
| `model/network_message.py` | Biểu diễn phong bì truyền tin: peer gửi/nhận, loại thông điệp, height và payload. |

### `crypto/`

Đường dẫn tính từ `src/blockchain/`.

| File | Trách nhiệm khi triển khai |
| --- | --- |
| `crypto/canonical_encoder.py` | Quy định bytes xác định dùng chung cho dữ liệu được ký và băm. |
| `crypto/crypto_service.py` | Cung cấp chức năng ký, xác minh chữ ký và băm; quản lý domain của chữ ký. |

### `execution/`

Đường dẫn tính từ `src/blockchain/`.

| File | Trách nhiệm khi triển khai |
| --- | --- |
| `execution/state.py` | Biểu diễn dữ liệu key–value và nonce của từng người gửi trong trạng thái cục bộ. |
| `execution/transaction_validator.py` | Kiểm tra chữ ký, chain, quyền sở hữu dữ liệu và nonce của transaction. |
| `execution/execution_engine.py` | Thực thi danh sách transaction theo thứ tự để tạo trạng thái kế tiếp một cách xác định. |

### `ledger/`

Đường dẫn tính từ `src/blockchain/`.

| File | Trách nhiệm khi triển khai |
| --- | --- |
| `ledger/blockchain.py` | Quản lý lịch sử block finalized và liên kết height/parent tại một node. |
| `ledger/block_validator.py` | Tách kiểm tra header với kiểm tra block đầy đủ, transaction và state commitment. |

### `network/`

Đường dẫn tính từ `src/blockchain/`.

| File | Trách nhiệm khi triển khai |
| --- | --- |
| `network/network_simulator.py` | Mô phỏng truyền tin, thời gian ảo, delay/drop/duplicate/reorder, rate limit và block peer. |

### `consensus/`

Đường dẫn tính từ `src/blockchain/`.

| File | Trách nhiệm khi triển khai |
| --- | --- |
| `consensus/consensus_engine.py` | Xử lý round, vote, quorum, lock và timeout để quyết định đồng thuận cục bộ. |
| `consensus/consensus_action.py` | Biểu diễn yêu cầu đầu ra của consensus: phát vote, đề xuất block, đặt timeout hoặc finalize. |
| `consensus/consensus_result.py` | Biểu diễn kết quả đồng thuận: height, round, block hash và trạng thái finalized. |

### `simulator/`

Đường dẫn tính từ `src/blockchain/`.

| File | Trách nhiệm khi triển khai |
| --- | --- |
| `simulator/node.py` | Kết nối các module của một node; giữ state, ledger và consensus riêng cho node đó. |
| `simulator/simulation_config.py` | Biểu diễn và kiểm tra tham số của một lần mô phỏng; chưa chốt định dạng file cấu hình. |
| `simulator/simulation_runner.py` | Khởi tạo các node, điều phối sự kiện và thu kết quả của một lần chạy mô phỏng. |
| `simulator/finalized_state.py` | Biểu diễn snapshot block và state đã finalized của một node. |

## File giữ chỗ

| File | Mục đích |
| --- | --- |
| `config/.gitkeep` | Giúp Git giữ thư mục chưa có nội dung; không phải cấu hình, test hoặc output chạy. |
| `logs/.gitkeep` | Giúp Git giữ thư mục chưa có nội dung; không phải cấu hình, test hoặc output chạy. |
| `scripts/.gitkeep` | Giúp Git giữ thư mục chưa có nội dung; không phải cấu hình, test hoặc output chạy. |
| `tests/unit/.gitkeep` | Giúp Git giữ thư mục chưa có nội dung; không phải cấu hình, test hoặc output chạy. |
| `tests/integration/.gitkeep` | Giúp Git giữ thư mục chưa có nội dung; không phải cấu hình, test hoặc output chạy. |
| `tests/fixtures/.gitkeep` | Giúp Git giữ thư mục chưa có nội dung; không phải cấu hình, test hoặc output chạy. |

## Thứ tự đọc source gợi ý

Đọc `model` để hiểu dữ liệu → `execution/state.py` để hiểu state → `crypto` và các validator để hiểu xác thực → `network`/`consensus` → `simulator` để hiểu nơi ghép hệ thống. Hiện đây là thứ tự đọc trách nhiệm; chưa có luồng thực thi để debug.

Chưa tạo entry point chạy ứng dụng, logger, config/scenario cụ thể, fixture hoặc test: chúng cần được bổ sung khi nhóm bắt đầu triển khai. Không có `main.py` giả hoặc test luôn PASS.
