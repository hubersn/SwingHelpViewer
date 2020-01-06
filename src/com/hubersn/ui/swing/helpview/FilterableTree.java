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

import java.util.Enumeration;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * Model-based filtered tree implementation - needs instances of FilterableNode in the tree model, and
 * works as a combination of FilterableTreeModel and FilterableNode and matches on TreeNode.toString().
 */
public class FilterableTree extends JTree {

  private static final long serialVersionUID = 1L;

  private FilterableTreeModel filterableTreeModel;

  /**
   * Call to filter the tree based on the given filter text with the given operation modes - automatically
   * expands the full resulting tree.
   *
   * @param filterText text to filter for.
   * @param leafsOnly only check leaf nodes for filter match (true), or all nodes (false).
   * @param caseSensitive match text case sensitive?
   */
  public void filterTree(final String filterText, final boolean leafsOnly, final boolean caseSensitive) {
    filterNodes(filterText, (FilterableNode)this.filterableTreeModel.getRoot(), leafsOnly, caseSensitive);
    this.filterableTreeModel.reload();
    expandAll(this);
  }

  private static void expandAll(final JTree tree) {
    expandAll(tree, new TreePath(tree.getModel().getRoot()));
  }

  private static void expandAll(final JTree tree, final TreePath parent) {
    TreeNode node = (TreeNode) parent.getLastPathComponent();
    if (node.getChildCount() >= 0) {
      for (Enumeration<?> e = node.children(); e.hasMoreElements();) {
        TreeNode n = (TreeNode) e.nextElement();
        TreePath path = parent.pathByAddingChild(n);
        expandAll(tree, path);
      }
    }
    tree.expandPath(parent);
  }

  private static void filterNodes(final String filterText,
                                  final FilterableNode root,
                                  final boolean leafsOnly,
                                  final boolean caseSensitive) {
    for (int i = 0; i < root.getChildCount(); i++) {
      final TreeNode tn = root.getChildAt(i);
      if (tn instanceof FilterableNode) {
        final FilterableNode in = (FilterableNode)tn;
        if (!leafsOnly || in.isLeaf()) {
          in.setVisible(matches(in, filterText, caseSensitive));
        }
        filterNodes(filterText, in, leafsOnly, caseSensitive);
      }
    }
  }

  private static boolean matches(final FilterableNode node, final String textToMatch, final boolean caseSensitive) {
    if (textToMatch.isEmpty()) {
      return true;
    }
    return containsText(node.toString(), textToMatch, caseSensitive);
  }

  private static boolean containsText(String haystack, String needle, final boolean caseSensitive) {
    if (!caseSensitive) {
      haystack = haystack.toLowerCase();
      needle = needle.toLowerCase();
    }
    return haystack.indexOf(needle) >= 0;
  }

  /**
   * Creates a new instance of FilterableTree with given node as root node.
   *
   * @param root root node.
   */
  public FilterableTree(final TreeNode root) {
    super();
    this.filterableTreeModel = new FilterableTreeModel(root);
    setModel(this.filterableTreeModel);
  }

  /**
   * TreeModel supporting filterable nodes.
   */
  public static class FilterableTreeModel extends DefaultTreeModel {

    private static final long serialVersionUID = 1L;

    private boolean filterActive = true;

    /**
     * Creates a new instance of FilterableTreeModel with given node as root node.
     *
     * @param root root node.
     */
    public FilterableTreeModel(final TreeNode root) {
      super(root);
    }

    /**
     * Overridden method to get a child node at given index supporting filtering.
     *
     * @param parent parent node.
     * @param index child index.
     * @return tree node at given index of given index.
     */
    @Override
    public Object getChild(final Object parent, final int index) {
      if (this.filterActive && parent instanceof FilterableNode) {
        return ((FilterableNode)parent).getChildAt(index, this.filterActive);
      }
      return ((TreeNode)parent).getChildAt(index);
    }

    /**
     * Returns the number of child nodes of the given parent.
     *
     * @param parent parent node.
     * @return number of children.
     */
    @Override
    public int getChildCount(final Object parent) {
      if (this.filterActive && parent instanceof FilterableNode) {
        return ((FilterableNode)parent).getChildCount(this.filterActive);
      }
      return ((TreeNode)parent).getChildCount();
    }

  }

  /**
   * TreeNode implementation providing a "visible" attribute.
   */
  public static class FilterableNode extends DefaultMutableTreeNode {

    private static final long serialVersionUID = 1L;

    private boolean visible = true;

    /**
     * Creates a new instance of FilterableNode containing given user object.
     *
     * @param userObject node user object.
     */
    public FilterableNode(final String userObject) {
      super(userObject);
    }

    private boolean isAnyChildVisible() {
      if (this.children == null) {
        return false;
      }
      final Enumeration<?> e = this.children.elements();
      while (e.hasMoreElements()) {
        final FilterableNode node = (FilterableNode)e.nextElement();
        if (node.isVisible() || node.isAnyChildVisible()) {
          return true;
        }
      }
      return false;
    }

    /**
     * Returns the child of this node at given index depending on given filter state.
     *
     * @param index index of child node to be returned.
     * @param filterIsActive filtering active?
     * @return child of node at given index.
     */
    public TreeNode getChildAt(final int index, final boolean filterIsActive) {
      if (!filterIsActive) {
        return super.getChildAt(index);
      }
      if (this.children == null) {
        throw new ArrayIndexOutOfBoundsException("node has no children");
      }

      int realIndex = -1;
      int visibleIndex = -1;
      final Enumeration<?> e = this.children.elements();
      while (e.hasMoreElements()) {
        final FilterableNode node = (FilterableNode)e.nextElement();
        if (node.isVisible() || node.isAnyChildVisible()) {
          visibleIndex++;
        }
        realIndex++;
        if (visibleIndex == index) {
          return (TreeNode)this.children.elementAt(realIndex);
        }
      }

      throw new ArrayIndexOutOfBoundsException("index unmatched");
    }

    /**
     * Returns the number of children of this node depending on given filter state.
     *
     * @param filterIsActive filtering active?
     * @return number of children of this node.
     */
    public int getChildCount(final boolean filterIsActive) {
      if (!filterIsActive) {
        return super.getChildCount();
      }
      if (this.children == null) {
        return 0;
      }

      int count = 0;
      final Enumeration<?> e = this.children.elements();
      while (e.hasMoreElements()) {
        final FilterableNode node = (FilterableNode)e.nextElement();
        if (node.isVisible() || node.isAnyChildVisible()) {
          count++;
        }
      }

      return count;
    }

    /**
     * Sets visibility of this node.
     *
     * @param visible node visible?
     */
    public void setVisible(final boolean visible) {
      this.visible = visible;
    }

    /**
     * Returns if this node is visible.
     *
     * @return node visible?
     */
    public boolean isVisible() {
      return this.visible;
    }
  }
}
