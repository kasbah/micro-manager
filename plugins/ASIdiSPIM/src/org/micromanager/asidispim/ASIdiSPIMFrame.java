///////////////////////////////////////////////////////////////////////////////
//FILE:          ASIdiSPIMFrame.java
//PROJECT:       Micro-Manager 
//SUBSYSTEM:     ASIdiSPIM plugin
//-----------------------------------------------------------------------------
//
// AUTHOR:       Nico Stuurman, Jon Daniels
//
// COPYRIGHT:    University of California, San Francisco, & ASI, 2013
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

package org.micromanager.asidispim;

import org.micromanager.asidispim.Data.Cameras;
import org.micromanager.asidispim.Data.Devices;
import org.micromanager.asidispim.Data.Joystick;
import org.micromanager.asidispim.Data.Positions;
import org.micromanager.asidispim.Data.Prefs;
import org.micromanager.asidispim.Data.Properties;
import org.micromanager.asidispim.Utils.ListeningJPanel;
import org.micromanager.asidispim.Utils.ListeningJTabbedPane;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.prefs.Preferences;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.micromanager.api.MMListenerInterface;
import org.micromanager.api.ScriptInterface;
import org.micromanager.MMStudioMainFrame; 
import org.micromanager.asidispim.Utils.StagePositionUpdater;
import org.micromanager.internalinterfaces.LiveModeListener; 

// TODO make sure to grab final camera frame
// TODO add support for PCO Edge
// TODO joystick labels dependent on which side you are on
// TODO finish adding 3rd camera (inverted scope camera)
// TODO figure out update of slider limits when devices changed
// TODO display certain properties like positions, e.g. scan amplitudes/offsets
// TODO figure out why NR Z, NV X, and NV Y are called by devices_.callListeners()
// TODO resolve whether Home/Stop should be added to 1axis stage API, use here if possible
// TODO add sethome property to device adapter and use it here

/**
 *
 * @author nico
 * @author Jon
 */
