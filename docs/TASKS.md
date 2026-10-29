# Lab01 - Bàn giao Phase 1 cho 5 thành viên

Ngày khóa: **28/10/2026**. Đọc cùng [INTERFACES.md](INTERFACES.md), nguồn chuẩn **contract v1**. TV5 là leader, một trong 5 người. Không thêm dependency hoặc tự commit/push trong lượt này.

**Kết luận Phase 1:** 10/10 P0 đã có quyết định thiết kế cụ thể, không còn yêu cầu thành viên tự chọn chain/domain, hash/genesis/NIL, payload, proposer/quorum/lock/timeout, clock/seed hoặc fixture. Các implementation ghi Phase 2 bên dưới vẫn chưa có; bộ T1-T8 chưa được chứng minh. Không dùng nhãn "Phase 1 xong" để báo blockchain hoàn tất.

## 1. Quyền sở hữu và file bàn giao

Đường dẫn dưới src/main/java/blockchain/, trừ khi ghi khác. Tên trong cột Phase 2 là **đầu ra phải tạo**, chưa phải file có sẵn.

| TV / tên MSSV | Source hiện có, được giao | File/API Phase 2 đã khóa | Test |
| --- | --- | --- | --- |
| TV1 / ______ | model/{Transaction,BlockHeader,Vote,Block,NetworkMessage}.java; crypto/{CryptoService,DefaultCryptoService,CanonicalEncoder}.java | crypto/IdentityFixtures.java; config/test-identities.properties; 5 encoder mới | Giữ ModelTest, DefaultCryptoServiceTest, HeaderSigningContractTest; thêm CanonicalEncoderTest, IdentityFixturesTest trong package crypto |
| TV2 / ______ | execution/{State,ExecutionEngine,DefaultExecutionEngine,TransactionValidator}.java; ledger/{Blockchain,BlockValidator}.java | ledger/DefaultBlockValidator.java, ledger/InMemoryBlockchain.java; overload TransactionValidator theo chainId | execution/{StateTest,ExecutionEngineTest,TransactionValidatorTest}; ledger/{BlockValidatorTest,BlockchainTest} |
| TV3 / ______ | network/NetworkSimulator.java | network/{NetworkSettings,DefaultNetworkSimulator}.java | network/{NetworkSimulatorTest,HeaderBodyProtocolTest} |
| TV4 / ______ | consensus/{ConsensusEngine,ConsensusAction,ConsensusResult}.java | consensus/DefaultConsensusEngine.java | consensus/{ConsensusEngineTest,ConsensusSafetyTest} |
| TV5 = leader / ______ | Main.java; simulator/{Node,SimulationRunner,SimulationConfig,FinalizedState}.java; docs/config/logs/pom/Wrapper | simulator/SimulationLogger.java; config/lab01.properties, scenario inputs; simulator wiring; scripts/check-determinism.ps1 | simulator/{SimulationRunnerTest,NodeIsolationTest}; integration/Lab01ScenarioTest |

Tests đều ở src/test/java/blockchain/... và là JUnit 5. Helper private/package-private được phép trong module, không tự thêm public API/model chung. Những tên helper cũ trong handoff như EventQueue/VoteCollector/ByzantineBehavior không phải API bắt buộc; owner có thể dùng helper nội bộ, không đổi các chữ ký public đã khóa.

TV1 giữ model/crypto; TV2 giữ execution/ledger; TV3 giữ network; TV4 giữ consensus; TV5 giữ simulator/config/logging/build/docs. TV3/TV4 **dùng** SimulationLogger theo contract, TV5 tạo implementation đó; không tạo logger riêng. Các thành viên không sửa file của nhau để làm compiler hết lỗi.

## 2. Những gì đã thay đổi ở Phase 1

Giữ toàn bộ test cũ, không di chuyển/xóa source hoặc thêm dependency. Chỉ mở rộng những điểm thiếu dữ liệu/điều khiển cần thiết:

