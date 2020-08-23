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

import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * HelpSet representation - data model for simple help sets.
 */
public class HelpSet {

  private List<ViewConfig> views;

  private String lastRef = "";

  private String title = "";

  private String rootPath;

  private String homeID;

  private HelpMapper helpMap;

  /**
   * Creates a new helpset by loading the named helpset via the classloader.
   *
   * @param helpSetName name of helpset to load and parse.
   * @throws on error, e.g. help set could not be found or parsed.
   */
  public HelpSet(final String helpSetName) throws Exception {
    this.views = new ArrayList<>();
    this.rootPath = helpSetName.substring(0, helpSetName.lastIndexOf('/'));
    // check if helpset exists, early exit if not
    final InputStream is = HelpSet.class.getResourceAsStream(helpSetName);
    if (is == null) {
      throw new IllegalArgumentException("HelpSet " + helpSetName + " not found in classpath.");
    }
    final XMLDocument helpSetDocument = new XMLDocument(is);
    this.title = helpSetDocument.getTagValue("title");

    this.homeID = helpSetDocument.getTagValue("maps" + XMLDocument.XML_PATH_SEPARATOR + "homeID");
    this.helpMap = new HelpMapper(
        getHelpInputStream(helpSetDocument.getTagAttribute("maps" + XMLDocument.XML_PATH_SEPARATOR + "mapref", "location")));
    List<XMLDocument.XMLTag> viewTags = helpSetDocument.getTags("view");
    for (final XMLDocument.XMLTag viewTag : viewTags) {
      List<XMLDocument.XMLTag> children = viewTag.children;
      this.views.add(new ViewConfig(children));
    }
  }

  /**
   * Returns the list of views defined in this help set.
   * 
   * @return list of views.
   */
  public List<ViewConfig> getViews() {
    return Collections.unmodifiableList(this.views);
  }

  /**
   * Returns the defined title of this help set.
   * 
   * @return help set title.
   */
  public String getTitle() {
    return this.title;
  }

  /**
   * Returns the title that should be used for the frame showing this help set content.
   * 
   * @return frame title.
   */
  public String getFrameTitle() {
    return getTitle();
  }

  /**
   * Returns the URL that represents the first page to be shown for this help set.
   * 
   * @return home URL.
   */
  public URL getHelpHomeURL() {
    return getHelpURL(this.helpMap.getURL(this.homeID));
  }

  /**
   * Returns the last reference that was asked for via getHelpURL.
   * 
   * @return last reference asked for via getHelpURL.
   */
  public String getLastRef() {
    return this.lastRef;
  }

  /**
   * Translates the given name into an URL that is showable in the content view.
   * 
   * @param name name.
   * @return URL representing the name.
   */
  public URL getHelpURL(final String name) {
    this.lastRef = "";
    URL returnURL = null;
    try {
      String ref = "";
      final int indexOfHash = name.indexOf('#');
      if (indexOfHash >= 0) {
        ref = name.substring(indexOfHash + 1);
        this.lastRef = ref;
      }
      // strip name from trailing reference
      String myname = indexOfHash < 0 ? name : name.substring(0, indexOfHash);
      String res = this.rootPath + "/" + URLEncoder.encode(myname, "UTF-8");
      returnURL = HelpSet.class.getResource(res);
      if (returnURL == null) {
        System.err.println("Failed to create URL for resource " + res + " based on name " + name);
      }
    } catch (Exception ex) {
      // TODO Auto-generated catch block
      ex.printStackTrace();
    }
    return returnURL;
  }

  public URL getMappedHelpURL(final String id) {
    return getHelpURL(this.helpMap.getURL(id));
  }

  public String getMappedHelpURLString(final String id) {
    return this.helpMap.getURL(id);
  }

  public InputStream getHelpInputStream(final String name) {
    return HelpSet.class.getResourceAsStream(this.rootPath + "/" + name);
  }

  public HelpMapper getHelpMapper() {
    return this.helpMap;
  }

  public static class ViewConfig {

    private String name;
    private String label;
    private String data;

    public ViewConfig(final List<XMLDocument.XMLTag> viewTags) {
      for (final XMLDocument.XMLTag innerTag : viewTags) {
        if ("name".equals(innerTag.tag)) {
          this.name = innerTag.text;
        }
        if ("label".equals(innerTag.tag)) {
          this.label = innerTag.text;
        }
        if ("data".equals(innerTag.tag)) {
          this.data = innerTag.text;
        }
      }
    }

    public String getName() {
      return this.name;
    }

    public String getLabel() {
      return this.label;
    }

    public String getData() {
      return this.data;
    }
  }
}
