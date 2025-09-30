package com.db.fms_sds.botchallenge.service;

import com.db.fms_sds.botchallenge.jaxb.pacs008.Document;
import com.db.fms_sds.botchallenge.utils.JaxbMarshallingUtil;
import jakarta.xml.bind.JAXBException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.InputStream;

// Task 1: Prepare the XML payload for your Interact message, add the correct values inside the pacs_008.xml file (test/resources/pacs_008.xml)

@Service
@RequiredArgsConstructor
public class FirstTask {

    private final JaxbMarshallingUtil jaxbMarshallingUtil;

    public Document buildPacs008(String xmlFilePath) throws JAXBException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(xmlFilePath);
        if (inputStream == null) {
            throw new IllegalArgumentException("File not found in classpath: " + xmlFilePath);
        }

        return jaxbMarshallingUtil.unmarshall(inputStream, Document.class);
    }

    public String validateAndTransform(Document document) throws JAXBException {
        return jaxbMarshallingUtil.marshall(document);
    }
}