| File | Thay đổi / lý do |
| --- | --- |
| model/BlockHeader.java | Thêm transactionsHash, validRound, prevoteProof; copy list proof. Thiếu commitment sẽ không ràng buộc ordered body; thiếu proof không xác minh được quyền prevote block khác đã lock |
| crypto/CanonicalEncoder.java | encodeHeader bao phủ cả ba trường mới và signed votes trong proof, không để metadata bị đổi mà chữ ký vẫn hợp lệ |
| ledger/BlockValidator.java | Thêm isHeaderValid(header,parent,parentState); full block validator không thể dùng trước khi nhận body |
| consensus/ConsensusEngine.java | Thêm startHeight, Step và timeout có phase; timeout cũ không phân biệt được timer stale giữa các step |
| consensus/ConsensusAction.java | Thêm RequestProposal, commitProof trong FinalizeBlock, step trong ScheduleTimeout; runner không phải đoán thời điểm/chứng cứ để tạo/chốt block |
| src/test/java/blockchain/crypto/HeaderSigningContractTest.java | 2 test chữ ký header bao phủ body commitment/round/validRound/proof, và verify sau gắn own signature |

Constructor header cũ và overload action/timeout cũ còn để tương thích, deprecated. Header legacy có transactionsHash rỗng, FinalizeBlock legacy proof rỗng: không đủ điều kiện giao thức. Không dùng các overload này trong Phase 2. ModelTest được giữ nguyên để không nhầm việc đổi contract với việc xóa test đang chạy.

**Chưa triển khai ở lượt này:** engine đồng thuận, mạng, ledger, canonical state/block hash, config loader, fixture loader/file, logger/CLI/E2E. TransactionValidator vẫn hardcode lab01 ở source; quyết định migration constructor đã khóa và giao TV2, không tuyên bố đã sửa runtime đa-chain.

## 3. Nhiệm vụ nhận ngay theo từng thành viên

### TV1 - Model/crypto và danh tính dùng chung

**Input:** record v1 + domain/key hoặc map state/ordered TX. **Output:** bytes theo INTERFACES mục 3, chữ ký Ed25519, SHA-256 hex, registry fixture bất biến.

- Giữ sign/verify/sha256 và encoder TX/Vote hiện có; triển khai CanonicalEncoder.encodeTransactions, encodeState, encodeBlockId, encodeGenesis, encodeProposalId đúng chữ ký/bảng bytes. Không đổi thứ tự field, prefix version, sort UTF-8, cách encode signature hoặc cách bỏ nonce 0.
- Kiểm tra encodeHeader mới bao phủ proof; triển khai guard null/Unicode/format ở biên phù hợp, hoàn thiện defensive copy signature (API-10 P1). Không đổi constructor record để cấm record test cũ khởi tạo dữ liệu mẫu chưa hợp lệ giao thức; protocol validation là ranh giới riêng.
- Tạo IdentityFixtures.load(Path): Map<String,KeyPair> và file test-identities.properties theo schema. Sinh khóa bằng SecureRandom một lần rồi cố định bytes; không seed keygen, không fallback sinh khóa khi thiếu. Cấp node IDs cho default 8 node và Alice/Bob; nghiệm thu cấu hình lớn hơn cần bổ sung fixture tương ứng trước run.
- Giao SHA-256 fixture, test signature/header/vote, và golden vectors thực tế so bảng trong INTERFACES. Chỉ TV1 sửa shared model nếu phát hiện cần thay contract; trước đó cập nhật quyết định với leader.

**Acceptance:** 4 golden hashes mục 3 phải khớp; hai map insertion order khác ra bytes/hash giống; đổi nonce đổi stateHash; reorder body đổi transactionsHash kể cả khi state cuối trùng; reprop round/proposer/proof đổi proposalId nhưng không blockHash. NIL encode được; chữ ký sai domain/chain/height/round/phase bị reject. File fixture missing/sai key pair fail rõ. Đóng góp T3; hỗ trợ T8.

### TV2 - Execution/ledger

**Input:** trusted chainId/registry, parent block/state, ordered TX/body. **Output:** boolean validation hoặc State mới/ledger snapshot.

