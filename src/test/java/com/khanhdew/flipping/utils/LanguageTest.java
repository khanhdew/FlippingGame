package com.khanhdew.flipping.utils;

import com.google.common.collect.BiMap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LanguageTest {

    @Test
    void testOnlineKeysEnglishAndVietnamese() {
        BiMap<String, String> en = Language.ENGLISH.getLanguage();
        BiMap<String, String> vi = Language.VIETNAMESE.getLanguage();

        String[] requiredKeys = {
            "onlineMultiplayer", "hostGame", "joinGame", "ipAddress",
            "port", "waitingForOpponent", "connecting", "connected",
            "connectionLost", "yourTurn", "opponentTurn", "chat",
            "send", "surrender", "rematch", "rematchPrompt"
        };

        for (String key : requiredKeys) {
            assertTrue(en.containsKey(key), "English missing key: " + key);
            assertTrue(vi.containsKey(key), "Vietnamese missing key: " + key);
            assertNotNull(en.get(key));
            assertNotNull(vi.get(key));
        }
    }
}
