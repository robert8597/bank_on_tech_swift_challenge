package com.db.fms_sds.botchallenge.service;

import com.db.fms_sds.botchallenge.jaxb.pacs008.Document;
import jakarta.xml.bind.JAXBException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.xmlunit.assertj.XmlAssert;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static com.db.fms_sds.botchallenge.constants.BotConstants.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Slf4j
public class FirstTaskTest {
    @Autowired
    private FirstTask firstTask;

    @Test
    void shouldReadPacs008File() throws JAXBException {
        Document document = firstTask.buildPacs008(TEST_XML_PATH);
        assertThat(document).isNotNull();
        assertThat(document.getFIToFICstmrCdtTrf()).isNotNull();
        assertThat(document.getFIToFICstmrCdtTrf().getGrpHdr().getMsgId()).isEqualTo("e820f01c-b693-4b93-931c-336c2a3826b");
        assertThat(document.getFIToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getAgrdRate().getPreAgrdXchgRate())
                .isEqualTo(XCHG_RATE);
        assertThat(document.getFIToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getCdtr().getNm()).isEqualTo(CDTR_NAME);
        assertThat(document.getFIToFICstmrCdtTrf().getCdtTrfTxInf().get(0).getDbtr().getNm()).isEqualTo(DBTR_NAME);
        log.info("Congratulations! You put in the correct values into the XML file!");
    }

    @Test
    void shouldValidateAndTransformPacs008File() throws JAXBException, IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(TEST_XML_PATH);
        if (inputStream == null) {
            throw new IllegalArgumentException("File not found in classpath: " + TEST_XML_PATH);
        }
        Document document = firstTask.buildPacs008(TEST_XML_PATH);
        String xmlString = firstTask.validateAndTransform(document);
        XmlAssert.assertThat(xmlString).and(IOUtils.toString(inputStream, StandardCharsets.UTF_8)).areSimilar();
        log.info("Successfully validated and transformed pacs.008.001.13.xml file to XML string");
    }
}
