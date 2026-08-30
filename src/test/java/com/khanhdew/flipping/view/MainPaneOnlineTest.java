package com.khanhdew.flipping.view;

import com.khanhdew.flipping.model.Piece;
import com.khanhdew.flipping.model.PieceState;
import com.khanhdew.flipping.net.NetworkEventListener;
import com.khanhdew.flipping.net.NetworkManager;
import com.khanhdew.flipping.utils.Language;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class MainPaneOnlineTest {

    @BeforeAll
    static void initJavaFX() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            // Platform already initialized
        }
    }

    static class TestNetworkManager extends NetworkManager {
        final AtomicInteger lastSentRow = new AtomicInteger(-1);
        final AtomicInteger lastSentCol = new AtomicInteger(-1);
        final AtomicReference<String> lastSentChat = new AtomicReference<>();
        final AtomicInteger restartReqCount = new AtomicInteger(0);
        final AtomicInteger restartAckCount = new AtomicInteger(0);
        final AtomicInteger surrenderCount = new AtomicInteger(0);

        @Override
        public void sendMove(int row, int col) {
            lastSentRow.set(row);
            lastSentCol.set(col);
        }

        @Override
        public void sendChat(String message) {
            lastSentChat.set(message);
        }

        @Override
        public void sendRestartReq() {
            restartReqCount.incrementAndGet();
        }

        @Override
        public void sendRestartAck() {
            restartAckCount.incrementAndGet();
        }

        @Override
        public void sendSurrender() {
            surrenderCount.incrementAndGet();
        }
    }

    @Test
    void testOnlineModeFlagsAndRoleSettings() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            MainPane mainPane = new MainPane("human", "human", Language.ENGLISH);
            assertFalse(mainPane.isOnlineMode(), "Default online mode should be false");
            assertEquals(1, mainPane.getLocalPlayerId());

            mainPane.setOnlineMode(true, 2);
            assertTrue(mainPane.isOnlineMode(), "Online mode should be true");
            assertEquals(2, mainPane.getLocalPlayerId(), "Local player ID should be 2");

            latch.countDown();
        });

        assertTrue(latch.await(3, TimeUnit.SECONDS));
    }

    @Test
    void testChatPaneEmbeddingAndNetworkListenerWiring() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            MainPane mainPane = new MainPane("human", "human", Language.ENGLISH);
            TestNetworkManager testNetManager = new TestNetworkManager();
            mainPane.setNetworkManager(testNetManager);
            mainPane.setOnlineMode(true, 1);
            mainPane.init(8, 8);

            assertNotNull(mainPane.getChatPane(), "ChatPane should be initialized in online mode");
            assertNotNull(mainPane.getScorePane().getBottom(), "ChatPane should be embedded in bottom of scorePane");
            assertEquals(mainPane.getChatPane(), mainPane.getScorePane().getBottom());

            NetworkEventListener listener = testNetManager.getEventListener();
            assertNotNull(listener, "NetworkEventListener should be registered on NetworkManager");

            // Test onChatReceived
            listener.onChatReceived("Opponent", "Hello, good luck!");

            Platform.runLater(() -> {
                assertEquals(1, mainPane.getChatPane().getMessageList().getItems().size());
                assertTrue(mainPane.getChatPane().getMessageList().getItems().get(0).contains("Opponent: Hello, good luck!"));
                latch.countDown();
            });
        });

        assertTrue(latch.await(3, TimeUnit.SECONDS));
    }

    @Test
    void testTurnGatingLocalPlayerMoveTransmitted() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            MainPane mainPane = new MainPane("human", "human", Language.ENGLISH);
            TestNetworkManager testNetManager = new TestNetworkManager();
            mainPane.setNetworkManager(testNetManager);
            mainPane.setOnlineMode(true, 1); // Local player is 1 (BLACK)
            mainPane.init(8, 8);

            assertEquals(1, mainPane.getTurn(), "Initial turn should be 1");

            // (2, 4) is a legal opening move for Player 1 (BLACK) in 8x8 Reversi
            mainPane.getMouse().setRow(2);
            mainPane.getMouse().setCol(4);
            mainPane.getMouse().x = 450;
            mainPane.getMouse().y = 250;
            mainPane.getMouse().mouseClicked = true;

            mainPane.manageTurn();

            assertEquals(2, mainPane.getTurn(), "Turn should change to Player 2 after legal local move");
            assertEquals(2, testNetManager.lastSentRow.get(), "Transmitted move row should be 2");
            assertEquals(4, testNetManager.lastSentCol.get(), "Transmitted move col should be 4");

            latch.countDown();
        });

        assertTrue(latch.await(3, TimeUnit.SECONDS));
    }

    @Test
    void testTurnGatingOpponentTurnIgnoredLocally() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            MainPane mainPane = new MainPane("human", "human", Language.ENGLISH);
            TestNetworkManager testNetManager = new TestNetworkManager();
            mainPane.setNetworkManager(testNetManager);
            mainPane.setOnlineMode(true, 2); // Local player is 2 (WHITE), but initial turn is 1 (BLACK)
            mainPane.init(8, 8);

            assertEquals(1, mainPane.getTurn(), "Initial turn is Player 1");

            // Local user tries to click at (2, 4) during opponent's turn
            mainPane.getMouse().setRow(2);
            mainPane.getMouse().setCol(4);
            mainPane.getMouse().x = 450;
            mainPane.getMouse().y = 250;
            mainPane.getMouse().mouseClicked = true;

            mainPane.manageTurn();

            assertEquals(1, mainPane.getTurn(), "Turn must remain 1 when local click occurs on opponent turn");
            assertEquals(-1, testNetManager.lastSentRow.get(), "No move should be transmitted over network");
            assertEquals(-1, testNetManager.lastSentCol.get(), "No move should be transmitted over network");

            latch.countDown();
        });

        assertTrue(latch.await(3, TimeUnit.SECONDS));
    }

    @Test
    void testApplyRemoteMoveAndTurnSwitch() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            MainPane mainPane = new MainPane("human", "human", Language.ENGLISH);
            TestNetworkManager testNetManager = new TestNetworkManager();
            mainPane.setNetworkManager(testNetManager);
            mainPane.setOnlineMode(true, 1); // Local player is 1
            mainPane.init(8, 8);

            // First, local player 1 plays at (2, 4)
            mainPane.getMouse().setRow(2);
            mainPane.getMouse().setCol(4);
            mainPane.getMouse().x = 450;
            mainPane.getMouse().y = 250;
            mainPane.manageTurn();

            assertEquals(2, mainPane.getTurn(), "Turn is now Player 2 (opponent)");

            // Opponent sends remote move at (2, 3)
            NetworkEventListener listener = testNetManager.getEventListener();
            assertNotNull(listener);
            listener.onMoveReceived(2, 3);

            Platform.runLater(() -> {
                assertEquals(1, mainPane.getTurn(), "Turn should switch back to Player 1 after remote move");
                assertEquals(2, mainPane.getMatrix()[2][3], "Cell (2, 3) should now be Player 2's piece (WHITE)");
                latch.countDown();
            });
        });

        assertTrue(latch.await(3, TimeUnit.SECONDS));
    }

    @Test
    void testRestartAcceptedResetsGameBoard() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            MainPane mainPane = new MainPane("human", "human", Language.ENGLISH);
            TestNetworkManager testNetManager = new TestNetworkManager();
            mainPane.setNetworkManager(testNetManager);
            mainPane.setOnlineMode(true, 1);
            mainPane.init(8, 8);

            // Make a move
            mainPane.getMouse().setRow(2);
            mainPane.getMouse().setCol(4);
            mainPane.getMouse().x = 450;
            mainPane.getMouse().y = 250;
            mainPane.manageTurn();
            assertEquals(2, mainPane.getTurn());

            NetworkEventListener listener = testNetManager.getEventListener();
            listener.onRestartAccepted();

            Platform.runLater(() -> {
                assertEquals(1, mainPane.getTurn(), "Game turn should reset to 1 on rematch accept");
                assertEquals(0, mainPane.getMatrix()[2][4], "Cell (2, 4) should be reset to empty");
                latch.countDown();
            });
        });

        assertTrue(latch.await(3, TimeUnit.SECONDS));
    }

    @Test
    void testButtonPaneControllerOnlineModeConfiguration() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            MainPane mainPane = new MainPane("human", "human", Language.ENGLISH);
            TestNetworkManager testNetManager = new TestNetworkManager();
            mainPane.setNetworkManager(testNetManager);
            mainPane.setOnlineMode(true, 1);
            mainPane.init(8, 8);

            ButtonPaneController controller = mainPane.getButtonPaneController();
            assertNotNull(controller, "ButtonPaneController should be loaded");

            assertTrue(controller.undo.isDisable(), "Undo should be disabled in online mode");
            assertTrue(controller.saveGame.isDisable(), "Save game should be disabled in online mode");
            assertTrue(controller.loadSavedGame.isDisable(), "Load game should be disabled in online mode");
            assertTrue(controller.player1.isDisable(), "Player 1 select should be disabled in online mode");
            assertTrue(controller.player2.isDisable(), "Player 2 select should be disabled in online mode");
            assertTrue(controller.row.isDisable(), "Row input should be disabled in online mode");
            assertTrue(controller.col.isDisable(), "Col input should be disabled in online mode");

            assertEquals("Rematch", controller.newGame.getText(), "New game button should say Rematch");
            assertEquals("Surrender", controller.reset.getText(), "Reset button should say Surrender");

            latch.countDown();
        });

        assertTrue(latch.await(3, TimeUnit.SECONDS));
    }

    @Test
    void testDisconnectionBannerUpdate() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            MainPane mainPane = new MainPane("human", "human", Language.ENGLISH);
            TestNetworkManager testNetManager = new TestNetworkManager();
            mainPane.setNetworkManager(testNetManager);
            mainPane.setOnlineMode(true, 1);
            mainPane.init(8, 8);

            NetworkEventListener listener = testNetManager.getEventListener();
            listener.onDisconnected("Remote peer disconnected");

            Platform.runLater(() -> {
                assertNotNull(mainPane.getOnlineStatusBanner());
                assertEquals("Disconnected", mainPane.getOnlineStatusBanner().getText());
                latch.countDown();
            });
        });

        assertTrue(latch.await(3, TimeUnit.SECONDS));
    }
}
