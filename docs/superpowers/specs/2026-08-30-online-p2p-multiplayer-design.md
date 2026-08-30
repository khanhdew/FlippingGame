# Online Peer-to-Peer Multiplayer Design Specification

## 1. Overview
This specification details the architecture, network protocol, state synchronization, user interface, and error-handling mechanics for implementing real-time online peer-to-peer (P2P) multiplayer in the **Flipping Game** JavaFX application.

The design enables two players to connect directly over LAN or direct IP/Port (one acting as Host and the other as Client), play a synchronized Reversi/Othello flipping match with strict turn validation, and chat in real-time.

---

## 2. Architecture & Component Hierarchy

### 2.1 Networking Package (`com.khanhdew.flipping.net`)

* **`NetworkRole`**: Enum with values `HOST`, `CLIENT`, `NONE`.
* **`NetworkManager`**: Singleton lifecycle manager responsible for creating and tearing down network sessions.
  * `startHost(int port, String hostPlayerName, int rows, int cols, NetworkEventListener listener)`: Spawns `ServerSocket` in a background daemon thread (`ExecutorService`) and awaits client connection.
  * `connectToHost(String hostIp, int port, String clientPlayerName, NetworkEventListener listener)`: Connects asynchronously via `Socket` with a 5000ms timeout.
  * `send(String rawMessage)`: Sends a line-delimited command across the active connection.
  * `disconnect()`: Cleanly terminates socket streams, heartbeats, and worker threads.
* **`GameConnection`**: Encapsulates socket read/write loops.
  * Maintains `BufferedReader` and `PrintWriter` (UTF-8).
  * Runs a dedicated background listener thread reading lines until EOF (`null`) or exception.
  * Dispatches parsed command events to `NetworkEventListener`.
* **`NetworkEventListener`**: Callback interface implemented by UI/Game controllers:
  * `void onConnected(String opponentName, int rows, int cols, int assignedPlayerId)`
  * `void onMoveReceived(int row, int col)`
  * `void onChatReceived(String sender, String message)`
  * `void onRestartRequested()`
  * `void onRestartAccepted()`
  * `void onRestartDeclined()`
  * `void onOpponentSurrendered()`
  * `void onDisconnected(String reason)`
  * `void onError(String errorMessage)`

### 2.2 Model & View Integration

* **`MainPane`**:
  * Fields: `boolean isOnlineMode`, `int localPlayerId` (1 for Host, 2 for Client), `GameConnection connection`.
  * Board interaction is gated: if `isOnlineMode && turn != localPlayerId`, mouse clicks and hover hint calculations are ignored.
  * When a move is executed locally (`move(col, row, localPlayerId)`), a `MOVE <row> <col>` command is transmitted.
  * When `onMoveReceived(int row, int col)` is fired, it runs inside `Platform.runLater()` to execute `move(col, row, remotePlayerId)` and flip pieces with animations.
  * The **Undo** button is disabled in online mode to prevent state divergence.
* **`OnlineDialog` / `OnlinePaneController`**:
  * Accessible from the main welcome screen (`WelcomePaneController`).
  * Provides tabs for **Host Game** (auto-detects local LAN IP, custom port, board dimensions) and **Join Game** (host IP input, port input).
* **`ChatPane`**:
  * Embedded in the right sidebar (`scorePane`).
  * Features message history (`ListView`), `TextField` input with `Enter` trigger, and character limit sanitization.

---

## 3. Communication Protocol

All messages are UTF-8 line-delimited plain-text strings: `<COMMAND> [ARG1] [ARG2] ...\n`.

| Command | Arguments | Sender | Description |
| :--- | :--- | :--- | :--- |
| `HANDSHAKE` | `<playerName> <rows> <cols>` | Host | Sent immediately upon socket connection to synchronize board parameters and host name. |
| `HANDSHAKE_ACK` | `<playerName>` | Client | Client acknowledges handshake and sends its player name. |
| `MOVE` | `<row> <col>` | Host / Client | Transmits a valid move placed at matrix coordinate `(row, col)`. |
| `CHAT` | `<base64Text>` | Host / Client | Sends base64-encoded chat text to safely handle spaces, punctuation, and newlines. |
| `RESTART_REQ` | None | Host / Client | Proposes a match rematch after game over. |
| `RESTART_ACK` | None | Host / Client | Confirms rematch; both boards reset. |
| `RESTART_DECL` | None | Host / Client | Declines rematch. |
| `SURRENDER` | None | Host / Client | Forfeits the game immediately. |
| `DISCONNECT` | None | Host / Client | Gracefully closes the online session. |
| `PING` / `PONG` | None | Both | Periodic heartbeat every 5s to monitor connection health. |

