package com.example.vulnapp;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;

public final class SecureXml {

    private SecureXml() {
    }

    // FIXED: XXE Injection - hardened DocumentBuilderFactory
    public static Document parseDom(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        hardenFactory(factory);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xml)));
    }

    // FIXED: XXE Injection - hardened SAXParserFactory
    public static void parseSax(String xml) throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        hardenFactory(factory);
        factory.setXIncludeAware(false);
        SAXParser parser = factory.newSAXParser();
        parser.getXMLReader().setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        parser.parse(new InputSource(new StringReader(xml)), new org.xml.sax.helpers.DefaultHandler());
    }

    // FIXED: XXE Injection - hardened TransformerFactory and parser
    public static String transform(String xml) throws Exception {
        Document doc = parseDom(xml);
        TransformerFactory tf = TransformerFactory.newInstance();
        tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
        tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        Transformer transformer = tf.newTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.toString();
    }

    private static void hardenFactory(DocumentBuilderFactory factory) throws Exception {
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
    }

    private static void hardenFactory(SAXParserFactory factory) throws Exception {
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setNamespaceAware(true);
    }
}
