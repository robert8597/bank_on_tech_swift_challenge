package com.db.fms_sds.botchallenge.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import static com.db.fms_sds.botchallenge.constants.BotAppConstants.HOST;


// Task 5: Post an Acknowledgement for a distribution

@Service
@RequiredArgsConstructor
@Slf4j
public class FifthTask {

    private final RestClient swiftApiClient;


    public String ackDistribution() {
        log.info("Acknowledging a distribution...");


        // TODO Task 5.1: Create the URL for posting an Acknowledgement for a distribution to the correct endpoint
        String uri = HOST + "?";

        ResponseEntity<String> response = swiftApiClient.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{}")
                .retrieve().toEntity(String.class);

        log.info("Response statuscode from SWIFT-API is {}", response.getStatusCode());

        log.info("Successfully acknowledged a distribution.");

        return uri;
    }


}
