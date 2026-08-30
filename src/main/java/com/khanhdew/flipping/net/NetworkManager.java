package com.khanhdew.flipping.net;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class NetworkManager {
    private static NetworkManager instance;

    private volatile NetworkRole role = NetworkRole.NONE;
    private volatile ServerSocket serverSocket;
    private volatile GameConnection connection;
    private volatile String localPlayerName = "Player";
    private volatile NetworkEventListener eventListener;
    private final ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "NetworkManager-Worker");
            t.setDaemon(true);
            return t;
        }
    });

    public NetworkManager() {}

    public static synchronized NetworkManager getInstance() {
        if (instance == null) {
            instance = new NetworkManager();
        }
        return instance;
    }

    public synchronized void setEventListener(NetworkEventListener listener) {
        this.eventListener = listener;
    }

    public NetworkEventListener getEventListener() {
        return this.eventListener;
    }

    public synchronized void startHost(int port, String hostName, int rows, int cols, NetworkEventListener listener) {
        disconnect();
        this.role = NetworkRole.HOST;
        this.localPlayerName = (hostName == null || hostName.trim().isEmpty()) ? "Player 1" : hostName.trim();
        this.eventListener = listener;

        executor.submit(() -> {
            try {
                serverSocket = new ServerSocket(port);
                Socket clientSocket = serverSocket.accept();

                NetworkEventListener hostInternalListener = new NetworkEventListener() {
                    @Override
                    public void onConnected(String opponentName, int r, int c, int assignedPlayerId) {
                        NetworkEventListener l = eventListener;
                        if (l != null) l.onConnected(opponentName, rows, cols, 1);
                    }
                    @Override public void onMoveReceived(int row, int col) {
                        NetworkEventListener l = eventListener;
                        if (l != null) l.onMoveReceived(row, col);
                    }
                    @Override public void onChatReceived(String sender, String message) {
                        NetworkEventListener l = eventListener;
                        if (l != null) l.onChatReceived(sender, message);
                    }
                    @Override public void onRestartRequested() {
                        NetworkEventListener l = eventListener;
                        if (l != null) l.onRestartRequested();
                    }
                    @Override public void onRestartAccepted() {
                        NetworkEventListener l = eventListener;
                        if (l != null) l.onRestartAccepted();
                    }
                    @Override public void onRestartDeclined() {
                        NetworkEventListener l = eventListener;
                        if (l != null) l.onRestartDeclined();
                    }
                    @Override public void onOpponentSurrendered() {
                        NetworkEventListener l = eventListener;
                        if (l != null) l.onOpponentSurrendered();
                    }
                    @Override public void onDisconnected(String reason) {
                        role = NetworkRole.NONE;
                        NetworkEventListener l = eventListener;
                        if (l != null) l.onDisconnected(reason);
                    }
                    @Override public void onError(String errorMessage) {
                        NetworkEventListener l = eventListener;
                        if (l != null) l.onError(errorMessage);
                    }
                };

                connection = new GameConnection(clientSocket, hostInternalListener);
                connection.startListening();

                // Send initial handshake
                connection.send(ProtocolMessage.encodeHandshake(localPlayerName, rows, cols));

            } catch (IOException e) {
                if (role == NetworkRole.HOST) {
                    NetworkEventListener l = eventListener;
                    if (l != null) l.onError("Host server error: " + e.getMessage());
                }
                disconnect();
            }
        });
    }

    public synchronized void connectToHost(String hostIp, int port, String clientName, NetworkEventListener listener) {
        disconnect();
        this.role = NetworkRole.CLIENT;
        this.localPlayerName = (clientName == null || clientName.trim().isEmpty()) ? "Player 2" : clientName.trim();
        this.eventListener = listener;

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
                        NetworkEventListener l = eventListener;
                        if (l != null) l.onConnected(opponentName, rows, cols, 2);
                    }
                    @Override public void onMoveReceived(int row, int col) {
                        NetworkEventListener l = eventListener;
                        if (l != null) l.onMoveReceived(row, col);
                    }
                    @Override public void onChatReceived(String sender, String message) {
                        NetworkEventListener l = eventListener;
                        if (l != null) l.onChatReceived(sender, message);
                    }
                    @Override public void onRestartRequested() {
                        NetworkEventListener l = eventListener;
                        if (l != null) l.onRestartRequested();
                    }
                    @Override public void onRestartAccepted() {
                        NetworkEventListener l = eventListener;
                        if (l != null) l.onRestartAccepted();
                    }
                    @Override public void onRestartDeclined() {
                        NetworkEventListener l = eventListener;
                        if (l != null) l.onRestartDeclined();
                    }
                    @Override public void onOpponentSurrendered() {
                        NetworkEventListener l = eventListener;
                        if (l != null) l.onOpponentSurrendered();
                    }
                    @Override public void onDisconnected(String reason) {
                        role = NetworkRole.NONE;
                        NetworkEventListener l = eventListener;
                        if (l != null) l.onDisconnected(reason);
                    }
                    @Override public void onError(String errorMessage) {
                        NetworkEventListener l = eventListener;
                        if (l != null) l.onError(errorMessage);
                    }
                };

                connection = new GameConnection(socket, clientInternalListener);
                connection.startListening();

            } catch (IOException e) {
                NetworkEventListener l = eventListener;
                if (l != null) l.onError("Connection failed: " + e.getMessage());
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
                if (connection.isConnected()) {
                    connection.send(ProtocolMessage.encodeDisconnect());
                }
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
            if (interfaces != null) {
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
            }
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }
}
