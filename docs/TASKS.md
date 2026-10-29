# Phân công dự kiến cho 5 thành viên

Bản Python hiện chỉ hoàn tất việc bố trí thư mục và tài liệu. Bảng này phân chia trách nhiệm cho giai đoạn sau, không ghi nhận các chức năng đã làm. Tên/MSSV để nhóm điền. Chi tiết file ở [PROJECT_STRUCTURE](PROJECT_STRUCTURE.md); ranh giới ở [INTERFACES](INTERFACES.md).

| Thành viên | Khu vực phụ trách | Kiểm thử dự kiến |
| --- | --- | --- |
| TV1 / … | `src/blockchain/model/`, `src/blockchain/crypto/`; phối hợp fixture danh tính | Model, bytes canonical, chữ ký/domain; hỗ trợ T3/T8 |
| TV2 / … | `src/blockchain/execution/`, `src/blockchain/ledger/` | State, quyền sở hữu, nonce/replay, validation block; T4 |
| TV3 / … | `src/blockchain/network/` | Delay/drop/duplicate/reorder, rate limit, header/body; T2/T5 |
| TV4 / … | `src/blockchain/consensus/` | Vote/quorum/lock/timeout; T6/T7 và safety |
| TV5 / … | `src/blockchain/simulator/`, `config/`, `logs/`, `scripts/`, cấu hình dự án và tài liệu | Tích hợp node, cô lập state, T1–T8 và tái lập |

Unit test đặt dưới `tests/unit/`; E2E dưới `tests/integration/`; dữ liệu kiểm thử cố định dưới `tests/fixtures/`. Có thể thêm thư mục con theo module khi bắt đầu viết test. API dùng chung cần được nhóm thống nhất trước khi các thành viên triển khai độc lập.

## Checklist nghiệm thu tương lai

| Test | Tình huống | Kết quả phải kiểm chứng |
| --- | --- | --- |
| T1 | Bình thường | Mọi correct node cùng chain finalized và state hash |
| T2 | Message lặp/đảo thứ tự | Không đếm vote hai lần, không finalization xung đột |
| T3 | Chữ ký/domain sai | Reject và log lý do |
| T4 | Transaction replay/lặp | Áp dụng tối đa một lần |
| T5 | Drop/delay trước synchrony | Safety luôn đúng; sau ổn định đáp ứng giả thiết liveness |
| T6 | Proposer im lặng/crash | Timeout và chuyển round để tiếp tục |
| T7 | Tối đa f Byzantine equivocate | Không chốt hai block xung đột |
| T8 | Cùng input/seed chạy hai lần | Log giống từng byte, final state hash giống nhau |

Tất cả test phải được gọi từ một entry point; dự kiến dùng `python -m pytest`. Script kiểm tra determinism sẽ nằm ở `scripts/` nhưng chưa được tạo. Không xem việc import package thành công là hoàn tất T1–T8.

## Thứ tự làm việc gợi ý

1. Chốt API/encoding/config/log giữa các thành viên.
2. Triển khai model/crypto và các module có unit test.
3. Ghép runner, chạy E2E và kiểm tra tính tái lập.
4. Viết README chạy thực tế, báo cáo tối đa 10 trang và đóng gói theo đề.
