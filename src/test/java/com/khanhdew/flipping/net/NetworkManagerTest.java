package com.khanhdew.flipping.net;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class NetworkManagerTest {

    private NetworkManager hostManager;
    private NetworkManager clientManager;

    @AfterEach
    void tearDown() {
        if (hostManager != null) hostManager.disconnect();
        if (clientManager != null) clientManager.disconnect();
    }

    @Test
    void testHostAndClientHandshake() throws InterruptedException {
        int testPort = 55556;
        hostManager = new NetworkManager();
        clientManager = new NetworkManager();

        CountDownLatch hostConnectedLatch = new CountDownLatch(1);
        CountDownLatch clientConnectedLatch = new CountDownLatch(1);

        AtomicReference<String> clientOpponent = new AtomicReference<>();
        AtomicInteger clientRows = new AtomicInteger();
        AtomicInteger clientCols = new AtomicInteger();
        AtomicInteger clientPlayerId = new AtomicInteger();

        AtomicReference<String> hostOpponent = new AtomicReference<>();
        AtomicInteger hostPlayerId = new AtomicInteger();

        NetworkEventListener hostListener = new NetworkEventListener() {
            @Override
            public void onConnected(String opponentName, int rows, int cols, int assignedPlayerId) {
                hostOpponent.set(opponentName);
                hostPlayerId.set(assignedPlayerId);
                hostConnectedLatch.countDown();
            }
            @Override public void onMoveReceived(int row, int col) {}
            @Override public void onChatReceived(String sender, String message) {}
            @Override public void onRestartRequested() {}
            @Override public void onRestartAccepted() {}
            @Override public void onRestartDeclined() {}
            @Override public void onOpponentSurrendered() {}
            @Override public void onDisconnected(String reason) {}
            @Override public void onError(String errorMessage) {}
        };

        NetworkEventListener clientListener = new NetworkEventListener() {
            @Override
            public void onConnected(String opponentName, int rows, int cols, int assignedPlayerId) {
                clientOpponent.set(opponentName);
                clientRows.set(rows);
                clientCols.set(cols);
                clientPlayerId.set(assignedPlayerId);
                clientConnectedLatch.countDown();
            }
            @Override public void onMoveReceived(int row, int col) {}
            @Override public void onChatReceived(String sender, String message) {}
            @Override public void onRestartRequested() {}
            @Override public void onRestartAccepted() {}
            @Override public void onRestartDeclined() {}
            @Override public void onOpponentSurrendered() {}
            @Override public void onDisconnected(String reason) {}
            @Override public void onError(String errorMessage) {}
        };

        hostManager.startHost(testPort, "Alice", 8, 8, hostListener);
        Thread.sleep(200); // brief pause for host socket binding
        clientManager.connectToHost("127.0.0.1", testPort, "Bob", clientListener);

        boolean clientOk = clientConnectedLatch.await(5, TimeUnit.SECONDS);
        boolean hostOk = hostConnectedLatch.await(5, TimeUnit.SECONDS);

        assertTrue(clientOk, "Client should receive handshake");
        assertTrue(hostOk, "Host should receive handshake ack");

        assertEquals("Alice", clientOpponent.get());
        assertEquals(8, clientRows.get());
        assertEquals(8, clientCols.get());
        assertEquals(2, clientPlayerId.get());

        assertEquals("Bob", hostOpponent.get());
        assertEquals(1, hostPlayerId.get());

        assertEquals(NetworkRole.HOST, hostManager.getRole());
        assertEquals(NetworkRole.CLIENT, clientManager.getRole());
        assertTrue(hostManager.isConnected());
        assertTrue(clientManager.isConnected());
    }

    @Test
    void testMessageExchange() throws InterruptedException {
        int testPort = 55557;
        hostManager = new NetworkManager();
        clientManager = new NetworkManager();

        CountDownLatch hostConnectedLatch = new CountDownLatch(1);
        CountDownLatch moveLatch = new CountDownLatch(1);
        CountDownLatch chatLatch = new CountDownLatch(1);
        CountDownLatch restartReqLatch = new CountDownLatch(1);
        CountDownLatch restartAckLatch = new CountDownLatch(1);
        CountDownLatch surrenderLatch = new CountDownLatch(1);

        AtomicInteger receivedRow = new AtomicInteger(-1);
        AtomicInteger receivedCol = new AtomicInteger(-1);
        AtomicReference<String> chatSender = new AtomicReference<>();
        AtomicReference<String> chatMsg = new AtomicReference<>();

        NetworkEventListener hostListener = new NetworkEventListener() {
            @Override public void onConnected(String opponentName, int rows, int cols, int assignedPlayerId) { hostConnectedLatch.countDown(); }
            @Override public void onMoveReceived(int row, int col) {
                receivedRow.set(row);
                receivedCol.set(col);
                moveLatch.countDown();
            }
            @Override public void onChatReceived(String sender, String message) {
                chatSender.set(sender);
                chatMsg.set(message);
                chatLatch.countDown();
            }
            @Override public void onRestartRequested() { restartReqLatch.countDown(); }
            @Override public void onRestartAccepted() { restartAckLatch.countDown(); }
            @Override public void onRestartDeclined() {}
            @Override public void onOpponentSurrendered() { surrenderLatch.countDown(); }
            @Override public void onDisconnected(String reason) {}
            @Override public void onError(String errorMessage) {}
        };

        NetworkEventListener clientListener = new NetworkEventListener() {
            @Override public void onConnected(String opponentName, int rows, int cols, int assignedPlayerId) {}
            @Override public void onMoveReceived(int row, int col) {}
            @Override public void onChatReceived(String sender, String message) {}
            @Override public void onRestartRequested() {}
            @Override public void onRestartAccepted() {}
            @Override public void onRestartDeclined() {}
            @Override public void onOpponentSurrendered() {}
            @Override public void onDisconnected(String reason) {}
            @Override public void onError(String errorMessage) {}
        };

        hostManager.startHost(testPort, "HostUser", 8, 8, hostListener);
        Thread.sleep(200);
        clientManager.connectToHost("127.0.0.1", testPort, "ClientUser", clientListener);

        assertTrue(hostConnectedLatch.await(5, TimeUnit.SECONDS));

        clientManager.sendMove(3, 4);
        assertTrue(moveLatch.await(3, TimeUnit.SECONDS));
        assertEquals(3, receivedRow.get());
        assertEquals(4, receivedCol.get());

        clientManager.sendChat("Hello host!");
        assertTrue(chatLatch.await(3, TimeUnit.SECONDS));
        assertEquals("ClientUser", chatSender.get());
        assertEquals("Hello host!", chatMsg.get());

        clientManager.sendRestartReq();
        assertTrue(restartReqLatch.await(3, TimeUnit.SECONDS));

        clientManager.sendRestartAck();
        assertTrue(restartAckLatch.await(3, TimeUnit.SECONDS));

        clientManager.sendSurrender();
        assertTrue(surrenderLatch.await(3, TimeUnit.SECONDS));
    }

    @Test
    void testDisconnection() throws InterruptedException {
        int testPort = 55558;
        hostManager = new NetworkManager();
        clientManager = new NetworkManager();

        CountDownLatch clientConnectedLatch = new CountDownLatch(1);
        CountDownLatch disconnectLatch = new CountDownLatch(1);

        NetworkEventListener hostListener = new NetworkEventListener() {
            @Override public void onConnected(String opponentName, int rows, int cols, int assignedPlayerId) {}
            @Override public void onMoveReceived(int row, int col) {}
            @Override public void onChatReceived(String sender, String message) {}
            @Override public void onRestartRequested() {}
            @Override public void onRestartAccepted() {}
            @Override public void onRestartDeclined() {}
            @Override public void onOpponentSurrendered() {}
            @Override public void onDisconnected(String reason) { disconnectLatch.countDown(); }
            @Override public void onError(String errorMessage) {}
        };

        NetworkEventListener clientListener = new NetworkEventListener() {
            @Override public void onConnected(String opponentName, int rows, int cols, int assignedPlayerId) { clientConnectedLatch.countDown(); }
            @Override public void onMoveReceived(int row, int col) {}
            @Override public void onChatReceived(String sender, String message) {}
            @Override public void onRestartRequested() {}
            @Override public void onRestartAccepted() {}
            @Override public void onRestartDeclined() {}
            @Override public void onOpponentSurrendered() {}
            @Override public void onDisconnected(String reason) {}
            @Override public void onError(String errorMessage) {}
        };

        hostManager.startHost(testPort, "Host", 6, 6, hostListener);
        Thread.sleep(200);
        clientManager.connectToHost("127.0.0.1", testPort, "Client", clientListener);

        assertTrue(clientConnectedLatch.await(5, TimeUnit.SECONDS));

        clientManager.disconnect();
        assertTrue(disconnectLatch.await(3, TimeUnit.SECONDS));

        assertEquals(NetworkRole.NONE, clientManager.getRole());
        assertFalse(clientManager.isConnected());
    }

    @Test
    void testGetLocalIpAddress() {
        String ip = NetworkManager.getLocalIpAddress();
        assertNotNull(ip);
        assertFalse(ip.isEmpty());
    }
}
