package com.khanhdew.flipping.utils;

import java.util.ArrayList;
import java.util.List;

public class PlayerRegister {
    private List<String> playerTypes;
    private static PlayerRegister instance;
    private Language language;

    public PlayerRegister(Language language) {
        this.language = language;
        if(language == Language.ENGLISH) {
            playerTypes = new ArrayList<>(List.of("Human", "Easy AI", "Minimax AI"));
        } else if(language == Language.VIETNAMESE) {
            playerTypes = new ArrayList<>(List.of("Người chơi", "Máy dễ", "Minimax AI"));
        }
        playerTypes.add("Greedy AI");
    }

    public PlayerRegister(){

    }

    public List<String> getPlayerTypes() {
        return playerTypes;
    }

    public void setLanguage(Language language) {

    }


    public static PlayerRegister getInstance(){
        if(instance == null)
            return new PlayerRegister(Language.ENGLISH);
        return instance;
    }

}
