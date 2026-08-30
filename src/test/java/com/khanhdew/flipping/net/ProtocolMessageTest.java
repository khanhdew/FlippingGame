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
    void testEncodeAndParseHandshakeAck() {
        String encoded = ProtocolMessage.encodeHandshakeAck("Bob");
        assertEquals("HANDSHAKE_ACK Bob", encoded);

        AtomicReference<String> oppName = new AtomicReference<>();
        AtomicInteger pId = new AtomicInteger();

        NetworkEventListener listener = new NetworkEventListener() {
            @Override
            public void onConnected(String opponentName, int rows, int cols, int assignedPlayerId) {
                oppName.set(opponentName);
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
        assertEquals("Bob", oppName.get());
        assertEquals(1, pId.get()); // Host remains player 1
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
    void testRestartProtocols() {
        AtomicBoolean req = new AtomicBoolean(false);
        AtomicBoolean ack = new AtomicBoolean(false);
        AtomicBoolean decl = new AtomicBoolean(false);

        NetworkEventListener listener = new NetworkEventListener() {
            @Override public void onConnected(String opponentName, int rows, int cols, int assignedPlayerId) {}
            @Override public void onMoveReceived(int row, int col) {}
            @Override public void onChatReceived(String sender, String message) {}
            @Override public void onRestartRequested() { req.set(true); }
            @Override public void onRestartAccepted() { ack.set(true); }
            @Override public void onRestartDeclined() { decl.set(true); }
            @Override public void onOpponentSurrendered() {}
            @Override public void onDisconnected(String reason) {}
            @Override public void onError(String errorMessage) {}
        };

        ProtocolMessage.parseAndDispatch(ProtocolMessage.encodeRestartReq(), listener);
        assertTrue(req.get());

        ProtocolMessage.parseAndDispatch(ProtocolMessage.encodeRestartAck(), listener);
        assertTrue(ack.get());

        ProtocolMessage.parseAndDispatch(ProtocolMessage.encodeRestartDecl(), listener);
        assertTrue(decl.get());
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

    @Test
    void testPingPong() {
        assertEquals("PING", ProtocolMessage.encodePing());
        assertEquals("PONG", ProtocolMessage.encodePong());

        NetworkEventListener listener = new NetworkEventListener() {
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

        // Should not throw exceptions or trigger error callbacks
        ProtocolMessage.parseAndDispatch(ProtocolMessage.encodePing(), listener);
        ProtocolMessage.parseAndDispatch(ProtocolMessage.encodePong(), listener);
    }

    @Test
    void testMalformedMessageTriggersError() {
        AtomicReference<String> errorRef = new AtomicReference<>();

        NetworkEventListener listener = new NetworkEventListener() {
            @Override public void onConnected(String opponentName, int rows, int cols, int assignedPlayerId) {}
            @Override public void onMoveReceived(int row, int col) {}
            @Override public void onChatReceived(String sender, String message) {}
            @Override public void onRestartRequested() {}
            @Override public void onRestartAccepted() {}
            @Override public void onRestartDeclined() {}
            @Override public void onOpponentSurrendered() {}
            @Override public void onDisconnected(String reason) {}
            @Override public void onError(String errorMessage) {
                errorRef.set(errorMessage);
            }
        };

        // Invalid integer in MOVE
        ProtocolMessage.parseAndDispatch("MOVE abc def", listener);
        assertNotNull(errorRef.get());
        assertTrue(errorRef.get().contains("Protocol parse error"));
    }

    @Test
    void testNullAndEmptyHandling() {
        NetworkEventListener listener = new NetworkEventListener() {
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

        ProtocolMessage.parseAndDispatch(null, listener);
        ProtocolMessage.parseAndDispatch("", listener);
        ProtocolMessage.parseAndDispatch("   ", listener);
        ProtocolMessage.parseAndDispatch("MOVE 1 1", null); // null listener safely ignored
    }

    @Test
    void testPrivateConstructor() throws Exception {
        java.lang.reflect.Constructor<ProtocolMessage> constructor = ProtocolMessage.class.getDeclaredConstructor();
        assertTrue(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()), "Constructor should be private");
        constructor.setAccessible(true);
        ProtocolMessage instance = constructor.newInstance();
        assertNotNull(instance);
    }
}
