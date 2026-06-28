package com.masl.goofy_protocol_fis_be.crypto;

import com.masl.goofy_protocol_fis_be.crypto.asymm.AsymmCrypto;
import com.masl.goofy_protocol_fis_be.crypto.asymm.GlobAsymmCrypto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HandleCrypto {
    private static final String HANDLE_WORDS_PATH = "data/handle_words.json";

    private static final Logger log = LoggerFactory.getLogger(HandleCrypto.class);
    private final GlobAsymmCrypto asymmCrypto = new GlobAsymmCrypto();

    private List<String> wordList;
    private Map<String, String> userKeyToHandleCache;
    private Map<String, String> generalKeyToHandleCache;
    private Map<String, String> sharedHandleToKeyCache;

    public HandleCrypto() throws IOException {
        this(true);
    }

    public HandleCrypto(boolean realEnv) throws IOException {
        userKeyToHandleCache = new ConcurrentHashMap<>();
        generalKeyToHandleCache = new ConcurrentHashMap<>();
        sharedHandleToKeyCache = new ConcurrentHashMap<>();
        wordList = new ArrayList<>();

        loadWordList();
        if (realEnv) {
            loadUserKeyToHandle();
            loadGeneralKeyToHandle();
            generateHandleToKeyMapping();
        }
    }

    // Load Word List (Currently ~15000 Entries)
    // Stored in resources/data/handle_words.json
    synchronized public void loadWordList() throws IOException {
        ClassPathResource resource = new ClassPathResource(HANDLE_WORDS_PATH);
        ObjectMapper mapper = new ObjectMapper();
        String[] words = mapper.readValue(resource.getInputStream(), String[].class);
        wordList = new ArrayList<>(Arrays.asList(words));
        log.info("Loaded {} words for handle generation", wordList.size());
    }

    // Persistence

    synchronized public void loadUserKeyToHandle() {
        userKeyToHandleCache = new ConcurrentHashMap<>();
        // TODO: Load in keys from registered/known users into extra cache
    }

    synchronized public void loadGeneralKeyToHandle() {
        generalKeyToHandleCache = new ConcurrentHashMap<>();
        // TODO: Add some system to persist the cache
    }

    synchronized public void saveGeneralKeyToHandle() {
        // TODO: Add some system to persist the cache
    }

    // Generate Reverse Mappings

    synchronized public void generateHandleToKeyMapping() {
        sharedHandleToKeyCache = new ConcurrentHashMap<>();
        userKeyToHandleCache.forEach((pubSplitKey, handle) ->
                sharedHandleToKeyCache.putIfAbsent(handle, pubSplitKey));
        generalKeyToHandleCache.forEach((pubSplitKey, handle) ->
                sharedHandleToKeyCache.putIfAbsent(handle, pubSplitKey));
    }

    // Adding Entries

    public void addUserKeyToHandle(String pubSplitKey, String handle) {
        if (!verifyKeyAndHandle(pubSplitKey, handle, false))
            throw new IllegalArgumentException("Invalid key-handle pair");

        userKeyToHandleCache.putIfAbsent(pubSplitKey, handle);
        sharedHandleToKeyCache.putIfAbsent(handle, pubSplitKey);
    }

    public void addGeneralKeyToHandle(String pubSplitKey, String handle) {
        if (!verifyKeyAndHandle(pubSplitKey, handle, false))
            throw new IllegalArgumentException("Invalid key-handle pair");

        generalKeyToHandleCache.putIfAbsent(pubSplitKey, handle);
        sharedHandleToKeyCache.putIfAbsent(handle, pubSplitKey);
    }

    // Verification

    public boolean verifyKeyAndHandle(String pubSplitKey, String handle) {
        return verifyKeyAndHandle(pubSplitKey, handle, true);
    }

    public boolean verifyKeyAndHandle(AsymmCrypto.AsymmPubKeyPair pubSplitKey, String handle) {
        return verifyKeyAndHandle(pubSplitKey.serialize(), handle, true);
    }


    public boolean verifyKeyAndHandle(String pubSplitKey, String handle, boolean useCache) {
        if (useCache)
            return deriveHandle(pubSplitKey).equals(handle);
        return _internalDeriveHandle(pubSplitKey).equals(handle);
    }

    // Keypair to Handle Derivation

    public String deriveHandle(AsymmCrypto.AsymmPubKeyPair pubSplitKey) {
        return deriveHandle(pubSplitKey.serialize());
    }

    public String deriveHandle(String pubSplitKey) {
        if (userKeyToHandleCache.containsKey(pubSplitKey))
            return userKeyToHandleCache.get(pubSplitKey);

        return generalKeyToHandleCache.computeIfAbsent(pubSplitKey, key -> {
            String handle = _internalDeriveHandle(pubSplitKey);
            if (handle != null)
                sharedHandleToKeyCache.putIfAbsent(handle, pubSplitKey);
            return handle;
        });
    }

    synchronized public String _internalDeriveHandle(String pubSplitKey) {
        // Check if it can be parsed / is valid
        AsymmCrypto.AsymmPubKeyPair pair = AsymmCrypto.AsymmPubKeyPair.parse(pubSplitKey);
        if (!pair.isSigValid(asymmCrypto.forType(pair.type())))
            throw new IllegalArgumentException("Invalid pubSplitKey");

        // Strength of handles
        // c = 2 -> ~44 bit (15000^2 * 10^5 = 2.3e13)
        // c = 3 -> ~58 bit (15000^3 * 10^5 = 3.4e17)
        // c = 4 -> ~72 bit (15000^4 * 10^5 = 5.1e21)

        byte[] rootRnd = SecretUtils.symmSecretFromSecret(pubSplitKey, SecretUtils.DEFAULT_HANDLE_ROOT_SALT, 128, SecretUtils.DEFAULT_HANDLE_ROOT_ITERATIONS);
        if (rootRnd == null)
            throw new IllegalArgumentException("Invalid pubSplitKey (unable to derive rootRnd)");
        int _c = rootRnd[0] & 0xFF;
        int _n = ((rootRnd[1] & 0xFF) << 8) | (rootRnd[2] & 0xFF);

        // Sanity Check
        if (_c < 0 || _n < 0)
            throw new IllegalArgumentException("Invalid pubSplitKey (negative c or n)");

        // Put into Bounds
        int c = 2 + _c % 3;
        int n = _n % 100000;

        // Select and write the words
        byte[] lastBytes = rootRnd;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < c; i++) {
            String newSeed = Base64.getEncoder().encodeToString(lastBytes);
            byte[] wordRnd = SecretUtils.symmSecretFromSecret(newSeed, SecretUtils.DEFAULT_HANDLE_WORD_SALT, 128, SecretUtils.DEFAULT_HANDLE_WORD_ITERATIONS);
            if (wordRnd == null)
                throw new IllegalArgumentException("Invalid pubSplitKey (unable to derive wordRnd)");
            lastBytes = wordRnd; // use as base for next word

            int _wordIdx = ((wordRnd[0] & 0xFF) << 8) | (wordRnd[1] & 0xFF);
            int wordIndex = _wordIdx % wordList.size();

            String word = wordList.get(wordIndex);
            builder.append(word);
            if (i < c - 1)
                builder.append("_");
        }

        // Append number
        builder.append(n);
        return builder.toString();
    }

    // Handle to Keypair Lookup

    public String getPublicSplitKeyFromHandle(String handle) {
        return sharedHandleToKeyCache.computeIfAbsent(handle, _handle -> {
            String pubSplitKey = _internalGetPublicSplitKeyFromHandle(_handle);
            if (pubSplitKey != null)
                generalKeyToHandleCache.putIfAbsent(pubSplitKey, _handle);
            return pubSplitKey;
        });
    }

    synchronized private String _internalGetPublicSplitKeyFromHandle(String handle) {
        // TODO: Specify and Check if handle contains domain path
        // TODO: Create and Connect External Service that can do a lookup of the handle with known FIS Servers or if the handle has a domain attached
        return null;
    }
}
