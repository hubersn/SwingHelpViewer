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

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;

/**
 * Provides icons and texts to HelpView UI elements, allows integration of
 * custom icons and texts via provide... methods - these need to be called
 * before actually constructing the UI.
 */
public class ResourceManager {

  public static final String TAB_TOC_ICON = "TAB_TOC_ICON_KEY";

  public static final String TAB_INDEX_ICON = "TAB_INDEX_ICON_KEY";

  public static final String TAB_SEARCH_ICON = "TAB_SEARCH_ICON_KEY";

  public static final String TOOLBAR_PREVIOUS_ICON = "TOOLBAR_PREVIOUS_ICON_KEY";

  public static final String TOOLBAR_NEXT_ICON = "TOOLBAR_NEXT_ICON_KEY";

  public static final String TOOLBAR_HOME_ICON = "TOOLBAR_HOME_ICON_KEY";

  private static Map<String, ImageIcon> iconResourceMap = new HashMap<>();

  private static ResourceBundle textResources;

  private static boolean textResourceLoaded = false;

  private static Map<String, String> textResourceMap = new HashMap<>();

  private ResourceManager() {
    // purely static, no instance allowed
  }

  /**
   * Overrides the included icon for the given key.
   *
   * @param iconKey key to store icon.
   * @param icon icon, or null to signal "no icon for this key".
   */
  public static void provideIcon(final String iconKey, final ImageIcon icon) {
    iconResourceMap.put(iconKey, icon);
  }

  /**
   * Overrides the included text for the given key.
   *
   * @param textKey key to store text.
   * @param text text, or null to signal "no text for this key".
   */
  public static void provideText(final String textKey, final String text) {
    textResourceMap.put(textKey, text);
  }

  /**
   * Returns the text for the given key.
   *
   * @param textKey key for text.
   * @return text represented by key.
   */
  public static String getText(final String textKey) {
    if (textResourceMap.containsKey(textKey)) {
      return textResourceMap.get(textKey);
    }
    // check if we need to load the resource bundle
    if (!textResourceLoaded) {
      loadTextResource();
    }
    if (textResources == null) {
      return textKey;
    }
    try {
      return textResources.getString(textKey);
    } catch (final Exception ex) {
      return textKey;
    }
  }

  private static void loadTextResource() {
    textResourceLoaded = true;
    try {
      textResources = ResourceBundle.getBundle(ResourceManager.class.getPackage().getName() + ".resources.helpview");
    } catch (final Exception ex) {
      // don't care, handle null
      textResources = null;
    }
  }

  /**
   * Returns the icon to use for the "TOC" tab icon.
   *
   * @return "TOC" tab icon, may be null.
   */
  public static ImageIcon getTabTOCIcon() {
    checkAndLoadIcon(TAB_TOC_ICON, "toc.png");
    return iconResourceMap.get(TAB_TOC_ICON);
  }

  /**
   * Returns the icon to use for the "Index" tab icon.
   *
   * @return "Index" tab icon, may be null.
   */
  public static ImageIcon getTabIndexIcon() {
    checkAndLoadIcon(TAB_INDEX_ICON, "index.png");
    return iconResourceMap.get(TAB_INDEX_ICON);
  }

  /**
   * Returns the icon to use for the "Search" tab icon.
   *
   * @return "Search" tab icon, may be null.
   */
  public static ImageIcon getTabSearchIcon() {
    checkAndLoadIcon(TAB_SEARCH_ICON, "search.png");
    return iconResourceMap.get(TAB_SEARCH_ICON);
  }

  /**
   * Returns the icon to use for the "Previous" toolbar button.
   *
   * @return "Previous" toolbar button icon.
   */
  public static ImageIcon getToolbarPreviousIcon() {
    checkAndLoadIcon(TOOLBAR_PREVIOUS_ICON, "previous.png");
    return iconResourceMap.get(TOOLBAR_PREVIOUS_ICON);
  }

  /**
   * Returns the icon to use for the "Next" toolbar button.
   *
   * @return "Next" toolbar button icon.
   */
  public static ImageIcon getToolbarNextIcon() {
    checkAndLoadIcon(TOOLBAR_NEXT_ICON, "next.png");
    return iconResourceMap.get(TOOLBAR_NEXT_ICON);
  }

  /**
   * Returns the icon to use for the "Home" toolbar button.
   *
   * @return "Home" toolbar button icon.
   */
  public static ImageIcon getToolbarHomeIcon() {
    checkAndLoadIcon(TOOLBAR_HOME_ICON, "home.png");
    return iconResourceMap.get(TOOLBAR_HOME_ICON);
  }

  private static synchronized void checkAndLoadIcon(final String iconKey, final String iconFilename) {
    if (!iconResourceMap.containsKey(iconKey)) {
      iconResourceMap.put(iconKey, new ImageIcon(HelpTOCView.class.getResource("resources/" + iconFilename)));
    }
  }
}