- Thêm constructor TransactionValidator(chainId,crypto,publicKeys), giữ overload legacy lab01. Validate input trước map lookup/encoding; dùng domain TX:+chainId. Chặn nonce âm/Long.MAX_VALUE, key không đủ prefix/suffix, value null/invalid Unicode, TX replay.
- Hoàn thiện State/DefaultExecutionEngine: không mutate input, batch atomic, thứ tự TX không thay đổi, state includes nextNonce. Không implement encoder/hash riêng; dùng API TV1.
- Tạo DefaultBlockValidator và InMemoryBlockchain theo constructor mục 5 INTERFACES. Header-only checks không execute; full validation kiểm tra transactionsHash và post-stateHash, parent/genesis/proposer/proof. Ledger append chỉ nối tiếp, duplicate same hash no-op, conflicting hash throws; ledger không tự nhận một block là finalized.
- Phối hợp TV5 commit state/ledger trong một callback. TV2 không viết Node hoặc cập nhật state chung cho cả mạng.

**Acceptance:** chain lab02 hợp lệ khi inject lab02 và reject lab01; Alice/message nonce 0 rồi 1; replay cùng batch/qua block không apply lần hai; Bob không sửa Alice/key. TX cuối sai làm state đầu vào giữ nguyên. Header legacy thiếu commitment, proposer/proof/signature/parent/body/stateHash sai bị reject. Genesis virtual/empty ledger đúng; append duplicate không thực thi lại. Chủ trì T4, hỗ trợ T1/T3/T8.

### TV3 - Network simulator

**Input:** NetworkSettings, seed, nodeIds, SimulationLogger; NetworkMessage đúng schema. **Output:** delivery due từ poll, virtual time và trace network.

- Tạo NetworkSettings/DefaultNetworkSimulator theo constructor mục 7 INTERFACES. Java Random riêng một instance, đúng thứ tự draw; clock long ms, queue (deliverAt,enqueueSequence), poll drain due và không gọi Node trực tiếp.
- Implement pre-GST faults, post-GST bounded delivery, fixed-window outbound rate/peer blocking, message IDs canonical. Không thêm drop ngẫu nhiên sau GST hoặc đếm duplicate là peer flood.
- Giữ guard body sau ACK được giao, keyed bằng sender/receiver/proposalId; echo-header ACK là loại đã chốt. Node TV5 vẫn phải tự validate header/body. Network không cast payload bừa, không đọc state/ledger/lock.
- Log qua SimulationLogger; không giữ writer/RNG/timestamp dùng chung với module khác. T3 độc lập chưa cần runner thật: test send header -> poll -> ACK -> poll -> body.

**Acceptance:** cùng seed/script/tick ra cùng delivery order; advance âm/overflow fail; poll hai lần không lặp event. Wrong payload/type/peer/height, body trước ACK và ACK sai ID không được giao như hợp lệ. Retry phục hồi mất ACK/body. Test block/unblock/rate có assert timestamp. Với correct traffic đủ quota sau GST, delivery<=send+Delta, không drop/block correct peer. Chủ trì T2/T5 cùng TV4; không tự viết vote counting.

### TV4 - Consensus

**Input:** config, fixed registry/local key/logger, proposal đã full-validate tại node, signed votes/timeouts. **Output:** action list và result; không mutate ledger/network bên ngoài.

- Tạo DefaultConsensusEngine đúng constructor mục 6, dùng typed timeout/startHeight/RequestProposal. Triển khai state/transition đã khóa; không thay bằng majority một pha hoặc tự đặt quorum.
- Vote verification/membership/chain/height/round/phase/domain trước dedup. Live tally chỉ một vote/validator/key, proof verify Q unique independently. Mỗi local validator ký tối đa một vote mỗi phase/(h,r).
- Giữ lock qua NIL/timeout; nâng lock khi certificate mới hơn với body hợp lệ, kể cả QC đến trễ; không ký precommit lần hai. Reproposal chọn validValue có QC cao nhất đã biết, ID ổn định qua round. Xử lý old-round commit, future-round f+1 jump, late body và stale timer.
- Ký/đếm local vote trước trả BroadcastVote; observer verify/finalize nhưng không vote. Đưa commitProof Q chính xác vào FinalizeBlock. Logger ghi lock/round/timeout từ chính engine; không buộc TV5 đoán state private qua console output.

