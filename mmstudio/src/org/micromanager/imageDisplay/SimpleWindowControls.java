///////////////////////////////////////////////////////////////////////////////
//FILE:          SimpleWindowControls.java
//PROJECT:       Micro-Manager
//SUBSYSTEM:     mmstudio
//-----------------------------------------------------------------------------
//
// AUTHOR:       Henry Pinkard, henry.pinkard@gmail.com, 2012
//
// COPYRIGHT:    University of California, San Francisco, 2012
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
package org.micromanager.imageDisplay;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import com.swtdesigner.SwingResourceManager;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.json.JSONObject;

import org.micromanager.internalinterfaces.DisplayControls;
import org.micromanager.MMStudioMainFrame;
import org.micromanager.utils.ReportingUtils;


public class SimpleWindowControls extends DisplayControls {

   private VirtualAcquisitionDisplay virtAcq_;
   private JButton showFolderButton_;
   private JButton snapButton_;
   private JButton liveButton_;
   private JLabel statusLabel_;
   private JLabel pixelInfoLabel_;

   private EventBus bus_;
   
   /**
    * Draws buttons at the bottom of the live/snap window
    * 
    * @param virtAcq - acquisition displayed in the live/snap window
    */
   public SimpleWindowControls(VirtualAcquisitionDisplay virtAcq, EventBus bus) {
      virtAcq_ = virtAcq;
      bus_ = bus;
      initComponents();
      showFolderButton_.setEnabled(false);
      bus.register(this);
   }
   
   
   private void initComponents() {
      
      setPreferredSize(new java.awt.Dimension(512, 150));
     
      showFolderButton_ = new JButton();
      showFolderButton_.setBackground(new java.awt.Color(255, 255, 255));
      showFolderButton_.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/icons/folder.png"))); // NOI18N
      showFolderButton_.setToolTipText("Show containing folder");
      showFolderButton_.setFocusable(false);
      showFolderButton_.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
      showFolderButton_.setMaximumSize(new java.awt.Dimension(30, 28));
      showFolderButton_.setMinimumSize(new java.awt.Dimension(30, 28));
      showFolderButton_.setPreferredSize(new java.awt.Dimension(30, 28));
      showFolderButton_.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
      showFolderButton_.addActionListener(new java.awt.event.ActionListener() {
         @Override
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            showFolderButtonActionPerformed();
         }
      });
      
