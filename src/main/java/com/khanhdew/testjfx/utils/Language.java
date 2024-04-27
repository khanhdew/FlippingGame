package com.khanhdew.testjfx.utils;


import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public enum Language {
    ENGLISH, VIETNAMESE;
    public static BiMap<String, String> english = HashBiMap.create();
    public static BiMap<String, String> vietnamese = HashBiMap.create();

    public BiMap<String, String> getLanguage() {
        initEnglish();
        initVietnamese();
        switch (this) {
            case ENGLISH:
                return english;
            case VIETNAMESE:
                return vietnamese;
            default:
                return english;
        }
    }

    public void initEnglish() {
        english.put("welcome", "Welcome to Flipping");
        english.put("human", "Human");
        english.put("easyai", "Easy AI");
        english.put("newgame", "New Game");
        english.put("exit", "Exit");
        english.put("row", "Row");
        english.put("col", "Column");
        english.put("error", "Error");
        english.put("player1", "Player 1");
        english.put("player2", "Player 2");
        english.put("choose", "Choose");
        english.put("language", "Language");
        english.put("invalidSelection", "Invalid selection");
        english.put("invalidInput", "Invalid input");
        english.put("adviceSelection", "Please select a valid option");
        english.put("adviceInput", "Please enter valid values for the fields");
        english.put("gameOver", "Game Over");
        english.put("win", "Wins!");
        english.put("winContext", " has won the game with a score of ");
        english.put("draw", "Draw!");
        english.put("drawContext", "The game has ended in a draw with both players scoring ");
        english.put("player1Name", "Player 1 Name");
        english.put("player2Name", "Player 2 Name");
        english.put("reset", "Reset");
        english.put("setting", "Settings");
        english.put("undo", "Undo");
    }

    public void initVietnamese() {
        vietnamese.put("welcome", "Chào mừng đến với Flipping");
        vietnamese.put("human", "Người chơi");
        vietnamese.put("easyai", "Máy dễ");
        vietnamese.put("newgame", "Trò chơi mới");
        vietnamese.put("exit", "Thoát");
        vietnamese.put("row", "Hàng");
        vietnamese.put("col", "Cột");
        vietnamese.put("error", "Lỗi");
        vietnamese.put("player1", "Người chơi 1");
        vietnamese.put("player2", "Người chơi 2");
        vietnamese.put("choose", "Chọn");
        vietnamese.put("language", "Ngôn ngữ");
        vietnamese.put("invalidSelection", "Lựa chọn không hợp lệ");
        vietnamese.put("invalidInput", "Dữ liệu không hợp lệ");
        vietnamese.put("adviceSelection", "Vui lòng chọn một lựa chọn hợp lệ");
        vietnamese.put("adviceInput", "Vui lòng nhập giá trị hợp lệ cho các trường");
        vietnamese.put("gameOver", "Kết thúc trò chơi");
        vietnamese.put("win", "Thắng!");
        vietnamese.put("winContext", " đã thắng trận đấu với số điểm là ");
        vietnamese.put("draw", "Hòa!");
        vietnamese.put("drawContext", "Trận đấu kết thúc hòa với cả hai người chơi đều có số điểm là ");
        vietnamese.put("player1Name", "Tên người 1");
        vietnamese.put("player2Name", "Tên người 2");
        vietnamese.put("reset", "Làm mới");
        vietnamese.put("setting", "Cài đặt");
        vietnamese.put("undo", "Hoàn tác");
    }
}
