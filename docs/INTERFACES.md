# Lab01 - Cấu trúc và hợp đồng API hiện có

Ngày rà soát: **28/10/2026**. Đối chiếu mã nguồn tại máy, [context bàn giao](../Lab01_CONTEXT_HANDOFF.md) và [đề Lab01, 5 trang](../Lab01.pdf). Phân công ở [TASKS.md](TASKS.md).

Tài liệu mô tả **code đang tồn tại**, không tuyên bố đã hoàn thành blockchain. Các quyết định chưa chốt nằm ở mục 5; thành viên và AI không tự suy diễn thành API mới.

## 1. Cấu trúc giữ nguyên

Java 21; Maven Wrapper **3.9.9**; JUnit **5.11.4**; `pom.xml` đã đặt `maven.compiler.release=21`. Không cần thêm dependency để bàn giao cấu trúc.

```text
Lab1-Blockchain/
├── .mvn/wrapper/maven-wrapper.properties
├── mvnw, mvnw.cmd, pom.xml
├── README.md
├── Lab01_CONTEXT_HANDOFF.md, Lab01.pdf
├── docs/
│   └── INTERFACES.md, TASKS.md, SETUP.md
├── config/.gitkeep
├── logs/.gitkeep
└── src/
    ├── main/java/blockchain/
    │   ├── Main.java
    │   ├── model/
    │   │   ├── Transaction.java, BlockHeader.java, Vote.java
    │   │   └── Block.java, NetworkMessage.java
    │   ├── crypto/
    │   │   └── CryptoService.java, DefaultCryptoService.java, CanonicalEncoder.java
    │   ├── execution/
    │   │   ├── State.java, ExecutionEngine.java, DefaultExecutionEngine.java
    │   │   └── TransactionValidator.java
    │   ├── ledger/
    │   │   └── Blockchain.java, BlockValidator.java
    │   ├── network/
    │   │   └── NetworkSimulator.java
    │   ├── consensus/
    │   │   └── ConsensusEngine.java, ConsensusAction.java, ConsensusResult.java
    │   └── simulator/
    │       ├── Node.java, SimulationRunner.java
    │       └── SimulationConfig.java, FinalizedState.java
    └── test/java/blockchain/
        ├── model/ModelTest.java
        └── crypto/DefaultCryptoServiceTest.java
```

Có **23 file Java production và 2 file test**. Package khớp đường dẫn Maven; giữ gốc `blockchain`, không chuyển lại sang `edu.hcmus.blockchain` theo `groupId`. Maven không yêu cầu `groupId` trùng package.

`config/` và `logs/` chưa có dữ liệu; `.gitkeep` giúp Git giữ thư mục khi được đưa vào commit. `target/` là output Maven. Chưa tạo package test còn thiếu khi chưa có test thật. Giữ `src/test/java` trong repository; thư mục `tests/` của bài nộp xử lý lúc đóng gói.

## 2. Trách nhiệm và mức độ hoàn thành

| Package | Chủ sở hữu | Trách nhiệm | Hiện có |
| --- | --- | --- | --- |
| `model` | TV1, phối hợp người dùng API | Dữ liệu dùng chung | 5 record; validation hạn chế |
| `crypto` | TV1 | Canonical bytes, Ed25519, SHA-256 | Interface và implementation; 3 unit test crypto |
| `execution` | TV2 | Transaction validation, nonce, cập nhật state | Implementation một phần; chưa có test riêng/state hash |
| `ledger` | TV2 | Kiểm tra block, lưu chuỗi finalized | 2 interface; chưa có implementation |
| `network` | TV3 | Giao message và clock mô phỏng | 1 interface; chưa có implementation |
| `consensus` | TV4 | PREVOTE/PRECOMMIT, quorum, lock, timeout, finality | Interface và kiểu action/result; chưa có engine thực thi |
| `simulator` và `Main` | TV5 = leader | Nối module, config, log, kịch bản | Tạo cố định 8 node/4 validator; chưa chạy đồng thuận |

Theo đề, mỗi node phải có ledger/state/view riêng và chỉ giao tiếp qua mạng mô phỏng. `Node` hiện chưa có các thành phần này.

## 3. Dữ liệu dùng chung

Chữ ký constructor của các record hiện có:

```java
Transaction(String chainId, String senderId, String key,
            String value, long nonce, byte[] signature)
BlockHeader(String chainId, long height, int round,
            String parentHash, String stateHash,
            String proposerId, byte[] signature)
Vote(String chainId, long height, int round, Phase phase,
     String blockHash, String validatorId, byte[] signature)
Block(BlockHeader header, List<Transaction> transactions)
NetworkMessage(String senderId, String receiverId, MessageType type,
               long height, Object payload)
```

