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
