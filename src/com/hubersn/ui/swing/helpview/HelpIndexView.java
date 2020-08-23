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
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Index view for simple help sets.
 */
public class HelpIndexView extends HelpAbstractOverviewView {

  private static final long serialVersionUID = 1L;

  /** Name from view config represented by this implementation. */
  public static final String VIEW_NAME = "Index";

  private ImageIcon indexIcon;

  /**
   * Creates a new instance of HelpIndexView, a view representing indexitem entries as tree nodes.
   */
  public HelpIndexView() {
    super(true);
    try {
      this.indexIcon = ResourceManager.getTabIndexIcon();
    } catch (Exception ex) {
      // no icon - no problem.
      this.indexIcon = null;
    }
  }

  /**
   * Creates the view based on the given XML document.
   * 
   * @param indexXMLDoc XML document.
   */
  public void createView(final XMLDocument indexXMLDoc) {
    super.createView(indexXMLDoc, "indexitem");
    expandTreeNode((DefaultMutableTreeNode)getTree().getModel().getRoot());
  }

  @Override
  public ImageIcon getIcon() {
    return this.indexIcon;
  }
}