- `Vote.Phase`: `PREVOTE`, `PRECOMMIT`.
- `NetworkMessage.MessageType`: `PROPOSAL_HEADER`, `HEADER_ACCEPTED`, `PROPOSAL_BODY`, `PREVOTE`, `PRECOMMIT`.
- `Transaction`, `BlockHeader`, `Vote` chưa kiểm tra trường hoặc copy `signature`. Mảng `byte[]` có thể bị sửa từ bên ngoài; equality mặc định của record không so sánh nội dung mảng theo giá trị.
- `Block` cấm header/list null, dùng `List.copyOf`, giữ thứ tự transaction. Copy nông này chưa bảo vệ các mảng signature bên trong.
- `NetworkMessage` cấm sender/receiver/type/payload null; chưa kiểm tra kiểu payload theo type, height hoặc tính khớp giữa envelope và nội dung đã ký. Envelope chưa có chainId/round; quy tắc đối chiếu với payload chưa chốt.
- Chưa chốt payload cho `HEADER_ACCEPTED`, cách nhận diện header được chấp nhận, body tương ứng hoặc body dùng `Block` hay danh sách transaction. Không tự tạo model payload riêng ở từng module.

## 4. API và hành vi thực tế

### 4.1. Crypto - TV1

| API hiện có | Hành vi |
| --- | --- |
| `CryptoService.generateKeyPair(): KeyPair` | Implementation tạo Ed25519 bằng Java standard library |
| `sign(byte[] message, String domain, PrivateKey privateKey): byte[]` | Ký payload gồm domain và message |
| `verify(byte[] message, String domain, byte[] signature, PublicKey publicKey): boolean` | Xác minh Ed25519; false khi gặp `GeneralSecurityException` |
| `sha256(byte[] data): String` | SHA-256, hex chữ thường |
| `CanonicalEncoder.encodeTransaction(Transaction): byte[]` | Encode chainId, senderId, key, value, nonce |
| `CanonicalEncoder.encodeHeader(BlockHeader): byte[]` | Encode chainId, height, round, parentHash, stateHash, proposerId |
| `CanonicalEncoder.encodeVote(Vote): byte[]` | Encode chainId, height, round, `phase.name()`, blockHash, validatorId |

Encoder dùng `DataOutputStream`: số nguyên big-endian, int 4 byte, long 8 byte; chuỗi UTF-8 đặt sau độ dài byte bằng int. Không encode signature; null chưa có biểu diễn và có thể gây lỗi.

Payload ký: `int32BE(domainUtf8.length) || domainUtf8 || canonicalMessage`. Caller chọn domain; API không tự ép `TX:<chainId>`, `HEADER:<chainId>`, `VOTE:<chainId>`. Encoder header/vote đã bao gồm height và round, nhưng chưa có test chống tái dùng chữ ký qua chain/height/round.

`verify` có thể ném lỗi runtime như null, không bảo đảm mọi input hỏng đều trả false. `TransactionValidator` có bắt `RuntimeException` quanh encode/verify. Chưa có canonical state encoding hoặc định nghĩa block hash.

### 4.2. Execution và ledger - TV2

| API hiện có | Hành vi / giới hạn |
| --- | --- |
| `new State()` | State rỗng, chứa map dữ liệu và nonce theo sender |
| `State.get(String key): String` | Tra key đầy đủ, ví dụ `Alice/message`; null nếu chưa có |
| `nextNonce(String senderId): long` | Sender mới có nonce tiếp theo là **0** |
| `withUpdate(String senderId, String key, String value): State` | Copy state, ghi nguyên key, tăng nonce sender bằng `Math.addExact`; không tự validate quyền/chữ ký |
| `data(): Map<String,String>`, `nonces(): Map<String,Long>` | Snapshot qua `Map.copyOf`, không cam kết thứ tự duyệt canonical |
| `copy(): State` | Copy hai map |
| `new TransactionValidator(CryptoService, Map<String,PublicKey>)` | Copy map public key qua `Map.copyOf` |
| `TransactionValidator.isValid(Transaction, State): boolean` | Nhận chain `lab01`, key có tiền tố `senderId + "/"`, nonce đúng `nextNonce`, signature đúng khóa sender/domain `TX:lab01` |
| `ExecutionEngine.execute(State, List<Transaction>): State` | Interface thực thi batch |
| `new DefaultExecutionEngine(TransactionValidator)` | Implementation hiện có |
| `DefaultExecutionEngine.execute(...)` | Copy state đầu vào, validate/apply theo thứ tự; transaction sai gây `IllegalArgumentException`; trả state mới nếu cả batch thành công |
| `Blockchain.finalizedBlocks(): List<Block>` | Interface danh sách finalized theo height |
| `finalizedHeight(): long`, `lastFinalizedHash(): String` | Interface truy vấn đầu chuỗi |
| `appendFinalized(Block): void` | Interface thêm block sau consensus finality; chưa có guard thực thi |
| `BlockValidator.isValid(Block block, Block parent, State parentState): boolean` | Interface kiểm tra full block; chưa có implementation |

