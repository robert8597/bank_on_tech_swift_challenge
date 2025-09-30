package com.db.fms_sds.botchallenge.service;

import com.db.fms_sds.botchallenge.openapi.model.InterActMessageDownloadResponse;
import com.db.fms_sds.botchallenge.openapi.model.InterActMessageEmission;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import static com.db.fms_sds.botchallenge.constants.BotAppConstants.HOST;

// Task 4: Retrieve a InterAct message by its distribution ID

@Service
@RequiredArgsConstructor
@Slf4j
public class FourthTask {

    private final RestClient swiftApiClient;


    public String pullInteractMessage() {
        // Task 4.1: Create the URL for retrieving a InterAct message by its distribution ID from the correct endpoint
        String uri = HOST + "/alliancecloud/v2/interact/messages/44984189499";

        log.info("Pulling an InterAct message...");

        InterActMessageDownloadResponse response = swiftApiClient.get()
                .uri(uri)
                .retrieve().body(InterActMessageDownloadResponse.class);

        log.info("Response from SWIFT-API is {}", response);

        log.info("Successfully pulled an InterAct message from API.");

        return uri;
    }
}
