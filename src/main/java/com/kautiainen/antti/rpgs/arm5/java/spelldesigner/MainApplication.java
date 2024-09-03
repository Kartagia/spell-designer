package com.kautiainen.antti.rpgs.arm5.java.spelldesigner;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.ApplicationPath;

@ApplicationPath("/")
public class MainApplication extends HttpServlet {


  /**
   * The attribute name for the content meta attribute.
   */
  public static final String META_CONTENT_ATTRIBUTE = "content";

  /**
   * The cotnent type http equivalent attrbiute name.
   */
  public static final String META_CONTENT_TYPE_HTTP_EQUIV_VALUE = "Content-Type";

  /**
   * The meta attribute for HTTP equipvaent.
   */
  public static final String MTA_HTTP_EQUIQ_ATTRIBUTE = "http-equiv";

  private final TransformerFactory domTransformerFactory = TransformerFactory.newInstance();

  private Transformer domTransformer;

  /**
   * Print the DOM document to the output stream.
   * 
   * @param out The output stream into which the document is written.
   * @throws TransformerException The transformation of hte documetn failed.
   */
  public static void printDomDocument(ServletOutputStream out, Document document, Transformer transformer) throws TransformerException {
    DOMSource source= new DOMSource(document);
    StreamResult result = new StreamResult(out);
    transformer.transform(source, result);
  }

  public static void printDomElement(ServletOutputStream out, Element element, Transformer transformer) throws TransformerException {
    DOMSource source= new DOMSource(element);
    StreamResult result = new StreamResult(out);
    transformer.transform(source, result);
  }

  public void printDomElement(ServletOutputStream out, Element element) throws TransformerException {
    printDomElement(out, element, this.domTransformer);
  }

  public void printDomDocument(ServletOutputStream out, Document document) throws TransformerException {
    printDomDocument(out, document, this.domTransformer);
  }
  

  @Override
  public void destroy() {
    this.domTransformer = null;
  }

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    try {
      this.domTransformer = domTransformerFactory.newTransformer();
      domTransformer.setOutputProperty("indent", "yes");
    } catch (TransformerConfigurationException e) {
      throw new ServletException("Invalid DOM transformer configuration", e);
    }
  }


  /**
   * Get content type from headers.
   * 
   * @param head The head element of the HTML document.
   */
  protected String[] getContentType(Element head) {
    DomDocumentEditor.DomElementList metas = new DomDocumentEditor.DomElementList(
      head.getElementsByTagName("meta"));
    String[] result = new String[3];
    for (Node meta: metas) {
      if (meta.getAttributes().getNamedItem("charset") != null) {
        result[2] = meta.getAttributes().getNamedItem("charset").getNodeValue();
      } else if (meta.getAttributes().getNamedItem(META_CONTENT_TYPE_HTTP_EQUIV_VALUE) != null) {
        String contentTypeString = meta.getAttributes().getNamedItem(META_CONTENT_TYPE_HTTP_EQUIV_VALUE).getNodeValue();
        String[] types = contentTypeString.split("(?:\\/|;)");
        if (types.length == 1) {
          result[0] = "application";
          result[1] = types[0];
        } else {
          System.arraycopy(types, 0, result, 0, types.length);
        }
      }
    }
    return result;
  }

  /**
   * Set content type.
   * 
   * @param head The HTML head element.
   * @param type The content type.
   * @param subType The sub type.
   */
  protected void setContentType(Element head, String type, String subType) {
    setContentType(head, type, subType, getContentType(head)[2]);
  }
  
  /**
   * Set content type.
   * 
   * @param head The HTML head element.
   * @param type The content type.
   * @param subType The sub type.
   * @param encodign The character encoding. Ignored, if undefined.
   */
  protected void setContentType(Element head, String type, String subType, String encoding) {
    NodeList meta = head.getElementsByTagName("meta");
    Node contentType = null;
    if (meta.getLength() > 0) {

    }
    if (contentType == null) {
      Element created = head.getOwnerDocument().createElement("meta");
      created.setAttribute(META_CONTENT_TYPE_HTTP_EQUIV_VALUE, META_CONTENT_TYPE_HTTP_EQUIV_VALUE);
      created.setAttribute(META_CONTENT_ATTRIBUTE, String.format("%s/%s%s", type, subType, 
      (encoding == null ? "" : String.format("; charset=%s", encoding))));
    }
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    // Generate the document.
    try {
      Document document = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder().newDocument();
      
      
      Element root, head, body, header, footer, main;
      root = document.createElement("html");
      document.appendChild(root);
      head = document.createElement("head");
      setContentType(head, "application", "html");
      root.appendChild(head); 
      body = document.createElement("body");
      root.appendChild(body); 
      header = document.createElement("header");
      main = document.createElement("main");
      footer = document.createElement("footer");
      body.appendChild(header);
      body.appendChild(main);
      body.appendChild(footer);

      // Print the document.
      try {
        printDomElement(resp.getOutputStream(), root);
      } catch (TransformerException ex) { 
        log("Malformed DOM document", ex);
      } 

      
    } catch (ParserConfigurationException e) {
      throw new ServletException("Invalid servlet initialization", e);
    }
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    super.doPost(req, resp);
  }

  @Override
  protected void doPut(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    super.doPut(req, resp);
  }
  
}
