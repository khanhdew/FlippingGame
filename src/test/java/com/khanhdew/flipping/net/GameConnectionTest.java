package com.khanhdew.flipping.net;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class GameConnectionTest {

    private ServerSocket serverSocket;
    private Socket hostSocket;
    private Socket clientSocket;
    private GameConnection hostConn;
    private GameConnection clientConn;

    @BeforeEach
    void setUp() throws IOException {
        serverSocket = new ServerSocket(0); // auto-assign free port
        int port = serverSocket.getLocalPort();

        Thread acceptThread = new Thread(() -> {
            try {
                hostSocket = serverSocket.accept();
            } catch (IOException ignored) {}
        });
        acceptThread.start();

        clientSocket = new Socket("127.0.0.1", port);
        try {
            acceptThread.join(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @AfterEach
    void tearDown() {
        if (hostConn != null) hostConn.close();
        if (clientConn != null) clientConn.close();
        try {
            if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
        } catch (IOException ignored) {}
    }

    @Test
    void testBidirectionalCommunication() throws Exception {
        CountDownLatch moveLatch = new CountDownLatch(1);
        AtomicInteger receivedRow = new AtomicInteger(-1);
        AtomicInteger receivedCol = new AtomicInteger(-1);

        NetworkEventListener hostListener = new NetworkEventListener() {
            @Override public void onConnected(String opponentName, int rows, int cols, int assignedPlayerId) {}
            @Override
            public void onMoveReceived(int row, int col) {
                receivedRow.set(row);
                receivedCol.set(col);
                moveLatch.countDown();
            }
            @Override public void onChatReceived(String sender, String message) {}
            @Override public void onRestartRequested() {}
            @Override public void onRestartAccepted() {}
            @Override public void onRestartDeclined() {}
            @Override public void onOpponentSurrendered() {}
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

        hostConn = new GameConnection(hostSocket, hostListener);
        clientConn = new GameConnection(clientSocket, clientListener);

        hostConn.startListening();
        clientConn.startListening();

        clientConn.send(ProtocolMessage.encodeMove(4, 5));

        boolean received = moveLatch.await(3, TimeUnit.SECONDS);
        assertTrue(received, "Host should receive move from client");
        assertEquals(4, receivedRow.get());
        assertEquals(5, receivedCol.get());
    }

    @Test
    void testDisconnectionDetection() throws Exception {
        CountDownLatch disconnectLatch = new CountDownLatch(1);
        AtomicReference<String> disconnectReason = new AtomicReference<>();

        NetworkEventListener hostListener = new NetworkEventListener() {
            @Override public void onConnected(String opponentName, int rows, int cols, int assignedPlayerId) {}
            @Override public void onMoveReceived(int row, int col) {}
            @Override public void onChatReceived(String sender, String message) {}
            @Override public void onRestartRequested() {}
            @Override public void onRestartAccepted() {}
            @Override public void onRestartDeclined() {}
            @Override public void onOpponentSurrendered() {}
            @Override
            public void onDisconnected(String reason) {
                disconnectReason.set(reason);
                disconnectLatch.countDown();
            }
            @Override public void onError(String errorMessage) {}
        };

        hostConn = new GameConnection(hostSocket, hostListener);
        hostConn.startListening();

        // Close client socket abruptly
        clientSocket.close();

        boolean disconnected = disconnectLatch.await(3, TimeUnit.SECONDS);
        assertTrue(disconnected, "Host should detect disconnection");
    }
}
