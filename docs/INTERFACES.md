# Ranh giới module của bản Python

Đây là tài liệu định hướng cấu trúc, **chưa phải hợp đồng API Python đã chốt**. Mọi `.py` hiện chỉ có docstring; không có class, function hoặc chữ ký API đã triển khai.


## Trách nhiệm và ranh giới

| Module | Nhận / cung cấp trong thiết kế dự kiến | Ranh giới |
| --- | --- | --- |
| `model` | Kiểu transaction, header, block, vote, message dùng chung | Không quyết định consensus hoặc truy cập mạng |
| `crypto` | Dữ liệu canonical, chữ ký, xác minh, hash | Không tự thay đổi state/ledger |
| `execution` | State cha + transaction có thứ tự → state kế tiếp | Không tự finalize block |
| `ledger` | Kiểm tra block/header, lưu chuỗi finalized cục bộ | Không tự coi proposal là finalized |
| `network` | Message gửi → sự kiện giao/drop/delay theo clock ảo | Không đọc state/lock để quyết định nội dung đồng thuận |
| `consensus` | Proposal đã được kiểm tra, vote và timeout → action/result | Không trực tiếp sửa state/ledger của node khác |
| `simulator` | Config + node + các module → vòng sự kiện và kết quả | Không dùng state chung hoặc kết quả hội tụ để ép node đồng thuận |

Luồng dự kiến: nhận header → kiểm tra header → chấp nhận header → nhận body → kiểm tra/thực thi thử → consensus → có chứng cứ finality → cập nhật ledger và state cục bộ. Đây là mô tả trách nhiệm, chưa có luồng chạy thật.

## Các yêu cầu thiết kế

- Mọi node có trạng thái nghiệp vụ riêng; giao tiếp qua mạng mô phỏng.
- Chữ ký gắn đúng domain/chain và các trường nhận diện thông điệp.
- Hash/chữ ký dùng bytes xác định; không dựa vào `repr()`, hash mặc định của object hoặc thứ tự dữ liệu chưa quy định.
- Phân biệt xác thực header với xác thực block đầy đủ: chưa có body thì chưa xác minh được kết quả thực thi.
- Không đếm duplicate vote hoặc áp dụng lại transaction; giữ quy tắc một vote/phase/height/round và lock của đề.
- Log dùng thời gian mô phỏng và thứ tự sự kiện xác định; seed mô phỏng không thay thế nguồn ngẫu nhiên mật mã.
- `node_count >= 8`, `validator_count <= node_count`, `validator_count = 3f + 1`; ngưỡng quorum dự kiến `2f + 1`. Observer không có quyền biểu quyết.

## Những việc nhóm cần chốt trước khi viết logic

1. Trường và kiểu Python của model, dữ liệu bất biến/copy, lỗi trả về và chữ ký API giữa module.
2. Thư viện chữ ký; bytes canonical, domain, state/transaction/block commitment và genesis. Quy định rõ cách biểu diễn bằng kiểu Python và cách chuyển sang bytes.
3. Consensus state machine, timeout, proof, điều kiện đổi lock và truyền block/vote cho node chậm.
4. Định dạng config/scenario, tên tham số, thứ tự sự kiện cùng timestamp, RNG và nguồn fixture cố định.
5. Schema log, tên CLI và cách script chạy hai lần/so sánh kết quả.
