package com.db.fms_sds.botchallenge.service;

import com.db.fms_sds.botchallenge.openapi.model.InterActMessageEmission;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import static com.db.fms_sds.botchallenge.constants.BotConstants.TEST_XML_PATH;

@SpringBootTest
@Slf4j
public class SecondTaskTest {

    @Autowired
    SecondTask secondTask;

    @Test
    public void submitValidXml() throws Exception {
        InterActMessageEmission msg = secondTask.processAndSendPacs008(TEST_XML_PATH);

        assertThat(msg.getSenderReference())
                .as("Sender reference is wrong")
                .isEqualTo("BankOnTech");

        assertThat(msg.getServiceCode())
                .as("Service code is wrong")
                .isEqualTo("swift.finplus!pc");

        assertThat(msg.getMessageType())
                .as("Message type is wrong")
                .isEqualTo("pacs.008.001.13");

        assertThat(msg.getRequestor())
                .as("Requestor is wrong")
                .isEqualTo("ou=xxx,o=deutdeff,o=swift");

        assertThat(msg.getResponder())
                .as("Responder is wrong")
                .isEqualTo("ou=xxx,o=bktrus33,o=swift");
    }
}
