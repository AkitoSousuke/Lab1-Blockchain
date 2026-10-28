# Lab01 - Hợp đồng tích hợp v1 (Phase 1)

Ngày khóa: **28/10/2026**. Tài liệu này là nguồn chuẩn cho 5 thành viên; [TASKS.md](TASKS.md) quy định quyền sửa file, đầu ra và acceptance tests. **Đã chốt thiết kế không có nghĩa đã triển khai hoặc chứng minh safety/liveness.**

`Lab01.pdf` và `Lab01_CONTEXT_HANDOFF.md` đang bị xóa trong working tree khi bắt đầu lượt này. Không khôi phục hoặc sửa trạng thái xóa của người dùng. Yêu cầu Lab01 được giữ theo nội dung đã đọc ở lượt rà soát trước; không còn đặt link tới file thiếu.

## 1. Phạm vi, cấu trúc và nguồn chuẩn

Java 21, Maven Wrapper 3.9.9, JUnit 5.11.4; giữ pom/Wrapper/dependency. Production ở `src/main/java/blockchain/`, test ở `src/test/java/blockchain/`; không đổi package theo Maven groupId.

| Module / người | File hiện có | Ranh giới |
| --- | --- | --- |
| model + crypto / TV1 | Transaction, BlockHeader, Vote, Block, NetworkMessage; CryptoService, DefaultCryptoService, CanonicalEncoder | Kiểu dùng chung, bytes, chữ ký/hash, nạp fixture danh tính |
| execution + ledger / TV2 | State, ExecutionEngine, DefaultExecutionEngine, TransactionValidator; Blockchain, BlockValidator | Validation transaction/header/block; state transition; ledger cục bộ |
| network / TV3 | NetworkSimulator | Clock ảo, delivery/fault/rate limit; không quyết định validity/finality |
| consensus / TV4 | ConsensusEngine, ConsensusAction, ConsensusResult | Vote/quorum/lock/timeout và yêu cầu proposal/finality cục bộ |
| simulator / TV5 = leader | Main, Node, SimulationRunner, SimulationConfig, FinalizedState | Wiring, config, mempool, xử lý action, log, E2E/release |

`docs/`, `config/`, `logs/` giữ vị trí hiện tại; config/logs mới có .gitkeep. Không tạo helper class nếu không phục vụ contract. Model dùng chung do TV1 sở hữu sau bàn giao; không tạo bản sao trong module khác.

**Mã đã có:** crypto và execution một phần; ledger/network/consensus chưa có implementation; Main mới in 8 node/4 validator. Phase 1 chỉ bổ sung model/API tối thiểu và encoding header tương ứng. Mọi API ghi **[Phase 2]** dưới đây là chữ ký đã khóa để chủ module triển khai, chưa tồn tại trong source; không được hiểu là chức năng đã chạy.

## 2. Quy ước chung - API-01, API-02, API-04, API-06

- chainId không cố định: chuỗi ASCII khớp `[A-Za-z0-9_-]{1,64}`, lấy từ SimulationConfig; mặc định lab01. So sánh chính xác, không trim/normalize.
- ID participant/node cùng định dạng ASCII trên, phân biệt hoa thường. Node tạo theo chỉ số số học node-0, node-1, ...; validator là node-0 đến node-(validatorCount-1), thứ tự theo chỉ số, **không sort lexicographic** node-10 trước node-2. Mỗi validator có trọng số 1, tập/khóa không đổi trong một run. Accounts Alice/Bob là participant có khóa, không tự động là validator.
- n=validatorCount=3f+1, n>=4; quorum Q=2f+1, f=(n-1)/3. NodeCount>=8 và >=n. Default 8 node, 4 validator, f=1, Q=3.
- height thực bắt đầu 1; genesis ảo là height 0; round bắt đầu 0 ở mỗi height. Height/round âm, nonce âm đều không hợp lệ.
- Transaction.key là key **đầy đủ** `senderId + "/" + suffix`, suffix không rỗng. Ví dụ Alice/message; không tự thêm prefix khi apply. value không null, cho phép rỗng; không có delete. Key/value phải là Unicode hợp lệ (không surrogate lẻ), không normalize; giới hạn UTF-8 key 256 byte, value 4096 byte.
- Nonce là số tiếp theo kỳ vọng, sender mới bắt đầu **0**. Apply đúng nonce rồi tăng 1; nonce Long.MAX_VALUE bị từ chối để tránh overflow. Mọi transaction trong ordered batch dùng state sau transaction trước. Sai một transaction làm cả batch thất bại, không sửa state gốc.
- NIL vote là literal ASCII **"NIL"**, không null/chuỗi rỗng/64 số 0. Hash thật là đúng 64 ký tự hex thường [0-9a-f]. Không bao giờ hash chữ NIL để biểu diễn NIL. NIL không là block/genesis và không được finalize.
- Chữ ký Ed25519, hash SHA-256. Domain chính xác `TX:<chainId>`, `HEADER:<chainId>`, `VOTE:<chainId>`; lấy chain tin cậy từ config, kiểm tra payload.chainId trước verify. Sai chain/domain/height/round/phase hoặc signer không có quyền -> reject.
- Không dùng record.equals, byte[].equals hoặc toString để nhận diện nội dung đã ký. So signature bằng Arrays.equals; so block theo blockHash bên dưới. Tại biên nhận, kiểm tra null/format trước tra public-key map hoặc encoder. boolean validation trả false cho dữ liệu không hợp lệ; lỗi config/programming mới ném IllegalArgumentException.

**Migration API-01 [Phase 2, TV2]:**
`TransactionValidator(String chainId, CryptoService crypto, Map<String,PublicKey> publicKeys)` là constructor dùng khi wiring. Giữ constructor cũ 2 tham số như overload gọi chainId="lab01"; xóa hardcode trong isValid, dùng "TX:"+chainId. Source hiện vẫn dùng hardcode, đây là việc triển khai đã giao, không là quyết định còn mở. ModelTest dùng nonce 1/key ngắn chỉ test record, không phải transaction hợp lệ; giữ nguyên test đó.

