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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

/**
 * Abstract base view for a tree visualisation of help view nodes.
 */
public abstract class HelpAbstractOverviewView extends JPanel {

  private static final long serialVersionUID = 1L;

  private FilterableTree tree;

  private final boolean activateSearch;

  private JPanel searchPanel;

  private JTextField searchField;

  /**
   * Creates the panel with a BorderLayout and sets the activateSearch property.
   * 
   * @param activateSearch show search textfield?
   */
  public HelpAbstractOverviewView(final boolean activateSearch) {
    super(new BorderLayout());
    this.activateSearch = activateSearch;
  }

  /**
   * Returns the tree, the main part of this view.
   * 
   * @return tree view.
   */
  protected FilterableTree getTree() {
    return this.tree;
  }

  /**
   * Clears the tree model by removing all nodes from root node.
   */
  protected void clear() {
    DefaultTreeModel treeModel = (DefaultTreeModel) this.tree.getModel();
    DefaultMutableTreeNode root = (DefaultMutableTreeNode) getTree().getModel().getRoot();
    while (root.getChildCount() > 0) {
      treeModel.removeNodeFromParent((MutableTreeNode) root.getChildAt(0));
    }
  }

  /**
   * Returns the icon associated with this view, e.g. to be placed in a tab - note that null is a valid return value that must be catered
   * for!
   *
   * @return icon associated with this view, or null if none is available.
   */
  public abstract ImageIcon getIcon();

  /**
   * Adds a selection listener to the tree.
   * 
   * @param tsl selection listener.
   */
  public void addSelectionListener(final TreeSelectionListener tsl) {
    getTree().addTreeSelectionListener(tsl);
  }

  /**
   * Tries to select the node that represents the given url.
   * 
   * @param hs helpset.
   * @param url url.
   */
  public void tryToSelectURL(final HelpSet hs, final URL url) {
    HelpOverviewNode matchingNode = getMatchingNode(hs, url, (HelpOverviewNode) getTree().getModel().getRoot());
    if (matchingNode != null) {
      final TreePath tp = new TreePath(matchingNode.getPath());
      getTree().setSelectionPath(tp);
      getTree().scrollPathToVisible(tp);
    }
  }

  private HelpOverviewNode getMatchingNode(final HelpSet hs, final URL url, final HelpOverviewNode root) {
    if (matches(hs, url, root)) {
      return root;
    }
    // checking direct hierarchy
    for (int i = 0; i < root.getChildCount(); i++) {
      HelpOverviewNode node = (HelpOverviewNode) root.getChildAt(i);
      if (matches(hs, url, node)) {
        return node;
      }
    }
    // recursing...
    for (int i = 0; i < root.getChildCount(); i++) {
      HelpOverviewNode node = (HelpOverviewNode) root.getChildAt(i);
      if (node.getChildCount() > 0) {
        HelpOverviewNode matchingNode = getMatchingNode(hs, url, node);
        if (matchingNode != null) {
          return matchingNode;
        }
      }
    }
    return null;
  }

  private static boolean matches(final HelpSet hs, final URL url, final HelpOverviewNode node) {
    if (url.toExternalForm().indexOf(node.target) >= 0) {
      return true;
    }
    // check if mapped id would work...
    String mappedTarget = hs.getMappedHelpURLString(node.target);
    if (mappedTarget != null && url.toExternalForm().indexOf(mappedTarget) >= 0) {
      return true;
    }
    return false;
  }

  /**
   * Extension point to create the view based on the given XML document.
   * 
   * @param xmlDoc XML document.
   */
  public abstract void createView(final XMLDocument xmlDoc);

