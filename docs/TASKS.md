# Lab01 - Bàn giao công việc cho đúng 5 thành viên

Ngày rà soát: **28/10/2026**. Nguồn: [context bàn giao](../Lab01_CONTEXT_HANDOFF.md), [đề Lab01](../Lab01.pdf), code thực tế và [INTERFACES.md](INTERFACES.md).

**TV5 là leader, một trong 5 người.** Giữ phân công module theo handoff; điền tên/MSSV khi nhận việc. Tách rõ code đã có, API chưa thực thi và việc tương lai. Đợt rà soát không triển khai thêm blockchain.

## 1. Bảng sở hữu file

Đường dẫn dưới `src/main/java/blockchain/`, trừ khi ghi khác. TV1 giữ đầu mối model; thay đổi model dùng chung cần phối hợp qua leader.

| Thành viên | Tên / MSSV | Module | File hiện có được giao | Test hiện có |
| --- | --- | --- | --- | --- |
| TV1 | __________ | model, crypto | `model/{Transaction,BlockHeader,Vote,Block,NetworkMessage}.java`; `crypto/{CryptoService,CanonicalEncoder,DefaultCryptoService}.java` | `src/test/java/blockchain/model/ModelTest.java`; `src/test/java/blockchain/crypto/DefaultCryptoServiceTest.java` |
| TV2 | __________ | execution, ledger | `execution/{State,ExecutionEngine,DefaultExecutionEngine,TransactionValidator}.java`; `ledger/{Blockchain,BlockValidator}.java` | Chưa có test execution/ledger |
| TV3 | __________ | network | `network/NetworkSimulator.java` | Chưa có test network |
| TV4 | __________ | consensus | `consensus/{ConsensusEngine,ConsensusAction,ConsensusResult}.java` | Chưa có test consensus |
| TV5 = leader | __________ | simulator, tích hợp, release | `Main.java`; `simulator/{Node,SimulationRunner,SimulationConfig,FinalizedState}.java`; `config/`, `logs/`, tài liệu, điều phối build | Chưa có test simulator/E2E |

Leader điều phối `pom.xml`, Wrapper và API dùng chung. Không tự thêm dependency, đổi cấu trúc Maven/chữ ký API hoặc tạo model trùng lặp ở từng nhánh.

## 2. Quyết định trước khi giao AI triển khai song song