## 3. Canonical bytes, commitments và genesis - API-03

Ký hiệu **S(x)** = I32(byteLength(UTF8(x))) || UTF8(x); **B(bytes)** = I32(length) || bytes; I32/I64 big-endian signed Java int/long, length/count luôn không âm. Nối byte bằng ||; không có delimiter ngầm, JSON/Java serialization/charset mặc định. Mọi chuỗi encode không null, Unicode hợp lệ. Hash là `crypto.sha256(bytes)`, trả hex thường; đưa hash vào encoding bằng S(hex), không tự decode sang 32 byte.

Các encoder transaction/vote đang có giữ nguyên thứ tự, bỏ signature của chính đối tượng:

| Nội dung | Bytes chính xác |
| --- | --- |
| unsigned TX | S(chainId) || S(senderId) || S(key) || S(value) || I64(nonce) |
| unsigned Vote | S(chainId) || I64(height) || I32(round) || S(phase.name()) || S(blockHash) || S(validatorId) |
| signed TX | B(encodeTransaction(tx)) || B(tx.signature) |
| signed Vote | B(encodeVote(vote)) || B(vote.signature) |
| ordered transactions | S("TXS:v1") || I32(count) || signedTX[0] || ... || signedTX[count-1] |
| state | S("STATE:v1") || I32(dataCount) || từng S(key),S(value) || I32(nonceCount) || từng S(senderId),I64(nextNonce) |
| block identity | S("BLOCK:v1") || S(chainId) || I64(height) || S(parentHash) || S(transactionsHash) || S(stateHash) |
| virtual genesis identity | S("GENESIS:v1") || S(chainId) || S(emptyStateHash) |

State encode data và nonce theo **thứ tự từ điển của byte UTF-8 unsigned** riêng từng map; không phụ thuộc insertion order/locale. Commit cả data **và nonce**. Nonce map bỏ các entry 0 (tương đương sender chưa dùng), cấm số âm; State bình thường chỉ lưu nonce dương sau apply. Empty state có hai count 0. transactionsHash = H(ordered transactions), stateHash = H(state), blockHash = H(block identity). Empty block được phép; max 100 transaction/block.

**BlockHeader sau Phase 1** (constructor canonical, signature cuối):
```java
BlockHeader(String chainId, long height, int round, String parentHash,
            String stateHash, String proposerId, String transactionsHash,
            int validRound, List<Vote> prevoteProof, byte[] signature)
```

Header unsigned bytes **đã cập nhật trong source**:
`S(chainId)||I64(height)||I32(round)||S(parentHash)||S(stateHash)||S(proposerId)||S(transactionsHash)||I32(validRound)||I32(proofCount)||signedVote[0]||...`.
Không include chữ ký của chính header. Proof đúng thứ tự validatorId ASCII tăng dần; encoder giữ thứ tự list, validator reject list không canonical (không tự sort dữ liệu nhận rồi verify).

Payload Ed25519 giữ nguyên: `I32(domainUtf8.length)||domainUtf8||canonicalBytes`. Một header có cùng value nhưng round/proposer/proof khác phải ký lại. **blockHash bỏ round/proposer/validRound/proof/signature**, vì đây là metadata proposal, không phải value đồng thuận; block được đề xuất lại vẫn có cùng ID. Header cam kết ordered body bằng transactionsHash để không thể thay body giữ nguyên ID chỉ vì state cuối giống nhau.

**proposalId** = H(S("PROPOSAL:v1") || B(encodeHeader(header)) || B(header.signature)). Handshake/cache proposal dùng proposalId; vote/parent/lock dùng blockHash. Không dùng lẫn hai ID.

Constructor header cũ 7 tham số giữ để tương thích, đặt transactionsHash="", validRound=-1, proof=[]; nó **không tạo header hợp lệ giao thức**. Không cho phép validator tự lấp hash thiếu. Không dùng overload này cho proposal thật.

**Genesis:** không tạo Block/transaction giả; finalizedBlocks=[], finalizedHeight=0, FinalizedState.lastBlock=null, state=new State(). lastFinalizedHash là H(virtual genesis identity). Block đầu height=1, parentHash=genesisHash. Với h>1, parent là block finalized h-1, parentHash=blockHash(parent), parentState phải hash bằng parent.header.stateHash. Không ký/vote cho genesis.

Golden vectors (SHA-256, tính độc lập từ byte layout trên; chưa phải output của encoder Phase 2):

| Input | Expected hash |
| --- | --- |
| empty state | `513cb1c4fcbb5bc8d42d12c0a323d63e39580abbe85eed39701f3362f021e433` |
| empty transaction list | `30e8220286646b2e7aad3811a0cce77103a8e2614602d3e68c25a2eb3c852553` |
| virtual genesis chain lab01 | `34325ffd03d4fb8b907a7e02531b31c1c5bac5aa3281ee976c6535c4c2dfc36b` |
| block h=1, parent=genesis lab01, empty body/state | `b2c4e375ce9062e401bb1fd8aa7c7e474509f229d59ba045033ee95d57d839e1` |

**API encoder [Phase 2, TV1]**, giữ API đã có và thêm đúng:
```java
static byte[] encodeTransactions(List<Transaction> transactions);
static byte[] encodeState(Map<String,String> data, Map<String,Long> nonces);
static byte[] encodeBlockId(BlockHeader header);
static byte[] encodeGenesis(String chainId, String emptyStateHash);
static byte[] encodeProposalId(BlockHeader header);
```
Các method mới trả bytes đúng bảng, caller dùng CryptoService.sha256; không thêm sha256 khác vào từng module. Tất cả thuộc CanonicalEncoder. encodeHeader đã sửa để tránh bỏ sót trường mới khỏi chữ ký; các encoder mới trên chưa triển khai.

## 4. Proof, proposer và header/body - API-04, API-05, API-06