**Acceptance traces:** Q-1 không lock/finalize; Q cùng PREVOTE mới lock; đủ tổng vote khác hash không finalize; chỉ Q PRECOMMIT cùng hash + full valid body mới commit. Duplicate/equivocation không tăng weight. Lock B/r0 -> proposal C không proof phải NIL; QC C/r1 ở proposal r2 cho phép C, NIL/timeout không unlock. QC trễ sau PRECOMMIT NIL chỉ cập nhật lock/validValue, không ký lại. Timer stale không phát vote; f message không jump, f+1 unique đúng round mới jump. Proposer silent và tối đa f equivocate không làm double-finality. Chủ trì T6/T7, hỗ trợ T2/T5.

### TV5 = leader - Wiring/logger/scenarios/release

**Input:** config, fixture bytes, scenario inputs và 4 implementation module. **Output:** run snapshots/logs, assertion T1-T8, CLI/script thật.

- Tạo SimulationLogger theo constructor/method mục 9 để TV3/TV4 compile độc lập; logger chỉ sink append, không điều khiển consensus. Giữ định dạng TSV, stable reason/detail và sequence đã khóa.
- Implement Node/SimulationRunner overload/method đúng mục 8. Counts lấy config; mỗi node có state/ledger/view/engine riêng, local key riêng; share transport/config/public keys và log sink đúng ranh giới. Giữ constructor Node/runner cũ cho source compatibility.
- Wiring header -> ACK -> body -> local validation -> consensus, action FIFO, virtual scheduler 1 ms. Commit proof verify trước atomic append/state update, observer tự execute; archive/retransmit/catch-up cả old height, không copy finalized state từ leader.
- Nạp UTF-8 Properties, fixture và scenario trước run, không fallback sinh key. Main CLI --config/--scenario như hợp đồng; chạy app bằng java -cp target/classes blockchain.Main sau compile, **đây là CLI Phase 2 chưa có**, không thêm Maven exec plugin chỉ để có lệnh chạy.
- Tạo Lab01ScenarioTest gọi T1-T8 qua cùng suite/entry point Maven Wrapper; script check-determinism.ps1 tạo hai run mới với log file khác nhau và so bytes/state hashes. Script gọi runner/CLI thật, exit nonzero nếu khác/deadline/invariant, không so console banners Maven.
- Cập nhật README/SETUP theo code thực tế, CI và đóng gói sau khi đạt nghiệm thu. Hiện chưa tạo report/ZIP. *.log đang ignore; output .tsv tại logs/ có thể được chọn lưu làm bằng chứng, không tự stage output mỗi lần chạy.

**Acceptance:** config 8/4 và 11/7 (fixture đủ) tạo đúng counts, không share mutable business state. Reproposal giữ value nhưng đổi proposal metadata; fake valid flag từ mạng không ảnh hưởng. Duplicate finalization no-op, conflicting commit invariant failure; slow/non-validator hội tụ qua mạng. Correct link bounds sau GST được assert; run deadline không được coi PASS. T8 reset toàn bộ instance và so logs từng byte/final hash từng correct node.

## 4. Scenario/test input và tiêu chí chung

TV5 là owner duy nhất của kịch bản, không giao mỗi AI tự chọn dữ liệu E2E. Input chuẩn mặc định: Alice nonce0 Alice/message=hello; Alice nonce1 Alice/message=world; Bob nonce0 Bob/message=hi. Được ký lúc setup bằng fixture cố định, nạp độc lập vào mempool từng node qua submitTransaction; chưa apply vào state. Default targetHeight=3, block có thể rỗng sau khi hết TX.

Lưu input ở config/scenarios/T1.properties ... T8.properties; schema UTF-8 Properties: tx.count, tx.i.senderId/key/value/nonce (i tăng từ 0); byzantineIds là ID CSV theo nodeIndex, rỗng nếu không có; silentProposerIds là CSV; equivocationIds là CSV subset Byzantine. Các fault mode cố định cho toàn run, không có script parser/time event mới ở Phase 2. Network params đọc từ lab01.properties, scenario chỉ override các khóa network đã liệt kê (khóa lạ reject); log hash cả base config và scenario bytes. Nếu cần test sự kiện chi tiết, unit test dùng callback script trong test code theo (virtualTime,scriptIndex), không tự mở rộng format config public.

