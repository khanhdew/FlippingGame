package com.khanhdew.flipping.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class GameConnection {
    private final Socket socket;
    private final NetworkEventListener listener;
    private final BufferedReader reader;
    private final PrintWriter writer;
    private Thread listenerThread;
    private volatile boolean running = false;

    public GameConnection(Socket socket, NetworkEventListener listener) throws IOException {
        this.socket = socket;
        this.listener = listener;
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        this.writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
    }

    public synchronized void startListening() {
        if (running) return;
        running = true;

        listenerThread = new Thread(() -> {
            try {
                String line;
                while (running && (line = reader.readLine()) != null) {
                    ProtocolMessage.parseAndDispatch(line, listener);
                }
                if (running) {
                    listener.onDisconnected("Connection closed by remote peer.");
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
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ignored) {}
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (IOException ignored) {}
        if (writer != null) {
            writer.close();
        }
    }
}
