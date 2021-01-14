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
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.Highlight;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

/**
 * Help content view for Swing based on HTML JEditorPane.
 */
public class HelpContentView extends JPanel {

  private static final long serialVersionUID = 1L;

  /** Property name to signal the change of the shown URL. */
  public static final String PAGE_URL_CHANGED_PROPERTY = "HELP_CONTENT_VIEW_PAGE_URL_CHANGED_PROPERTY";

  private static final String HTML_MIME_TYPE = "text/html";

  private JEditorPane contentView;

  private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

  private AbstractAction previousPageAction;

  private AbstractAction nextPageAction;

  private PageHistory pageHistory;

  private URL lastPage = null;

  /** Flag to signal to ignore that next "setPage" should not manipulate page history. */
  private boolean ignoreNextAdd = false;

  /**
   * Creates a new instance of HelpContentView, including HTML-capable JEditorPane.
   */
  public HelpContentView() {
    super(new BorderLayout());
    this.pageHistory = new PageHistory();
    this.contentView = new JEditorPane();
    this.contentView.setEditable(false);
    this.contentView.setEditorKitForContentType(HTML_MIME_TYPE, new SyncHTMLEditorKit());
    this.contentView.setContentType(HTML_MIME_TYPE);
    add(new JScrollPane(this.contentView), BorderLayout.CENTER);
    this.contentView.addHyperlinkListener(new HyperlinkListener() {

      @Override
      public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == EventType.ACTIVATED) {
          try {
            setPage(e.getURL());
          } catch (Exception ex) {
            // Nothing we could possibly do, this comes from interpreting URLs from given HTML.
            ex.printStackTrace();
          }
        }
      }
    });

    this.nextPageAction = new AbstractAction() {

      private static final long serialVersionUID = 1L;

      @Override
      public void actionPerformed(ActionEvent e) {
        try {
          final URL nextPage = HelpContentView.this.pageHistory.nextFromHistory();
          HelpContentView.this.ignoreNextAdd = true;
          setPageInternal(nextPage);
        } catch (IOException ex) {
          // Nothing we could possibly do, this comes from interpreting URLs from given HTML.
          ex.printStackTrace();
        }
      }
    };
    ResourceManager.addIconToActionIfAvailable(this.nextPageAction, ResourceManager.getToolbarNextIcon());

    this.previousPageAction = new AbstractAction() {

      private static final long serialVersionUID = 1L;

      @Override
      public void actionPerformed(ActionEvent e) {
        try {
          final URL previousPage = HelpContentView.this.pageHistory.previousFromHistory();
          HelpContentView.this.ignoreNextAdd = true;
          setPageInternal(previousPage);
        } catch (IOException ex) {
          // Nothing we could possibly do, this comes from interpreting URLs from given HTML.
          ex.printStackTrace();
        }
      }
    };
    ResourceManager.addIconToActionIfAvailable(this.previousPageAction, ResourceManager.getToolbarPreviousIcon());

    enableNavigationActions();
  }

  /**
   * Returns the action to go to the previous help page.
   *
   * @return action to go to previous help page.
   */
  public Action getPreviousPageAction() {
    return this.previousPageAction;
  }

  /**
   * Returns the action to go to the next help page.
   *
   * @return action to go to next help page.
   */
  public Action getNextPageAction() {
    return this.nextPageAction;
  }

  private void enableNavigationActions() {
    this.previousPageAction.setEnabled(this.pageHistory.isPreviousFromHistoryAvailable());
    this.nextPageAction.setEnabled(this.pageHistory.isNextFromHistoryAvailable());
  }

  /**
   * Adds the given listener to listen for PAGE_URL_CHANGED events.
   *
   * @param listener property change listener.
   */
  public void addPageURLChangedListener(final PropertyChangeListener listener) {
    this.pcs.addPropertyChangeListener(listener);
  }

  /**
   * Removes the given listener from the chain of listeners.
   *
   * @param listener property change listener to remove.
   */
  public void removePageURLChangedListener(final PropertyChangeListener listener) {
    this.pcs.removePropertyChangeListener(listener);
  }

  /**
   * Sets the given URL (or better, the content represented by this URL) as the content.
   *
   * @param url URL to show in this content view.
   * @throws IOException on connection error.
   */
  public void setPage(final URL url) throws IOException {
    if (url != null) {
      setPageInternal(url);
      if (!this.ignoreNextAdd) {
        this.pageHistory.addPageToHistory(url);
      }
      this.ignoreNextAdd = false;
    }
    enableNavigationActions();
  }

  /**
   * Returns the plain text (i.e. without HTML tags) from this content view.
   * 
   * @return plain text.
   */
  public String getPlainText() {
    try {
      return this.contentView.getText(0, this.contentView.getDocument().getLength());
    } catch (BadLocationException ex) {
      // internal JEditorPane error, never seen...
      ex.printStackTrace();
    }
    return "";
  }

  /**
   * Remove possibly existing highlights in this view.
   */
  public void clearHighlights() {
    this.contentView.getHighlighter().removeAllHighlights();
  }

  /**
   * Add a highlight at given start offset with given length and given colour.
   * 
   * @param startOffset start offset for highlight.
   * @param length length for highlight.
   * @param highlightColour colour for highlight.
   */
  public void addHighlight(final int startOffset, final int length, final Color highlightColour) {
    final Highlighter highlighter = this.contentView.getHighlighter();
    final DefaultHighlighter.DefaultHighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(highlightColour);
    try {
      highlighter.addHighlight(startOffset, startOffset + length, painter);
    } catch (BadLocationException ex) {
      // internal JEditorPane error, never seen...
      ex.printStackTrace();
    }
  }

  /**
   * Returns the title of this content view, or the empty string if the title is undefined.
   * 
   * @return title of this content view, or empty string.
   */
  public String getTitle() {
    Document doc = this.contentView.getDocument();
    if (doc instanceof HTMLDocument) {
      HTMLDocument hdoc = (HTMLDocument)doc;
      Object title = hdoc.getProperty(Document.TitleProperty);
      if (title != null) {
        return title.toString();
      }
    }
    return "";
  }

  private void setPageInternal(final URL url) throws IOException {
    if (url != null) {
      this.contentView.getHighlighter().removeAllHighlights();
      this.contentView.setPage(url);
      this.pcs.firePropertyChange(PAGE_URL_CHANGED_PROPERTY, this.lastPage, url);
      this.lastPage = url;
    }
    enableNavigationActions();
  }

  /**
   * Scrolls the current content view to the given reference.
   *
   * @param reference reference to scroll to.
   */
  public void scrollToReference(final String reference) {
    if (reference != null && !reference.equals("")) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          HelpContentView.this.contentView.scrollToReference(reference);
        }
      });
    }
  }

  /**
   * Scrolls the current content view to the first highlight (if it exists).
   */
  public void scrollToFirstHighlight() {
    final JEditorPane ep = this.contentView;
    if (ep.getHighlighter() != null) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          final Highlighter h =  ep.getHighlighter();
          final Highlight[] highlights = h.getHighlights();
          if (highlights != null && highlights.length > 0) {
            final Highlight highlight = highlights[0];
            try {
              final Rectangle scrollPoint = ep.modelToView(highlight.getStartOffset());
              ep.scrollRectToVisible(makeRectangleBigger(scrollPoint));
            } catch (BadLocationException e) {
              // will never happen, highlight position is always inside doc.
              e.printStackTrace();
            }
          }
        }
      });
    }
  }

  private static Rectangle makeRectangleBigger(final Rectangle sourceRect) {
    final int OFFSET = 30;
    sourceRect.y = Math.max(0, sourceRect.y - OFFSET);
    sourceRect.height = sourceRect.height + 2 * OFFSET;
    return sourceRect;
  }

  private static void dumpTags(HTMLDocument d, HTML.Tag tag) {
    HTMLDocument.Iterator iterator = d.getIterator(tag);
    System.out.println("Iterator for tag "+tag+" - null: "+(iterator == null));
    if (iterator != null) {
      System.out.println("  iterator valid: "+iterator.isValid());
    }
  }

  private void detectTitle() {
    Document doc = this.contentView.getDocument();
    if (doc instanceof HTMLDocument) {
      HTMLDocument hdoc = (HTMLDocument)doc;

      System.out.println("Document Property:" + hdoc.getProperty(Document.TitleProperty));
      // the following stuff did not work and I don't know why!
      Element element = hdoc.getElement(hdoc.getDefaultRootElement(), StyleConstants.NameAttribute, HTML.Tag.TITLE);
      if (element != null && element.getStartOffset() >= 0) {
        try {
          System.out.println("Element-based NameAttribute search: " + doc.getText(element.getStartOffset(), element.getEndOffset() - element.getStartOffset()));
        } catch (BadLocationException ex) {
          // TODO Auto-generated catch block
          ex.printStackTrace();
        }
      } else {
        System.out.println("Element with NameAttribute TITLE not found: "+element);
      }
      dumpTags(hdoc, HTML.Tag.HEAD);
      dumpTags(hdoc, HTML.Tag.BODY);
      dumpTags(hdoc, HTML.Tag.TITLE);
      dumpTags(hdoc, HTML.Tag.H1);
      HTMLDocument.Iterator iterator = hdoc.getIterator(HTML.Tag.TITLE);
      if (iterator != null && iterator.isValid()) {
        int startOffset = iterator.getStartOffset();
        if (startOffset >= 0) {
          try {
            System.out.println("HTMLDocument.Iterator for tags TITLE: " + doc.getText(startOffset, iterator.getEndOffset() - startOffset));
          } catch (BadLocationException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
          }
        }
      } else {
        System.out.println("No valid iterator for tag TITLE found: "+iterator);
      }
    }
  }

  /**
   * Minimal subclass to force new documents to load synchronously to allow a scrollToReference to work always when called immediately after
   * setPage.
   */
  private static class SyncHTMLEditorKit extends HTMLEditorKit {

    private static final long serialVersionUID = 1L;

    public SyncHTMLEditorKit() {
      super();
    }

    @Override
    public Document createDefaultDocument() {
      Document newDoc = super.createDefaultDocument();
      if (newDoc instanceof AbstractDocument) {
        ((AbstractDocument) newDoc).setAsynchronousLoadPriority(-1);
      }
      return newDoc;
    }
  }

  private static class PageHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    // index of last (after add) or current (after prev/next) entry
    private int index = 0;

    private List<URL> urlList = new ArrayList<>();

    public void addPageToHistory(final URL url) {
      // remove items from list up to current position
      for (int indexToDelete = this.urlList.size() - 1; indexToDelete > this.index; indexToDelete--) {
        this.urlList.remove(indexToDelete);
      }
      // check if URL to add is already at current position
      if (this.urlList.isEmpty() || !url.toString().equals(this.urlList.get(this.urlList.size() - 1).toString())) {
        this.urlList.add(url);
        this.index = this.urlList.size() - 1;
      }
    }

    public URL nextFromHistory() {
      if (!isNextFromHistoryAvailable()) {
        return null;
      }
      this.index++;
      return this.urlList.get(this.index);
    }

    public URL previousFromHistory() {
      if (!isPreviousFromHistoryAvailable()) {
        return null;
      }
      this.index--;
      return this.urlList.get(this.index);
    }

    public boolean isNextFromHistoryAvailable() {
      return this.index < this.urlList.size() - 1;
    }

    public boolean isPreviousFromHistoryAvailable() {
      return this.index > 0;
    }
  }
}
