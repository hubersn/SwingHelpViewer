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

import javax.swing.ImageIcon;

/**
 * Search view for searching help content and displaying results TODO Full-text
 * search not implemented yet!
 */
public class HelpSearchView extends HelpAbstractOverviewView {

  private static final long serialVersionUID = 1L;

  /** Name from view config represented by this implementation. */
  public static final String VIEW_NAME = "Search";

  private ImageIcon searchIcon;

  public HelpSearchView() {
    super(true);
    try {
      this.searchIcon = ResourceManager.getTabSearchIcon();
    } catch (Exception ex) {
      // no icon - no problem.
      this.searchIcon = null;
    }
  }

  public void createView(final XMLDocument xmlDoc) {
    super.createView(null, "");
    getTree().expandRow(0);
  }

  @Override
  public ImageIcon getIcon() {
    return this.searchIcon;
  }

}