**Prevote certificate:** chính xác Q vote PREVOTE cùng chain/height/round/blockHash non-NIL, Q validator khác nhau, signature hợp lệ, list sort theo validatorId. Nếu biết >Q, chọn Q ID nhỏ nhất trong các vote đang biết; chứng cứ khác tập signer vẫn hợp lệ nếu đủ Q. Certificate không phụ thuộc thứ tự arrival để serialize.

Proposal mới: validRound=-1 và proof rỗng. Reproposal: 0<=validRound<header.round; proof là prevote certificate ở validRound cho **blockHash(header)**. validRound không tự chứng minh gì nếu thiếu proof. Proof sai/missing/duplicate signer bị reject trước ACK. Mọi phiếu trong proof cũng được đưa vào kho bằng chứng cục bộ sau verify, nhưng không được làm một validator tăng hai phiếu trong live tally.

**Proposer(h,r)** = validatorIds[(h-1+r) mod n], tính số học bằng long trước modulo. Header phải ký bằng khóa proposerId này. TV5 tạo và ký proposal theo yêu cầu của engine; engine của mọi node tự tính cùng schedule. Mỗi proposer đúng tạo tối đa 1 proposal cho (h,r).

Giữ NetworkMessage hiện có: (senderId, receiverId, type, height, Object payload). Object đã được khóa kiểu bằng bảng dưới; receiver reject type mismatch trước cast:

| MessageType | Kiểu payload chính xác | Liên kết và xử lý |
| --- | --- | --- |
| PROPOSAL_HEADER | BlockHeader | Kiểm tra header-only; cache theo proposalId và gửi ACK nếu hợp lệ |
| HEADER_ACCEPTED | BlockHeader | Echo nguyên header đã chấp nhận; xác minh proposalId với header sender đã gửi cho đúng peer |
| PROPOSAL_BODY | Block | Header trong Block phải khớp proposalId được receiver chấp nhận; kiểm tra transactionsHash rồi execute/validate |
| PREVOTE | Vote có phase PREVOTE | Envelope.height=vote.height; verify chữ ký original validator |
| PRECOMMIT | Vote có phase PRECOMMIT | Tương tự, phase phải đúng type |

Mọi envelope.height bằng payload header/vote height, sender/receiver là node có thật. Envelope sender là **peer chuyển tiếp**, không nhất thiết proposer/validator đã ký payload. Xác thực nội dung theo proposerId/validatorId và public keys, không theo sender relay. Simulator bảo đảm link origin: node chỉ gửi dưới ID của mình; Byzantine driver cũng không được giả envelope origin node khác. ACK là tín hiệu transport trên link này, không phải chứng cứ đồng thuận và không cần chữ ký mới; echo header vẫn giữ chữ ký proposer.

**Luồng bắt buộc:**

1. Sender gửi header cho từng peer (broadcast là fan-out theo chỉ số node tăng, không có receiver "*"). Receiver chạy BlockValidator.isHeaderValid. Chỉ checks cấu trúc/chain/height/parent/expected proposer/signature/format hashes/prevoteProof; chưa chạy transaction hoặc khẳng định stateHash đúng.
2. Receiver cache accepted proposalId và gửi HEADER_ACCEPTED echo header. Sau khi ACK được giao, network ghi quyền (bodySender, receiver, proposalId); sender mới gửi body cho peer đó. Mọi PROPOSAL_BODY không có quyền này bị drop/log HEADER_NOT_ACCEPTED. Node đích cũng kiểm tra lại accepted ID, không dựa riêng vào network.
3. Full body validation qua TV2; chỉ kết quả của validator cục bộ mới được truyền vào onProposal(block, valid). Cờ valid không bao giờ lấy từ payload/mạng. Thiếu body không được prevote non-NIL.
4. Header/body/vote lặp xử lý idempotent. Duplicate header hợp lệ vẫn gửi lại ACK để phục hồi body bị mất; duplicate body không apply/vote lần hai. ACK sai peer/ID bị reject.
5. Headers future-height chưa có parent: chỉ kiểm tra định dạng/chữ ký rồi buffer, **chưa ACK**; revalidate khi parent finalized. Với current-height/future-round có thể cache header/body đã validate, chưa vote cho tới round của mình hoặc quy tắc jump. Old-height đã finalized chỉ phục vụ duplicate/catch-up, không sửa ledger.

**Availability/catch-up (TV5):** mỗi node lưu bản sao header/body/certificate đã nhận hợp lệ cho toàn bộ run hữu hạn. Mỗi retransmitMillis, duyệt archive theo (height, round, proposalId), gửi lại header tới mọi peer; body vẫn đợi ACK. Gửi lại các signed vote đã xác minh theo (height,round,phase,validatorId,blockHash), giữ chữ ký gốc. Correct relay giúp thông điệp Byzantine gửi riêng vẫn tới mọi correct node. Node chậm nhận tuần tự header/body + precommit certificate và tự execute; không tải state của node khác. Không xóa archive khi engine sang height mới; không giới hạn replay chỉ ở current height làm node chậm kẹt vĩnh viễn.

## 5. Execution/ledger boundary - API-02, API-03, API-06, API-09

State giữ API hiện có: get, nextNonce, withUpdate, data, nonces, copy. withUpdate là primitive nội bộ được gọi **sau validation**, trả copy, không tự thêm prefix. data()/nonces() trả snapshot không sửa được, nhưng không có thứ tự canonical.

ExecutionEngine.execute(parentState, orderedTransactions) trả State mới; không mutate parentState/list/TX; invalid batch ném IllegalArgumentException. Không tự bỏ qua TX sai hoặc reorder. TransactionValidator dùng chain config, key owner, expected nonce, signature; null/format lỗi -> false.

BlockValidator bổ sung isHeaderValid đã có declaration trong source:
```java
boolean isHeaderValid(BlockHeader header, Block parent, State parentState);
boolean isValid(Block block, Block parent, State parentState);
```
TV2 giữ cả hai không side effect. Full validation = header validation + count<=100 + transactionsHash + execute + resulting stateHash. parent=null chỉ hợp lệ cho h=1/empty parentState/genesisHash. Current simulation height do Node chọn parent tương ứng; không validate trên global ledger. Validation header không kiểm tra local lock hoặc buộc header.round=local round; đó là việc TV4.

