///////////////////////////////////////////////////////////////////////////////
//FILE:          MMDialog.java
//PROJECT:       Micro-Manager
//SUBSYSTEM:     mmstudio
//-----------------------------------------------------------------------------
//
// AUTHOR:       Nenad Amodaj, nenad@amodaj.com, October 1, 2006
//
// COPYRIGHT:    University of California, San Francisco, 2006
//
// LICENSE:      This file is distributed under the BSD license.
//               License text is included with the source distribution.
//
//               This file is distributed in the hope that it will be useful,
//               but WITHOUT ANY WARRANTY; without even the implied warranty
//               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.
//
// CVS:          $Id$
//
package org.micromanager.utils;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.prefs.Preferences;
import javax.swing.JDialog;
import org.micromanager.MMStudio;

/**
 * Base class for the Micro-Manager dialogs.
 * Saves and restores window size and position.
 */
public class MMDialog extends JDialog {
   private static final long serialVersionUID = -3144618980027203294L;
   private Preferences mmDialogPrefs_;
   private static final String WINDOW_X = "mmdlg_y";
   private static final String WINDOW_Y = "mmdlg_x";
   private static final String WINDOW_WIDTH = "mmdlg_width";
   private static final String WINDOW_HEIGHT = "mmdlg_height";
   
   public MMDialog() {
      super();
      finishConstructor();
   }
   public MMDialog(Frame owner) {
      super(owner);
      finishConstructor();
   }
   public MMDialog(Frame owner, boolean isModal) {
      super(owner, isModal);
      finishConstructor();
   }

   private void finishConstructor() {
      mmDialogPrefs_ = Preferences.userNodeForPackage(this.getClass());
      MMStudio mfr = MMStudio.getInstance();
      if (mfr != null) {
         mfr.addMMBackgroundListener(this);
    	   setBackground(mfr.getBackgroundColor());
      }
   }

   private void ensureSafeWindowPosition(int x, int y) {
      // if a saved position exists then make sure it falls on the screen
      // (useful when screen size changes between invocations)
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      if (screenSize.width < mmDialogPrefs_.getInt(WINDOW_X, 0)) {
         mmDialogPrefs_.putInt(WINDOW_X, x);
      }
      if (screenSize.height < mmDialogPrefs_.getInt(WINDOW_Y, 0)) {
         mmDialogPrefs_.putInt(WINDOW_Y, y);
      }
   }

    /**
    * Load window position and size from preferences
    * Makes sure that the window can be displayed
    * Attaches a listener to the window that will save the position when the
    * window closing event is received
    * @param x - X position of this dialog
    * @param y - y position of this dialog
    * @param width - width of this dialog
    * @param height - height of this dialog
    */
   protected void loadAndRestorePosition(int x, int y, int width, int height) {
      loadPosition(x, y, width, height);
      this.addWindowListener(new WindowAdapter() {
         @Override
         public void windowClosing(WindowEvent arg0) {
            savePosition();
         }
      }
      );
   }
   
    /**
    * Load window position and size from preferences
    * Makes sure that the window can be displayed
    * Attaches a listener to the window that will save the position when the
    * window closing event is received
    * @param x - X position of this dialog
    * @param y - y position of this dialog
    */
   protected void loadAndRestorePosition(int x, int y) {
      loadPosition(x, y);
      this.addWindowListener(new WindowAdapter() {
         @Override
         public void windowClosing(WindowEvent arg0) {
            savePosition();
         }
      }
      );
   }
   
   /**
    * Load window position and size from preferences
    * Makes sure that the window can be displayed
    * @param x - X position of this dialog
    * @param y - y position of this dialog
    * @param width - width of this dialog
    * @param height - height of this dialog
    */
   protected void loadPosition(int x, int y, int width, int height) {
      ensureSafeWindowPosition(x, y);
      setBounds(mmDialogPrefs_.getInt(WINDOW_X, x),
                mmDialogPrefs_.getInt(WINDOW_Y, y),
                mmDialogPrefs_.getInt(WINDOW_WIDTH, width),
                mmDialogPrefs_.getInt(WINDOW_HEIGHT, height));
   }
   
   @Override
   public void dispose() {
      savePosition();
      super.dispose();
   }
   
   protected void loadPosition(int x, int y) {
      ensureSafeWindowPosition(x, y);
      setLocation(mmDialogPrefs_.getInt(WINDOW_X, x),
                mmDialogPrefs_.getInt(WINDOW_Y, y));
   }

   /**
    * Writes window position and size to preferences
    */
   protected void savePosition() {
      Rectangle r = getBounds();
      if (r != null) {
         mmDialogPrefs_.putInt(WINDOW_X, r.x);
         mmDialogPrefs_.putInt(WINDOW_Y, r.y);
         mmDialogPrefs_.putInt(WINDOW_WIDTH, r.width);
         mmDialogPrefs_.putInt(WINDOW_HEIGHT, r.height);
      }
   }
   
   public Preferences getPrefsNode() {
      return mmDialogPrefs_;
   }
   
   public void setPrefsNode(Preferences p) {
      mmDialogPrefs_ = p;
   }


}
