package com.khanhdew.flipping.view;

import com.khanhdew.flipping.net.NetworkManager;
import com.khanhdew.flipping.utils.Language;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class ChatPaneTest {

    @BeforeAll
    static void initJavaFX() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            // Platform already initialized
        }
    }

    @Test
    void testChatPaneCreationEnglishAndVietnamese() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);

        Platform.runLater(() -> {
            ChatPane englishPane = new ChatPane(Language.ENGLISH);
            assertNotNull(englishPane);
            assertEquals(280, englishPane.getPrefWidth());
            assertEquals("Send", englishPane.getSendButton().getText());
            latch.countDown();
        });

        Platform.runLater(() -> {
            ChatPane vietnamesePane = new ChatPane(Language.VIETNAMESE);
            assertNotNull(vietnamesePane);
            assertEquals(280, vietnamesePane.getPrefWidth());
            assertEquals("Gửi", vietnamesePane.getSendButton().getText());
            latch.countDown();
        });

        assertTrue(latch.await(3, TimeUnit.SECONDS));
    }

    @Test
    void testAppendMessage() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<ChatPane> paneRef = new AtomicReference<>();

        Platform.runLater(() -> {
            ChatPane chatPane = new ChatPane(Language.ENGLISH);
            paneRef.set(chatPane);
            chatPane.appendMessage("Alice", "Hello World!");

            Platform.runLater(() -> {
                assertEquals(1, chatPane.getMessageList().getItems().size());
                String item = chatPane.getMessageList().getItems().get(0);
                assertTrue(item.contains("Alice: Hello World!"), "Item should contain sender and message: " + item);
                assertTrue(item.matches("\\[\\d{2}:\\d{2}\\] Alice: Hello World!"), "Item should match timestamp format: " + item);
                latch.countDown();
            });
        });

        assertTrue(latch.await(3, TimeUnit.SECONDS));
    }

    @Test
    void testSendMessageNormalAndClearInput() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            ChatPane chatPane = new ChatPane(Language.ENGLISH);
            chatPane.getInputField().setText("Testing chat message");
            chatPane.getSendButton().fire();

            Platform.runLater(() -> {
                assertEquals("", chatPane.getInputField().getText(), "Input field should be cleared");
                assertEquals(1, chatPane.getMessageList().getItems().size());
                String item = chatPane.getMessageList().getItems().get(0);
                assertTrue(item.contains("Testing chat message"));
                latch.countDown();
            });
        });

        assertTrue(latch.await(3, TimeUnit.SECONDS));
    }

    @Test
    void testSendMessageTruncationOver200Chars() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        String longText = "A".repeat(250);

        Platform.runLater(() -> {
            ChatPane chatPane = new ChatPane(Language.ENGLISH);
            chatPane.getInputField().setText(longText);
            chatPane.sendMessage();

            Platform.runLater(() -> {
                assertEquals(1, chatPane.getMessageList().getItems().size());
                String item = chatPane.getMessageList().getItems().get(0);
                String expectedTruncated = "A".repeat(200);
                assertTrue(item.contains(expectedTruncated));
                assertFalse(item.contains("A".repeat(201)));
                latch.countDown();
            });
        });

        assertTrue(latch.await(3, TimeUnit.SECONDS));
    }

    @Test
    void testSendMessageEmptyOrWhitespaceIgnored() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            ChatPane chatPane = new ChatPane(Language.ENGLISH);
            chatPane.getInputField().setText("   ");
            chatPane.sendMessage();

            Platform.runLater(() -> {
                assertEquals(0, chatPane.getMessageList().getItems().size(), "Empty/whitespace message should not be sent");
                latch.countDown();
            });
        });

        assertTrue(latch.await(3, TimeUnit.SECONDS));
    }

    @Test
    void testSetNetworkManagerAndNullSafety() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            ChatPane chatPane = new ChatPane(null);
            NetworkManager customNetManager = new NetworkManager();
            chatPane.setNetworkManager(customNetManager);

            chatPane.appendMessage(null, null);

            Platform.runLater(() -> {
                assertEquals(1, chatPane.getMessageList().getItems().size());
                String item = chatPane.getMessageList().getItems().get(0);
                assertTrue(item.contains("Unknown: "));
                latch.countDown();
            });
        });

        assertTrue(latch.await(3, TimeUnit.SECONDS));
    }
}
