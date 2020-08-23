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

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 * Search view for searching help content and displaying results.
 */
public class HelpSearchView extends HelpAbstractOverviewView {

  private static final long serialVersionUID = 1L;

  /** Name from view config represented by this implementation. */
  public static final String VIEW_NAME = "Search";

  private ImageIcon searchIcon;

  private HelpSet helpSet;

  /**
   * Creates a new instance of HelpSearchView, a tree-based view implementing full-text search and
   * visualising the hits as nodes in the tree.
   * 
   * @param helpSet source help set.
   */
  public HelpSearchView(final HelpSet helpSet) {
    super(true);
    this.helpSet = helpSet;
    try {
      this.searchIcon = ResourceManager.getTabSearchIcon();
    } catch (Exception ex) {
      // no icon - no problem.
      this.searchIcon = null;
    }
  }

  /**
   * Creates the search view based on the given XML document.
   * 
   * @param xmlDoc XML document.
   */
  public void createView(final XMLDocument xmlDoc) {
    super.createView(null, "");
  }

  @Override
  public ImageIcon getIcon() {
    return this.searchIcon;
  }

  @Override
  public void doSearch(final String searchText) {
    clear();
    if (searchText == null || "".equals(searchText)) {
      return;
    }
    // Search through all mapped docs
    HelpMapper helpMapper = this.helpSet.getHelpMapper();
    final String[] targets = helpMapper.getTargets();
    // first, collect all URLs - there might be different targets that point to the same URL
    final Map<URL, String> urls = new HashMap<>();
    for (final String target : targets) {
      urls.put(this.helpSet.getMappedHelpURL(target), target);
    }
    final List<SearchNode> searchResults = new ArrayList<>();
    for (final URL url : urls.keySet()) {
      try {
        HelpContentView pane = new HelpContentView();
        pane.setPage(url);
        String toSearch = pane.getPlainText().toLowerCase();
        List<Integer> matches = new ArrayList<>();
        String realSearchText = searchText.toLowerCase();
        int index = toSearch.indexOf(realSearchText);
        while (index >= 0) {
          matches.add(index);
          index = toSearch.indexOf(realSearchText, index + 1);
        }
        if (!matches.isEmpty()) {
          String title = pane.getTitle();
          if (title == null) {
            title = searchText;
          }
          SearchNode result = new SearchNode(urls.get(url), title, matches, realSearchText.length());
          searchResults.add(result);
        }
      } catch (Exception ex) {
        // TODO Auto-generated catch block
        ex.printStackTrace();
      }
    }
    DefaultMutableTreeNode root = (DefaultMutableTreeNode)getTree().getModel().getRoot();
    if (!searchResults.isEmpty()) {
      Collections.sort(searchResults);
      Collections.reverse(searchResults);
      for (final SearchNode searchResult : searchResults) {
        ((DefaultTreeModel)getTree().getModel()).insertNodeInto(searchResult, root, root.getChildCount());
      }
    } else {
      ((DefaultTreeModel)getTree().getModel()).insertNodeInto(new SearchNode(null, "no matches", new ArrayList<Integer>(), 0), root, root.getChildCount());
    }
    expandTreeNode(root);
  }

  /**
   * Class representing a search result inside a tree view.
   */
  public static class SearchNode extends HelpAbstractOverviewView.HelpOverviewNode implements Comparable<SearchNode> {
    private static final long serialVersionUID = 1L;
    private final List<Integer> matches;
    private final int length;
    public SearchNode(final String target, final String text, final List<Integer> matches, final int length) {
      super(target, text);
      this.matches = matches;
      this.length = length;
    }
    public List<Integer> getMatches() {
      return this.matches;
    }
    public int getLength() {
      return this.length;
    }
    @Override
    public String toString() {
      return this.matches.size() + " " + super.toString();
    }
    @Override
    public int compareTo(final SearchNode o) {
      if (Objects.equals(this, o)) {
        return 0;
      }
      if (this.matches.size() == o.matches.size()) {
        return 0;
      }
      return this.matches.size() > o.matches.size() ? 1 : -1;
    }
  }
}