**Constructors [Phase 2, TV2]**:
```java
DefaultBlockValidator(String chainId, List<String> validatorIds,
                     Map<String,PublicKey> publicKeys, CryptoService crypto,
                     ExecutionEngine execution);
InMemoryBlockchain(String chainId, CryptoService crypto);
```
ValidatorIds phải đúng thứ tự cấu hình; header proof validation theo mục 4. Blockchain interface giữ nguyên 4 method. Ledger giữ list finalized riêng, trả immutable snapshot; genesis không trong list. appendFinalized chỉ được Node gọi sau verified commit proof, kiểm tra height nối tiếp/parent/ID; append lại cùng height/hash là no-op, khác hash ném IllegalStateException. Ledger không tự chứng minh finality bằng cách nhìn một Block (không có proof trong interface).

Node giữ candidate State theo blockHash, không append khi mới nhận proposal/PREVOTE quorum. FinalizedState giữ shape (lastBlock,state); chỉ genesis cho lastBlock=null; state copy. Khi commit h, Node kiểm tra proof Q PRECOMMIT cùng h/r/hash, revalidate block trên snapshot finalized h-1, lấy nextState; append ledger rồi thay FinalizedState trong **một callback single-thread không xen event**. Nếu validation/append lỗi, không thay state; không gọi startHeight mới. State/ledger không được expose cho node khác; shared immutable config/public keys được phép.

## 6. Consensus boundary và transitions - API-06, API-07