Theo code hiện tại, transaction đầu của Alice dùng **key `Alice/message`, nonce `0`**. `ModelTest` dùng key `message`, nonce `1` chỉ kiểm tra record, không chứng minh transaction thực thi hợp lệ. Batch có kiểm tra nonce tuần tự, nhưng chưa có test T4.

`TransactionValidator.isValid` cũng chưa bảo đảm false cho mọi record lỗi: senderId null có thể gây lỗi khi tra immutable map trước khối try/catch. Cần chốt cách từ chối input hỏng ở ranh giới module, thay vì để caller hiểu mọi thất bại đều được biểu diễn bằng boolean.

Chưa có state hash canonical, validation header độc lập hoặc quy ước genesis. `Blockchain` chỉ mô tả finalized, chưa quy định nơi lưu proposal/pending view hay state theo block. Không dùng thứ tự duyệt HashMap/Map.copyOf để băm state.

### 4.3. Network - TV3

```java
void send(NetworkMessage message);
List<NetworkMessage> poll();
long currentTime();
void advanceTime(long milliseconds);
```

Chỉ có interface `NetworkSimulator`. `send` đưa message vào mạng; `poll` được mô tả lấy message đến hạn. Chưa chốt đơn vị `currentTime`, poll có loại event khỏi queue ra sao, bước thời gian âm, thứ tự event cùng timestamp hoặc cách giao đến node đích.

Handoff chọn mạng in-memory, single-threaded discrete-event với đồng hồ ảo. Chưa có queue, delay/drop/duplicate/reorder, rate limit, block/unblock peer, stabilization/Delta và log. Interface chưa thể hiện handshake header/body, broadcast hay fault config; TV3/TV5 phải thống nhất trách nhiệm, không tự tạo API mới khi chỉ chuẩn hóa.

### 4.4. Consensus - TV4

```java
List<ConsensusAction> onProposal(Block block, boolean valid);
List<ConsensusAction> onVote(Vote vote);
List<ConsensusAction> onTimeout(long height, int round);
ConsensusResult getResult();
```

`ConsensusAction` là sealed interface gồm `BroadcastVote(Vote vote)`, `FinalizeBlock(Block block)`, `ScheduleTimeout(long height, int round, long delayMillis)`.

`ConsensusResult(long height, int round, String blockHash, boolean finalized)` chỉ chứa dữ liệu. Chưa có engine hoặc validation các record action/result.

`onProposal` nhận cờ valid từ caller; chưa chỉ rõ ai chạy BlockValidator, kiểm tra proposer/header trước khi truyền cờ. `onVote` chưa xác minh chữ ký/membership hoặc đếm/loại duplicate. Các action chưa quy định ai tạo/phát proposal đầu round và chuyển height.

Đích theo đề: n=3f+1, quorum 2f+1; tối đa một chữ ký mỗi phase cho `(height, round)`; tối đa một vote hợp lệ mỗi validator cho `(height, round, phase)`; chỉ prevote block hợp lệ; lock sau quorum PREVOTE non-NIL; chỉ đổi block đã lock theo quorum hợp lệ của block khác ở round sau. Điều kiện finalize/NIL/timeout cần đặc tả chung trước implementation. Chưa có bằng chứng safety/liveness.

### 4.5. Simulator và entry point - TV5

| API hiện có | Hành vi / giới hạn |
| --- | --- |
| `Node(String id, boolean validator)`, `getId()`, `isValidator()` | Chỉ lưu ID/cờ validator; chưa giữ ledger/state/view/engine/network |
| `SimulationRunner.createNodes(): List<Node>` | Cố định node-0 đến node-7, bốn node đầu là validator; trả `List.copyOf` |
| `SimulationConfig(String chainId, int nodeCount, int validatorCount, long seed, long roundTimeoutMillis)` | Kiểm tra chain không trống, nodeCount >= 8, validatorCount >= 4 và <= nodeCount, validatorCount=3f+1, timeout dương |
| `maxByzantine(): int`, `quorum(): int` | `(validatorCount-1)/3` và `2f+1` |
| `SimulationConfig.defaultConfig()` | lab01, 8 node, 4 validator, seed 42, timeout 1000 ms |
| `FinalizedState(Block lastBlock, State state)` | Cấm state null, copy state; cho phép lastBlock null; chưa kiểm tra state tương ứng block |
| `Main.main(String[] args): void` | Tạo node và in ID/validator; chưa đọc config/args, chạy consensus hoặc T1-T8 |

