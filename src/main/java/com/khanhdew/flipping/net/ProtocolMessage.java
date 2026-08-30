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
        String safeText = (text == null) ? "" : text;
        String encodedText = Base64.getEncoder().encodeToString(safeText.getBytes(StandardCharsets.UTF_8));
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