Chốt Tendermint-style theo lock/validValue; dùng certificate tường minh trong header để module không phải đoán validRound. Tham chiếu cơ chế khóa/giá trị có chứng cứ trong [paper gốc](https://arxiv.org/html/1807.04938v3); đây là hợp đồng Lab01 với deterministic scheduler và explicit proof, không tuyên bố là toàn bộ Tendermint production.

**API đã khóa/declaration đã có:**
```java
enum ConsensusEngine.Step { PROPOSE, PREVOTE, PRECOMMIT }
List<ConsensusAction> startHeight(long height);
List<ConsensusAction> onProposal(Block block, boolean valid);
List<ConsensusAction> onVote(Vote vote);
List<ConsensusAction> onTimeout(long height, int round, Step step);
ConsensusResult getResult();
```
Overload timeout 2 tham số cũ chỉ ánh xạ PROPOSE, deprecated; code mới luôn truyền step.

**Constructor [Phase 2, TV4]:**
```java
DefaultConsensusEngine(String nodeId, SimulationConfig config,
    List<String> validatorIds, Map<String,PublicKey> publicKeys,
    PrivateKey signingKey, CryptoService crypto, SimulationLogger logger);
```
signingKey chỉ là khóa local node, null cho observer; không đưa map private keys toàn mạng vào engine. Engine mọi node verify/count và có thể finalize; chỉ validator ký/broadcast vote. Observer không tạo RequestProposal hay timeout/action bỏ phiếu; vẫn theo dõi mọi round để commit. startHeight(1) sau khởi tạo; lần sau chỉ h=previousFinalizedHeight+1 sau Node commit. Trước start: result=(0,0,"NIL",false); sau start: (h,0,"NIL",false). Không gọi startHeight để tự reset lock ở cùng height.

Action hiện có đã mở rộng:
```java
BroadcastVote(Vote vote)
RequestProposal(long height, int round, String blockHash,
                int validRound, List<Vote> prevoteProof)
ScheduleTimeout(long height, int round, Step step, long delayMillis)
FinalizeBlock(Block block, List<Vote> commitProof)
```
Proof lists được copy. Overload FinalizeBlock(block) cũ có proof rỗng, **không đủ quyền commit**; overload timeout cũ chỉ cho tương thích. blockHash="NIL", validRound=-1, proof=[] trong RequestProposal nghĩa là tạo value mới. Trường hợp còn lại yêu cầu đúng candidate đã cache và certificate kèm; Node không được thay transactions/stateHash/parent/height khi repropose, chỉ thay round/proposer/proof/signature.

Engine không gọi network/ledger/clock thật. Mọi callback trả list action không null, không side effect bên ngoài. TV5 thực thi action theo thứ tự list, không gọi reentrant vào engine khi đang xử lý callback. Engine đã ghi nhận/đếm vote của chính mình trước khi trả BroadcastVote; chỉ fan-out đến peer, không gọi onVote lại cho self.

### 6.1. State và thứ tự xử lý

Mỗi height giữ round, step, lockedHash=NIL/lockedRound=-1, validHash=NIL/validRound=-1, candidate body hợp lệ, certificates, vote map và các cờ đã ký/đã đặt timer. Giữ lock/validValue qua round; reset chỉ khi height đã finalized. getResult trước commit trả (h,currentRound,"NIL",false); sau commit trả (h,commitRound,hash,true) cho tới startHeight kế tiếp.

Vote key=(height,round,phase,validatorId). Xác minh trước dedup. Vote giống nhau no-op; equivocation lưu evidence/log nhưng live tally giữ phiếu hợp lệ đầu tiên, không cộng phiếu thứ hai hoặc trừ phiếu đã đếm. Verify certificate độc lập theo Q signer khác nhau trong chính certificate; không cộng chứng cứ đó như phiếu thứ hai vào live tally, không thu hồi certificate đã verify chỉ vì thấy equivocation sau đó.

Sau mỗi callback, xử lý các điều kiện đến khi ổn định theo ưu tiên: (1) finalize đã đủ proof+body, (2) round jump hợp lệ, (3) cập nhật valid certificate, (4) PREVOTE/PRECOMMIT transition, (5) đặt timer chưa đặt. Khi nhiều round có commit proof, chọn round nhỏ nhất; nếu phát hiện 2 certified hash khác nhau cùng height thì fail invariant, không âm thầm chọn một. Một transition/action không được lặp lại khi callback duplicate.

### 6.2. Transitions bắt buộc

| Trigger | Điều kiện / thay đổi / output |
| --- | --- |
| startHeight / vào round r | step=PROPOSE; giữ lock/validValue nếu cùng height; ScheduleTimeout(PROPOSE,T(r)). Nếu local proposer: RequestProposal(validHash,validRound,proof) hoặc NIL/-1/[] nếu chưa có validValue |
| Proposal current round | Engine tự verify chain/height/proposer/header signature/proof; caller valid chỉ xác nhận TV2 full execution. Ở PROPOSE, prevote hash khi valid và (chưa lock, cùng lockedHash, hoặc proof cho hash khác có lockedRound<validRound<r); còn lại prevote NIL. Ký một lần rồi step=PREVOTE |
| Timeout PROPOSE | Đúng h/r/step, chưa prevote -> PREVOTE NIL, step=PREVOTE. Không xóa lock |
| Q PREVOTE bất kỳ value, cùng current round | Khi step=PREVOTE, đặt PREVOTE timer T(r) đúng một lần, kể cả phiếu bị chia giữa NIL/các hash |
| Q PREVOTE hash B non-NIL + full valid body | Cập nhật validHash/validRound/proof nếu round mới hơn; lock B ở r nếu r>lockedRound, kể cả certificate đến trễ sau PRECOMMIT NIL. Nếu đang PREVOTE tại round đó và chưa precommit thì phát PRECOMMIT B, step=PRECOMMIT. Nếu đã precommit thì không ký lại |
| Q PREVOTE NIL hoặc timeout PREVOTE | Khi step=PREVOTE -> PRECOMMIT NIL, step=PRECOMMIT; **giữ lock**. NIL quorum không cho unlock block khác |
| Q PRECOMMIT bất kỳ value cùng current round | Đặt PRECOMMIT timer T(r) đúng một lần. Không tự finalize khi chỉ tổng phiếu đủ mà hash bị chia |
| Q PRECOMMIT cùng B non-NIL | Chỉ FinalizeBlock khi có full valid body/proposal ở commitRound và Q đúng chain/h/r/hash; chấp nhận commitRound cũ hơn currentRound. Thiếu body thì cache proof, đợi body qua handshake; không tạo state giả |
| Timeout PRECOMMIT | Đúng h/r chưa finalized -> vào r+1, giữ lock và validValue. Kể cả Q NIL vẫn chờ timer, không nhảy round ngay |
| f+1 validator khác nhau có signed vote ở cùng round r'>current | Jump tới r' lớn nhất đủ điều kiện ở current height, khởi tạo round đó và xử lý cache. Đếm signer một lần giữa hai phase; một Byzantine/future header không đủ gây jump |
| Old/future/invalid event | Sai chain/signature/membership/format reject. Old-height không làm engine đổi state; old-round vote vẫn lưu để xác minh proof/commit. Future-height do Node buffer, không trực tiếp đưa vào engine |

Header r sai format/proposer/signature/proof bị reject, không ép PREVOTE NIL chỉ vì attacker gửi rác. Với header hợp lệ và full block invalid, onProposal(...,false) có thể phát NIL khi đang PROPOSE. First eligible proposal cho (h,r) điều khiển lần bỏ phiếu đó; proposer equivocation chỉ tạo evidence, không ký lại. Đến body/proof trễ phải kiểm tra lại điều kiện đã buffer, không chỉ xét message vừa đến. Certificate trễ chỉ nâng validRound/lockedRound, không hạ chúng và không ký vote cho round cũ. Cùng round hai certificate hash khác nhau là invariant error. Khóa khi quan sát QC hợp lệ, kể cả sau khi đã precommit NIL, là lựa chọn tuân thủ quy tắc lock của Lab01; không được tự đổi thành unlock khi timeout/NIL hoặc ký precommit lần hai.

T(r)=roundTimeoutMillis*(r+1) cho cả 3 phase, nhân exact; mỗi loại timer tối đa 1 lần/(h,r). Overflow là run error, không clamp hoặc quay về số âm. Timer sai height/round hoặc đã finalized -> no action, log STALE_TIMEOUT. PROPOSE/PREVOTE còn phải khớp step hiện tại; riêng PRECOMMIT timer đúng h/r thúc round khi chưa finalized, kể cả local step chưa tới PRECOMMIT do nhận dữ liệu khác thứ tự. Không reset timer vì duplicate. Safety phải độc lập thời gian; liveness chỉ nghiệm thu sau GST với delivery/rate đúng contract.

## 7. Network, scheduler và config - API-08

Giữ interface NetworkSimulator: send(message), poll(), currentTime(), advanceTime(milliseconds). Toàn bộ dùng **virtual milliseconds long**, bắt đầu 0. advanceTime(x>=0) tăng exact; x<0/overflow -> IllegalArgumentException; không sleep hoặc đọc wall clock. send không gọi callback trực tiếp. poll loại và trả toàn bộ delivery đang due tại currentTime theo (deliverAt,enqueueSequence); poll lần hai không trả lại cùng delivery, trừ bản duplicate đã được tạo thành event riêng.

**[Phase 2, TV3]** tạo đúng:
```java
NetworkSettings(long stabilizationTimeMillis, long deltaMillis,
    long preSyncMaxDelayMillis, int dropPercent, int duplicatePercent,
    int outboundMessagesPerWindow, long windowMillis,
    int peerDistinctMessageLimit, long blockDurationMillis)
DefaultNetworkSimulator(NetworkSettings settings, long seed, List<String> nodeIds,
                        SimulationLogger logger)
```
NetworkSettings là record trong blockchain.network. Defaults theo thứ tự: (0,200,500,0,0,4096,1000,8192,1000). Tất cả time>=0, delta/window/blockDuration>0, percentages 0..100, rate/peerLimit>0. nodeIds theo chỉ số và không trùng. Constructor lưu copy, không tự sinh ID/khóa.

Thứ tự send: kiểm tra schema/peer/height/quyền ACK -> outbound quota -> RNG/enqueue. Reject hoặc hết quota không rút RNG; attempt qua quota vẫn tiêu thụ quota nếu sau đó random drop. Trước GST, rút d=nextInt(100), u=nextInt(100), delay=nextLong(preSyncMaxDelayMillis+1), luôn cả ba. Nếu d<dropPercent thì drop, không rút thêm; nếu không, enqueue bản gốc, và chỉ khi u<duplicatePercent mới rút delay thứ hai độc lập/enqueue duplicate. Tối đa 2 copies/send. Sau GST không drop/duplicate ngẫu nhiên, chỉ rút delay=1+nextLong(deltaMillis). Dùng java.util.Random(seed) một instance riêng cho network. Resequence theo deliverAt/sequence; không shuffle bằng map iteration. Bounds RNG phải không overflow. Inbound peer blocking áp dụng khi delivery đến hạn, không rút RNG mới. Expiry/GST unblock xử lý tại advanceTime trước poll, log tại currentTime khi quan sát hết hạn (details chứa scheduledExpiry); duyệt maintenance theo (scheduledExpiry,senderIndex,receiverIndex). Với runner tick 1 ms thì expiry được quan sát đúng ms đó.

Rate tính **logical outbound message** mỗi node trong fixed window [k*window,(k+1)*window), gồm retransmit, không tính network-created duplicate; quá hạn mức drop/log RATE_LIMIT. Receiver đếm distinct message ID/peer/window, vượt peerLimit thì block peer đến now+blockDuration, drop/log BLOCK; duplicates không tăng bộ đếm. Đến expiry tự log UNBLOCK; GST giải phóng block cũ để fault trước synchrony không giữ correct peer kẹt. Scenario hợp lệ phải cấp đủ rate/peerLimit cho correct traffic sau GST; runner assert không RATE_LIMIT/BLOCK/DROP giữa correct nodes sau GST và mỗi send đó delivered<=sendTime+deltaMillis. Vi phạm là cấu hình/test FAIL, không được tuyên bố liveness. Byzantine flood vẫn bị hạn mức riêng.

Message ID = H(S(type.name())||S(senderId)||S(receiverId)||I64(height)||B(payloadBytes)). Với header/ACK payloadBytes=B(encodeHeader)||B(signature); vote là signedVote; body=B(encodeHeader)||B(signature)||encodeTransactions. Nhận dạng distinct không dùng Object.hashCode. send sai type/peer/height/ACK guard bị reject/log, không tạo delivery. Bản body duplicate do network tạo dùng quyền ACK của send gốc.

TV5 là người duy nhất tiến clock. Scheduler tick 1 ms: tại t, network.poll và xử lý messages theo queue order; drain action FIFO và delivery mới due tới khi rỗng; tiếp đó timeout due theo (dueTime,nodeIndex,height,round,step ordinal,insertionSequence); cuối cùng retransmit tick theo nodeIndex, archive order. Delivery ưu tiên timeout khi cùng timestamp. Newly generated delivery có delay ít nhất 1 ms sau GST; pre-GST delay 0 phải drain đến ổn định, chỉ relay mỗi distinct payload một lần trong cùng tick để tránh vòng lặp 0-time. Sequence counter bắt đầu 0 mỗi run, tăng ở enqueue; không dùng thread hoặc unordered parallel stream.

**Config [Phase 2, TV5]:** Java Properties qua UTF-8 Reader, không dùng load(InputStream) mặc định charset. Giữ SimulationConfig 5 trường/defaultConfig; Main truyền giá trị thật, không hardcode 8/4. File config/lab01.properties mặc định phải tồn tại; từng khóa bảng dưới có thể bỏ và dùng default đã ghi. Khóa không biết/sai kiểu bị reject trước run. Scenario file cần tx.count và đủ 4 trường của mỗi tx.i; các danh sách fault bỏ qua nghĩa là rỗng. Fixture cần fixtureVersion=1 và key pair đủ ID; không có default key.

| Nhóm khóa | Giá trị default / owner |
| --- | --- |
| chainId, nodeCount, validatorCount, seed, roundTimeoutMillis | lab01, 8, 4, 42, 1000 / SimulationConfig, TV5 |
| network.stabilizationTimeMillis, network.deltaMillis, network.preSyncMaxDelayMillis | 0, 200, 500 / NetworkSettings, TV3 |
| network.dropPercent, network.duplicatePercent | 0, 0; chỉ pre-GST |
| network.outboundMessagesPerWindow, network.windowMillis | 4096, 1000 |
| network.peerDistinctMessageLimit, network.blockDurationMillis | 8192, 1000 |
| targetHeight, maxSimulationTimeMillis, retransmitMillis | 3, 60000, 100 / TV5; số dương |
| identitiesFile, scenario | config/test-identities.properties, T1 / TV5; scenario T1..T8 |

Main --config <path> --scenario <T1..T8|ALL>; chưa có CLI này trong source. File là input đã lưu, override scenario là CLI override duy nhất. Seed chỉ ảnh hưởng network; danh sách TX/fault script cố định theo scenario và nằm trong config/test inputs đã version. Fault script phát sự kiện theo (time,scriptIndex), không dùng RNG thứ hai không đặc tả; fault driver chỉ điều khiển node thuộc tập Byzantine <=f cố định của run.

## 8. Node isolation, action handling và finality - API-09

**[Phase 2, TV5]** API wiring đã khóa, giữ constructor/getters hiện có cho demo:
```java
Node(String id, boolean validator, SimulationConfig config,
     List<String> validatorIds, Map<String,PublicKey> publicKeys,
     PrivateKey signingKey, CryptoService crypto, Blockchain ledger, State initialState,
     ExecutionEngine execution, BlockValidator blockValidator,
     ConsensusEngine consensus, NetworkSimulator network, SimulationLogger logger);
boolean Node.submitTransaction(Transaction transaction);
void Node.onMessage(NetworkMessage message);
void Node.onTimeout(long height, int round, ConsensusEngine.Step step);
FinalizedState Node.finalizedState();
SimulationRunner(SimulationConfig config);
SimulationRunner(SimulationConfig config, NetworkSettings networkSettings,
    Path identitiesFile, Path logFile, long targetHeight,
    long maxSimulationTimeMillis, long retransmitMillis, String scenario);
List<Node> SimulationRunner.createNodes(); // config counts, không hardcode
Map<String,FinalizedState> SimulationRunner.run();
```
Các method mới/overload chưa tồn tại; TV5 triển khai. signingKey là khóa local node, observer không dùng để bỏ phiếu; không đưa khóa vào NetworkMessage. Node.submitTransaction trả true cho TX mới có format/chain/owner/signature đúng và nonce>=nextNonce hiện tại; false cho duplicate/invalid/stale nonce. TX nonce tương lai đợi trong mempool, không apply ngay; ID dedup=H(S("TXID:v1")||signedTX). TV5 được thêm private helper, không đổi public contract. Constructor Node dùng ledger riêng rỗng, initialState.copy(), engine riêng; cùng một network transport phục vụ mọi node nhưng không giữ state/ledger/consensus của node. Không share map pending, locks, nonce hoặc finalized pointer. Shared logger là append-only sink, không là nơi lưu quyết định consensus.

Runner constructor không tham số cũ gọi SimulationConfig.defaultConfig(); overload chỉ config dùng network defaults, fixture path và target/deadline/retransmit mặc định mục 7, scenario T1, logFile=logs/T1.tsv. Constructor đầy đủ là đường CLI/E2E để không phải ngầm đọc config toàn cục. createNodes tạo graph đúng một lần mỗi runner, gọi lại trả cùng snapshot list, không tạo thêm engine hoặc đọc lại fixture; run chỉ gọi được một lần. Kết quả run là snapshot tất cả node theo nodeIndex, immutable map và FinalizedState copy; timeout/invariant/I/O lỗi làm run fail, không trả kết quả thành công thiếu height. Harness xác định correct nodes từ scenario để assert, không lọc giấu Byzantine trong output.

Action handling:
- RequestProposal: TV5 lấy candidate đúng blockHash trong cache, hoặc tạo mới từ mempool local. New value chọn tối đa 100 TX theo (senderId ASCII,nonce,key UTF-8 unsigned); loại TX không còn đúng nonce trên tentative copy; reject/log invalid TX, không đưa vào block. Mempool input TX ký sẵn được nạp độc lập cho từng node từ scenario input. Cho phép empty proposal để không kẹt khi mempool rỗng.
- Reproposal giữ nguyên ordered TX/parent/state/height/transactionsHash, ký header round/proposer/proof mới; không tạo value khác dưới cùng blockHash. Đưa proposal local vào đường full validation/onProposal như peer (không cần network self-ACK); fan-out header tới peers.
- BroadcastVote: fan-out signed vote tới tất cả peer, kể cả non-validator; archive để retransmit.
- ScheduleTimeout: lưu tuple h/r/step và due=now+delay, không sleep; không reset vì cùng action duplicate.
- FinalizeBlock: validate certificate và thực hiện commit atomic mục 5, loại TX đã apply khỏi mempool; snapshot/log FINALIZED. Lưu block/proof archive trước startHeight(h+1); không start height mới nếu đạt targetHeight, nhưng vẫn phục vụ retransmit đến khi mọi correct node đạt target hoặc deadline.
- Observer nhận vote/body, tự verify certificate và execute như validator, chỉ không ký/đề xuất. Runner không gọi append thay cho observer hoặc đọc state node khác để copy.

Finalization duplicate cùng h/hash là no-op (không execute lần nữa); khác hash là invariant error. Node chậm buffer future-height theo receipt sequence, sau commit drain lần lượt những message giờ có parent, không áp dụng đảo height. Runner chỉ đọc snapshots để assert convergence, không dùng kết quả so sánh này điều khiển việc chọn/chốt block.

## 9. Identity fixtures và deterministic logging - API-11

**Quyết định:** khóa cho simulator là **fixture cố định được version cùng test input**, không sinh lại từ seed và không gọi generateKeyPair trong run cần T8. Fixture chỉ dành cho lab/test, không dùng cho tài khoản thật.

**[Phase 2, TV1]** `IdentityFixtures.load(Path path): Map<String,KeyPair>` trong blockchain.crypto. Dùng UTF-8 Properties:
```properties
fixtureVersion=1
node-0.public=<Base64 X.509 Ed25519>
node-0.private=<Base64 PKCS#8 Ed25519>
# ... node-1 đến node-(nodeCount-1); Alice, Bob nếu scenario dùng
```
Tạo file một lần bằng SecureRandom qua generateKeyPair, lưu đủ public/private cho từng ID cần dùng; lần sau chỉ load bằng KeyFactory("Ed25519"), không regenerate/fallback. Thiếu key, key sai cặp/algorithm hoặc trùng public key cho hai ID -> fail trước run. TV1 giao chính file cố định và checksum SHA-256; TV5 kiểm tra node/account IDs đủ, đưa public-key snapshots cố định vào modules, chỉ inject private key tương ứng node. Fixture plaintext là test data có chủ đích trong .properties (không đổi .gitignore để commit khóa thật). Secret test fixture không log; log fixture SHA-256. Fixture được nạp xong trước start node, không cần runtime key-generation determinism.

Contract khóa không yêu cầu TV1 cung cấp sẵn toàn bộ implementation trong Phase 1: hiện **chưa có file fixture/loader**, được ghi rõ trong TASKS. Những AI còn lại dùng API/load path này, không tự sinh map khóa khác. Ed25519 ký cùng bytes/cùng key phải cho cùng signature; seed mô phỏng không là entropy mật mã.

**[Phase 2, TV5]** tạo blockchain.simulator.SimulationLogger với constructor `(LongSupplier virtualTime, Path logFile)` và method `void log(String nodeId, String eventType, long height, int round, Map<String,String> details)`. Logger tự lấy virtualTime và cấp logSequence; TV3/TV4 chỉ gọi sink này cho event nội bộ, không tự mở file hoặc dùng clock thật. Runner sở hữu biến virtualTime ban đầu 0, cập nhật trước advanceTime mỗi tick để logger/network luôn cùng timestamp. Đây là callback đã khóa cho owner, class chưa được tạo trong Phase 1.

Log UTF-8 không BOM, newline LF, 7 cột TSV:
`virtualTimeMs<TAB>logSequence<TAB>nodeId<TAB>eventType<TAB>height<TAB>round<TAB>details<LF>`.
Sequence toàn run bắt đầu 0, tăng theo callback deterministic. Global event nodeId="-", không có h/r dùng 0/-1. details là key=value sort key ASCII, nối bằng dấu &, percent-escape UTF-8 mọi byte ngoài [A-Za-z0-9_.-]; không ghi wall clock, absolute path, map.toString, object identity hay stack trace. Run header log config giá trị đã sort, scenario, seed, fixture hash và SHA-256 của base config/scenario input bytes, không dùng timestamp metadata file.

Các event tối thiểu SEND/DELIVER/DROP/DELAY/DUPLICATE/RATE_LIMIT/BLOCK/UNBLOCK, HEADER_ACCEPT/REJECT, PROPOSAL, PREVOTE/PRECOMMIT, LOCK, ROUND_CHANGE, TIMEOUT/STALE_TIMEOUT, FINALIZED. Rejection reason ổn định: MALFORMED, CHAIN_MISMATCH, UNKNOWN_SIGNER, BAD_SIGNATURE, BAD_NONCE, NOT_OWNER, BAD_PARENT, BAD_PROPOSER, BAD_TX_HASH, BAD_STATE_HASH, BAD_PROOF, DUPLICATE, EQUIVOCATION, HEADER_NOT_ACCEPTED. boolean validator giữ chữ ký hiện có; Node log REJECT/INVALID_BLOCK hoặc INVALID_TRANSACTION nếu chưa phân loại sâu được, không invent thành công. Không cần enum rejection dùng chung ở Phase 1 (API-10 P1).

T8 chạy **hai instance simulation mới** với cùng config + scenario bytes + fixture bytes, reset RNG/clock/sequence/queues/state; so log bằng Files.mismatch==-1 và final state hash của từng correct node (không chỉ node-0). Nếu deadline/max round/overflow hoặc thiếu fixture thì FAIL, không in PASS. Khác fixture là khác input, không phải cùng config đã định nghĩa.

## 10. Ma trận P0 và acceptance contract

**Đã đóng toàn bộ 10 P0 ở mức thiết kế**; implementation/acceptance chưa hoàn thành không được đổi nhãn thành "blockchain đã chạy".

| ID | Quyết định khóa | Acceptance phải triển khai |
| --- | --- | --- |
| API-01 | Inject chainId; domain từ config, overload lab01 legacy | Chạy chain lab02 ký/execute đúng, reject lab01; TX/HEADER/VOTE không dùng chéo |
| API-02 | Full owned key, nonce bắt đầu 0, batch atomic | Nonce 0 rồi 1; replay sai, cross-owner sai; state gốc giữ nguyên khi batch lỗi |
| API-03 | Canonical maps+nonce/body commitment, stable blockHash, virtual genesis | Golden bytes/hashes; map order không ảnh hưởng; reorder body đổi transactionsHash; reprop đổi proposalId nhưng giữ blockHash |
| API-04 | NIL literal, không null, proof Q signer canonical | Encode NIL được, không NIL finality; validRound thiếu/sai proof bị reject |
| API-05 | Typed payload table, echo-header ACK, proposalId handshake | Wrong type/peer/height/ACK rejected; mất ACK/body phục hồi bằng retry; body trước ACK bị drop |
| API-06 | Fixed registry/ordered proposers, local validation và signed QC | Sai proposer/signer/domain rejected; non-validator không có vote weight; valid flag mạng không được tin |
| API-07 | Step timers/startHeight/RequestProposal/commitProof và transitions mục 6 | Unit trace quorum/locks/late votes/jump/stale timers; T6/T7 safety và eventual progress |
| API-08 | Config counts, ms clock/RNG/event order/rate/GST | Counts tùy config; tie-break replay giống; Delta assertion sau GST; không timer wall clock |
| API-09 | Ledger/state/engine riêng, commit atomic sau QC, catch-up có body | 8 node không share mutable state; observer tự validate; duplicate commit no-op; slow node hội tụ |
| API-11 | Fixed versioned test keys, full input checksum, log TSV | Rerun không generateKeyPair; byte-identical logs + final hashes; missing/mismatched key fail rõ |

API-10 (defensive copy toàn bộ signature/payload, rejection detail) và API-12 (unit/E2E đầy đủ) còn P1. Contract đã buộc dữ liệu invalid bị reject và archive snapshot; việc hiện thực copy/guard/tests vẫn thuộc chủ module. Không tuyên bố P1 hoàn thành.

## 11. Kiểm chứng Phase 1 và giới hạn

Giữ các test cũ. Chỉ thêm HeaderSigningContractTest để chứng minh chữ ký header bao phủ transactionsHash, round, validRound, chứng cứ prevote và vẫn verify khi gắn signature vào record. Không viết engine/network/ledger/hash algorithm trong lượt này; encoder header chỉ cập nhật theo model để không ký thiếu metadata.

Chạy `./mvnw clean test` qua Git Bash; kết quả lượt này ghi trong TASKS mục 6. Mọi signature cũ của header không có metadata v1 cần ký lại; không hứa wire compatibility giữa encoder cũ và v1. Constructor deprecated chỉ giữ source compatibility cho record tests/demo.

Điều kiện bàn giao Phase 1: decisions và chữ ký trên là chuẩn; owner thực hiện đúng migration [Phase 2] đã giao. Những stub/fixture/helper ghi Phase 2 chưa có, vì người dùng ưu tiên tài liệu và không yêu cầu triển khai thuật toán. Mỗi AI có đủ chữ ký/hành vi/tests để làm phần mình, nhưng runtime chỉ tích hợp sau khi các implementation phụ thuộc được merge. Không dùng build xanh hiện tại thay bằng chứng T1-T8.
