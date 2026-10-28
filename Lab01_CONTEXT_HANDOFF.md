# CONTEXT BÀN GIAO — LAB 01 BLOCKCHAIN (JAVA)

> **Mục đích:** Dán hoặc tải file này vào một chat mới để tiếp tục hỗ trợ dự án mà không phải giải thích lại từ đầu.  
> **Ngày cập nhật:** 28/10/2026.  
> **Nguyên tắc:** Chỉ còn khoảng **1 tuần**; ưu tiên **có chương trình chạy được và có bằng chứng kiểm thử trước**, sau đó mới học sâu code để vấn đáp. Không tuyên bố đã đạt yêu cầu nếu chưa có test xác nhận.

## 1. Bối cảnh và cách hỗ trợ

- Đây là **Lab 01** môn Blockchain, nhóm **5 sinh viên**, đều có rất ít kiến thức về blockchain/mạng phân tán. Leader cũng chưa từng làm leader, dùng **VS Code**, không học chuyên ngành Mạng.
- Yêu cầu trao đổi bằng **tiếng Việt tự nhiên**, giải thích đơn giản, thẳng vào việc. **Mỗi phản hồi hướng dẫn tối đa 3 bước/tác vụ**; đợi kết quả rồi mới sang chặng tiếp theo.
- Leader muốn **khóa cứng công nghệ, cấu trúc project, API, skeleton code, nhiệm vụ từng người**; thành viên chỉ cần điền tên vào phân công, nhận file/hàm đã định nghĩa sẵn rồi triển khai. Không giao nhiệm vụ mơ hồ kiểu “nghiên cứu consensus”.
- Trong buổi meeting, ưu tiên clone/chạy test/giao file và kiểm tra đầu ra; phần hiểu kỹ thuật để ôn vấn đáp sau, nhưng mỗi người phải giải thích được code mình viết trước khi nộp.
- **Không tự động coi những file được đề xuất trong tài liệu này là đã tạo trên máy.** Phân biệt rõ `ĐÃ XÁC NHẬN` và `CHƯA TRIỂN KHAI`.

## 2. Yêu cầu gốc từ Lab01.pdf (5 trang)

Mục tiêu: mô phỏng một **Layer 1 blockchain tối giản** có transaction ký số, block, trạng thái key–value và đồng thuận có finality trong mạng không đáng tin cậy.

- Mạng mô phỏng hỗ trợ **ít nhất 8 node**; mỗi node có ledger, state, view riêng; **không chia sẻ state/ledger** để giả lập đồng thuận.
- Tập validator cố định theo lần chạy, có `n = 3f + 1`, tối đa `f` Byzantine. Khởi đầu đề xuất **8 node = 4 validator + 4 non-validator**, nên `f = 1` và quorum `2f+1 = 3`.
- Mạng **partially synchronous**: trước stabilization có thể delay/duplicate/reorder/drop; sau stabilization thông điệp giữa node đúng được chuyển trong giới hạn `Δ`. Có rate limit và block/unblock peer. **Header gửi trước body**; chỉ gửi body khi receiver chấp nhận header.
- Ký **transaction, block header và vote**; domain separation theo loại (`TX:chain_id`, `HEADER:chain_id`, `VOTE:chain_id`), chữ ký không thể tái sử dụng qua domain/height. Hash chống va chạm; encoding nhất quán giữa node; header chứa state hash.
- Consensus hai pha **PREVOTE → PRECOMMIT**. Mỗi validator đúng ký tối đa 1 PREVOTE và 1 PRECOMMIT cho từng `(height, round)`; đếm nhiều nhất 1 phiếu hợp lệ mỗi validator cho `(height, round, phase)`; chỉ prevote block hợp lệ. Lock khi có quorum PREVOTE cho block khác NIL; unlock theo quy tắc quorum hợp lệ ở round sau. Finalize chỉ khi có điều kiện quorum PRECOMMIT đúng giao thức.
- Execution phải deterministic, transaction chỉ sửa dữ liệu thuộc sender, xác thực chữ ký, chống replay, state hash giống nhau nếu cùng ordered transactions. **Không cần gas/fee/parallel execution**.
- Same config + seed phải cho **log giống nhau từng byte** và final state hash giống nhau. Log có timestamp mô phỏng, node ID, event type, height và các sự kiện ảnh hưởng kết quả.
- Test bắt buộc: **T1** bình thường; **T2** duplicate/reorder; **T3** chữ ký/domain sai; **T4** replay transaction; **T5** drop/delay trước synchrony; **T6** proposer silent/crash; **T7** Byzantine equivocation tối đa `f`; **T8** chạy hai lần cùng seed ra log/state hash giống hệt. Tất cả gọi được qua một entry point.
- Nộp thư mục `Lab01_ID1_ID2_ID3_ID4_ID5` gồm `src/`, `tests/`, `logs/`, `config/`, `README.md`, `REPORT.pdf`; nén thành ZIP cùng tên. **REPORT.pdf tối đa 10 trang**, nêu team, cấu trúc, kiến trúc/flow, implementation, bug đã sửa, test/observations, nguồn tham khảo; **không chèn mã nguồn**. Maven có thể tổ chức test dưới `src/test` trong repo, nhưng **trước khi nộp cần kiểm tra và chuyển/đóng gói sang `tests/` đúng yêu cầu nếu cần**. Sai quy cách có nguy cơ nhận 0 điểm.

