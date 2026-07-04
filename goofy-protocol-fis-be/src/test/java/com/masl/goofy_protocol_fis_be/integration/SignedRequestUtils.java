package com.masl.goofy_protocol_fis_be.integration;

import com.masl.goofy_protocol_core.crypto.connected.GenericHandleCrypto;
import com.masl.goofy_protocol_core.crypto.connected.request.SignedRequest;
import com.masl.goofy_protocol_core.crypto.isolated.asymm.AsymmCrypto;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class SignedRequestUtils {
    public static ResultActions performSignedRequest(HttpMethod method, String path, AsymmCrypto.AsymmFullKeyPair keypair, MockMvc mvc, GenericHandleCrypto handleCrypto) {
        return performSignedRequest(method, path, null, keypair, mvc, handleCrypto);
    }

    public static ResultActions performSignedRequestStr(HttpMethod method, String path, String body, AsymmCrypto.AsymmFullKeyPair keypair, MockMvc mvc, GenericHandleCrypto handleCrypto) {
        return performSignedRequest(method, path, body.getBytes(StandardCharsets.UTF_8), keypair, mvc, handleCrypto);
    }

    public static ResultActions performSignedRequest(HttpMethod method, String path, byte[] body, AsymmCrypto.AsymmFullKeyPair keypair, MockMvc mvc, GenericHandleCrypto handleCrypto) {
        try {
            // Create Signed Request
            SignedRequest req = SignedRequest.fromParts(keypair, method.name(), path, body, handleCrypto);

            // Get Headers as MultiValueMap
            Map<String, String> headers = req.toHeadersWithPubKey();
            MultiValueMap<String, String> multiHeaders = new LinkedMultiValueMap<>();
            headers.forEach(multiHeaders::add);

            // Perform Request
            return mvc.perform(MockMvcRequestBuilders.request(method, path)
                    .headers(new HttpHeaders(multiHeaders))
                    .contentType("application/json")
                    .content(body));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ResultActions performUnsignedRequest(HttpMethod method, String path, MockMvc mvc) {
        return performUnsignedRequest(method, path, null, mvc);
    }

    public static ResultActions performUnsignedRequestStr(HttpMethod method, String path, String body, MockMvc mvc) {
        return performUnsignedRequest(method, path, body.getBytes(StandardCharsets.UTF_8), mvc);
    }

    public static ResultActions performUnsignedRequest(HttpMethod method, String path, byte[] body, MockMvc mvc) {
        try {
            // Perform Request
            return mvc.perform(MockMvcRequestBuilders.request(method, path)
                    .contentType("application/json")
                    .content(body));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
