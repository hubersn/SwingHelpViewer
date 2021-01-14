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

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/**
 * Simple help viewer in a JFrame.
 */
public class HelpViewer {

  private final JFrame helpViewerFrame;

  private final JPanel mainPanel = new JPanel(new BorderLayout());

  private final HelpView helpView;

  private final HelpSet helpSet;

  /**
   * Creates a new instance of HelpViewer.
   *
   * @param helpSetName name of helpset.
   * @throws Exception on error, e.g. when parsing fails or helpset is not found.
   */
  public HelpViewer(final String helpSetName) throws Exception {
    // load and parse helpset - will throw an Exception if anything goes wrong
    this.helpSet = new HelpSet(helpSetName);
    this.helpViewerFrame = new JFrame(this.helpSet.getFrameTitle());
    this.helpView = new HelpView(this.helpSet);
    this.mainPanel.add(this.helpView.getToolBar(), BorderLayout.NORTH);
    this.mainPanel.add(this.helpView, BorderLayout.CENTER);
    this.helpViewerFrame.setContentPane(this.mainPanel);
    this.helpViewerFrame.pack();
    this.helpViewerFrame.setSize(1024, 768);
  }

  /**
   * Returns the underlying helpset.
   * 
   * @return helpset.
   */
  public HelpSet getHelpSet() {
    return this.helpSet;
  }

  /**
   * Returns the frame used for HelpViewer.
   *
   * @return frame used for HelpViewer.
   */
  public JFrame getFrame() {
    return this.helpViewerFrame;
  }

  private void setVisible(final boolean visible) {
    this.helpViewerFrame.setVisible(visible);
  }

  /**
   * Opens this help viewer frame and shows the help ID - null shows the "Home" help page.
   * 
   * @param helpId helpId to show, null to show "Home" help page.
   */
  public void showHelp(final String helpId) {
    setVisible(true);
    this.helpView.showTarget(helpId);
  }

  /**
   * Starts the help viewer with the given helpset as first and only argument.
   *
   * @param args
   */
  public static void main(final String[] args) {
    if (args == null || args.length != 1) {
      System.err.println("HelpViewer needs the path to a helpset as argument.");
      System.exit(1);
    }
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        final HelpViewer f;
        try {
          f = new HelpViewer(args[0]);
          f.helpViewerFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
          f.setVisible(true);
        } catch (Exception ex) {
          ex.printStackTrace();
          System.exit(1);
        }
      }
    });
  }

}
