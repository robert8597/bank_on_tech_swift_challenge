package com.db.fms_sds.botchallenge.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import static com.db.fms_sds.botchallenge.constants.BotAppConstants.HOST;

// Task 3: Retrieve a list of distributions

@Service
@RequiredArgsConstructor
@Slf4j
public class ThirdTask {

    private final RestClient swiftApiClient;


    public void pullListOfDistributions() {
        // TODO Task 3.1: Create the URI for retrieving the list of distributions from the correct endpoint
        String uri = HOST + "?";

        log.info("Pulling a list of distributions...");

        String response = swiftApiClient.get()
                .uri(uri)
                .retrieve().body(String.class);

        log.info("Response from SWIFT-API is {}", response);

        log.info("Successfully pulled list of distributions from API.");
    }
}