Silent node ngừng phát proposal ở mọi slot mình làm proposer, vẫn có thể nhận dữ liệu; crash scenario node đó ngừng mọi callback outbound. Equivocation node ký hai valid-content candidate/vote khác hash ở cùng (h,r,phase), gửi cho hai nhóm validator chia theo nodeIndex chẵn/lẻ; không giả chữ ký node đúng. Harness lựa chọn mode, không network tự bịa phiếu. Với default f=1 chỉ một node Byzantine trong T6/T7, luôn giữ <=f. Mode crash dùng thuộc tính scenario crashNodeIds CSV subset Byzantine (mặc định rỗng).

| Test | Input đặc thù / owner | Assertion cần có |
| --- | --- | --- |
| T1 | Network sạch, GST=0; TV5 | Tất cả correct nodes tới targetHeight, cùng ordered blockHash/stateHash; state chứa world và hi, nonce Alice=2/Bob=1 |
| T2 | GST=2000 ms, duplicatePercent=100, preSyncMaxDelay=500; TV3+TV4 | Thực sự quan sát duplicate/reorder trước GST; không double-vote count/double finality, cuối cùng hội tụ |
| T3 | Chạy sạch rồi đưa TX/header/vote modified signature hoặc domain vào đường receive; TV1+TV5 | Từng loại bị reject/log lý do; không tăng valid tally hoặc state do input lỗi; correct chain vẫn tiếp tục |
| T4 | Thử TX Alice nonce0 đã ký ở setup lặp lại trong/qua batch/block; TV2+TV5 | Validator reject replay, execution không apply hai lần; ledger không chứa block invalid |
| T5 | GST=2000, dropPercent=30, preSyncMaxDelay=1000; TV3+TV4 | Có drop/delay thực tế; safety mọi thời điểm, sau GST bounds đúng và eventual progress |
| T6 | node-0 silent, biến thể node-0 crash; TV4+TV5 | Timeout/round tăng thực sự, proposer đúng sau đó giúp chain tiếp tục |
| T7 | node-0 equivocate, <=f; TV4+TV5 | Evidence hai signed value thật; không conflicting finalization qua mọi round/height; các correct node hội tụ |
| T8 | Hai run mới của cùng cấu hình T2, scenario/input/key bytes giữ nguyên; TV5+TV1+TV3 | Files.mismatch==-1 cho TSV và state hash tương ứng bằng nhau; không keygen/runtime nondeterminism |

T3/T4 negative checks có thể là các nhánh trong cùng E2E suite + unit tests; không đưa assertion sai thành blockchain demo output. Scenario input cuối cùng phải được owner version để rerun, không sinh random input mỗi lần. Chứng minh safety kiểm tra toàn bộ lịch sử finalized của từng correct node, không chỉ so state cuối.

Mốc chạy chung khi Phase 2 hoàn tất: ./mvnw clean test gọi integration/Lab01ScenarioTest cùng unit tests; JUnit suite không dùng disabled/giả PASS. T8 script là bằng chứng bổ sung và có thể gọi bằng PowerShell; chưa tồn tại ở Phase 1.

## 5. Bàn giao đồng thời và thứ tự tích hợp

Mỗi người nhận INTERFACES.md + TASKS.md + source/test module mình; không phụ thuộc hai tài liệu Lab01 đang bị xóa. Model/API đã sửa trong Phase 1 là baseline chung.

Có thể bắt đầu **cùng lúc** theo chữ ký đã khóa. Nếu một method/class Phase 2 của owner khác chưa có, chỉ dùng test double của interface có sẵn hoặc chờ merge foundation; không commit stub trùng FQN/public API vào module mình, không sửa dependency của người khác. Helper test-only không đóng vai implementation thật trong E2E.