## 3. Công nghệ và quy tắc đã chốt

| Hạng mục | Chọn |
|---|---|
| Ngôn ngữ | Java 21 |
| IDE | VS Code + Extension Pack for Java (Microsoft) |
| Build/Test | Maven Wrapper 3.9.9 + JUnit 5 |
| Mã hóa/chữ ký | Java standard library, Ed25519, SHA-256 (dự kiến khóa cứng) |
| State | Key–value map, canonical ordering/encoding |
| Network | Simulator in-memory, **single-threaded discrete-event queue** với đồng hồ ảo |
| Consensus | Tendermint-style hai pha theo chính xác rule của đề, phải đặc tả thêm transition/timeout/NIL |
| Git | GitHub, nhánh `main`, feature branches và Pull Requests |
| Không triển khai | Spring Boot, database ngoài, UI, socket thật, Docker, Ethereum/Solidity |

**Những quyết định thiết kế cần hoàn thiện trước khi giao coding thực tế:** canonical bytes cho mỗi loại message, mapping danh tính → public key, biểu diễn NIL vote, message/header/body protocol, proposer schedule, round timeout, finalized-state update, deterministic event ordering, seed/config. Đây không phải thứ thành viên tự chọn.

## 4. Môi trường và tình trạng thực tế — ĐÃ XÁC NHẬN

Máy leader chạy Windows 11, Git Bash; thư mục project:

`C:\Study\Blockchain\Lab1-Blockchain`

Đã xác nhận:

- `java -version` = **Java 21.0.10**.
- `javac -version` = **21.0.10**.
- `git --version` = **2.49.0.windows.1**.
- Maven global trên máy leader = **3.10.0**, nhưng thành viên **không cần cài Maven global**.
- Maven Wrapper trong repo có `.mvn/wrapper/maven-wrapper.properties`:
  ```properties
  wrapperVersion=3.3.4
  distributionType=only-script
  distributionUrl=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.9/apache-maven-3.9.9-bin.zip
  ```
- `./mvnw clean test` đã báo **BUILD SUCCESS**, **3 tests / 0 failures / 0 errors / 0 skipped**.
- Đã có `pom.xml`, `mvnw`, `mvnw.cmd`, `.mvn/`, `.gitignore`, `README.md`, thư mục `docs/`, `src/`, `config/`, `logs/`.
- Đã tạo ba Java `record` + test tương ứng. Ban đầu nằm ở `edu.hcmus.blockchain.model`, sau đã **chuyển sang package ngắn `blockchain.model`**; người dùng xác nhận bước chuyển đã hoàn thành.
- Git đã `init`; lần cuối `git status` hiển thị nhánh `master`, no commits, files untracked. Có hướng dẫn đổi `main`/commit/push, **chưa nhận xác nhận đã push GitHub**. Chat mới phải kiểm tra `git status`, `git branch`, `git remote -v` trước khi kết luận GitHub đã sẵn sàng.
- Tài liệu Markdown tiếng Việt `README.md`, `docs/TASKS.md`, `docs/INTERFACES.md`, `docs/SETUP.md` đã được hướng dẫn tạo; người dùng cho biết đã làm xong các bước liên quan, nhưng nội dung thực tế trên máy chưa được đọc trực tiếp.

### Cấu trúc hiện hữu đã xác nhận ở mức logic

```text
Lab1-Blockchain/
├── .mvn/wrapper/maven-wrapper.properties
├── .gitignore
├── mvnw
├── mvnw.cmd
├── pom.xml
├── README.md
├── docs/
│   ├── TASKS.md
│   ├── INTERFACES.md
│   └── SETUP.md
├── config/
├── logs/
└── src/
    ├── main/java/blockchain/model/
    │   ├── Transaction.java
    │   ├── BlockHeader.java
    │   └── Vote.java
    └── test/java/blockchain/model/
        └── ModelTest.java
```

