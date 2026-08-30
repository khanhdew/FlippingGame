# Online Peer-to-Peer Multiplayer Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement full online peer-to-peer (P2P) multiplayer functionality in FlippingGame, allowing two players to connect directly over LAN or direct IP/Port, play synchronized matches with turn gating and timer synchronization, chat in real-time, and handle rematches and disconnects cleanly.

**Architecture:** A dedicated asynchronous networking layer (`NetworkManager`, `GameConnection`, `NetworkEventListener`) built on Java standard sockets (`ServerSocket` and `Socket`) in background worker threads, communicating via a line-delimited command protocol (`HANDSHAKE`, `MOVE`, `CHAT`, `RESTART_REQ`, `SURRENDER`, `DISCONNECT`, `PING`). `MainPane` acts as the game controller, receiving network events via `Platform.runLater()` to update the UI and game board while restricting local input to assigned turns.

**Tech Stack:** Java 11, JavaFX 17 (`javafx-controls`, `javafx-fxml`), BootstrapFX, JUnit 5 (`junit-jupiter-api`, `junit-jupiter-engine`), Maven.

**Spec:** `docs/superpowers/specs/2026-08-30-online-p2p-multiplayer-design.md`

## Global Constraints

- Target Java version is 11 (`<source>11</source>`, `<target>11</target>`).
- JavaFX 17 modules and Java 11 compatibility must be preserved.
- Network socket I/O must never run on the JavaFX Application Thread.
- UI updates from network callbacks must be wrapped in `Platform.runLater()`.
- Base64 encoding/decoding (`java.util.Base64`) must be used for chat messages to protect against delimiter injection.
- Undo is disabled during online matches to prevent state desynchronization.

---

### Task 1: Protocol & Network Event System

**Files:**
- Create: `src/main/java/com/khanhdew/flipping/net/NetworkRole.java`
- Create: `src/main/java/com/khanhdew/flipping/net/NetworkEventListener.java`
- Create: `src/main/java/com/khanhdew/flipping/net/ProtocolMessage.java`
- Modify: `src/main/java/module-info.java:1-15`
- Test: `src/test/java/com/khanhdew/flipping/net/ProtocolMessageTest.java`

**Interfaces:**
- Produces:
  - `enum NetworkRole { HOST, CLIENT, NONE }`
  - `interface NetworkEventListener`:
    - `void onConnected(String opponentName, int rows, int cols, int assignedPlayerId)`
    - `void onMoveReceived(int row, int col)`
    - `void onChatReceived(String sender, String message)`
    - `void onRestartRequested()`
    - `void onRestartAccepted()`
    - `void onRestartDeclined()`
    - `void onOpponentSurrendered()`
    - `void onDisconnected(String reason)`
    - `void onError(String errorMessage)`
  - `class ProtocolMessage`:
    - `static String encodeHandshake(String name, int rows, int cols)`
    - `static String encodeHandshakeAck(String name)`
    - `static String encodeMove(int row, int col)`
    - `static String encodeChat(String sender, String text)`
    - `static String encodeRestartReq()`
    - `static String encodeRestartAck()`
    - `static String encodeRestartDecl()`
    - `static String encodeSurrender()`
    - `static String encodeDisconnect()`
    - `static String encodePing()`
    - `static String encodePong()`
    - `static void parseAndDispatch(String line, NetworkEventListener listener)`

- [ ] **Step 1: Write the failing unit tests for ProtocolMessage**

Create `src/test/java/com/khanhdew/flipping/net/ProtocolMessageTest.java`:

