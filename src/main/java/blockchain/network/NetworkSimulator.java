package blockchain.network;

import blockchain.model.NetworkMessage;
import java.util.List;

public interface NetworkSimulator {

    // Đưa message vào mạng mô phỏng
    void send(NetworkMessage message);

    // Lấy các message đã đến hạn giao
    List<NetworkMessage> poll();

    // Thời gian mô phỏng hiện tại
    long currentTime();

    // Tiến đồng hồ mô phỏng
    void advanceTime(long milliseconds);
}