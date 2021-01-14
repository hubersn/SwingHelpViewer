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

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

/**
 * Collection of static utility methods to support context-sensitive help with HelpViewer.
 */
public class ContextSensitiveHelpViewTools {

  private static final String HELP_ID_KEY = "HelpID";

  private static final String HELP_VIEWER_KEY = "HelpViewer";

  private static Cursor helpTrackingCursor;

  private static class GlassPaneManager implements MouseListener {

    private final Component glassPane;

    private final Window associatedMainWindow;

    private final HelpViewer helpViewer;

    public GlassPaneManager(final Component glassPane, final Window associatedMainWindow, final HelpViewer helpViewer) {
      this.glassPane = glassPane;
      this.associatedMainWindow = associatedMainWindow;
      this.helpViewer = helpViewer;
    }

    public void activate() {
      this.glassPane.setVisible(true);
      this.glassPane.setCursor(getHelpTrackingCursor());
      this.glassPane.addMouseListener(this);
    }

    public void deactivate() {
      this.glassPane.setCursor(Cursor.getDefaultCursor());
      this.glassPane.setVisible(false);
      this.glassPane.removeMouseListener(this);
    }

    @Override
    public void mouseClicked(MouseEvent me) {
      // not needed
    }

    @Override
    public void mousePressed(MouseEvent me) {
      // glasspane no longer active, removes the listener, too!
      deactivate();
      // convert coordinate systems since glasspane is not the same as parent window
      me = SwingUtilities.convertMouseEvent(this.glassPane, me, this.associatedMainWindow);
      Component c = SwingUtilities.getDeepestComponentAt(this.associatedMainWindow, me.getX(), me.getY());
      if (c instanceof JComponent) {
        JComponent jc = (JComponent)c;
        // get component's help id, or show main help if it fails
        final String helpId = (String)jc.getClientProperty(HELP_ID_KEY);
        final HelpViewer hv = (HelpViewer)jc.getClientProperty(HELP_VIEWER_KEY);
        if (helpId != null) {
          hv.showHelp(helpId);
        } else {
          this.helpViewer.showHelp(null);
        }
      }
    }

    @Override
    public void mouseReleased(MouseEvent me) {
      // not needed
    }

    @Override
    public void mouseEntered(MouseEvent me) {
      // not needed
    }

    @Override
    public void mouseExited(MouseEvent me) {
      // not needed
    }
  }

  private static Cursor getHelpTrackingCursor() {
    if (helpTrackingCursor == null) {
      ImageIcon imageToUseForTrackingHelpCursor = ResourceManager.getTrackingHelpCursor();
      if (imageToUseForTrackingHelpCursor != null) {
        helpTrackingCursor = Toolkit.getDefaultToolkit().createCustomCursor(imageToUseForTrackingHelpCursor
          .getImage(), new Point(0, 0), "SwingHelpViewerCursor");
      } else {
        // Fallback: standard cursor "hand"
        helpTrackingCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
      }
    }
    return helpTrackingCursor;
  }

  /**
   * Returns an action listener that can be added to any Swing component to start context-sensitive
   * help when clicked - a glasspane is used to visualize a special help cursor, clicking on any
   * visible component checks if there is a specific help id registered using enableHelpKey, if
   * yes the specified ID is shown inside the given help viewer, or else the "home" help page is
   * shown.
   * 
   * @param helpViewer help viewer to use.
   * @return action listener to start context-sensitive tracking help.
   */
  public static ActionListener getDisplayHelpAfterTrackingActionListener(final HelpViewer helpViewer) {
    final ActionListener act = new ActionListener(){
      @Override
      public void actionPerformed(final ActionEvent ae) {
        Object eventSource = ae.getSource();
        if (eventSource instanceof JComponent) {
          final JComponent c = (JComponent)eventSource;
          final Window w = SwingUtilities.getWindowAncestor(c);
          Component glassPane = null;
          if (w instanceof JDialog) {
            glassPane = ((JDialog)w).getGlassPane();
          } else if (w instanceof JFrame) {
            glassPane = ((JFrame)w).getGlassPane();
          } else if (w instanceof JWindow) {
            glassPane = ((JWindow)w).getGlassPane();
          }
          if (glassPane != null) {
            GlassPaneManager gpm = new GlassPaneManager(glassPane, w, helpViewer);
            gpm.activate();
          }
        }
      }
    };
    return act;
  }

  /**
   * Registers the given help ID and the given help viewer with the given component, so that specific
   * context-sensitive help can be shown for this component.
   * 
   * @param jc component to associate with a help ID.
   * @param helpId help ID to associated with this component.
   * @param helpViewer help viewer to show the help ID.
   */
  public static void enableHelpKey(final JComponent jc, final String helpId, final HelpViewer helpViewer) {
    jc.putClientProperty(HELP_ID_KEY, helpId);
    jc.putClientProperty(HELP_VIEWER_KEY, helpViewer);
  }

}
