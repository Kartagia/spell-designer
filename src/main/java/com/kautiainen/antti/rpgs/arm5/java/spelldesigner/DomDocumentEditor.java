package com.kautiainen.antti.rpgs.arm5.java.spelldesigner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Class for building and editing DOM documents.
 */
public class DomDocumentEditor {

  /**
   *  Createa a list view of the DOM node list.
   */
  public static class DomNodeList extends java.util.AbstractList<Node> {

    private final NodeList nodes; 

    public DomNodeList(NodeList nodes) {
      this.nodes = nodes;
    }

    @Override
    public int size() {
      return nodes.getLength();
    }

    @Override
    public Node get(int index) {
      return nodes.item(index);
    }
  }

  /**
   * The element-only node list.
   */
  public static class DomElementList extends java.util.AbstractList<Element> {


    /**
     * Create a dom element list containing only the elements of the given node list.
     * 
     * @param nodes The node list.
     * @return THe list containing only the elements of the node list.
     */
    public static DomElementList elementsOf(List<? extends Node> nodes) {
      List<? extends Node> nodeList = nodes.stream().filter( node -> node instanceof Element).toList();
      return new DomElementList(nodeList);
    }

    /**
     * Create a dom element list containing only the elements of the given node list.
     * 
     * @param nodes The node list.
     * @return THe list containing only the elements of the node list.
     */
    public static DomElementList elementsOf(NodeList nodes) {
      List<Node> list = new DomNodeList(nodes);
      return elementsOf(list);
    }


    /**
     * The members of the element list.
     */
    List<? extends Node> members;

    /**
     * Create a dom element list with nodes of the given list.
     * @param nodes The element nodes of the dom element list.
     */
    public DomElementList(List<? extends Node> nodes) {
      this.members = nodes;
      if (!stream().allMatch( node -> node instanceof Element)) {
        throw new IllegalArgumentException("Invalid nodes", new ClassCastException("A member node not an element"));
      }
    }

    /**
     * Create an element list from given nodes.     
     * @param nodes The nodes of the element list.
     * @throws IllegalArgumentException The node list contained a non-element node.
     */
    public DomElementList(NodeList nodes) throws IllegalArgumentException {
      super();
      try {
        List<Element> nodeList = (new DomNodeList(nodes)).stream().map( 
          node -> {
            if (node instanceof Element element) {
              return (Element)element;
            } else {
              throw new ClassCastException("Member was not an element");
            }
          }
        ).toList();
        this.members = nodeList;
      } catch(ClassCastException cce) {
        throw new IllegalArgumentException("Invalid source nodes", cce);
      }
      if (!stream().allMatch(node -> (node instanceof Element))) {
        throw new IllegalArgumentException("Invalid nodes", new ClassCastException("A non-Element node found"));
      }
    }

    /**
     * Create an empty element list.
     */
    public DomElementList() {
      this(Collections.emptyList());
    }

    @Override
    public Element get(int index) {
      return (Element)members.get(index);
    }

    @Override
    public int size() {
      return members.size();
    }
  }

  /**
   * A mutable DOM node list. The elements are added before their successor.
   */
  public static class MutableDomNodeList<TYPE extends Node> extends java.util.AbstractList<TYPE> {

    /**
     * The parent node of all members.
     */
    private final Element parent; 

    private List<TYPE> members = new ArrayList<>();

    /**
     * Crete a mutable dom node list from a parent node.
     * 
     * @param parent The parent node.
     */
    public MutableDomNodeList(Element parent) {
      this(parent, parent.getChildNodes());
    }