```java
package com.khanhdew.flipping.net;

import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class ProtocolMessageTest {

    @Test
    void testEncodeAndParseHandshake() {
        String encoded = ProtocolMessage.encodeHandshake("Alice", 8, 8);
        assertEquals("HANDSHAKE Alice 8 8", encoded);

        AtomicReference<String> oppName = new AtomicReference<>();
        AtomicInteger r = new AtomicInteger();
        AtomicInteger c = new AtomicInteger();
        AtomicInteger pId = new AtomicInteger();

        NetworkEventListener listener = new NetworkEventListener() {
            @Override
            public void onConnected(String opponentName, int rows, int cols, int assignedPlayerId) {
                oppName.set(opponentName);
                r.set(rows);
                c.set(cols);
                pId.set(assignedPlayerId);
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

        ProtocolMessage.parseAndDispatch(encoded, listener);
        assertEquals("Alice", oppName.get());
        assertEquals(8, r.get());
        assertEquals(8, c.get());
        assertEquals(2, pId.get()); // Client gets assigned player 2
    }

    @Test
    void testEncodeAndParseMove() {
        String encoded = ProtocolMessage.encodeMove(3, 4);
        assertEquals("MOVE 3 4", encoded);

        AtomicInteger movedRow = new AtomicInteger(-1);
        AtomicInteger movedCol = new AtomicInteger(-1);

        NetworkEventListener listener = new NetworkEventListener() {
            @Override public void onConnected(String opponentName, int rows, int cols, int assignedPlayerId) {}
            @Override
            public void onMoveReceived(int row, int col) {
                movedRow.set(row);
                movedCol.set(col);
            }
            @Override public void onChatReceived(String sender, String message) {}
            @Override public void onRestartRequested() {}
            @Override public void onRestartAccepted() {}
            @Override public void onRestartDeclined() {}
            @Override public void onOpponentSurrendered() {}
            @Override public void onDisconnected(String reason) {}
            @Override public void onError(String errorMessage) {}
        };

        ProtocolMessage.parseAndDispatch(encoded, listener);
        assertEquals(3, movedRow.get());
        assertEquals(4, movedCol.get());
    }

    @Test
    void testEncodeAndParseChat() {
        String encoded = ProtocolMessage.encodeChat("Alice", "Hello World! Special characters: @#$% & \n multiline");
        AtomicReference<String> senderRef = new AtomicReference<>();
        AtomicReference<String> msgRef = new AtomicReference<>();

        NetworkEventListener listener = new NetworkEventListener() {
            @Override public void onConnected(String opponentName, int rows, int cols, int assignedPlayerId) {}
            @Override public void onMoveReceived(int row, int col) {}
            @Override
            public void onChatReceived(String sender, String message) {
                senderRef.set(sender);
                msgRef.set(message);
            }
            @Override public void onRestartRequested() {}
            @Override public void onRestartAccepted() {}
            @Override public void onRestartDeclined() {}
            @Override public void onOpponentSurrendered() {}
            @Override public void onDisconnected(String reason) {}
            @Override public void onError(String errorMessage) {}
        };

        ProtocolMessage.parseAndDispatch(encoded, listener);
        assertEquals("Alice", senderRef.get());
        assertEquals("Hello World! Special characters: @#$% & \n multiline", msgRef.get());
    }

    @Test
    void testSurrenderAndDisconnect() {
        AtomicBoolean surrendered = new AtomicBoolean(false);
        AtomicBoolean disconnected = new AtomicBoolean(false);

        NetworkEventListener listener = new NetworkEventListener() {
            @Override public void onConnected(String opponentName, int rows, int cols, int assignedPlayerId) {}
            @Override public void onMoveReceived(int row, int col) {}
            @Override public void onChatReceived(String sender, String message) {}
            @Override public void onRestartRequested() {}
            @Override public void onRestartAccepted() {}
            @Override public void onRestartDeclined() {}
            @Override public void onOpponentSurrendered() { surrendered.set(true); }
            @Override public void onDisconnected(String reason) { disconnected.set(true); }
            @Override public void onError(String errorMessage) {}
        };

        ProtocolMessage.parseAndDispatch(ProtocolMessage.encodeSurrender(), listener);
        assertTrue(surrendered.get());

        ProtocolMessage.parseAndDispatch(ProtocolMessage.encodeDisconnect(), listener);
        assertTrue(disconnected.get());
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=ProtocolMessageTest`
Expected: Compilation failure or missing classes (`NetworkRole`, `NetworkEventListener`, `ProtocolMessage`).

- [ ] **Step 3: Implement `NetworkRole`, `NetworkEventListener`, `ProtocolMessage`, and update `module-info.java`**

Create `src/main/java/com/khanhdew/flipping/net/NetworkRole.java`:
```java
package com.khanhdew.flipping.net;

public enum NetworkRole {
    HOST,
    CLIENT,
    NONE
}
```

Create `src/main/java/com/khanhdew/flipping/net/NetworkEventListener.java`:
```java
package com.khanhdew.flipping.net;

public interface NetworkEventListener {
    void onConnected(String opponentName, int rows, int cols, int assignedPlayerId);
    void onMoveReceived(int row, int col);
    void onChatReceived(String sender, String message);
    void onRestartRequested();
    void onRestartAccepted();
    void onRestartDeclined();
    void onOpponentSurrendered();
    void onDisconnected(String reason);
    void onError(String errorMessage);
}
```

Create `src/main/java/com/khanhdew/flipping/net/ProtocolMessage.java`:
```java
package com.khanhdew.flipping.net;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class ProtocolMessage {

    public static String encodeHandshake(String name, int rows, int cols) {
        String safeName = (name == null || name.trim().isEmpty()) ? "Player" : name.trim().replace(" ", "_");
        return "HANDSHAKE " + safeName + " " + rows + " " + cols;
    }

    public static String encodeHandshakeAck(String name) {
        String safeName = (name == null || name.trim().isEmpty()) ? "Player" : name.trim().replace(" ", "_");
        return "HANDSHAKE_ACK " + safeName;
    }

    public static String encodeMove(int row, int col) {
        return "MOVE " + row + " " + col;
    }

    public static String encodeChat(String sender, String text) {
        String safeSender = (sender == null || sender.trim().isEmpty()) ? "Player" : sender.trim().replace(" ", "_");
        String encodedText = Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
        return "CHAT " + safeSender + " " + encodedText;
    }

    public static String encodeRestartReq() {
        return "RESTART_REQ";
    }

    public static String encodeRestartAck() {
        return "RESTART_ACK";
    }

    public static String encodeRestartDecl() {
        return "RESTART_DECL";
    }

    public static String encodeSurrender() {
        return "SURRENDER";
    }

    public static String encodeDisconnect() {
        return "DISCONNECT";
    }

    public static String encodePing() {
        return "PING";
    }

    public static String encodePong() {
        return "PONG";
    }

    public static void parseAndDispatch(String line, NetworkEventListener listener) {
        if (line == null || line.trim().isEmpty() || listener == null) {
            return;
        }

        String[] parts = line.trim().split("\\s+");
        String command = parts[0].toUpperCase();

        try {
            switch (command) {
                case "HANDSHAKE":
                    if (parts.length >= 4) {
                        String name = parts[1].replace("_", " ");
                        int rows = Integer.parseInt(parts[2]);
                        int cols = Integer.parseInt(parts[3]);
                        listener.onConnected(name, rows, cols, 2);
                    }
                    break;
                case "HANDSHAKE_ACK":
                    if (parts.length >= 2) {
                        String name = parts[1].replace("_", " ");
                        listener.onConnected(name, -1, -1, 1);
                    }
                    break;
                case "MOVE":
                    if (parts.length >= 3) {
                        int row = Integer.parseInt(parts[1]);
                        int col = Integer.parseInt(parts[2]);
                        listener.onMoveReceived(row, col);
                    }
                    break;
                case "CHAT":
                    if (parts.length >= 3) {
                        String sender = parts[1].replace("_", " ");
                        byte[] decoded = Base64.getDecoder().decode(parts[2]);
                        String msg = new String(decoded, StandardCharsets.UTF_8);
                        listener.onChatReceived(sender, msg);
                    }
                    break;
                case "RESTART_REQ":
                    listener.onRestartRequested();
                    break;
                case "RESTART_ACK":
                    listener.onRestartAccepted();
                    break;
                case "RESTART_DECL":
                    listener.onRestartDeclined();
                    break;
                case "SURRENDER":
                    listener.onOpponentSurrendered();
                    break;
                case "DISCONNECT":
                    listener.onDisconnected("Opponent disconnected.");
                    break;
                case "PING":
                case "PONG":
                    break;
                default:
                    // Unknown command ignored or logged
                    break;
            }
        } catch (Exception e) {
            listener.onError("Protocol parse error: " + e.getMessage());
        }
    }
}
```