SimulationConfig/FinalizedState chưa nối vào runner/node. Chưa có logger, Byzantine scenario hoặc script chạy hai lần so log/state hash. `pom.xml` chưa cấu hình plugin chạy ứng dụng; không quảng bá lệnh Maven chạy simulator chưa tồn tại.

## 5. Điểm chưa nhất quán và quyết định cần chốt

Các mục dưới **chưa được sửa bằng code**. P0 cần chốt trước khi module phụ thuộc triển khai song song; P1 cần hoàn thiện và kiểm thử trong module.

| ID | Mức | Vấn đề / quyết định | Chủ trì, phối hợp |
| --- | --- | --- | --- |
| API-01 | P0 | Config cho chainId tùy chọn nhưng validator hardcode lab01/TX:lab01; chốt truyền chain/domain nhất quán. | TV5 + TV1, TV2 |
| API-02 | P0 | Nonce bắt đầu 0, key phải đầy đủ senderId/key; ví dụ/model test dùng nonce 1/key ngắn. Chốt quy ước và test; giữ test record đang có. | TV2 + TV1, TV5 |
| API-03 | P0 | Thiếu canonical state hash (có gồm nonce không), blockHash, genesis/parent ban đầu, quy tắc header gắn ordered body. Không tự chọn cách hash khác nhau. | TV1, TV2 + TV4, TV5 |
| API-04 | P0 | NIL chưa có biểu diễn; encodeVote không nhận blockHash null. Chốt sentinel/encoding/semantics. | TV4 + TV1 |
| API-05 | P0 | Payload Object, header ACK/body tương ứng, kiểm tra header trước body, đối chiếu envelope chưa chốt. Full BlockValidator không thay thế header validation ở handshake. | TV3 + TV1, TV2, TV5 |
| API-06 | P0 | Chốt nguồn validator set/public keys cố định, proposer schedule, nơi validate proposal/vote, ý nghĩa cờ valid; không tin sender/cờ do mạng tự khai. | TV4 + TV1, TV2, TV5 |
| API-07 | P0 | Thiếu transition round/phase/NIL, lock/unlock, quorum PRECOMMIT finalize, timeout cũ và tạo proposal; action chưa mô tả đủ luồng. | TV4 + TV3, TV5 |
| API-08 | P0 | Runner cố định 8/4, chưa dùng config/seed. Chốt clock/timeout chung, event tie-break, RNG, stabilization/Delta; dùng clock ảo. | TV5 + TV3, TV4 |
| API-09 | P0 | Node thiếu ledger/state/view riêng; thiếu luồng FinalizeBlock -> append ledger + state và ngăn finalize xung đột. Chốt pending state và FinalizedState/genesis. | TV5 + TV2, TV4 |
| API-10 | P1 | Signature byte[] có thể bị sửa, model/payload validation thiếu; verify boolean thiếu lý do và có thể ném runtime error. Chốt validate/copy và log rejection cho T3. | TV1 + các module nhận dữ liệu, TV5 |
| API-11 | P0 | Khóa Ed25519 dùng randomness hệ thống; seed mô phỏng chưa bảo đảm rerun. Chốt key fixture/input ổn định cho T8; không dùng PRNG mô phỏng làm nguồn an toàn mật mã. | TV1 + TV5 |
| API-12 | P1 | Thiếu unit test canonical/state/ledger/network/vote counting và E2E T1-T8, log/entry point chung. | Từng chủ module + TV5 |

Leader ghi quyết định tại đây trước khi đổi contract. Đợt chuẩn hóa không thêm method/record/dependency để khắc phục các mục trên.

## 6. Kiểm chứng và giới hạn bàn giao

Hai lớp test hiện có gồm 6 test: ModelTest đọc 3 record; DefaultCryptoServiceTest kiểm tra chữ ký hợp lệ, sai domain, message bị sửa. Đây chưa phải T1-T8; T3 còn thiếu đường nhận message thực tế và log lý do.

Chạy từ thư mục có pom.xml:

```bash
./mvnw clean test
```

PowerShell dùng `./mvnw.cmd clean test`. Kết quả thực tế đợt rà soát ghi tại [TASKS.md, mục 6](TASKS.md#6-kiểm-chứng-và-điều-kiện-bàn-giao).

Git cục bộ đang ở main, có upstream origin/main và remote origin. Nhiều API/implementation/test mới cùng hai tài liệu nguồn Lab01 còn **untracked**; chưa thể kết luận người khác clone sẽ nhận đủ file đang có trên máy. Chưa fetch/kiểm tra nội dung remote; chưa stage, commit hoặc push trong đợt này.