**Model đã tạo** (đang ở dạng record nền tảng, chưa có validation/defensive copy):

```java
record Transaction(String chainId, String senderId, String key,
                   String value, long nonce, byte[] signature) {}
record BlockHeader(String chainId, long height, int round,
                   String parentHash, String stateHash,
                   String proposerId, byte[] signature) {}
record Vote(String chainId, long height, int round, Phase phase,
            String blockHash, String validatorId, byte[] signature) {
    enum Phase { PREVOTE, PRECOMMIT }
}
```

`ModelTest` có ba test khởi tạo/đọc record. **Không nhầm đây là blockchain logic hoặc test đạt T1–T8.**

## 5. Phân công KHÓA CỨNG cho đúng 5 người

> **Leader là một trong 5 người**, không phải người thứ sáu. Điền tên và MSSV ở bảng dưới trong meeting, **không tự thay đổi module/kiến trúc**. Phương án này để leader chịu trách nhiệm tích hợp; test/log/report chia cho tất cả theo trách nhiệm, không đẩy cho một thành viên thứ 6.

| Người | Điền tên/MSSV | Module sở hữu | File được giao (DỰ KIẾN TẠO SKELETON) | Đầu ra nghiệm thu |
|---|---|---|---|---|
| **TV1** | __________ | Crypto + message models | `model/*` (phối hợp), `crypto/CryptoService.java`, `crypto/CanonicalEncoder.java`, `crypto/DefaultCryptoService.java` | Ed25519 ký/xác minh, SHA-256, domain separation, test chữ ký/domain sai (T3) |
| **TV2** | __________ | Execution + ledger | `execution/State.java`, `execution/ExecutionEngine.java`, `execution/TransactionValidator.java`, `ledger/Blockchain.java`, `ledger/BlockValidator.java` | State deterministic, block validation, replay TX (T4), state hash |
| **TV3** | __________ | Network simulation | `network/EventQueue.java`, `network/NetworkEvent.java`, `network/NetworkSimulator.java`, `network/RateLimiter.java` | Delay/drop/duplicate/reorder, rate limit, header-before-body, hỗ trợ T2/T5 |
| **TV4** | __________ | Consensus | `consensus/ConsensusEngine.java`, `consensus/ConsensusState.java`, `consensus/VoteCollector.java`, `consensus/ProposerSelector.java` | PREVOTE/PRECOMMIT, quorum, lock/unlock, timeout, finality, hỗ trợ T6/T7 |
| **TV5 = LEADER** | __________ | Integration + release | `Main.java`, `simulator/Node.java`, `simulator/SimulationRunner.java`, `simulator/ByzantineBehavior.java`, `simulator/SimulationLogger.java`, `config/*`, E2E tests, CI, README/release | Chạy đầu-cuối, điều phối hợp nhất, T1/T8, chạy tất cả T1–T8, đóng gói đúng quy định |

**Không cần từng người tự sáng tạo class mới.** Leader tạo skeleton và test contract trước; nếu cần sửa tên file/hàm/interface phải thảo luận, sửa tài liệu và thông báo tất cả. **TV5/leader không viết thay toàn bộ test**: mỗi người có unit tests cùng module; leader viết E2E và tổng hợp. TV4 rất nặng, phải được TV1 hỗ trợ xác thực vote, TV2 hỗ trợ block validation, TV3 hỗ trợ timeout/network, TV5 hỗ trợ Byzantine tests.

## 6. Kết quả cuối cùng khi merge — CHƯA CÓ, là mục tiêu

Chương trình chạy được từ một lệnh thống nhất (lệnh `Main`/runner **chưa chốt**, phải thiết lập `pom.xml`/Maven plugin hoặc script thật trước khi hướng dẫn sử dụng). Luồng hành vi tối thiểu:

1. Đọc config/seed; tạo **8 node**, trong đó **4 validator**, mỗi node có ledger/state độc lập.
2. Tạo transaction ký số (ví dụ Alice lưu `Alice/message=Hello`), xác minh chữ ký, chống replay.
3. Proposer hợp lệ đề xuất block chứa transaction và state hash; từng node tự validate proposal/execution.
4. Network gửi header trước body, chuyển message qua hàng đợi sự kiện (có delay/drop/duplicate/reorder tùy config).
5. Validator bỏ PREVOTE rồi PRECOMMIT theo rules/lock; chỉ finalize block khi đủ precommit hợp lệ; mọi node trung thực hội tụ về ledger và state hash giống nhau.
6. Ghi log deterministic, chạy lại cùng config/seed phải ra output tương đương từng byte; chạy T1–T8 từ entry point duy nhất.