@SuppressWarnings("serial")
public class ASIdiSPIMFrame extends javax.swing.JFrame  
      implements MMListenerInterface {
   
   private Properties props_; 
   private Prefs prefs_;
   private Devices devices_;
   private Joystick joystick_;
   private Positions positions_;
   private Cameras cameras_;
   
   private final DevicesPanel devicesPanel_;
   private final SpimParamsPanel spimParamsPanel_;
   private final SetupPanel setupPanelA_;
   private final SetupPanel setupPanelB_;
   private final NavigationPanel navigationPanel_;
   private final HelpPanel helpPanel_;
   
   private static final String MAIN_PREF_NODE = "Main"; 
   
   /**
    * Creates the ASIdiSPIM plugin frame
    * @param gui - Micro-Manager script interface
    */
   public ASIdiSPIMFrame(ScriptInterface gui)  {

      // create interface objects used by panels
      prefs_ = new Prefs(Preferences.userNodeForPackage(this.getClass()));
      devices_ = new Devices(prefs_);
      props_ = new Properties(devices_);
      positions_ = new Positions(devices_);
      joystick_ = new Joystick(devices_, props_);
      cameras_ = new Cameras(devices_, props_);
      
      final StagePositionUpdater stagePosUpdater = new StagePositionUpdater(positions_);
      
      final ListeningJTabbedPane tabbedPane = new ListeningJTabbedPane();
        
      // all added tabs must be of type ListeningJPanel
      // only use addLTab, not addTab to guarantee this
      devicesPanel_ = new DevicesPanel(devices_, props_);
      tabbedPane.addLTab(devicesPanel_);
      spimParamsPanel_ = new SpimParamsPanel(devices_, props_, cameras_, prefs_);
      tabbedPane.addLTab(spimParamsPanel_);
      
      setupPanelA_ = new SetupPanel(devices_, props_, joystick_, 
            Devices.Sides.A, positions_, cameras_, prefs_);
      tabbedPane.addLTab(setupPanelA_);
      stagePosUpdater.addPanel(setupPanelA_);
      MMStudioMainFrame.getInstance().addLiveModeListener((LiveModeListener) setupPanelA_);
      
      setupPanelB_ = new SetupPanel(devices_, props_, joystick_,
            Devices.Sides.B, positions_, cameras_, prefs_);
      tabbedPane.addLTab(setupPanelB_);
      stagePosUpdater.addPanel(setupPanelB_);
      MMStudioMainFrame.getInstance().addLiveModeListener((LiveModeListener) setupPanelB_);
      
      // get initial positions, even if user doesn't want continual refresh
      // these used by NavigationPanel 
      stagePosUpdater.oneTimeUpdate();
      
      navigationPanel_ = new NavigationPanel(devices_, props_, joystick_,
            positions_, stagePosUpdater, prefs_, cameras_);
      tabbedPane.addLTab(navigationPanel_);
      stagePosUpdater.addPanel(navigationPanel_);
      MMStudioMainFrame.getInstance().addLiveModeListener((LiveModeListener) navigationPanel_);
      
      helpPanel_ = new HelpPanel();
      tabbedPane.addLTab(helpPanel_);
               
      // make sure gotSelected() gets called whenever we switch tabs
      tabbedPane.addChangeListener(new ChangeListener() {
         public void stateChanged(ChangeEvent e) {
            ((ListeningJPanel) tabbedPane.getSelectedComponent()).gotSelected();
         }
      });
      
      // make sure plugin window is on the screen (if screen size changes)
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      if (screenSize.width < prefs_.getInt(MAIN_PREF_NODE, Prefs.Keys.WIN_LOC_X, 0)) {
         prefs_.putInt(MAIN_PREF_NODE, Prefs.Keys.WIN_LOC_X, 100);
      }
      if (screenSize.height < prefs_.getInt(MAIN_PREF_NODE, Prefs.Keys.WIN_LOC_Y, 0)) {
         prefs_.putInt(MAIN_PREF_NODE, Prefs.Keys.WIN_LOC_Y, 100);
      }
    
      // put pane back where it was last time
      // gotSelected will be called because we put this after adding the ChangeListener
      setLocation(prefs_.getInt(MAIN_PREF_NODE, Prefs.Keys.WIN_LOC_X, 100), prefs_.getInt(MAIN_PREF_NODE, Prefs.Keys.WIN_LOC_Y, 100));
      tabbedPane.setSelectedIndex(prefs_.getInt(MAIN_PREF_NODE, Prefs.Keys.TAB_INDEX, 0));

      // set up the window
      add(tabbedPane);  // add the pane to the GUI window
      setTitle("ASI diSPIM Control"); 
      pack();           // shrinks the window as much as it can
      setResizable(false);
      
      // take care of shutdown tasks when window is closed
      addWindowListener(new java.awt.event.WindowAdapter() {
         @Override
         public void windowClosing(java.awt.event.WindowEvent evt) {
            // stop the timer for updating stages
            stagePosUpdater.stop();

            // save selections as needed
            devices_.saveSettings();
            setupPanelA_.saveSettings();
            setupPanelB_.saveSettings();

            // save pane location in prefs
            prefs_.putInt(MAIN_PREF_NODE, Prefs.Keys.WIN_LOC_X, evt.getWindow().getX());
            prefs_.putInt(MAIN_PREF_NODE, Prefs.Keys.WIN_LOC_Y, evt.getWindow().getY());
            prefs_.putInt(MAIN_PREF_NODE, Prefs.Keys.TAB_INDEX, tabbedPane.getSelectedIndex());
         }
      });
      
   }
  

   // MMListener mandated member functions
   public void propertiesChangedAlert() {
      // doesn't seem to actually be called by core when property changes
     // props_.callListeners();
   }

   public void propertyChangedAlert(String device, String property, String value) {
     // props_.callListeners();
   }

   public void configGroupChangedAlert(String groupName, String newConfig) {
   }

   public void systemConfigurationLoaded() {
   }

   public void pixelSizeChangedAlert(double newPixelSizeUm) {
   }

   public void stagePositionChangedAlert(String deviceName, double pos) {
   }

   public void xyStagePositionChanged(String deviceName, double xPos, double yPos) {
   }

   public void exposureChanged(String cameraName, double newExposureTime) {
   }

}