Update `src/main/java/module-info.java` to export and open `com.khanhdew.flipping.net`:
```java
module com.khanhdew.flipping {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;
    requires google.collections;
    requires java.sql;

    opens com.khanhdew.flipping to javafx.fxml;
    exports com.khanhdew.flipping;
    exports com.khanhdew.flipping.view;
    opens com.khanhdew.flipping.view to javafx.fxml;
    exports com.khanhdew.flipping.net;
    opens com.khanhdew.flipping.net to javafx.fxml;
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=ProtocolMessageTest`
Expected: `BUILD SUCCESS`, 4 tests pass.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/module-info.java src/main/java/com/khanhdew/flipping/net/NetworkRole.java src/main/java/com/khanhdew/flipping/net/NetworkEventListener.java src/main/java/com/khanhdew/flipping/net/ProtocolMessage.java src/test/java/com/khanhdew/flipping/net/ProtocolMessageTest.java
git commit -m "feat(net): add protocol message encoder, decoder, and event listener interface"
```

---

### Task 2: Game Connection & Socket Read/Write Engine

**Files:**
- Create/Overwrite: `src/main/java/com/khanhdew/flipping/net/GameConnection.java`
- Test: `src/test/java/com/khanhdew/flipping/net/GameConnectionTest.java`

**Interfaces:**
- Consumes: `NetworkEventListener`, `ProtocolMessage`
- Produces:
  - `class GameConnection`:
    - `GameConnection(Socket socket, NetworkEventListener listener)`
    - `void startListening()`
    - `void send(String line)`
    - `void close()`
    - `boolean isConnected()`

- [ ] **Step 1: Write the failing unit test for GameConnection**

Create `src/test/java/com/khanhdew/flipping/net/GameConnectionTest.java`:

```java
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
    void testBidirectionalCommunication() throws InterruptedException {
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
    void testDisconnectionDetection() throws InterruptedException {
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
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=GameConnectionTest`
Expected: Compilation failure or failure in `GameConnection`.

- [ ] **Step 3: Implement `GameConnection`**

Update `src/main/java/com/khanhdew/flipping/net/GameConnection.java`:

```java
package com.khanhdew.flipping.net;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class GameConnection {
    private final Socket socket;
    private final NetworkEventListener listener;
    private BufferedReader reader;
    private PrintWriter writer;
    private Thread listenerThread;
    private volatile boolean running = false;

    public GameConnection(Socket socket, NetworkEventListener listener) throws IOException {
        this.socket = socket;
        this.listener = listener;
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        this.writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
    }

    public void startListening() {
        if (running) return;
        running = true;

        listenerThread = new Thread(() -> {
            try {
                String line;
                while (running && (line = reader.readLine()) != null) {
                    ProtocolMessage.parseAndDispatch(line, listener);
                }
            } catch (IOException e) {
                if (running) {
                    listener.onDisconnected("Connection lost: " + e.getMessage());
                }
            } finally {
                close();
            }
        }, "GameConnection-Listener");
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    public synchronized void send(String line) {
        if (writer != null && !socket.isClosed()) {
            writer.println(line);
        }
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public synchronized void close() {
        running = false;
        try {
            if (reader != null) reader.close();
        } catch (IOException ignored) {}
        if (writer != null) writer.close();
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {}
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=GameConnectionTest`
Expected: `BUILD SUCCESS`, 2 tests pass.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/khanhdew/flipping/net/GameConnection.java src/test/java/com/khanhdew/flipping/net/GameConnectionTest.java
git commit -m "feat(net): implement GameConnection socket listener and writer"
```

---

### Task 3: Network Manager & Handshake Lifecycle

**Files:**
- Create: `src/main/java/com/khanhdew/flipping/net/NetworkManager.java`
- Test: `src/test/java/com/khanhdew/flipping/net/NetworkManagerTest.java`

**Interfaces:**
- Consumes: `GameConnection`, `NetworkRole`, `NetworkEventListener`, `ProtocolMessage`
- Produces:
  - `class NetworkManager`:
    - `static NetworkManager getInstance()`
    - `void startHost(int port, String hostName, int rows, int cols, NetworkEventListener listener)`
    - `void connectToHost(String hostIp, int port, String clientName, NetworkEventListener listener)`
    - `void sendMove(int row, int col)`
    - `void sendChat(String message)`
    - `void sendRestartReq()`
    - `void sendRestartAck()`
    - `void sendRestartDecl()`
    - `void sendSurrender()`
    - `void disconnect()`
    - `NetworkRole getRole()`
    - `String getLocalIpAddress()`
    - `boolean isConnected()`

- [ ] **Step 1: Write the failing unit test for NetworkManager**

Create `src/test/java/com/khanhdew/flipping/net/NetworkManagerTest.java`:

```java
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
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=NetworkManagerTest`
Expected: Compilation failure (missing `NetworkManager`).

- [ ] **Step 3: Implement `NetworkManager`**

Create `src/main/java/com/khanhdew/flipping/net/NetworkManager.java`:

```java
package com.khanhdew.flipping.net;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NetworkManager {
    private static NetworkManager instance;

    private NetworkRole role = NetworkRole.NONE;
    private ServerSocket serverSocket;
    private GameConnection connection;
    private String localPlayerName = "Player";
    private ExecutorService executor = Executors.newCachedThreadPool();

    public NetworkManager() {}

    public static synchronized NetworkManager getInstance() {
        if (instance == null) {
            instance = new NetworkManager();
        }
        return instance;
    }

    public synchronized void startHost(int port, String hostName, int rows, int cols, NetworkEventListener listener) {
        disconnect();
        this.role = NetworkRole.HOST;
        this.localPlayerName = (hostName == null || hostName.trim().isEmpty()) ? "Player 1" : hostName.trim();

        executor.submit(() -> {
            try {
                serverSocket = new ServerSocket(port);
                Socket clientSocket = serverSocket.accept();

                NetworkEventListener hostInternalListener = new NetworkEventListener() {
                    @Override
                    public void onConnected(String opponentName, int r, int c, int assignedPlayerId) {
                        listener.onConnected(opponentName, rows, cols, 1);
                    }
                    @Override public void onMoveReceived(int row, int col) { listener.onMoveReceived(row, col); }
                    @Override public void onChatReceived(String sender, String message) { listener.onChatReceived(sender, message); }
                    @Override public void onRestartRequested() { listener.onRestartRequested(); }
                    @Override public void onRestartAccepted() { listener.onRestartAccepted(); }
                    @Override public void onRestartDeclined() { listener.onRestartDeclined(); }
                    @Override public void onOpponentSurrendered() { listener.onOpponentSurrendered(); }
                    @Override public void onDisconnected(String reason) { listener.onDisconnected(reason); }
                    @Override public void onError(String errorMessage) { listener.onError(errorMessage); }
                };

                connection = new GameConnection(clientSocket, hostInternalListener);
                connection.startListening();

                // Send initial handshake
                connection.send(ProtocolMessage.encodeHandshake(localPlayerName, rows, cols));

            } catch (IOException e) {
                if (role == NetworkRole.HOST) {
                    listener.onError("Host server error: " + e.getMessage());
                }
            }
        });
    }

    public synchronized void connectToHost(String hostIp, int port, String clientName, NetworkEventListener listener) {
        disconnect();
        this.role = NetworkRole.CLIENT;
        this.localPlayerName = (clientName == null || clientName.trim().isEmpty()) ? "Player 2" : clientName.trim();

        executor.submit(() -> {
            try {
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(hostIp, port), 5000);

                NetworkEventListener clientInternalListener = new NetworkEventListener() {
                    @Override
                    public void onConnected(String opponentName, int rows, int cols, int assignedPlayerId) {
                        // Respond with handshake ack
                        if (connection != null) {
                            connection.send(ProtocolMessage.encodeHandshakeAck(localPlayerName));
                        }
                        listener.onConnected(opponentName, rows, cols, 2);
                    }
                    @Override public void onMoveReceived(int row, int col) { listener.onMoveReceived(row, col); }
                    @Override public void onChatReceived(String sender, String message) { listener.onChatReceived(sender, message); }
                    @Override public void onRestartRequested() { listener.onRestartRequested(); }
                    @Override public void onRestartAccepted() { listener.onRestartAccepted(); }
                    @Override public void onRestartDeclined() { listener.onRestartDeclined(); }
                    @Override public void onOpponentSurrendered() { listener.onOpponentSurrendered(); }
                    @Override public void onDisconnected(String reason) { listener.onDisconnected(reason); }
                    @Override public void onError(String errorMessage) { listener.onError(errorMessage); }
                };

                connection = new GameConnection(socket, clientInternalListener);
                connection.startListening();

            } catch (IOException e) {
                listener.onError("Connection failed: " + e.getMessage());
                disconnect();
            }
        });
    }

    public void sendMove(int row, int col) {
        if (connection != null) {
            connection.send(ProtocolMessage.encodeMove(row, col));
        }
    }

    public void sendChat(String message) {
        if (connection != null) {
            connection.send(ProtocolMessage.encodeChat(localPlayerName, message));
        }
    }

    public void sendRestartReq() {
        if (connection != null) {
            connection.send(ProtocolMessage.encodeRestartReq());
        }
    }

    public void sendRestartAck() {
        if (connection != null) {
            connection.send(ProtocolMessage.encodeRestartAck());
        }
    }

    public void sendRestartDecl() {
        if (connection != null) {
            connection.send(ProtocolMessage.encodeRestartDecl());
        }
    }

    public void sendSurrender() {
        if (connection != null) {
            connection.send(ProtocolMessage.encodeSurrender());
        }
    }

    public synchronized void disconnect() {
        if (connection != null) {
            try {
                connection.send(ProtocolMessage.encodeDisconnect());
            } catch (Exception ignored) {}
            connection.close();
            connection = null;
        }
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException ignored) {}
            serverSocket = null;
        }
        this.role = NetworkRole.NONE;
    }

    public NetworkRole getRole() {
        return role;
    }

    public String getLocalPlayerName() {
        return localPlayerName;
    }

    public boolean isConnected() {
        return connection != null && connection.isConnected();
    }

    public static String getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (ni.isLoopback() || !ni.isUp()) continue;
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=NetworkManagerTest`
Expected: `BUILD SUCCESS`, 1 test passes.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/khanhdew/flipping/net/NetworkManager.java src/test/java/com/khanhdew/flipping/net/NetworkManagerTest.java
git commit -m "feat(net): implement NetworkManager singleton and connection orchestrator"
```

---

### Task 4: Localization Updates for Online Mode

**Files:**
- Modify: `src/main/java/com/khanhdew/flipping/utils/Language.java:25-88`
- Test: `src/test/java/com/khanhdew/flipping/utils/LanguageTest.java`

**Interfaces:**
- Produces: Keys in `Language.getLanguage()` for English and Vietnamese:
  - `onlineMultiplayer`, `hostGame`, `joinGame`, `ipAddress`, `port`, `waitingForOpponent`, `connecting`, `connected`, `connectionLost`, `yourTurn`, `opponentTurn`, `chat`, `send`, `surrender`, `rematch`, `rematchPrompt`

- [ ] **Step 1: Write the failing unit test for Language keys**

Create `src/test/java/com/khanhdew/flipping/utils/LanguageTest.java`:

```java
package com.khanhdew.flipping.utils;

import com.google.common.collect.BiMap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LanguageTest {

    @Test
    void testOnlineKeysEnglishAndVietnamese() {
        BiMap<String, String> en = Language.ENGLISH.getLanguage();
        BiMap<String, String> vi = Language.VIETNAMESE.getLanguage();

        String[] requiredKeys = {
            "onlineMultiplayer", "hostGame", "joinGame", "ipAddress",
            "port", "waitingForOpponent", "connecting", "connected",
            "connectionLost", "yourTurn", "opponentTurn", "chat",
            "send", "surrender", "rematch"
        };

        for (String key : requiredKeys) {
            assertTrue(en.containsKey(key), "English missing key: " + key);
            assertTrue(vi.containsKey(key), "Vietnamese missing key: " + key);
            assertNotNull(en.get(key));
            assertNotNull(vi.get(key));
        }
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=LanguageTest`
Expected: FAIL on missing keys.

- [ ] **Step 3: Update `Language.java`**

Modify `src/main/java/com/khanhdew/flipping/utils/Language.java`:

Add to `initEnglish()`:
```java
        english.put("onlineMultiplayer", "Online Multiplayer");
        english.put("hostGame", "Host Game");
        english.put("joinGame", "Join Game");
        english.put("ipAddress", "IP Address");
        english.put("port", "Port");
        english.put("waitingForOpponent", "Waiting for opponent to connect...");
        english.put("connecting", "Connecting to host...");
        english.put("connected", "Connected");
        english.put("connectionLost", "Connection lost with opponent.");
        english.put("yourTurn", "Your Turn");
        english.put("opponentTurn", "Opponent's Turn");
        english.put("chat", "Chat");
        english.put("send", "Send");
        english.put("surrender", "Surrender");
        english.put("rematch", "Rematch");
        english.put("rematchPrompt", "Opponent requested a rematch. Do you accept?");
```

Add to `initVietnamese()`:
```java
        vietnamese.put("onlineMultiplayer", "Chơi trực tuyến");
        vietnamese.put("hostGame", "Tạo phòng");
        vietnamese.put("joinGame", "Vào phòng");
        vietnamese.put("ipAddress", "Địa chỉ IP");
        vietnamese.put("port", "Cổng");
        vietnamese.put("waitingForOpponent", "Đang chờ đối thủ tham gia...");
        vietnamese.put("connecting", "Đang kết nối tới chủ phòng...");
        vietnamese.put("connected", "Đã kết nối");
        vietnamese.put("connectionLost", "Mất kết nối với đối thủ.");
        vietnamese.put("yourTurn", "Lượt của bạn");
        vietnamese.put("opponentTurn", "Lượt của đối thủ");
        vietnamese.put("chat", "Trò chuyện");
        vietnamese.put("send", "Gửi");
        vietnamese.put("surrender", "Đầu hàng");
        vietnamese.put("rematch", "Đấu lại");
        vietnamese.put("rematchPrompt", "Đối thủ yêu cầu đấu lại. Bạn có chấp nhận không?");
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=LanguageTest`
Expected: `BUILD SUCCESS`, 1 test passes.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/khanhdew/flipping/utils/Language.java src/test/java/com/khanhdew/flipping/utils/LanguageTest.java
git commit -m "feat(i18n): add online multiplayer localization keys for English and Vietnamese"
```

---

### Task 5: Online Play Dialog & Welcome Screen Integration

**Files:**
- Create: `src/main/java/com/khanhdew/flipping/view/OnlineDialog.java`
- Modify: `src/main/java/com/khanhdew/flipping/view/WelcomePaneController.java`
- Modify: `src/main/resources/com/khanhdew/flipping/welcomepane.fxml`

**Interfaces:**
- Consumes: `NetworkManager`, `Language`, `MainPane`
- Produces:
  - `class OnlineDialog`: Opens modal dialog with Host and Join tabs, connecting players and launching `MainPane` in online mode.

- [ ] **Step 1: Create `OnlineDialog.java`**

Create `src/main/java/com/khanhdew/flipping/view/OnlineDialog.java`:

```java
package com.khanhdew.flipping.view;

import com.google.common.collect.BiMap;
import com.khanhdew.flipping.net.NetworkEventListener;
import com.khanhdew.flipping.net.NetworkManager;
import com.khanhdew.flipping.utils.Language;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class OnlineDialog {
    private final Stage parentStage;
    private final Language language;
    private final BiMap<String, String> langMap;
    private Stage dialogStage;
    private NetworkManager netManager = NetworkManager.getInstance();

    public OnlineDialog(Stage parentStage, Language language) {
        this.parentStage = parentStage;
        this.language = language;
        this.langMap = language.getLanguage();
    }

    public void show() {
        dialogStage = new Stage();
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(parentStage);
        dialogStage.setTitle(langMap.get("onlineMultiplayer"));

        TabPane tabPane = new TabPane();

        // --- HOST TAB ---
        Tab hostTab = new Tab(langMap.get("hostGame"));
        hostTab.setClosable(false);

        VBox hostBox = new VBox(10);
        hostBox.setPadding(new Insets(15));
        hostBox.setAlignment(Pos.CENTER_LEFT);

        String localIp = NetworkManager.getLocalIpAddress();
        Label ipLabel = new Label(langMap.get("ipAddress") + ": " + localIp);
        ipLabel.setStyle("-fx-font-weight: bold;");

        TextField hostPortField = new TextField("55555");
        TextField hostNameField = new TextField(langMap.get("player1"));
        TextField hostRowsField = new TextField("8");
        TextField hostColsField = new TextField("8");

        GridPane hostGrid = new GridPane();
        hostGrid.setHgap(10);
        hostGrid.setVgap(8);
        hostGrid.add(new Label(langMap.get("port") + ":"), 0, 0);
        hostGrid.add(hostPortField, 1, 0);
        hostGrid.add(new Label(langMap.get("player1Name") + ":"), 0, 1);
        hostGrid.add(hostNameField, 1, 1);
        hostGrid.add(new Label(langMap.get("row") + ":"), 0, 2);
        hostGrid.add(hostRowsField, 1, 2);
        hostGrid.add(new Label(langMap.get("col") + ":"), 0, 3);
        hostGrid.add(hostColsField, 1, 3);

        Label hostStatusLabel = new Label("");
        Button startHostBtn = new Button(langMap.get("hostGame"));
        startHostBtn.getStyleClass().addAll("btn", "btn-primary");

        startHostBtn.setOnAction(e -> {
            try {
                int port = Integer.parseInt(hostPortField.getText().trim());
                int rows = Integer.parseInt(hostRowsField.getText().trim());
                int cols = Integer.parseInt(hostColsField.getText().trim());
                String name = hostNameField.getText().trim();

                hostStatusLabel.setText(langMap.get("waitingForOpponent"));
                startHostBtn.setDisable(true);

                netManager.startHost(port, name, rows, cols, new NetworkEventListener() {
                    @Override
                    public void onConnected(String opponentName, int r, int c, int assignedPlayerId) {
                        Platform.runLater(() -> {
                            dialogStage.close();
                            launchOnlineGame(rows, cols, name, opponentName, assignedPlayerId);
                        });
                    }
                    @Override public void onMoveReceived(int row, int col) {}
                    @Override public void onChatReceived(String sender, String message) {}
                    @Override public void onRestartRequested() {}
                    @Override public void onRestartAccepted() {}
                    @Override public void onRestartDeclined() {}
                    @Override public void onOpponentSurrendered() {}
                    @Override public void onDisconnected(String reason) {
                        Platform.runLater(() -> {
                            hostStatusLabel.setText(reason);
                            startHostBtn.setDisable(false);
                        });
                    }
                    @Override public void onError(String errorMessage) {
                        Platform.runLater(() -> {
                            hostStatusLabel.setText(errorMessage);
                            startHostBtn.setDisable(false);
                        });
                    }
                });
            } catch (NumberFormatException ex) {
                hostStatusLabel.setText(langMap.get("invalidInput"));
            }
        });

        hostBox.getChildren().addAll(ipLabel, hostGrid, startHostBtn, hostStatusLabel);
        hostTab.setContent(hostBox);

        // --- JOIN TAB ---
        Tab joinTab = new Tab(langMap.get("joinGame"));
        joinTab.setClosable(false);

        VBox joinBox = new VBox(10);
        joinBox.setPadding(new Insets(15));
        joinBox.setAlignment(Pos.CENTER_LEFT);

        TextField joinIpField = new TextField("127.0.0.1");
        TextField joinPortField = new TextField("55555");
        TextField joinNameField = new TextField(langMap.get("player2"));

        GridPane joinGrid = new GridPane();
        joinGrid.setHgap(10);
        joinGrid.setVgap(8);
        joinGrid.add(new Label(langMap.get("ipAddress") + ":"), 0, 0);
        joinGrid.add(joinIpField, 1, 0);
        joinGrid.add(new Label(langMap.get("port") + ":"), 0, 1);
        joinGrid.add(joinPortField, 1, 1);
        joinGrid.add(new Label(langMap.get("player2Name") + ":"), 0, 2);
        joinGrid.add(joinNameField, 1, 2);

        Label joinStatusLabel = new Label("");
        Button connectBtn = new Button(langMap.get("joinGame"));
        connectBtn.getStyleClass().addAll("btn", "btn-success");

        connectBtn.setOnAction(e -> {
            try {
                String ip = joinIpField.getText().trim();
                int port = Integer.parseInt(joinPortField.getText().trim());
                String name = joinNameField.getText().trim();

                joinStatusLabel.setText(langMap.get("connecting"));
                connectBtn.setDisable(true);

                netManager.connectToHost(ip, port, name, new NetworkEventListener() {
                    @Override
                    public void onConnected(String opponentName, int r, int c, int assignedPlayerId) {
                        Platform.runLater(() -> {
                            dialogStage.close();
                            launchOnlineGame(r, c, opponentName, name, assignedPlayerId);
                        });
                    }
                    @Override public void onMoveReceived(int row, int col) {}
                    @Override public void onChatReceived(String sender, String message) {}
                    @Override public void onRestartRequested() {}
                    @Override public void onRestartAccepted() {}
                    @Override public void onRestartDeclined() {}
                    @Override public void onOpponentSurrendered() {}
                    @Override public void onDisconnected(String reason) {
                        Platform.runLater(() -> {
                            joinStatusLabel.setText(reason);
                            connectBtn.setDisable(false);
                        });
                    }
                    @Override public void onError(String errorMessage) {
                        Platform.runLater(() -> {
                            joinStatusLabel.setText(errorMessage);
                            connectBtn.setDisable(false);
                        });
                    }
                });
            } catch (NumberFormatException ex) {
                joinStatusLabel.setText(langMap.get("invalidInput"));
            }
        });

        joinBox.getChildren().addAll(joinGrid, connectBtn, joinStatusLabel);
        joinTab.setContent(joinBox);

        tabPane.getTabs().addAll(hostTab, joinTab);

        dialogStage.setOnCloseRequest(e -> netManager.disconnect());

        Scene scene = new Scene(tabPane, 420, 320);
        dialogStage.setScene(scene);
        dialogStage.setResizable(false);
        dialogStage.showAndWait();
    }

    private void launchOnlineGame(int rows, int cols, String p1Name, String p2Name, int localPlayerId) {
        MainPane mainPane = new MainPane("human", "human", language);
        mainPane.setOnlineMode(true, localPlayerId);
        mainPane.setPlayersName(p1Name, p2Name);
        mainPane.init(rows, cols);

        mainPane.setPrefSize(MainPane.getWIDTH(), MainPane.getHEIGHT());
        parentStage.setResizable(true);
        parentStage.setScene(new Scene(mainPane, MainPane.getWIDTH(), MainPane.getHEIGHT()));

        Screen screen = Screen.getPrimary();
        parentStage.setX((screen.getBounds().getWidth() - MainPane.getWIDTH()) / 2);
        parentStage.setY((screen.getBounds().getHeight() - MainPane.getHEIGHT()) / 2 - 50);

        parentStage.setOnCloseRequest(e -> {
            netManager.disconnect();
            Platform.exit();
        });
    }
}
```

- [ ] **Step 2: Update `WelcomePaneController.java` and `welcomepane.fxml`**

Modify `src/main/resources/com/khanhdew/flipping/welcomepane.fxml` to add an `onlineButton` below `startButton`:
```xml
<Button fx:id="onlineButton" mnemonicParsing="false" onAction="#openOnlineDialog" text="Online Multiplayer" />
```

Modify `src/main/java/com/khanhdew/flipping/view/WelcomePaneController.java`:
- Add `@FXML public Button onlineButton;`
- In `setLanguage(Language language)`: update `if (onlineButton != null) onlineButton.setText(languageMap.get("onlineMultiplayer"));`
- Add handler method:
```java
@FXML
private void openOnlineDialog() {
    OnlineDialog dialog = new OnlineDialog(stage, this.language);
    dialog.show();
}
```

- [ ] **Step 3: Compile and verify UI bindings**

Run: `mvn compile`
Expected: `BUILD SUCCESS`.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/khanhdew/flipping/view/OnlineDialog.java src/main/java/com/khanhdew/flipping/view/WelcomePaneController.java src/main/resources/com/khanhdew/flipping/welcomepane.fxml
git commit -m "feat(ui): add OnlineDialog and online play button in welcome screen"
```

---

### Task 6: In-Game Chat Component (`ChatPane`)

**Files:**
- Create: `src/main/java/com/khanhdew/flipping/view/ChatPane.java`

**Interfaces:**
- Consumes: `NetworkManager`, `Language`
- Produces:
  - `class ChatPane extends VBox`:
    - `ChatPane(Language language)`
    - `void appendMessage(String sender, String message)`
    - `void setNetworkManager(NetworkManager netManager)`

- [ ] **Step 1: Implement `ChatPane.java`**

Create `src/main/java/com/khanhdew/flipping/view/ChatPane.java`:

```java
package com.khanhdew.flipping.view;

import com.google.common.collect.BiMap;
import com.khanhdew.flipping.net.NetworkManager;
import com.khanhdew.flipping.utils.Language;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ChatPane extends VBox {
    private final ListView<String> messageList;
    private final TextField inputField;
    private final Button sendButton;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private NetworkManager networkManager = NetworkManager.getInstance();

    public ChatPane(Language language) {
        BiMap<String, String> langMap = language.getLanguage();
        this.setPadding(new Insets(5));
        this.setSpacing(5);
        this.setPrefWidth(280);

        Label chatTitle = new Label(langMap.get("chat"));
        chatTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        messageList = new ListView<>();
        messageList.setPrefHeight(160);
        VBox.setVgrow(messageList, Priority.ALWAYS);

        inputField = new TextField();
        inputField.setPromptText("...");
        HBox.setHgrow(inputField, Priority.ALWAYS);

        sendButton = new Button(langMap.get("send"));
        sendButton.setOnAction(e -> sendMessage());
        inputField.setOnAction(e -> sendMessage());

        HBox inputBar = new HBox(5, inputField, sendButton);
        this.getChildren().addAll(chatTitle, messageList, inputBar);
    }

    private void sendMessage() {
        String text = inputField.getText().trim();
        if (!text.isEmpty()) {
            if (text.length() > 200) {
                text = text.substring(0, 200);
            }
            networkManager.sendChat(text);
            appendMessage(networkManager.getLocalPlayerName(), text);
            inputField.clear();
        }
    }

    public void appendMessage(String sender, String message) {
        Platform.runLater(() -> {
            String time = LocalTime.now().format(timeFormatter);
            String entry = "[" + time + "] " + sender + ": " + message;
            messageList.getItems().add(entry);
            messageList.scrollTo(messageList.getItems().size() - 1);
        });
    }

    public void setNetworkManager(NetworkManager networkManager) {
        this.networkManager = networkManager;
    }
}
```

- [ ] **Step 2: Compile to verify syntax**

Run: `mvn compile`
Expected: `BUILD SUCCESS`.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/khanhdew/flipping/view/ChatPane.java
git commit -m "feat(ui): create in-game ChatPane component"
```

---

### Task 7: Game Synchronization, Turn-Gating & Rematch in `MainPane`

**Files:**
- Modify: `src/main/java/com/khanhdew/flipping/view/MainPane.java:43-789`
- Modify: `src/main/java/com/khanhdew/flipping/view/ButtonPaneController.java:38-102`

**Interfaces:**
- Consumes: `NetworkManager`, `NetworkEventListener`, `ChatPane`
- Produces:
  - `MainPane.setOnlineMode(boolean isOnline, int localPlayerId)`
  - Turn click restriction when `turn != localPlayerId` in online matches
  - Remote move execution and animations via `onMoveReceived`
  - Rematch request dialog and disconnect recovery

- [ ] **Step 1: Update `MainPane.java`**

Modify `src/main/java/com/khanhdew/flipping/view/MainPane.java`:
1. Add fields:
   ```java
   private boolean isOnlineMode = false;
   private int localPlayerId = 1;
   private ChatPane chatPane;
   private Label onlineStatusBanner;
   ```
2. Add `setOnlineMode(boolean isOnline, int localPlayerId)`:
   ```java
   public void setOnlineMode(boolean isOnline, int localPlayerId) {
       this.isOnlineMode = isOnline;
       this.localPlayerId = localPlayerId;
       if (isOnline) {
           registerNetworkListener();
       }
   }
   ```
3. Implement `registerNetworkListener()`:
   ```java
   private void registerNetworkListener() {
       NetworkManager.getInstance().startHost // if already running, attach listener to connection
       // or attach active connection listener:
   }
   ```
   Wire `onMoveReceived(row, col)` to call `Platform.runLater(() -> applyRemoteMove(row, col))`.
   Wire `onChatReceived(sender, msg)` to call `chatPane.appendMessage(sender, msg)`.
   Wire `onRestartRequested()` to prompt for rematch: `NetworkManager.getInstance().sendRestartAck()`.
   Wire `onDisconnected(reason)` to display alert and return to menu.
4. Gate mouse interaction in `manageTurn()` and `handleHumanMove()`:
   ```java
   if (isOnlineMode && turn != localPlayerId) {
       // Do not accept human clicks for opponent
       return;
   }
   ```
5. On local move success:
   ```java
   if (isOnlineMode) {
       NetworkManager.getInstance().sendMove(mouse.getRow(), mouse.getCol());
   }
   ```
6. Embed `ChatPane` in `scorePane` when `isOnlineMode` is true.

- [ ] **Step 2: Update `ButtonPaneController.java`**

Modify `src/main/java/com/khanhdew/flipping/view/ButtonPaneController.java`:
- When `mainPane.isOnlineMode()`, disable `undo`, `saveGame`, `loadSavedGame`, and change `newGame` to request rematch via `NetworkManager.getInstance().sendRestartReq()`.

- [ ] **Step 3: Compile and run test suite**

Run: `mvn clean test`
Expected: `BUILD SUCCESS`, all tests pass.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/khanhdew/flipping/view/MainPane.java src/main/java/com/khanhdew/flipping/view/ButtonPaneController.java
git commit -m "feat(game): integrate online turn synchronization, move transmission, and chat panel"
```

---

### Task 8: End-to-End Verification & Documentation

**Files:**
- Modify: `README.md:25-35`

- [ ] **Step 1: Update README.md to mark online play as supported**

Modify `README.md`:
```markdown
## Features
- [x] Offline Player vs Player.
- [x] Player vs AI.
- [x] Database.
- [x] Customize board size.
- [x] Play with friends (Online Peer-to-Peer / LAN).
- [x] In-game real-time chat.
```

- [ ] **Step 2: Run full build and test verification**

Run: `mvn clean install`
Expected: `BUILD SUCCESS`.

- [ ] **Step 3: Commit**

```bash
git add README.md
git commit -m "docs: update features list with online peer-to-peer multiplayer and chat"
```
