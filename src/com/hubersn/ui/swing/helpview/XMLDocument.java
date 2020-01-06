/*
 * (c) hubersn Software
 * www.hubersn.com
 */

/*
This is free and unencumbered software released into the public domain.

Anyone is free to copy, modify, publish, use, compile, sell, or
distribute this software, either in source code form or as a compiled
binary, for any purpose, commercial or non-commercial, and by any
means.

In jurisdictions that recognize copyright laws, the author or authors
of this software dedicate any and all copyright interest in the
software to the public domain. We make this dedication for the benefit
of the public at large and to the detriment of our heirs and
successors. We intend this dedication to be an overt act of
relinquishment in perpetuity of all present and future rights to this
software under copyright law.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
OTHER DEALINGS IN THE SOFTWARE.

For more information, please refer to <http://unlicense.org/>
*/

package com.hubersn.ui.swing.helpview;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * To simplify XML DOM handling. Read only, minimal wrapper for Java DOM standard API.
 */
public class XMLDocument {

  /** Character to use for separating XML path elements. */
  public static final String XML_PATH_SEPARATOR = ":";

  private Document xmlDoc;

  private Element xmlRoot;

  private List<XMLTag> xmlTags = new ArrayList<>();

  /**
   * Creates a new XML document representation based on given source stream.
   *
   * @param source source input stream.
   * @throws Exception on error.
   */
  public XMLDocument(final InputStream source) throws Exception {
    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    // speed up XML parsing
    factory.setNamespaceAware(false);
    factory.setValidating(false);
    factory.setFeature("http://xml.org/sax/features/namespaces", false);
    factory.setFeature("http://xml.org/sax/features/validation", false);
    factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
    factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

    final DocumentBuilder builder = factory.newDocumentBuilder();

    this.xmlDoc = builder.parse(new BufferedInputStream(source, 16384));
    this.xmlDoc.getDocumentElement().normalize();
    this.xmlRoot = this.xmlDoc.getDocumentElement();
    populate();
  }

  private void populate() {
    NodeList children = this.xmlRoot.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node child = children.item(i);
      if (child instanceof Element) {
        add((Element) child, this.xmlTags);
      }
    }
  }

  private static void add(Element child, List<XMLTag> tags) {
    XMLTag tag = getTag(child);
    if (tags != null) {
      tags.add(tag);
    }
    // children
    NodeList children = child.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node node = children.item(i);
      if (node instanceof Element) {
        add((Element) node, tag.children);
      }
    }
  }

  private static XMLTag getTag(Element source) {
    XMLTag tag = new XMLTag(source.getTagName(), getNodeTextContent(source));
    // attributes
    NamedNodeMap nnm = source.getAttributes();
    for (int i = 0; i < nnm.getLength(); i++) {
      Node node = nnm.item(i);
      if (node instanceof Attr) {
        tag.attributes.add(new XMLAttribute((Attr) node));
      }
    }
    return tag;
  }

  private static String getNodeTextContent(Node node) {
    final StringBuilder result = new StringBuilder();
    if (!node.hasChildNodes()) return "";

    NodeList list = node.getChildNodes();
    for (int i = 0; i < list.getLength(); i++) {
      Node subnode = list.item(i);
      if (subnode.getNodeType() == Node.TEXT_NODE) {
        result.append(subnode.getNodeValue());
      } else if (subnode.getNodeType() == Node.CDATA_SECTION_NODE) {
        result.append(subnode.getNodeValue());
      } else if (subnode.getNodeType() == Node.ENTITY_REFERENCE_NODE) {
        result.append(getNodeTextContent(subnode));
      }
    }

    return result.toString();
  }

  /**
   * Returns the tag for the given path.
   *
   * @param path XML path.
   * @return tag for given path, null if not found.
   */
  public XMLTag getTag(final String path) {
    return getTag(p(path));
  }

  private XMLTag getTag(final String[] path) {
    return getTag(path, 0, this.xmlTags);
  }

  /**
   * Returns the text inside the tag for the given path.
   *
   * @param path XML path.
   * @return text inside tag for given path, empty string if not found.
   */
  public String getTagValue(final String path) {
    return getTagValue(p(path));
  }

  /**
   * Returns the attribute's value of the tag for the given path and attribute name.
   *
   * @param path XML path.
   * @param attributeName name of attribute
   * @return attribute's value of tag for given path, empty string if not found.
   */
  public String getTagAttribute(final String path, final String attributeName) {
    XMLTag tag = getTag(path);
    if (tag != null) {
      return tag.getAttribute(attributeName);
    }
    return "";
  }

  private String getTagValue(final String[] path) {
    XMLTag tag = getTag(path, 0, this.xmlTags);
    if (tag != null) {
      return tag.text;
    }
    return "";
  }

  private XMLTag getTag(final String[] path, final int pathIndex, final List<XMLTag> listToRecurse) {
    if (pathIndex >= path.length) {
      return null;
    }
    List<XMLTag> allTags = getTags(path, pathIndex, listToRecurse);
    if (!allTags.isEmpty()) {
      return allTags.get(0);
    }
    return null;
  }

  public List<XMLTag> getTags(final String path) {
    return getTags(p(path), 0, this.xmlTags);
  }

  private List<XMLTag> getTags(final String[] path, final int pathIndex, final List<XMLTag> listToRecurse) {
    if (pathIndex >= path.length) {
      return Collections.emptyList();
    }
    String pathElement = path[pathIndex];
    final boolean lastElement = path.length == pathIndex + 1;
    // check if element exists
    List<XMLTag> returnTags = new ArrayList<>();
    for (final XMLTag tag : listToRecurse) {
      if (tag.tag.equals(pathElement)) {
        if (lastElement) {
          returnTags.add(tag);
        } else {
          // TODO need to collect all, not only one
          return getTags(path, pathIndex + 1, tag.children);
        }
      }
    }
    return returnTags;
  }

  private static String[] p(final String pipeSeparatedPathString) {
    return pipeSeparatedPathString.split(XML_PATH_SEPARATOR);
  }

  /**
   * Thin wrapper object for attributes of an XML tag.
   */
  public static class XMLAttribute {
    public final String name;
    public final String value;

    public XMLAttribute(final String name, final String value) {
      this.name = name;
      this.value = value;
    }

    public XMLAttribute(Attr attr) {
      this.name = attr.getName();
      this.value = attr.getValue();
    }

    public String toString() {
      return "XMLAttribute[" + this.name + "/" + this.value + "]";
    }
  }

  /**
   * Thin wrapper object for an XML tag.
   */
  public static class XMLTag {
    public final String tag;
    public final String text;
    public final List<XMLTag> children = new ArrayList<>();
    public final List<XMLAttribute> attributes = new ArrayList<>();

    public XMLTag(final String tag, final String text) {
      this.tag = tag;
      this.text = text;
    }

    public String getAttribute(final String name) {
      for (final XMLAttribute attribute : this.attributes) {
        if (name.equals(attribute.name)) {
          return attribute.value;
        }
      }
      return "";
    }

    public XMLTag getChild(final String tag) {
      for (final XMLTag xmlTag : this.children) {
        if (tag.equals(xmlTag.tag)) {
          return xmlTag;
        }
      }
      return null;
    }

    public String toString() {
      return "XMLTag[" + this.tag + "/" + this.text + "/" + this.attributes + "/childcount="+this.children.size()+"]";
    }
  }
}