**Minh họa console mong muốn (KHÔNG phải kết quả đã chạy):**

```text
Simulation start: nodes=8 validators=4 seed=42
Height=1 round=0 proposal received
PREVOTE quorum=3/4
PRECOMMIT quorum=3/4
FINALIZED height=1 hash=...
Correct nodes agree: true
Tests T1..T8: ...
```

Không giả lập `PASS` bằng output hardcode; phải thực sự chạy luật giao thức và assertions.

## 7. Thứ tự triển khai trong 7 ngày (ưu tiên sản phẩm)

| Ngày | Mốc đầu ra |
|---|---|
| 1 | Leader dựng đủ **skeleton + API + test contracts + Main demo tạo 8 node**; GitHub repo clone/build được. Họp ngắn, mỗi người nhận file cụ thể. |
| 2 | Crypto và State/Execution thực thi được transaction, có unit tests. |
| 3 | Network simulator + consensus đường bình thường: finalize 1 block qua PREVOTE/PRECOMMIT; chứng minh T1. |
| 4 | Chạy nhiều heights; proposer schedule, timeout, locking/unlocking, recovery sau proposer im lặng. |
| 5 | Delay/drop/duplicate/reorder, Byzantine equivocation, bảo vệ safety, các test lỗi còn lại. |
| 6 | Tích hợp, hoàn thiện T1–T8, so sánh logs byte-identical; CI. |
| 7 | Ôn giải thích code, chạy lại toàn bộ test, hoàn tất `REPORT.pdf` ≤10 trang, chuyển đúng cấu trúc nộp và ZIP. |

Lộ trình là **mốc ưu tiên**, không phải bảo đảm đủ trong 7 ngày. Phải giữ code trên `main` build được liên tục. Không dành thời gian cho UI, framework ngoài hoặc tối ưu không cần thiết.

## 8. Workflow GitHub

- Leader tạo **private repository** (nếu chưa tạo), chuyển nhánh thành `main`, commit/push code nền; mời 4 thành viên còn lại.
- Đặt nhánh công việc cố định:
  - `feature/crypto` — TV1
  - `feature/execution` — TV2
  - `feature/network` — TV3
  - `feature/consensus` — TV4
  - `feature/integration` — TV5/leader
- Mỗi Issue ghi **file nào**, **hàm nào**, **input/output**, **test nào**, **điều kiện nghiệm thu**, **deadline**. Mỗi người code trong branch, chạy `./mvnw clean test`, commit/push, tạo PR, ít nhất một người khác review, mới merge vào `main`.
- Skeleton chưa triển khai dùng `UnsupportedOperationException` hoặc test đang disabled rõ ràng; **không làm test giả `PASS`**. CI nên phân biệt rõ phần chưa triển khai.
- Trong meeting (~30–45 phút), demo clone → `./mvnw clean test` → tạo branch → PR, giao nhiệm vụ đầu tiên 24 giờ và chốt tên/MSSV.

## 9. VIỆC CẦN LÀM NGAY TRONG CHAT MỚI (tối đa 3 bước mỗi lần)

1. **Xác minh GitHub**: hỏi kết quả `git status`, `git branch`, `git remote -v`; nếu chưa push thì hướng dẫn push và clone thử. Không hỏi lại môi trường đã kiểm tra.
2. **Leader tạo skeleton chạy được**: ưu tiên `Main.java`, `simulator/Node.java`, `simulator/SimulationRunner.java` và config tối thiểu; lệnh chạy thực tế, test tạo 8 node. Không nhảy ngay sang cả consensus trong một phản hồi.
3. **Khóa các API rồi phân phát cho thành viên**: viết/điền `docs/ARCHITECTURE.md`, cập nhật `TASKS.md` với danh sách file, chữ ký hàm, test/acceptance criteria. Tạo trước bộ khung compile được, giao Issue theo thứ tự phụ thuộc; mỗi lần tối đa 3 việc.

**Nhắc nhở quan trọng cho assistant ở chat mới:** Đây là bài tập học thuật có thời hạn rất sát. Người dùng muốn hướng dẫn thao tác trực tiếp, ít lý thuyết, tối đa 3 bước mỗi lượt; ưu tiên kết quả thực tế có thể chạy và kiểm thử, rồi mới giải thích sâu để vấn đáp. Không hứa làm việc nền; không nhầm kế hoạch, skeleton và feature đã hoàn thành.
