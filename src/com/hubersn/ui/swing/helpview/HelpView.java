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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

/**
 * SplitPane view with TOC, index, search (in a tabbed pane, left) and content (right).
 */
public class HelpView extends JPanel {

  private static final long serialVersionUID = 1L;

  private JSplitPane sp;

  private HelpContentView contentView;

  private JTabbedPane tabbedPane;

  private AbstractAction homePageAction;

  private JToolBar toolbar;

  /**
   * Creates a new instance of the HelpView panel.
   *
   * @param helpSetToShow helpset to show.
   * @throws Exception on error.
   */
  public HelpView(final HelpSet helpSetToShow) throws Exception {
    super(new BorderLayout());
    this.tabbedPane = new JTabbedPane(SwingConstants.TOP);
    this.tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    this.contentView = new HelpContentView();

    final List<HelpSet.ViewConfig> views = helpSetToShow.getViews();
    for (final HelpSet.ViewConfig view : views) {
      HelpAbstractOverviewView viewForTab = null;
      if (view.getName().equals(HelpTOCView.VIEW_NAME)) {
        viewForTab = new HelpTOCView();
        viewForTab.createView(new XMLDocument(helpSetToShow.getHelpInputStream(view.getData())));
        viewForTab.addSelectionListener(new TreeSelectionListener() {

          @Override
          public void valueChanged(TreeSelectionEvent e) {
            if (e != null && e.getPath() != null) {
              Object obj = e.getPath().getLastPathComponent();
              if (obj instanceof HelpAbstractOverviewView.HelpOverviewNode) {
                HelpAbstractOverviewView.HelpOverviewNode selectedNode = (HelpAbstractOverviewView.HelpOverviewNode) obj;
                showTarget(helpSetToShow, selectedNode.getTarget());
              }
            }
          }
        });

        final HelpAbstractOverviewView localView = viewForTab;
        this.contentView.addPageURLChangedListener(new PropertyChangeListener() {

          @Override
          public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(HelpContentView.PAGE_URL_CHANGED_PROPERTY) && evt.getNewValue() instanceof URL) {
              localView.tryToSelectURL(helpSetToShow, (URL)evt.getNewValue());
            }
          }
        });
      } else if (view.getName().equals(HelpIndexView.VIEW_NAME)) {
        viewForTab = new HelpIndexView();
        viewForTab.createView(new XMLDocument(helpSetToShow.getHelpInputStream(view.getData())));
        viewForTab.addSelectionListener(new TreeSelectionListener() {

          @Override
          public void valueChanged(TreeSelectionEvent e) {
            if (e != null && e.getPath() != null) {
              Object obj = e.getPath().getLastPathComponent();
              if (obj instanceof HelpAbstractOverviewView.HelpOverviewNode) {
                HelpAbstractOverviewView.HelpOverviewNode selectedNode = (HelpAbstractOverviewView.HelpOverviewNode) obj;
                showTarget(helpSetToShow, selectedNode.getTarget());
              }
            }
          }
        });
      }
      if (viewForTab != null) {
        // check for icon - either icon for tab and text as tooltip, or text as tab text
        final ImageIcon icon = viewForTab.getIcon();
        if (icon == null) {
          this.tabbedPane.addTab(view.getLabel(), viewForTab);
        } else {
          this.tabbedPane.addTab("", icon, viewForTab, view.getLabel());
        }
      }
    }

    this.homePageAction = new AbstractAction() {

      private static final long serialVersionUID = 1L;

      @Override
      public void actionPerformed(ActionEvent e) {
        try {
          HelpView.this.contentView.setPage(helpSetToShow.getHelpHomeURL());
        } catch (IOException ex) {
          // only log for now - won't happen on consistent helpsets
          ex.printStackTrace();
        }
      }
    };
    this.homePageAction.putValue(Action.LARGE_ICON_KEY, ResourceManager.getToolbarHomeIcon());

    this.contentView.setPage(helpSetToShow.getHelpHomeURL());
    this.tabbedPane.setMinimumSize(new Dimension(200,200));
    this.sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, this.tabbedPane, this.contentView);
    this.sp.setOneTouchExpandable(true);
    this.sp.setResizeWeight(0.0d);
    add(this.sp, BorderLayout.CENTER);
  }

  public JToolBar getToolBar() {
    if (this.toolbar == null) {
      this.toolbar = new JToolBar();
      this.toolbar.setFloatable(false);
      postprocessToolbarButton(this.toolbar.add(this.contentView.getPreviousPageAction()));
      postprocessToolbarButton(this.toolbar.add(this.contentView.getNextPageAction()));
      postprocessToolbarButton(this.toolbar.add(this.homePageAction));
    }
    return this.toolbar;
  }

  private static void postprocessToolbarButton(final JButton btn) {
    btn.setDefaultCapable(false);;
    btn.setFocusable(false);
  }

  private void showTarget(final HelpSet hs, final String targetId) {
    try {
      HelpView.this.contentView.setPage(hs.getMappedHelpURL(targetId));
      String lastRef = hs.getLastRef();
      HelpView.this.contentView.scrollToReference(lastRef);
    } catch (Exception ex) {
      // only log for now - won't happen on consistent helpsets
      System.err.println("Target failed: " + targetId);
      System.err.println("URL failed: " + hs.getMappedHelpURL(targetId));
      ex.printStackTrace();
    }
  }
}
