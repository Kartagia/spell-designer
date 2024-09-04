package com.kautiainen.antti.rpgs.arm5.java.spelldesigner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DomDocumentEditorTest {


    static Document getTestDocument() {
      try {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document testDocument = builder.newDocument();
        Element root = testDocument.createElement("html");
        testDocument.appendChild(root);
        Element head = testDocument.createElement("head");
        root.appendChild(head);
        Element body = testDocument.createElement("body");
        root.appendChild(body);
        return testDocument;
      } catch (ParserConfigurationException e) {
        System.out.println("DOM Parser configuration error: " + e);
      }
      return null;
    }

    @Test
    void testAddChild() {
      Document testDocument = getTestDocument();
      assumeTrue(testDocument != null);
      DomDocumentEditor tested = new DomDocumentEditor(testDocument);
      Element added = testDocument.createElement("h1");
      try {
        tested.addChild(added);
      } catch(Exception e) {
      }
      assertTrue(!tested.getChildrenByTag("h1").isEmpty());
      assertTrue(testDocument.getElementsByTagName("h1").getLength() == 1);


    }

    @Test
    void testGetChildrenByTag() {
      Document testDocument = getTestDocument();
      assumeTrue(testDocument != null);
      
      DomDocumentEditor tested = new DomDocumentEditor(testDocument);
      assertEquals(tested.getChildrenByTag("head"), 
      DomDocumentEditor.DomElementList.elementsOf(testDocument.getDocumentElement().getChildNodes()).stream().filter(
        (Element node) -> "head".equals(node.getNodeName())
      ).toList());
      assertEquals(tested.getChildrenByTag("body"), 
      DomDocumentEditor.DomElementList.elementsOf(testDocument.getDocumentElement().getChildNodes()).stream().filter(
        (Element node) -> "body".equals(node.getNodeName())
      ).toList());
    }

    @Test
    void testGetChildrenByTag2() {
      Document testDocument = getTestDocument();
      assumeTrue(testDocument != null);
    }

    @Test
    void testGetElementsByTag() {
      Document testDocument = getTestDocument();
      assumeTrue(testDocument != null);
      DomDocumentEditor tested = new DomDocumentEditor(testDocument);
      assertEquals(new DomDocumentEditor.DomElementList(tested.getElementsByTag("html")), tested.getElementsByTag("html"));
    }

    @Test
    void testValidDocumentElement() {
      Document testDocument = getTestDocument();
      assumeTrue(testDocument != null);
      DomDocumentEditor.XhtmlDocumentEditor tested = new DomDocumentEditor.XhtmlDocumentEditor();
      assertTrue(tested.validDocumentElement(testDocument.getDocumentElement()));
    }
}
