package com.db.fms_sds.botchallenge.service;

import com.db.fms_sds.botchallenge.jaxb.pacs008.Document;
import com.db.fms_sds.botchallenge.openapi.model.InterActMessageEmission;
import com.db.fms_sds.botchallenge.utils.Utils;
import jakarta.xml.bind.JAXBException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import static com.db.fms_sds.botchallenge.constants.BotAppConstants.HOST;


// Task 2: Post an Interact message

@Service
@RequiredArgsConstructor
@Slf4j
public class SecondTask {

    private final FirstTask firstTask;
    private final RestClient swiftApiClient;

    /**
     * Orchestrates the process of generating, signing, and sending a pacs.008 message.
     *
     * @param xmlFilePath The classpath path to the source pacs.008 XML file.
     */
    public InterActMessageEmission processAndSendPacs008(String xmlFilePath) throws JAXBException {
        log.info("Starting pacs.008 processing and sending...");

        Document document = firstTask.buildPacs008(xmlFilePath);
        String xmlString = firstTask.validateAndTransform(document);


        // Task 2.1: Create the InteractMessage object and set its properties
        InterActMessageEmission interActMessageEmission = new InterActMessageEmission();

        interActMessageEmission.setSenderReference("BankOnTech");
        interActMessageEmission.setServiceCode("swift.finplus!pc");
        interActMessageEmission.setMessageType("pacs.008.001.13");
        interActMessageEmission.setRequestor("ou=xxx,o=deutdeff,o=swift");
        interActMessageEmission.setResponder("ou=xxx,o=bktrus33,o=swift");

        interActMessageEmission.setPayload(Utils.base64Encode(xmlString));
        interActMessageEmission.setFormat(InterActMessageEmission.FormatEnum.MX);

        // Task 2.2: Create the URI for posting the InterAct message to the correct endpoint
        String uri = HOST + "/alliancecloud/v2/interact/messages";

        String response = swiftApiClient.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .body(interActMessageEmission)
                .retrieve().body(String.class);

        log.info("Response from SWIFT-API is {}", response);

        log.info("Successfully sent the InterAct message to the target API.");

        return interActMessageEmission;
    }


}