  /**
   * Creates a tree based on the given xmlDoc where it parses through all tags specified by given tag name.
   *
   * @param xmlDoc XML document, may be null (for creation of an empty view).
   * @param tagName tag name to parse for.
   */
  protected void createView(final XMLDocument xmlDoc, final String tagName) {
    HelpOverviewNode root = new HelpOverviewNode("root", "root");
    if (xmlDoc != null) {
      createNodes(xmlDoc, tagName, root);
    }
    this.tree = new FilterableTree(root);
    getTree().setCellRenderer(createTreeCellRenderer());
    add(new JScrollPane(getTree()), BorderLayout.CENTER);
    getTree().setRootVisible(false);
    if (this.activateSearch) {
      this.searchPanel = new JPanel(new BorderLayout());
      this.searchField = new JTextField();
      this.searchPanel.add(new JLabel(ResourceManager.getText("search")), BorderLayout.WEST);
      this.searchPanel.add(this.searchField, BorderLayout.CENTER);
      add(this.searchPanel, BorderLayout.NORTH);
      this.searchField.addKeyListener(new KeyAdapter() {
        public void keyPressed(final KeyEvent kev) {
          if (kev.getKeyCode() == KeyEvent.VK_ENTER) {
            doSearch(HelpAbstractOverviewView.this.searchField.getText());
          }
        }
      });
    }
  }

  /**
   * Starts the search for the given text.
   * 
   * @param searchText text to search for.
   */
  public void doSearch(final String searchText) {
    getTree().filterTree(searchText, false, false);
  }

  /**
   * Creates the TreeCellRenderer for this tree-based view - override to provide custom renderer.
   *
   * @return tree renderer.
   */
  protected TreeCellRenderer createTreeCellRenderer() {
    return new HelpOverviewTreeCellRenderer();
  }

  /**
   * Expands the tree so that the given node is visible.
   *
   * @param treeNodeToExpand node to make visible.
   */
  public void expandTreeNode(final DefaultMutableTreeNode treeNodeToExpand) {
    TreePath treePath = new TreePath(treeNodeToExpand.getPath());
    getTree().expandPath(treePath);
    getTree().expandRow(getTree().getRowForPath(treePath));
    if (treeNodeToExpand.getChildCount() > 0) {
      for (int i = 0; i < treeNodeToExpand.getChildCount(); i++) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) treeNodeToExpand.getChildAt(i);
        expandTreeNode(node);
      }
    }
  }

  private static void createNodes(final XMLDocument toc, final String tocTagPath, HelpOverviewNode root) {
    List<XMLDocument.XMLTag> tocItems = toc.getTags(tocTagPath);
    for (final XMLDocument.XMLTag tocItem : tocItems) {
      HelpOverviewNode newNode = createNode(tocItem);
      root.add(newNode);
      createNodes(tocItem.children, newNode);
    }
  }

  private static void createNodes(final List<XMLDocument.XMLTag> tocItems, HelpOverviewNode root) {
    for (final XMLDocument.XMLTag tocItem : tocItems) {
      HelpOverviewNode newNode = createNode(tocItem);
      root.add(newNode);
      createNodes(tocItem.children, newNode);
    }
  }

  private static HelpOverviewNode createNode(final XMLDocument.XMLTag tocItem) {
    return new HelpOverviewNode(tocItem.getAttribute("target"), tocItem.getAttribute("text"));
  }

  /**
   * TreeNode specialisation encapsulating an URL target and a text to be visualised.
   */
  public static class HelpOverviewNode extends FilterableTree.FilterableNode {

    private static final long serialVersionUID = 1L;

    private String target;

    private String text;

    public HelpOverviewNode(final String target, final String text) {
      super(text);
      this.target = target;
      this.text = text;
    }

    public String getTarget() {
      return this.target;
    }

    @Override
    public String toString() {
      return this.text;
    }
  }

  /**
   * Tree renderer for overview trees; currently only the default renderer.
   */
  public static class HelpOverviewTreeCellRenderer extends DefaultTreeCellRenderer {

    private static final long serialVersionUID = 1L;

    @Override
    public Component getTreeCellRendererComponent(JTree tree,
                                                  Object value,
                                                  boolean sel,
                                                  boolean expanded,
                                                  boolean leaf,
                                                  int row,
                                                  boolean hasFocus) {
      return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
    }

  }
}