    /**
     * Crete a mutalbe dom node list with nodes added after the previous node of the list, or
     * after the last node of the parent.
     * @param parent The parent node.
     * @param nodes The nodes belonging to the mutable list.
     * @throws IllegalArgumentException Either parent or nodes was invalid.
     */
    @SuppressWarnings("unchecked")
    public MutableDomNodeList(Element parent, NodeList nodes) {
      final AtomicReference<Element> parentCandidate = new AtomicReference<>(parent);
      try {
        members = new ArrayList<>( (new DomNodeList(nodes)).stream().map(
          node -> {
            if (parentCandidate.get() == null) {
              parentCandidate.set((Element)node.getParentNode());
            } else if (!parentCandidate.get().equals(node.getParentNode())) {
              throw new IllegalArgumentException("Invalid member not sharing parent with others");
            }
            return (TYPE)node; 
          }
        ).toList() );
        this.parent = parentCandidate.get();
      } catch(ClassCastException|IllegalArgumentException cce) {
        throw new IllegalArgumentException("Invalid nodes", cce);
      }
      if (parent == null)  {
        throw new IllegalArgumentException("Invalid missing parent node");
      }
    }

    @Override
    public boolean add(TYPE added) {
      synchronized (parent) {
        if (this.members.add(added)) {
          this.parent.appendChild(added);
          return true;
        } 
        return false;
      }
    }

    @Override
    public synchronized void add(int index, TYPE added) {
        if (members.isEmpty() && index == members.size()) {
          members.add(index, added);
          parent.appendChild(added);          
        } else {
          Node next = members.get(index);
          members.add(index, added);
          parent.insertBefore(added, next);
        }
    }

    @Override
    public synchronized TYPE get(int index) {
      return members.get(index);
    }

    @Override
    public synchronized TYPE remove(int index) {
      synchronized (parent) {
        TYPE removed = this.members.remove(index);
        this.parent.removeChild(removed);
        return removed;
      }
    }

    @Override
    public synchronized TYPE set(int index, TYPE replacement) {
      synchronized (parent) {
        TYPE result = members.set(index, replacement);
        parent.replaceChild(replacement, result);
        return result;
      }
    }

    @Override
    public synchronized int size() {
      return members.size();
    }

    
  }


  /**
   * A DOM based codument editor.
   */
  public static class XhtmlDocumentEditor extends DomDocumentEditor {

    /**
     * The XHTML DTD version format with placeholder for the version.
     */
    public static final String XHTML_DTD_VERSION_FORMAT = "-//W3C//DTD XHTML %s//EN";
    /**
     * The XHTML namespace URI.
     */
    public static final String XHTML_NAMESPACE_URI = "http://www.w3.org/1999/xhtml";

    /**
     * Get the default document builder.
     */
    public static DocumentBuilder defaultDocumentBuilder() {
        try {
          return DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
          return null;
        }
      
    }

    /**
     * Get the DTD file name.
     * @param version The XHTML version.
     * @return The DTD file name.
     */
    public static String getXHtmlDtDFileName(String version) {
      switch (version) {
        case "1", "1.0": 
        return "http//www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd";
        case "1.1", "1.2":
        return "http//www.w3.org/TR/xhtml11/DTD/xhtml11.dtd";
        default:
          return null;
      }
    }

    /**
     * Get the XHTML document document type.
     * 
     * @param builder The document builder.
     * @param version The version XHTML version number as string.
     */
    public static DocumentType getXHtmlDocType(DocumentBuilder builder, String version)  {
      return builder.getDOMImplementation().createDocumentType(
        "html", 
        String.format(XHTML_DTD_VERSION_FORMAT, version), 
        getXHtmlDtDFileName(version)
      );
    }

    /**
     * Get the default Xhtml documetn type.
     * @param builder The builder.
     * @return The XHTML 1.2 version document type.
     */
    public static DocumentType getXHtmlDocType(DocumentBuilder builder) {
      return getXHtmlDocType(builder, "1.2");
    }

    public XhtmlDocumentEditor(DocumentBuilder builder) {
      super(builder,
      XHTML_NAMESPACE_URI, "html", 
      getXHtmlDocType(builder));
    }