Lượt merge foundation đầu: TV1 encoder/fixture; TV5 logger; TV3 NetworkSettings. Tiếp theo TV2 validators/ledger, TV3 network, TV4 engine, TV5 runner/scenarios. Đây là thứ tự merge, không bắt các thành viên đợi mới bắt đầu viết phần mình. Mỗi PR phải báo contract version và acceptance thật đã chạy; nếu phát hiện contract lỗi thì ghi API-xx + trace phản ví dụ, leader cập nhật tài liệu và thông báo trước khi đổi public API.

Mẫu giao AI:

> Tôi là TV[n], làm Phase 2 của contract v1. Đọc docs/INTERFACES.md và docs/TASKS.md, chỉ sửa file thuộc owner của tôi, triển khai đúng chữ ký/input/output/behavior đã khóa, giữ code/test cũ. Đánh dấu rõ dependency foundation chưa có; không tự đổi schema/hash/NIL/quorum/timeout hoặc thêm model/logger/stub của người khác. Viết acceptance tests thật, chạy Maven Wrapper, báo phần chưa chứng minh. Không tự commit/push.

## 6. Kiểm chứng Phase 1, P0 và việc còn lại

Đã chạy đúng ./mvnw clean test qua Git Bash, Java 21.0.10 ngày 28/10/2026: **BUILD SUCCESS; 8 tests, 0 failures, 0 errors, 0 skipped**. Biên dịch 23 production files và 3 test files. Sáu test cũ giữ nguyên; hai test mới chỉ kiểm chứng contract ký header, chưa chứng minh valid prevote certificate hoặc consensus. Surefire reports ở target/surefire-reports/. Có deprecation notice từ ModelTest dùng constructor header cũ; đó là compatibility có chủ đích, không là test failure.

| P0 | Trạng thái thiết kế | Còn phải hiện thực |
| --- | --- | --- |
| API-01 | Đã khóa chain injection/domain | TV2 migration constructor/logic; TV5 truyền config |
| API-02 | Đã khóa key đầy đủ/nonce0/batch atomic | TV2 input guards và tests |
| API-03 | Đã khóa bytes/hash/body binding/genesis/reproposal ID | TV1 encoder helpers, TV2 validation/ledger, TV5 wiring |
| API-04 | Đã khóa NIL/certificate/validRound | TV1 guards, TV2/TV4 proof verification |
| API-05 | Đã khóa payload/ACK/proposalId/retry | TV3 guard transport, TV5 receiver/cache/catch-up |
| API-06 | Đã khóa registry/proposer/local validation | TV1 fixture, TV2 header validator, TV4 vote checks |
| API-07 | Đã khóa phase machine/timers/action/finality | TV4 engine và acceptance traces; chưa có chứng minh thực nghiệm |
| API-08 | Đã khóa config/clock/RNG/order/GST/rate | TV3 simulator, TV5 scheduler/config/scenarios |
| API-09 | Đã khóa node isolation/atomic commit/observer catch-up | TV2 ledger, TV5 wiring |
| API-11 | Đã khóa fixed test fixture và log/replay contract | TV1 tạo fixture/loader; TV5 logging/T8/script |

Không còn P0 **chưa quyết định** trong phạm vi contract v1. API-10/API-12 còn P1: defensive copies/validation chi tiết và đủ unit/E2E. Implementation, CI, fixture thực tế, config/scenario files, CLI, deterministic script, T1-T8 và release/report vẫn là Phase 2; không tuyên bố chúng đã có.

Git đầu lượt này ở main có upstream origin/main; Lab01.pdf và Lab01_CONTEXT_HANDOFF.md đã được người dùng xóa, giữ nguyên. Chưa fetch/xác minh remote, stage, commit hoặc push. Kiểm tra diff sau lượt này trước khi tự đưa baseline mới cho nhóm.

Bài nộp sau cùng giữ quy định Lab01_ID1_ID2_ID3_ID4_ID5/{src,tests,logs,config,README.md,REPORT.pdf}, ZIP cùng tên; report <=10 trang không chèn source. TV5 chuẩn bị packaging tests/ mà không phá Maven src/test/java trong quá trình phát triển. Chưa tạo report/ZIP ở Phase 1.
