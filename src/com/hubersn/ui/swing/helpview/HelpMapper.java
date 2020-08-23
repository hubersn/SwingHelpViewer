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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Maps helpIDs ("target" attribute in a mapID tag) to true target URLs ("url" attribute in a mapID tag).
 */
public class HelpMapper {

  private final Map<String, String> helpMap = new HashMap<>();

  /**
   * Creates a new instance of HelpMapper based on given input stream with the XML .jhm source.
   *
   * @param helpMapInputStream XML map-target source input stream.
   * @throws Exception on error.
   */
  public HelpMapper(final InputStream helpMapInputStream) throws Exception {
    final XMLDocument doc = new XMLDocument(helpMapInputStream);
    final List<XMLDocument.XMLTag> mapTags = doc.getTags("mapID");
    for (final XMLDocument.XMLTag map : mapTags) {
      this.helpMap.put(map.getAttribute("target"), map.getAttribute("url"));
    }
  }

  /**
   * Returns the URL as a string for the given target (help ID).
   * 
   * @param target help ID.
   * @return URL representing given help ID, or null if it does not exist.
   */
  public String getURL(final String target) {
    return this.helpMap.get(target);
  }

  /**
   * Returns all target keys for this help map.
   * 
   * @return all target keys.
   */
  public String[] getTargets() {
    return this.helpMap.keySet().toArray(new String[0]);
  }

}
