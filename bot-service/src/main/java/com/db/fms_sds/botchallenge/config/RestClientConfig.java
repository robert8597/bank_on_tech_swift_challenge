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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.endpoint.RestClientJwtBearerTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.client.RestClient;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.MediaType; // neu
import org.springframework.http.HttpHeaders; // neu

import java.nio.charset.StandardCharsets; // neu
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import static com.db.fms_sds.botchallenge.constants.BotAppConstants.JWT_AUDIENCE;
import static com.db.fms_sds.botchallenge.constants.BotAppConstants.JWT_SUBJECT;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class RestClientConfig {

    // Bean for logging headers and body
    @Bean
    public ClientHttpRequestInterceptor loggingInterceptor() {
        return (request, body, execution) -> {
            if (log.isInfoEnabled()) {
                log.info("[RestClient] -> {} {}", request.getMethod(), request.getURI());

                if (body != null && body.length > 0) {
                    String contentType = contentTypeString(request.getHeaders());
                    if (isLikelyText(contentType)) {
                        String bodyStr = new String(body, StandardCharsets.UTF_8);

                        log.info("[RestClient] -> Body ({} bytes, content-type={}): {}", body.length, contentType, bodyStr);
                    } else {
                        log.info("[RestClient] -> Body ({} bytes, content-type={}) <binary/multipart suppressed>", body.length, contentType);
                    }
                } else {
                    log.info("[RestClient] -> Body: <empty>");
                }
                request.getHeaders().forEach((k, v) -> log.info("[RestClient] -> Header {}={}", k, String.join(",", v)));
            }
            var response = execution.execute(request, body);
            if (log.isInfoEnabled()) {
                try {
                    log.info("[RestClient] <- Status {} {}", response.getStatusCode().value(), response.getStatusCode());
                } catch (Exception ignored) {
                }
                response.getHeaders().forEach((k, v) -> log.info("[RestClient] <- Header {}={}", k, String.join(",", v)));
            }
            return response;
        };
    }

    private String contentTypeString(HttpHeaders headers) {
        MediaType ct = headers.getContentType();
        return ct != null ? ct.toString() : "<unknown>";
    }

    private boolean isLikelyText(String contentType) {
        if (contentType == null) return false;
        String ct = contentType.toLowerCase();
        return ct.startsWith("text/")
                || ct.contains("json")
                || ct.contains("xml")
                || ct.contains("+json")
                || ct.contains("+xml")
                || ct.contains("form-urlencoded");
    }

    @Bean
    public RestClient restClient(RestClient.Builder builder,
                                 XSwiftSignatureInterceptor xSwiftSignatureInterceptor,
                                 ClientHttpRequestInterceptor loggingInterceptor) {

        return builder
                .requestInterceptor(xSwiftSignatureInterceptor)
//                .requestInterceptor(loggingInterceptor)
                .build();
    }

    @Bean
    RestClientJwtBearerTokenResponseClient jwtBearerTokenResponseClient() {
        return new RestClientJwtBearerTokenResponseClient();
    }

    @Bean
    JwtBearerOAuth2AuthorizedClientProvider jwtBearerOAuth2AuthorizedClientProvider() {
        JwtBearerOAuth2AuthorizedClientProvider authorizedClientProvider = new JwtBearerOAuth2AuthorizedClientProvider();
        authorizedClientProvider.setJwtAssertionResolver(jwtResolver());
        authorizedClientProvider.setAccessTokenResponseClient(jwtBearerTokenResponseClient());
        return authorizedClientProvider;
    }

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService,
            JwtBearerOAuth2AuthorizedClientProvider jwtBearerOAuth2AuthorizedClientProvider) {


        AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientService);
        authorizedClientManager.setAuthorizedClientProvider(jwtBearerOAuth2AuthorizedClientProvider);

        return authorizedClientManager;
    }


    private Function<OAuth2AuthorizationContext, Jwt> jwtResolver() {
        return context -> {
            ClientRegistration clientRegistration = context.getClientRegistration();

            Instant now = Instant.now();
            Instant exp = now.plus(15, ChronoUnit.MINUTES);

            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .audience(JWT_AUDIENCE)
                    .subject(JWT_SUBJECT)
                    .jwtID(UUID.randomUUID().toString())
                    .issuer(clientRegistration.getClientId())
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(exp))
                    .build();

            JWSHeader jwsHeader = new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .type(JOSEObjectType.JWT)
                    .x509CertChain(List.of(com.nimbusds.jose.util.Base64.from(getCertificate())))
                    .build();

            SignedJWT signedJWT = new SignedJWT(jwsHeader, claimsSet);

            try {
                String privateKeyString = getPrivateKey();
                byte[] keyBytes = Base64.getDecoder().decode(privateKeyString);

                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) keyFactory.generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
                JWSSigner signer = new RSASSASigner(rsaPrivateKey);
                signedJWT.sign(signer);
                return new Jwt(signedJWT.serialize(), Instant.now(), exp, jwsHeader.toJSONObject(), claimsSet.toJSONObject());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    private RestClient getBasicRestClient() {
        return RestClient.builder().baseUrl("https://sandbox.swift.com").build();
    }

    private String getPrivateKey() {
        return getBasicRestClient().get()
                .uri("/sandbox-selfsigned-dummy-secret/privatekey2")
                .retrieve()
                .body(String.class);
    }

    private String getCertificate() {
        return getBasicRestClient().get()
                .uri("/sandbox-selfsigned-dummy-secret/certificate2")
                .retrieve()
                .body(String.class);
    }

}