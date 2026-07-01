package com.masl.goofy_protocol_fis_be.crypto;

import com.masl.goofy_protocol_core.crypto.connected.GenericHandleCrypto;
import com.masl.goofy_protocol_core.crypto.connected.HandleCryptoHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class HandleHelper implements HandleCryptoHelper {
    private static final String HANDLE_WORDS_PATH = "data/handle_words.json";
    private static final Logger log = LoggerFactory.getLogger(HandleHelper.class);

    public HandleHelper() {
        // TODO: Init Stuff?
    }

    // Load Word List (Currently ~15000 Entries)
    // Stored in resources/data/handle_words.json
    @Override
    synchronized public List<String> loadWordList() {
        try {
            ClassPathResource resource = new ClassPathResource(HANDLE_WORDS_PATH);
            ObjectMapper mapper = new ObjectMapper();
            String[] words = mapper.readValue(resource.getInputStream(), String[].class);
            List<String> wordList = new ArrayList<>(Arrays.asList(words));
            log.debug("Loaded {} words for handle generation", wordList.size());
            return wordList;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load handle words from " + HANDLE_WORDS_PATH, e);
        }
    }

    @Override
    public Map<String, String> loadPersistedKeyToHandleMapCache() {
        // TODO: Implement
        return Map.of();
    }

    @Override
    public boolean storePersistedKeyToHandleMapCache(Map<String, String> keyToHandleMap) {
        // TODO: Implement
        return false;
    }

    @Override
    public Map<String, String> loadUserKeyToHandleMap() {
        // TODO: Implement
        return Map.of();
    }

    @Override
    public String lookupPubSplitKeyForHandleExternally(String handle) {
        String strippedHandle = GenericHandleCrypto.stripPotentialDomainFromHandle(handle);
        String optDomain = GenericHandleCrypto.getPotentialDomainFromHandle(handle);

        // Check internal Storage / DBs for potential Mappings
        // TODO: Implement

        // Potential Look up
        if (optDomain == null) {
            // TODO: Look up, by asking the domain FIS
            return null;
        } else {
            // TODO: Potentially ask already known FIS Servers or throw Exception
            return null;
        }
    }
}
