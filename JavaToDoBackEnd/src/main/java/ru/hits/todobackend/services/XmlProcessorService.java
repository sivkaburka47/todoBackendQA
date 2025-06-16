package ru.hits.todobackend.services;

import org.springframework.stereotype.Service;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import java.io.ByteArrayInputStream;

@Service
public class XmlProcessorService {

    public String parseXml(String xml) {
        try {
            // УЯЗВИМОСТЬ: включен XXE
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false);
            dbf.setFeature("http://xml.org/sax/features/external-general-entities", true);
            dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", true);
            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", true);

            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new ByteArrayInputStream(xml.getBytes()));
            return doc.getDocumentElement().getTextContent();
        } catch (Exception e) {
            return "Error parsing XML: " + e.getMessage();
        }
    }
}
