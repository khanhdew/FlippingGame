package com.khanhdew.flipping.utils;

import java.util.ArrayList;
import java.util.List;

public class PlayerRegister {
    private List<String> playerTypes;

    public PlayerRegister() {
    }

    public List<String> getPlayerTypes() {
        return playerTypes;
    }

    public void setLanguage(Language language) {
        if(language == Language.ENGLISH) {
            playerTypes = new ArrayList<>(List.of("Human", "Easy AI", "Minimax AI"));
        } else {
            playerTypes = new ArrayList<>(List.of("Người chơi", "Máy dễ", "Minimax AI"));
        }
        playerTypes.add("Greedy AI");
    }
}