Cấu trúc đã nhất quán; **hợp đồng hành vi chưa khóa hoàn toàn**. Dùng ID tại [INTERFACES.md, mục 5](INTERFACES.md#5-điểm-chưa-nhất-quán-và-quyết-định-cần-chốt) để tránh mỗi AI tự chọn giao thức khác nhau.

| Gói quyết định | Hợp đồng phải ghi rõ | Người chốt và người dùng |
| --- | --- | --- |
| Dữ liệu/chữ ký/hash | Chain/domain, key đầy đủ/nonce, state/block hash, genesis, NIL, ID/public key và fixture ổn định (API-01/02/03/04/06/11) | TV1 + TV2 + TV4, leader xác nhận |
| Message/consensus | Payload từng type, ACK/header/body, validate header/full block, proposer/phase/round/timeout, lock/finality, cập nhật state riêng từng node (API-05/06/07/09) | TV2 + TV3 + TV4 + TV5; TV1 xác thực |
| Mô phỏng/bằng chứng | Clock ảo, event ordering, seed/config, stabilization/Delta, log và entry point T1-T8 (API-08/10/11/12) | TV3 + TV5; mỗi module bổ sung test |

Ghi quyết định vào INTERFACES.md trước khi đổi API. Các helper handoff dự kiến như `EventQueue`, `NetworkEvent`, `RateLimiter`, `ConsensusState`, `VoteCollector`, `ProposerSelector`, `ByzantineBehavior`, `SimulationLogger` **chưa tồn tại**; không coi là skeleton/API đã chốt. Nếu cần, chủ module đề xuất ở giai đoạn triển khai; đợt chuẩn hóa này không tạo chúng.

## 3. Nhiệm vụ cụ thể theo thành viên

### TV1 - Crypto và model

**Đã có:** Ed25519 sign/verify, SHA-256 hex, encoder transaction/header/vote, 5 record, 3 test crypto và 3 test model. Giữ phần đang hoạt động.

**Còn thiếu, được giao khi triển khai:**

- Hoàn thiện `generateKeyPair`, `sign`, `verify`, `sha256`, `encodeTransaction`, `encodeHeader`, `encodeVote` theo contract; xác nhận canonical bytes/domain và input hỏng. Chốt state/block hash cùng TV2, NIL cùng TV4 trước khi bổ sung API cần thiết.
- Chốt ID -> public key và fixture khóa T8 với TV5, membership với TV4. Bảo vệ signature/validation model sau khi thống nhất; phối hợp TV3 về payload/ACK, không đổi riêng model đang dùng chung.
- Giữ test hiện tại; thêm test encoder, sai khóa/signature, đổi chain/height/round/phase và domain TX/HEADER/VOTE. Assertions phải dùng bytes/chữ ký thật.

**Input/output:** model + key + domain -> canonical bytes, signature/kết quả verify; bytes -> hash hex. **Nghiệm thu:** bytes nhất quán giữa node, không tái dùng chữ ký qua context; cùng TV5 đóng góp T3 có rejection và log lý do. Test crypto hiện tại chưa đủ đánh dấu T3 E2E hoàn tất.

### TV2 - Execution và ledger

**Đã có:** State copy/update, kiểm tra chữ ký/nonce/quyền key, execution batch tuần tự. Blockchain/BlockValidator mới là interface.

**Còn thiếu, được giao khi triển khai:**

- Hoàn thiện/test `State.{get,nextNonce,withUpdate,data,nonces,copy}`, `TransactionValidator.isValid`, `DefaultExecutionEngine.execute`. Làm rõ nonce 0, key `Alice/message`, chainId hardcode so với config; không tự đổi quy ước dùng chung.
- Triển khai `BlockValidator.isValid(Block, Block, State)` và `Blockchain.{finalizedBlocks,finalizedHeight,lastFinalizedHash,appendFinalized}` sau khi chốt hash/genesis/finality. Kiểm tra parent/height/proposer/header signature/ordered transactions/post-state hash; chỉ append sau finality. Chốt nơi giữ pending state và cập nhật ledger/state với TV5.
- Canonical state hash phải ổn định khi thứ tự đưa key vào map khác nhau; chốt có bao gồm nonce cùng TV1. Không băm bằng thứ tự HashMap hoặc toString.
- Thêm test thật dưới `src/test/java/blockchain/execution/` và `ledger/`: quyền key, signature sai, nonce/replay trong batch và qua block, state gốc không đổi khi batch lỗi, block/state hash sai, parent/append không hợp lệ.

**Input/output:** parentState + ordered transactions -> state mới hoặc lỗi; block/parent/parentState -> validation; finalized block -> ledger theo height. **Nghiệm thu:** deterministic, không sửa state gốc, chỉ cập nhật dữ liệu sender, replay áp dụng tối đa một lần (T4). Hiện chưa có test chứng minh.

### TV3 - Network simulator

**Đã có:** Interface NetworkSimulator với send/poll/currentTime/advanceTime; envelope do TV1 sở hữu. Chưa có implementation.

**Còn thiếu, được giao khi triển khai:**

- Triển khai single-threaded discrete-event queue sau khi chốt clock, event tie-break và seed cùng TV5. Không dùng giờ máy/sleep thật để quyết định thứ tự message.
- Hỗ trợ delay/drop/duplicate/reorder trước stabilization, giao trong Delta giữa node đúng sau stabilization theo config; outbound rate limit, block/unblock peer.
- Header-before-body: body chỉ được gửi sau khi receiver chấp nhận đúng header. Cùng TV1/TV2/TV5 chốt payload và validation; full BlockValidator chưa giải quyết bước kiểm tra header riêng.
- Log send/drop/delay/block/unblock với timestamp mô phỏng, node, type, height qua logger TV5 tích hợp. Thêm test `src/test/java/blockchain/network/` cho event ordering, poll/delivery, bounded delay, rate limit và handshake.

**Input/output:** NetworkMessage + fault config/clock -> message đến hạn và trace. **Nghiệm thu:** delivery tái lập, không body trước ACK hợp lệ; hỗ trợ E2E T2/T5. TV4 loại vote trùng ở nơi nhận.

### TV4 - Consensus

**Đã có:** ConsensusEngine, action BroadcastVote/FinalizeBlock/ScheduleTimeout, record ConsensusResult. Chưa có engine chạy được.

**Còn thiếu, được giao khi triển khai:**

- Đặc tả transition cho onProposal/onVote/onTimeout/getResult cùng leader: proposer schedule, NIL, round/phase, lock/unlock/finality, timeout cũ, chuyển height. Chốt nơi tạo/phát proposal vì action hiện tại chưa mô tả phần này.
- Kiểm tra proposal qua TV2, chữ ký/danh tính qua TV1. Ký tối đa 1 PREVOTE và 1 PRECOMMIT cho `(height, round)`; đếm tối đa 1 vote hợp lệ mỗi validator cho `(height, round, phase)`; kiểm tra chain/height/round/phase/domain/membership.
- Lock sau quorum PREVOTE non-NIL, đổi block đã lock theo quorum hợp lệ ở round sau; finalize theo điều kiện PRECOMMIT đã chốt. Phát action cho TV5, không ghi vào ledger/state dùng chung giữa node.
- Thêm test `src/test/java/blockchain/consensus/`: quorum thiếu/đủ, duplicate/equivocation, sai validator/context, lock/unlock, NIL, timeout, finality xung đột. Cùng TV5 chạy proposer silent/crashed (T6), tối đa f Byzantine equivocate (T7).

**Input/output:** proposal + kết quả validation, signed vote hoặc timeout -> actions và result. **Nghiệm thu:** 4 validator, f=1, quorum=3; không hai block khác nhau finalized cùng height; sau synchrony và proposer đúng, chain tiếp tục. TV1 hỗ trợ verify, TV2 block validation, TV3 network/timeout, TV5 scenario.

### TV5 = leader - Tích hợp và release

**Đã có:** Main in 8 node/4 validator do runner tạo cố định. SimulationConfig có validation/quorum, FinalizedState copy state; cả hai chưa nối vào runner.

**Còn thiếu, được giao khi triển khai:**

- Chốt API-01 đến API-11 với chủ module, cập nhật tài liệu trước thay contract. Nối config/seed vào runner, đọc config, tạo ledger/state/view và engine riêng cho từng node; điều phối pom/Wrapper.
- Nối delivery -> header/body validation -> consensus action -> network/timeout/finalized update. Cập nhật ledger/state nhất quán, cho non-validator hội tụ; không dùng một state/ledger chung để giả lập đồng thuận.
- Tích hợp logger deterministic và Byzantine scenarios, log mọi event ảnh hưởng kết quả. Chốt key fixture TV1/event ordering TV3; tránh wall-clock và randomness không kiểm soát trong log T8.
- Viết test `src/test/java/blockchain/simulator/` và E2E dưới `src/test/java/` theo package thống nhất. Tạo entry point thật chạy T1-T8 và script chạy hai lần so log từng byte/final state hash; chỉ ghi lệnh chạy đã kiểm chứng. Mỗi chủ module tự viết unit test, leader viết E2E/tổng hợp bằng chứng.
- Chuẩn bị CI, README/lệnh chạy thật và đóng gói sau nghiệm thu. Config/logs hiện chỉ có placeholder; `.gitignore` bỏ qua `*.log`, cần thống nhất cách lưu bằng chứng và đưa log vào ZIP.

**Input/output:** config/seed/keys/implementation -> mô phỏng, log, ledger/state cuối, kết quả T1-T8. **Nghiệm thu:** >=8 node, validator set cố định n=3f+1, T1/T8 qua và toàn bộ T1-T8 gọi được qua một entry point; build xanh chưa thay thế các tiêu chí này.

## 4. Ma trận nghiệm thu theo đề

| Test | Chủ trì; hỗ trợ | Bằng chứng | Hiện trạng |
| --- | --- | --- | --- |
| T1 bình thường | TV5; tất cả | Node đúng finalized cùng chuỗi/state hash | Chưa có E2E |
| T2 duplicate/reorder | TV3 + TV4; TV5 | Không đếm vote hai lần/finality xung đột | Chưa có |
| T3 chữ ký/domain sai | TV1; TV2, TV4, TV5 | Message bị từ chối, log lý do | Có 3 test crypto cơ bản, chưa đạt T3 E2E |
| T4 replay transaction | TV2; TV5 | Transaction áp dụng tối đa một lần | Code kiểm tra nonce, chưa có test |
| T5 drop/delay trước synchrony | TV3; TV4, TV5 | Safety mọi thời điểm, không double finalization | Chưa có |
| T6 proposer silent/crashed | TV4; TV3, TV5 | Timeout đổi round, chain tiếp tục khi đủ điều kiện liveness | Chưa có |
| T7 tối đa f Byzantine equivocate | TV4; TV1, TV5 | Không finality xung đột | Chưa có |
| T8 rerun cùng seed/config | TV5; TV1, TV3 | Script kiểm tra log byte-identical và state hash bằng nhau | Chưa có |

Đề còn yêu cầu unit test crypto, state update, vote counting, block validation. Test model hiện chỉ chứng minh khởi tạo/đọc record.

## 5. Nhận việc bằng AI và phối hợp

Gửi AI mỗi người context handoff, INTERFACES.md, TASKS.md, file được giao và test hiện có. Khi tài liệu cũ mâu thuẫn code, đối chiếu API-xx thay vì tự đổi contract.

Mẫu giao việc sau khi leader chốt quyết định liên quan:

> Tôi là TV[1-5]. Chỉ triển khai phần giao ở mục 3 TASKS.md, dùng đúng API hiện có và quyết định trong INTERFACES.md. Giữ code/test đang hoạt động; không thêm dependency hoặc tự thay API/model người khác. Nếu contract còn thiếu, nêu API-xx cụ thể trước khi sửa phần phụ thuộc. Viết unit test thật cho module, chạy Maven Wrapper, báo phần đã kiểm chứng/còn thiếu. Không tự commit/push.

Nhánh dự kiến theo handoff: feature/crypto, feature/execution, feature/network, feature/consensus, feature/integration. Đợt rà soát không tạo/chuyển nhánh. Chủ module sửa implementation của mình; đổi API/model cần cập nhật tài liệu cùng PR.

Thứ tự tích hợp: contract -> crypto/model và execution/state -> ledger/network -> consensus -> E2E/fault scenarios/release. Có thể chuẩn bị song song khi contract phụ thuộc đã rõ. Không giả PASS hoặc disabled test rồi coi là hoàn thành.

## 6. Kiểm chứng và điều kiện bàn giao

Từ thư mục project:

```bash
./mvnw clean test
```

PowerShell dùng `./mvnw.cmd clean test`; không cần Maven global. Đã chạy đúng `./mvnw clean test` qua Git Bash ngày 28/10/2026 với Java 21.0.10: **BUILD SUCCESS; 6 tests, 0 failures, 0 errors, 0 skipped** (3 crypto, 3 model). Maven biên dịch đủ 23 file production và 2 file test. Surefire reports nằm ở `target/surefire-reports/`, không cần commit.

Đã đối chiếu SHA-256 trước/sau: toàn bộ Java source, test, pom và Wrapper giữ nguyên. Chỉ sửa hai tài liệu INTERFACES.md/TASKS.md và thêm config/.gitkeep, logs/.gitkeep; không di chuyển hoặc xóa file. `git diff --check` không báo lỗi khoảng trắng. Kết quả build xác nhận project hiện tại không hỏng, chưa chứng minh T1-T8.

Git cục bộ: main có upstream origin/main, origin đã cấu hình. Nhiều file Java/API, test crypto, Lab01_CONTEXT_HANDOFF.md và Lab01.pdf đang untracked. Hai tài liệu nguồn vẫn hữu ích nhưng có mô tả tiến độ cũ; dùng bảng hiện trạng ở trên. Chưa kiểm tra nội dung mới nhất trên GitHub.

Leader kiểm tra diff và chọn đủ source/tài liệu/placeholder trước khi tự commit/push ở bước sau để người clone nhận đủ nội dung. Đợt chuẩn hóa không stage, commit hoặc push.

Khi nộp, tạo `Lab01_ID1_ID2_ID3_ID4_ID5/` gồm src/, tests/, logs/, config/, README.md, REPORT.pdf và ZIP cùng tên. Giữ Maven tests ở src/test/java khi phát triển; leader thiết kế/kiểm tra đóng gói sang tests/ cùng hướng dẫn build/test chạy được. REPORT.pdf tối đa 10 trang, có thành viên/cấu trúc/flow/implementation/bug/test/nguồn tham khảo, không chèn source. Hiện chưa có report, script đóng gói hoặc bộ log nghiệm thu.
