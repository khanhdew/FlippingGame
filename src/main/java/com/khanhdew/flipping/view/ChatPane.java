package com.khanhdew.flipping.view;

import com.google.common.collect.BiMap;
import com.khanhdew.flipping.net.NetworkManager;
import com.khanhdew.flipping.utils.Language;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
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
        BiMap<String, String> langMap = language != null ? language.getLanguage() : null;
        this.setPadding(new Insets(5));
        this.setSpacing(5);
        this.setPrefWidth(280);

        String chatText = (langMap != null && langMap.containsKey("chat")) ? langMap.get("chat") : "Chat";
        Label chatTitle = new Label(chatText);
        chatTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        messageList = new ListView<>();
        messageList.setPrefHeight(160);
        VBox.setVgrow(messageList, Priority.ALWAYS);

        inputField = new TextField();
        inputField.setPromptText("...");
        HBox.setHgrow(inputField, Priority.ALWAYS);

        String sendText = (langMap != null && langMap.containsKey("send")) ? langMap.get("send") : "Send";
        sendButton = new Button(sendText);
        sendButton.setOnAction(e -> sendMessage());
        inputField.setOnAction(e -> sendMessage());

        HBox inputBar = new HBox(5, inputField, sendButton);
        this.getChildren().addAll(chatTitle, messageList, inputBar);
    }

    void sendMessage() {
        if (inputField.getText() == null) {
            return;
        }
        String text = inputField.getText().trim();
        if (!text.isEmpty()) {
            if (text.length() > 200) {
                text = text.substring(0, 200);
            }
            if (networkManager != null) {
                networkManager.sendChat(text);
                String sender = networkManager.getLocalPlayerName();
                appendMessage(sender != null ? sender : "Player", text);
            }
            inputField.clear();
        }
    }

    public void appendMessage(String sender, String message) {
        final String safeSender = sender != null ? sender : "Unknown";
        final String safeMessage = message != null ? message : "";
        Platform.runLater(() -> {
            String time = LocalTime.now().format(timeFormatter);
            String entry = "[" + time + "] " + safeSender + ": " + safeMessage;
            messageList.getItems().add(entry);
            messageList.scrollTo(messageList.getItems().size() - 1);
        });
    }

    public void setNetworkManager(NetworkManager networkManager) {
        this.networkManager = networkManager;
    }

    public ListView<String> getMessageList() {
        return messageList;
    }

    public TextField getInputField() {
        return inputField;
    }

    public Button getSendButton() {
        return sendButton;
    }
}