    /**
     * Test validity of the documetn root element.
     * 
     * @return True, if and only if the document element root is valid.
     */
    @Override
    public boolean validDocumentElement(Element element) {
      return element != null && "html".equals(element.getNodeName());
    }


    public XhtmlDocumentEditor() {
      this(defaultDocumentBuilder());
    }

    public XhtmlDocumentEditor(Document document) {
      super(document);
    }
  }

  /**
   * The edited and build document.
   */
  private Document document;
  private Element root; 

  /**
   * Create a new empty default document.
   */
  public DomDocumentEditor() {
    try {
        this.document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    } catch (ParserConfigurationException e) {
      throw new IllegalStateException("Could not create new XML document");
    }
  }

  /**
   * Create a new empty document.
   * @param builder The builder used to build the document.
   */
  @SuppressWarnings("")
  public DomDocumentEditor(DocumentBuilder builder) {
    this.document = builder.newDocument();
    init(this.document);
  }

  /**
   * 
   */
  public DomDocumentEditor(
    DocumentBuilder builder,
    String namespaceURI,
    String rootElementName,
    DocumentType type) {
    this.document = builder.getDOMImplementation().createDocument(namespaceURI, rootElementName, type);
  }

  /**
   * Create a new docuemnt element editor from a document.
   * 
   * @param doc The edited docuemnt. The editor will alter the document. 
   * @throws IllegalArgumentException The given document was not suitable for the editor.
   */
  @SuppressWarnings("")
  public DomDocumentEditor(Document doc) throws IllegalArgumentException{
    if (doc == null) throw new NullPointerException("An undefined document not supported");
    this.document = doc;
    init(doc);
  }

  /**
   * Test validity of the documetn root element.
   * 
   * @return True, if and only if the document element root is valid.
   */
  public boolean validDocumentElement(Element element) {
    return element != null;
  }

  /**
   * Initializes the document.
   * 
   * @param doc The initialized document.
   * @throws IllegalArgumentException The document is invalid.
   * @implNote The implementor of the subclass must either call this method, or
   * redefine all methods of this class, as the method initializes the root node.
   */
  protected void init(Document doc) throws IllegalArgumentException {
    
    root = doc.getDocumentElement();
    if (!validDocumentElement(root)) {
      throw new IllegalArgumentException("Invalid document root element name");
    }
    DomNodeList nodes = new DomNodeList(root.getChildNodes());
    if (!nodes.stream().anyMatch( node -> "head".equals(node.getNodeName()))) {
      root.appendChild(doc.createElement("head"));
    }
    if (!nodes.stream().anyMatch( node -> ("body".equals(node.getNodeName())))) {
      root.appendChild(doc.createElement("body"));
    }
  }


  /**
   * Append child to the root element.
   * 
   * @param child The added child.
   * @throws IllegalStateException The child node could not be appended to the document due
   * docuemnt state.
   * @throws IllegalArgumentException The given child node is invalid for the document.
   */
  public DomDocumentEditor addChild(Node child) throws IllegalArgumentException, 
  IllegalStateException  {
    root.appendChild(child);
    return this;
  }

  /**
   * Get the elements with given tag of the document.
   * @param tag The seeked tag.
   * @return The list of elements iwth given tag.
   */
  public java.util.List<Element> getElementsByTag(String tag) {
    return new DomElementList(this.document.getElementsByTagName(tag));
  }


  public java.util.List<Element> getChildrenByTag(Element parent, String tag) {
    if (parent == null || tag == null) return Collections.emptyList();
    return new DomElementList(DomElementList.elementsOf(parent.getChildNodes()).stream().filter(
      element -> (tag.equals(element.getNodeName()))
    ).toList());
  }

  /**
   * Get the root element children with the given tag name.
   */
  public java.util.List<Element> getChildrenByTag(String tag) {
    return getChildrenByTag(root, tag);
  }

  /**
   * Create the document.
   * @return The creted docuemnt.
   */
  public Document build() {
    return document;
  }
}
