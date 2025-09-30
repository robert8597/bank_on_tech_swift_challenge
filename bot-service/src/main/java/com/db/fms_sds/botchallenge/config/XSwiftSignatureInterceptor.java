package com.db.fms_sds.botchallenge.config;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import com.db.fms_sds.botchallenge.dto.OAuthTokenResponse; // neu

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class XSwiftSignatureInterceptor implements ClientHttpRequestInterceptor {

    private static final String X_SWIFT_SIGNATURE_HEADER = "X-SWIFT-Signature";
    private static final String JWT_SUBJECT = "CN=desktop, O=sandbox, O=swift";

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        log.debug("Retrieve token");
        try {
            request.getHeaders().add("Authorization", createAuthorizationHeader());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // Only apply signature for POST/PUT requests with a body
        if (body.length > 0 && (request.getMethod() == HttpMethod.POST || request.getMethod() == HttpMethod.PUT)) {
            try {
                String signature = createXSwiftSignature(request.getURI(), body);
                request.getHeaders().add(X_SWIFT_SIGNATURE_HEADER, signature);
                log.debug("Added X-SWIFT-Signature header to the request.");


            } catch (Exception e) {
                log.error("Failed to create X-SWIFT-Signature", e);
                // Depending on policy, you might want to re-throw as an IOException
                throw new IOException("Could not generate X-SWIFT-Signature", e);
            }
        }
        return execution.execute(request, body);
    }

    private String createXSwiftSignature(URI url, byte[] body) throws Exception {
        String bodyAsString = new String(body, StandardCharsets.UTF_8);

        // 1. Create the special digest
        String digest = createSwiftDigest(bodyAsString);

        // 2. Prepare JWT claims
        Instant now = Instant.now();
        Instant exp = now.plus(15, ChronoUnit.MINUTES);

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .audience(url.getHost() + url.getPath())
                .subject(JWT_SUBJECT)
                .jwtID(UUID.randomUUID().toString())
                .expirationTime(Date.from(exp))
                .issueTime(Date.from(now))
                .notBeforeTime(Date.from(now.minusSeconds(15))) // A small buffer for clock skew
                .claim("digest", digest)
                .build();

        // 3. Prepare JWS Header with certificate
        JWSHeader jwsHeader = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .type(JOSEObjectType.JWT)
                .x509CertChain(List.of(com.nimbusds.jose.util.Base64.from(getCertificate())))
                .build();

        // 4. Sign the JWT
        SignedJWT signedJWT = new SignedJWT(jwsHeader, claimsSet);
//        log.info(getPrivateKey());

        String privateKey = getPrivateKey();

        byte[] keyBytes = Base64.getDecoder().decode(privateKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) keyFactory.generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
        JWSSigner signer = new RSASSASigner(rsaPrivateKey);
        signedJWT.sign(signer);

        return signedJWT.serialize();
    }

    /**
     * Replicates the specific digest logic from the Python script.
     * sha256_base64(data) -> base64(sha256(urlsafe_base64(data)))
     */
    private String createSwiftDigest(String data) throws NoSuchAlgorithmException {
        // 1. URL-safe Base64 encode the data (with padding)
        String urlSafeBase64Data = Base64.getUrlEncoder().encodeToString(data.getBytes(StandardCharsets.UTF_8));

        // 2. SHA-256 hash the result
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] digestBytes = sha256.digest(urlSafeBase64Data.getBytes(StandardCharsets.UTF_8));

        // 3. Standard Base64 encode the final hash
        return Base64.getEncoder().encodeToString(digestBytes);
    }

    // --- Helper methods to fetch keys/certs (same as in your existing config) ---

    private RestClient getBasicRestClient() {
        return RestClient.builder().baseUrl("https://sandbox.swift.com").build();
    }

    private String getPrivateKey() {
        String privateKey = getBasicRestClient().get()
                .uri("/sandbox-selfsigned-dummy-secret/privatekey")
                .retrieve()
                .body(String.class);

        return privateKey.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
    }

    private String getCertificate() {
        return getBasicRestClient().get().uri("/sandbox-selfsigned-dummy-secret/certificate2").retrieve().body(String.class);
    }

    private String createAssertion() throws Exception {
        Instant now = Instant.now();
        Instant exp = now.plus(15, ChronoUnit.MINUTES);

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issueTime(Date.from(now))
                .notBeforeTime(Date.from(now.minusSeconds(15))) // A small buffer for clock skew
                .expirationTime(Date.from(exp))
                .jwtID(UUID.randomUUID().toString())
                .issuer("5msX6Fd3lY6UeUy0xe8A8AW3TtbNAwMI")
                .audience("sandbox.swift.com/oauth2/v1/token")
                .subject("CN=demo-swift-sandbox-consumer, O=Demo, L=London, S=London, C=GB")
                .build();

        JWSHeader jwsHeader = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .type(JOSEObjectType.JWT)
                .x509CertChain(List.of(com.nimbusds.jose.util.Base64.from(getCertificate())))
                .build();

        SignedJWT signedJWT = new SignedJWT(jwsHeader, claimsSet);
//        log.info(getPrivateKey());

        String privateKey = getPrivateKey();

        byte[] keyBytes = Base64.getDecoder().decode(privateKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) keyFactory.generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
        JWSSigner signer = new RSASSASigner(rsaPrivateKey);
        signedJWT.sign(signer);

        return signedJWT.serialize();
    }

    private String createAuthorizationHeader() throws Exception {
        String assertion = createAssertion();

        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer");
        data.add("assertion", assertion);
        data.add("scope", "swift.alliancecloud.api");

        String authstr = "Basic " + Base64.getEncoder()
                .encodeToString(("5msX6Fd3lY6UeUy0xe8A8AW3TtbNAwMI" + ":" + "sKTyGChnlMOFjntk")
                        .getBytes(StandardCharsets.UTF_8));

        OAuthTokenResponse token = getBasicRestClient().post()
                .uri("/oauth2/v1/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header("Authorization", authstr)
                .body(data)
                .retrieve()
                .body(OAuthTokenResponse.class);

        if (token == null || token.getAccessToken() == null) {
            throw new IllegalStateException("No access_token field in token response");
        }
        String type = token.getTokenType() != null ? token.getTokenType() : "Bearer";
        return type + " " + token.getAccessToken();
    }
}