---

## 4. Game State Synchronization & Rules

### 4.1 Role Assignment & Initial Handshake
1. Host opens port (default: `55555`). Host is assigned **Player 1 (Black)**.
2. Client connects to Host's IP. Client is assigned **Player 2 (White)**.
3. Host sends: `HANDSHAKE Alice 8 8`.
4. Client receives `HANDSHAKE`, initializes its board to 8x8, sets Player 1 name to "Alice", and responds with `HANDSHAKE_ACK Bob`.
5. Host receives `HANDSHAKE_ACK`, sets Player 2 name to "Bob", and transitions both applications to the active gameplay view.

### 4.2 Move Synchronization & Turn Execution
1. Turn starts at `turn = 1` (Player 1 / Host).
2. Host clicks cell `(row, col)`:
   * Validated via `BoardHelper.isLegalMove(matrix, 1, row, col)`.
   * Pieces flip with animation, `matrix` updates, turn becomes `2`.
   * Host sends: `MOVE 2 3`.
3. Client receives `MOVE 2 3`:
   * Dispatched to JavaFX thread via `Platform.runLater()`.
   * `move(3, 2, 1)` is called on Client's board, flipping the matching pieces.
   * Client sets `turn = 2`.
   * Client's board becomes interactive for Bob (Player 2).

### 4.3 Timekeeping & Clock Synchronization
* Host acts as the authoritative clock manager.
* On each `MOVE` transmission, remaining time can be optionally included or validated.
* When either player's timer hits zero, game over is triggered on both sides.

---

## 5. Error Handling, Resilience & Teardown

1. **Port Binding Collision (`BindException`)**:
   * Host setup catches error and alerts the user to select another port.
2. **Connection Timeout (`SocketTimeoutException`, `ConnectException`)**:
   * Client attempts connection for 5 seconds. If unreachable, notifies player with an error alert.
3. **Mid-Game Drop / Crash (`SocketException` / EOF)**:
   * Connection listener catches socket termination.
   * `onDisconnected("Connection with opponent was lost.")` triggers an alert on the JavaFX thread, disables board interactions, and provides a button to return to the Main Menu.
4. **Clean Exit**:
   * Stage close hook (`stage.setOnCloseRequest(...)`) sends `DISCONNECT` before shutting down background thread pools and closing sockets.

---

## 6. Localization

New localized strings in `Language.java`:
* English:
  * `onlineMultiplayer`: "Online Multiplayer"
  * `hostGame`: "Host Game"
  * `joinGame`: "Join Game"
  * `ipAddress`: "IP Address"
  * `port`: "Port"
  * `waitingForOpponent`: "Waiting for opponent to connect..."
  * `connecting`: "Connecting to host..."
  * `connected`: "Connected"
  * `connectionLost`: "Connection lost"
  * `yourTurn`: "Your Turn"
  * `opponentTurn`: "Opponent's Turn"
  * `chat`: "Chat"
  * `send`: "Send"
  * `surrender`: "Surrender"
  * `rematch`: "Rematch"
* Vietnamese:
  * `onlineMultiplayer`: "Chơi trực tuyến"
  * `hostGame`: "Tạo phòng"
  * `joinGame`: "Vào phòng"
  * `ipAddress`: "Địa chỉ IP"
  * `port`: "Cổng"
  * `waitingForOpponent`: "Đang chờ đối thủ tham gia..."
  * `connecting`: "Đang kết nối tới chủ phòng..."
  * `connected`: "Đã kết nối"
  * `connectionLost`: "Mất kết nối"
  * `yourTurn`: "Lượt của bạn"
  * `opponentTurn`: "Lượt của đối thủ"
  * `chat`: "Trò chuyện"
  * `send`: "Gửi"
  * `surrender`: "Đầu hàng"
  * `rematch`: "Đấu lại"