      JButton saveButton = new JButton();
      saveButton.setBackground(new java.awt.Color(255, 255, 255));
      saveButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/micromanager/icons/disk.png"))); // NOI18N
      saveButton.setToolTipText("Save as...");
      saveButton.setFocusable(false);
      saveButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
      saveButton.setMaximumSize(new Dimension(30, 28));
      saveButton.setMinimumSize(new Dimension(30, 28));
      saveButton.setPreferredSize(new Dimension(30, 28));
      saveButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
      saveButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent evt) {
            saveButtonActionPerformed();
         }
      });
      
      
      snapButton_ = new JButton();
      snapButton_.setFocusable(false);
      snapButton_.setIconTextGap(6);
      snapButton_.setText("Snap");
      snapButton_.setMinimumSize(new Dimension(99,28));
      snapButton_.setPreferredSize(new Dimension(99,28));
      snapButton_.setMaximumSize(new Dimension(99,28));
      snapButton_.setIcon(SwingResourceManager.getIcon(
            MMStudioMainFrame.class, "/org/micromanager/icons/camera.png"));
      snapButton_.setFont(new Font("Arial", Font.PLAIN, 10));
      snapButton_.setToolTipText("Snap single image");
      snapButton_.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            MMStudioMainFrame.getInstance().doSnap();
         }
         
      });
      
      liveButton_ = new JButton();
      liveButton_.setIcon(SwingResourceManager.getIcon(
            MMStudioMainFrame.class,
            "/org/micromanager/icons/camera_go.png"));
      liveButton_.setIconTextGap(6);
      liveButton_.setText("Live");
      liveButton_.setMinimumSize(new Dimension(99,28));
      liveButton_.setPreferredSize(new Dimension(99,28));
      liveButton_.setMaximumSize(new Dimension(99,28));
      liveButton_.setFocusable(false);
      liveButton_.setToolTipText("Continuous live view");
      liveButton_.setFont(new Font("Arial", Font.PLAIN, 10));
      liveButton_.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {            
            liveButtonAction();
         }
      });
     
      JButton addToSeriesButton = new JButton("Album");
      addToSeriesButton.setIcon(SwingResourceManager.getIcon(MMStudioMainFrame.class,
              "/org/micromanager/icons/arrow_right.png"));
      addToSeriesButton.setIconTextGap(6);
      addToSeriesButton.setToolTipText("Add current image to album");
      addToSeriesButton.setFocusable(false);
      addToSeriesButton.setMaximumSize(new Dimension(90, 28));
      addToSeriesButton.setMinimumSize(new Dimension(90, 28));
      addToSeriesButton.setPreferredSize(new Dimension(90, 28));
      addToSeriesButton.setFont(new Font("Arial", Font.PLAIN, 10));
      addToSeriesButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent evt) {
            addToSeriesButtonActionPerformed();
         }});

      // Put in a single space; otherwise no vertical space is 
      // allocated for the label and it can't display properly. Actual
      // text will be filled in when onMouseMoved() is called.
      pixelInfoLabel_ = new JLabel(" ");
      pixelInfoLabel_.setFocusable(false);
      pixelInfoLabel_.setFont(new java.awt.Font("Lucida Grande", 0, 10));

      statusLabel_ = new JLabel("                                            ");
      statusLabel_.setFocusable(false);
      statusLabel_.setFont(new java.awt.Font("Lucida Grande", 0, 10));
      
      this.setLayout(new BorderLayout());

      JPanel buttonPanel = new JPanel();
      buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
      
      JPanel textPanel = new JPanel();
      textPanel.setLayout(new BorderLayout());

      this.add(pixelInfoLabel_, BorderLayout.NORTH);
      this.add(new ScrollerPanel(
               bus_, new String[]{"z"}, new Integer[]{10}), 
            BorderLayout.NORTH);
      this.add(buttonPanel, BorderLayout.CENTER);
      this.add(textPanel,BorderLayout.SOUTH); 
      
      buttonPanel.add(showFolderButton_);
      buttonPanel.add(new JLabel(" "));
      buttonPanel.add(saveButton);
      buttonPanel.add(new JLabel(" "));
      buttonPanel.add(snapButton_);
      buttonPanel.add(new JLabel(" "));
      buttonPanel.add(liveButton_);
      buttonPanel.add(new JLabel(" "));
      buttonPanel.add(addToSeriesButton);
      buttonPanel.add(new JLabel(" "));
      
      textPanel.add(new JLabel(" "));
      textPanel.add(statusLabel_, BorderLayout.CENTER);      
   }

   /**
    * Our ScrollerPanel is informing us that we need to display a different
    * image.
    */
   @Subscribe
   public void onSetImage(ScrollerPanel.SetImageEvent event) {
      int channel = event.getPositionForAxis("c");
      int frame = event.getPositionForAxis("t");
      int slice = event.getPositionForAxis("z");
      virtAcq_.getHyperImage().setPosition(channel, slice, frame);
   }

   @Subscribe 
   public void onMouseMoved(MouseIntensityEvent event) {
      // TODO: Ideally there'd be some way to recognize multi-channel images 
      // and display each channel's intensity at this point. 
      pixelInfoLabel_.setText(String.format("<%d, %d>: %s", 
               event.x_, event.y_, event.intensities_[0]));
   }

   
   private void showFolderButtonActionPerformed() {
      virtAcq_.showFolder();
   }
   
    private void addToSeriesButtonActionPerformed() {
      try {
         MMStudioMainFrame gui = MMStudioMainFrame.getInstance();
         gui.copyFromLiveModeToAlbum(virtAcq_);
      } catch (Exception ex) {
         ReportingUtils.logError(ex);
      }
   }
   
    private void liveButtonAction() {
       MMStudioMainFrame.getInstance().enableLiveMode(!MMStudioMainFrame.getInstance().isLiveModeOn());
    }
    
     private void saveButtonActionPerformed() {
        new Thread() {
         @Override
           public void run() {
              virtAcq_.saveAs(false);
           }
        }.start();
        //showFolderButton_.setEnabled(true);
   }
     
   @Override
   public void newImageUpdate(JSONObject tags) {
      //showFolderButton_.setEnabled(false);
   }
   
   @Override
   public void imagesOnDiskUpdate(boolean onDisk) {

   }

   // called when live mode activated or deactivated
   @Override
   public void acquiringImagesUpdate(boolean acquiring) {
      snapButton_.setEnabled(!acquiring);
      liveButton_.setIcon(acquiring ? SwingResourceManager.getIcon(MMStudioMainFrame.class,
              "/org/micromanager/icons/cancel.png")
              : SwingResourceManager.getIcon(MMStudioMainFrame.class,
              "/org/micromanager/icons/camera_go.png"));
      liveButton_.setText(acquiring ? "Stop Live" : "Live");
      if (!acquiring) {
         statusLabel_.setText("");
      }

   }

   @Override
   public void setStatusLabel(String text) {
      statusLabel_.setText(text);
   }

   
}

