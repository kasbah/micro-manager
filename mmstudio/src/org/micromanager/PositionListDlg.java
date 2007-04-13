///////////////////////////////////////////////////////////////////////////////
//FILE:          XYPositionListDlg.java
//PROJECT:       Micro-Manager
//SUBSYSTEM:     mmstudio
//-----------------------------------------------------------------------------

//AUTHOR:       Nenad Amodaj, nenad@amodaj.com, March 8, 2007

//COPYRIGHT:    University of California, San Francisco, 2007

//LICENSE:      This file is distributed under the BSD license.
//License text is included with the source distribution.

//This file is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty
//of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

//IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.

//CVS:          $Id$

package org.micromanager;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.AbstractTableModel;

import mmcorej.CMMCore;
import mmcorej.DeviceType;
import mmcorej.StrVector;

import org.micromanager.navigation.MultiStagePosition;
import org.micromanager.navigation.PositionList;
import org.micromanager.navigation.StagePosition;
import org.micromanager.utils.MMDialog;
import org.micromanager.utils.MMSerializationException;

import com.swtdesigner.SwingResourceManager;

public class PositionListDlg extends MMDialog {

   private static String POSITION_LIST_FILE_NAME = "MMPositionList.pos";
   private String POSITION_LIST_DIR = "PosListDIR";
   private String posListDir_;
   private File curFile_;

   private JTable posTable_;
   private SpringLayout springLayout;
   private CMMCore core_;

   private JTextArea curPostextArea_;

   private class PosTableModel extends AbstractTableModel {
      public final String[] COLUMN_NAMES = new String[] {
            "Label",
            "Position [um]"
      };
      private PositionList posList_;

      public void setData(PositionList pl) {
         posList_ = pl;
      }

      public PositionList getPositionList() {
         return posList_;
      }

      public int getRowCount() {
         return posList_.getNumberOfPositions();
      }
      public int getColumnCount() {
         return COLUMN_NAMES.length;
      }
      public String getColumnName(int columnIndex) {
         return COLUMN_NAMES[columnIndex];
      }
      public Object getValueAt(int rowIndex, int columnIndex) {
         if (columnIndex == 0) {
            MultiStagePosition msp = posList_.getPosition(rowIndex);
            return msp.getLabel();
         } else if (columnIndex == 1) {
            MultiStagePosition msp = posList_.getPosition(rowIndex);
            StringBuffer sb = new StringBuffer();
            for (int i=0; i<msp.size(); i++) {
               StagePosition sp = msp.get(i);
               if (i!=0)
                  sb.append(";");
               sb.append(sp.getVerbose());
            }
            return sb.toString();
         } else
            return null;
      }
   }

   /**
    * File filter class for Open/Save file choosers 
    */
   private class PosFileFilter extends FileFilter {
      final private String EXT_POS;
      final private String DESCRIPTION;

      public PosFileFilter() {
         super();
         EXT_POS = new String("pos");
         DESCRIPTION = new String("MM position files (*.pos)");
      }

      public boolean accept(File f){
         if (f.isDirectory())
            return true;

         if (EXT_POS.equals(getExtension(f)))
            return true;
         return false;
      }

      public String getDescription(){
         return DESCRIPTION;
      }

      private String getExtension(File f) {
         String ext = null;
         String s = f.getName();
         int i = s.lastIndexOf('.');

         if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
         }
         return ext;
      }
   }

   /**
    * Create the dialog
    */
   public PositionListDlg(CMMCore core, PositionList posList) {
      super();
      addWindowListener(new WindowAdapter() {
         public void windowClosing(WindowEvent arg0) {
            savePosition();
         }
      });
      core_ = core;
      setTitle("Stage-position List");
      springLayout = new SpringLayout();
      getContentPane().setLayout(springLayout);
      setBounds(100, 100, 362, 495);

      Preferences root = Preferences.userNodeForPackage(this.getClass());
      setPrefsNode(root.node(root.absolutePath() + "/XYPositionListDlg"));

      Rectangle r = getBounds();
      loadPosition(r.x, r.y, r.width, r.height);

      final JScrollPane scrollPane = new JScrollPane();
      getContentPane().add(scrollPane);
      springLayout.putConstraint(SpringLayout.SOUTH, scrollPane, -16, SpringLayout.SOUTH, getContentPane());
      springLayout.putConstraint(SpringLayout.NORTH, scrollPane, 15, SpringLayout.NORTH, getContentPane());

      posTable_ = new JTable();
      PosTableModel model = new PosTableModel();
      model.setData(posList);
      posTable_.setModel(model);
      posTable_.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      scrollPane.setViewportView(posTable_);

      final JButton markButton = new JButton();
      markButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent arg0) {
            markPosition();
         }
      });
      markButton.setIcon(SwingResourceManager.getIcon(PositionListDlg.class, "icons/flag_green.png"));
      markButton.setText("Mark");
      getContentPane().add(markButton);
      springLayout.putConstraint(SpringLayout.SOUTH, markButton, 40, SpringLayout.NORTH, getContentPane());
      springLayout.putConstraint(SpringLayout.NORTH, markButton, 17, SpringLayout.NORTH, getContentPane());

      final JButton removeButton = new JButton();
      removeButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent arg0) {
            removeCurentPosition();
         }
      });
      removeButton.setIcon(SwingResourceManager.getIcon(PositionListDlg.class, "icons/cross.png"));
      removeButton.setText("Remove");
      getContentPane().add(removeButton);
      springLayout.putConstraint(SpringLayout.SOUTH, removeButton, 65, SpringLayout.NORTH, getContentPane());
      springLayout.putConstraint(SpringLayout.NORTH, removeButton, 42, SpringLayout.NORTH, getContentPane());
      springLayout.putConstraint(SpringLayout.EAST, markButton, -9, SpringLayout.EAST, getContentPane());
      springLayout.putConstraint(SpringLayout.WEST, markButton, 0, SpringLayout.WEST, removeButton);
      springLayout.putConstraint(SpringLayout.EAST, removeButton, -9, SpringLayout.EAST, getContentPane());
      springLayout.putConstraint(SpringLayout.WEST, removeButton, -109, SpringLayout.EAST, getContentPane());

      final JButton closeButton = new JButton();
      closeButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent arg0) {
            savePosition();
            dispose();
         }
      });
      closeButton.setText("Close");
      getContentPane().add(closeButton);
      springLayout.putConstraint(SpringLayout.SOUTH, closeButton, 0, SpringLayout.SOUTH, scrollPane);
      springLayout.putConstraint(SpringLayout.NORTH, closeButton, -23, SpringLayout.SOUTH, scrollPane);
      springLayout.putConstraint(SpringLayout.EAST, scrollPane, -5, SpringLayout.WEST, closeButton);
      springLayout.putConstraint(SpringLayout.WEST, scrollPane, 10, SpringLayout.WEST, getContentPane());
      springLayout.putConstraint(SpringLayout.EAST, closeButton, -5, SpringLayout.EAST, getContentPane());
      springLayout.putConstraint(SpringLayout.WEST, closeButton, 0, SpringLayout.WEST, removeButton);

      final JButton gotoButton = new JButton();
      gotoButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent arg0) {
            goToCurrentPosition();
         }
      });
      gotoButton.setIcon(SwingResourceManager.getIcon(PositionListDlg.class, "icons/resultset_next.png"));
      gotoButton.setText("Go to");
      getContentPane().add(gotoButton);
      springLayout.putConstraint(SpringLayout.SOUTH, gotoButton, 140, SpringLayout.NORTH, getContentPane());
      springLayout.putConstraint(SpringLayout.NORTH, gotoButton, 117, SpringLayout.NORTH, getContentPane());

      final JButton refreshButton = new JButton();
      refreshButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent arg0) {
            refreshCurrentPosition();
         }
      });
      refreshButton.setIcon(SwingResourceManager.getIcon(PositionListDlg.class, "icons/arrow_refresh.png"));
      refreshButton.setText("Refresh");
      getContentPane().add(refreshButton);
      springLayout.putConstraint(SpringLayout.SOUTH, refreshButton, 165, SpringLayout.NORTH, getContentPane());
      springLayout.putConstraint(SpringLayout.NORTH, refreshButton, 142, SpringLayout.NORTH, getContentPane());

      final JButton removeAllButton = new JButton();
      removeAllButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent arg0) {
            clearAllPositions();
         }
      });
      removeAllButton.setText("Remove All");
      getContentPane().add(removeAllButton);
      springLayout.putConstraint(SpringLayout.SOUTH, removeAllButton, 88, SpringLayout.NORTH, getContentPane());
      springLayout.putConstraint(SpringLayout.NORTH, removeAllButton, 65, SpringLayout.NORTH, getContentPane());
      springLayout.putConstraint(SpringLayout.EAST, removeAllButton, 100, SpringLayout.WEST, removeButton);
      springLayout.putConstraint(SpringLayout.WEST, removeAllButton, 0, SpringLayout.WEST, removeButton);
      springLayout.putConstraint(SpringLayout.EAST, refreshButton, 0, SpringLayout.EAST, removeAllButton);
      springLayout.putConstraint(SpringLayout.WEST, refreshButton, -100, SpringLayout.EAST, removeAllButton);
      springLayout.putConstraint(SpringLayout.EAST, gotoButton, 0, SpringLayout.EAST, removeAllButton);
      springLayout.putConstraint(SpringLayout.WEST, gotoButton, -100, SpringLayout.EAST, removeAllButton);
      //

      curPostextArea_ = new JTextArea();
      curPostextArea_.setFont(new Font("Arial", Font.PLAIN, 10));
      curPostextArea_.setBorder(new LineBorder(Color.black, 1, false));
      getContentPane().add(curPostextArea_);


      refreshCurrentPosition();

      final JLabel currentPositionLabel = new JLabel();
      currentPositionLabel.setText("Current position:");
      getContentPane().add(currentPositionLabel);
      springLayout.putConstraint(SpringLayout.SOUTH, curPostextArea_, 120, SpringLayout.SOUTH, currentPositionLabel);
      springLayout.putConstraint(SpringLayout.NORTH, curPostextArea_, 5, SpringLayout.SOUTH, currentPositionLabel);
      springLayout.putConstraint(SpringLayout.EAST, curPostextArea_, 104, SpringLayout.WEST, currentPositionLabel);
      springLayout.putConstraint(SpringLayout.WEST, curPostextArea_, 0, SpringLayout.WEST, currentPositionLabel);
      springLayout.putConstraint(SpringLayout.SOUTH, currentPositionLabel, 185, SpringLayout.NORTH, getContentPane());
      springLayout.putConstraint(SpringLayout.EAST, currentPositionLabel, 99, SpringLayout.WEST, refreshButton);
      springLayout.putConstraint(SpringLayout.WEST, currentPositionLabel, 0, SpringLayout.WEST, refreshButton);

      final JButton loadButton = new JButton();
      loadButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent arg0) {
            loadPositionList();
         }
      });
      loadButton.setText("Load...");
      getContentPane().add(loadButton);
      springLayout.putConstraint(SpringLayout.SOUTH, loadButton, 343, SpringLayout.NORTH, getContentPane());
      springLayout.putConstraint(SpringLayout.NORTH, loadButton, 320, SpringLayout.NORTH, getContentPane());
      springLayout.putConstraint(SpringLayout.EAST, loadButton, 0, SpringLayout.EAST, curPostextArea_);
      springLayout.putConstraint(SpringLayout.WEST, loadButton, 0, SpringLayout.WEST, curPostextArea_);

      final JButton saveAsButton = new JButton();
      saveAsButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent arg0) {
            savePositionListAs();
         }
      });
      saveAsButton.setText("Save As...");
      getContentPane().add(saveAsButton);
      springLayout.putConstraint(SpringLayout.EAST, saveAsButton, -4, SpringLayout.EAST, getContentPane());
      springLayout.putConstraint(SpringLayout.WEST, saveAsButton, -109, SpringLayout.EAST, getContentPane());
      springLayout.putConstraint(SpringLayout.SOUTH, saveAsButton, 370, SpringLayout.NORTH, getContentPane());
      springLayout.putConstraint(SpringLayout.NORTH, saveAsButton, 347, SpringLayout.NORTH, getContentPane());

      final JButton setOriginButton = new JButton();
      setOriginButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent arg0) {
            calibrate(); //setOrigin();
         }
      });
      //setOriginButton.setIcon(SwingResourceManager.getIcon(PositionListDlg.class, "icons/resultset_next.png"));
      setOriginButton.setText("Calibrate");
      getContentPane().add(setOriginButton);
      springLayout.putConstraint(SpringLayout.SOUTH, setOriginButton, 113, SpringLayout.NORTH, getContentPane());
      springLayout.putConstraint(SpringLayout.NORTH, setOriginButton, 90, SpringLayout.NORTH, getContentPane());
      springLayout.putConstraint(SpringLayout.EAST, setOriginButton, 100, SpringLayout.WEST, removeButton);
      springLayout.putConstraint(SpringLayout.WEST, setOriginButton, 0, SpringLayout.WEST, removeButton);      


   }

   protected boolean savePositionListAs() {
      JFileChooser fc = new JFileChooser();
      boolean saveFile = true;

      do {
         if (curFile_ == null)
            curFile_ = new File(POSITION_LIST_FILE_NAME);

         fc.setSelectedFile(curFile_);
         int retVal = fc.showSaveDialog(this);
         if (retVal == JFileChooser.APPROVE_OPTION) {
            curFile_ = fc.getSelectedFile();

            // check if file already exists
            if( curFile_.exists() ) { 
               int sel = JOptionPane.showConfirmDialog( this,
                     "Overwrite " + curFile_.getName(),
                     "File Save",
                     JOptionPane.YES_NO_OPTION);

               if(sel == JOptionPane.YES_OPTION)
                  saveFile = true;
               else
                  saveFile = false;
            }
         } else {
            return false; 
         }
      } while (saveFile == false);

      try {
         String serList = getPositionList().serialize();
         FileWriter fw = new FileWriter(curFile_);
         fw.write(serList);
         fw.close();
         posListDir_ = curFile_.getParent();
      } catch (FileNotFoundException e) {
         handleError(e.getMessage());
         return false;
      } catch (IOException e) {
         handleError(e.getMessage());
         return false;
      } catch (MMSerializationException e) {
         handleError(e.getMessage());
      }
      return true;
   }

   protected void loadPositionList() {
      JFileChooser fc = new JFileChooser();
      fc.addChoosableFileFilter(new PosFileFilter());

      if (posListDir_ != null)
         fc.setCurrentDirectory(new File(posListDir_));
      int retVal = fc.showOpenDialog(this);
      if (retVal == JFileChooser.APPROVE_OPTION) {
         curFile_ = fc.getSelectedFile();
         try {
            StringBuffer contents = new StringBuffer();
            BufferedReader input = new BufferedReader(new FileReader(curFile_));
            String line = null;
            while (( line = input.readLine()) != null){
               contents.append(line);
               contents.append(System.getProperty("line.separator"));
            }
            getPositionList().restore(contents.toString());
            posListDir_ = curFile_.getParent();
         } catch (Exception e) {
            handleError(e.getMessage());
         } finally {
            PosTableModel ptm = (PosTableModel)posTable_.getModel();
            ptm.fireTableDataChanged();            
         }
      }
   }

   protected void goToCurrentPosition() {
      PosTableModel ptm = (PosTableModel)posTable_.getModel();
      MultiStagePosition msp = ptm.getPositionList().getPosition(posTable_.getSelectedRow());
      if (msp == null)
         return;

      try {
         MultiStagePosition.goToPosition(msp, core_);
      } catch (Exception e) {
         handleError(e.getMessage());
      }
   }

   protected void clearAllPositions() {
      PosTableModel ptm = (PosTableModel)posTable_.getModel();
      ptm.getPositionList().clearAllPositions();
      ptm.fireTableDataChanged();
   }

   protected void removeCurentPosition() {
      PosTableModel ptm = (PosTableModel)posTable_.getModel();
      ptm.getPositionList().removePosition(posTable_.getSelectedRow());
      ptm.fireTableDataChanged();
   }

   /**
    * Store current xyPosition.
    */
   private void markPosition() {
//    String stage = core_.getXYStageDevice();
//    if (stage.length() == 0) {
//    handleError("Default XYStage device not defined.");
//    return;
//    }
      refreshCurrentPosition();

      MultiStagePosition msp = new MultiStagePosition();


      // read 1-axis stages
      try {
         StrVector stages = core_.getLoadedDevicesOfType(DeviceType.StageDevice);
         for (int i=0; i<stages.size(); i++) {
            StagePosition sp = new StagePosition();
            sp.stageName = stages.get(i);
            sp.numAxes = 1;
            sp.x = core_.getPosition(stages.get(i));
            msp.add(sp);
         }

         // read 2-axis stages
         StrVector stages2D = core_.getLoadedDevicesOfType(DeviceType.XYStageDevice);
         for (int i=0; i<stages2D.size(); i++) {
            StagePosition sp = new StagePosition();
            sp.stageName = stages2D.get(i);
            sp.numAxes = 2;
            sp.x = core_.getXPosition(stages2D.get(i));
            sp.y = core_.getYPosition(stages2D.get(i));
            msp.add(sp);
         }
      } catch (Exception e) {
         handleError(e.getMessage());
      }

      PosTableModel ptm = (PosTableModel)posTable_.getModel();
      msp.setLabel(ptm.getPositionList().generateLabel());
      ptm.getPositionList().addPosition(msp);
      ptm.fireTableDataChanged();
   }

   /**
    * Update display of the current xy position.
    */
   private void refreshCurrentPosition() {
      StringBuffer sb = new StringBuffer();

      // read 1-axis stages
      try {
         StrVector stages = core_.getLoadedDevicesOfType(DeviceType.StageDevice);
         for (int i=0; i<stages.size(); i++) {
            StagePosition sp = new StagePosition();
            sp.stageName = stages.get(i);
            sp.numAxes = 1;
            sp.x = core_.getPosition(stages.get(i));
            sb.append(sp.getVerbose() + "\n");
         }

         // read 2-axis stages
         StrVector stages2D = core_.getLoadedDevicesOfType(DeviceType.XYStageDevice);
         for (int i=0; i<stages2D.size(); i++) {
            StagePosition sp = new StagePosition();
            sp.stageName = stages2D.get(i);
            sp.numAxes = 2;
            sp.x = core_.getXPosition(stages2D.get(i));
            sp.y = core_.getYPosition(stages2D.get(i));
            sb.append(sp.getVerbose() + "\n");
         }
      } catch (Exception e) {
         handleError(e.getMessage());
      }

      curPostextArea_.setText(sb.toString());
   }


   private PositionList getPositionList() {
      PosTableModel ptm = (PosTableModel)posTable_.getModel();
      return ptm.getPositionList();

   }

   private void handleError(String txt) {
      JOptionPane.showMessageDialog(this, txt);      
   }

   /**
    * Calibrate the XY stage.
    */
   private void calibrate() {

      JOptionPane.showMessageDialog(this, "ALERT! Please REMOVE objectives! It may damage lens!", 
            "Calibrate the XY stage", JOptionPane.WARNING_MESSAGE);

      // calibrate xy-axis stages
      try {

         // read 2-axis stages
         StrVector stages2D = core_.getLoadedDevicesOfType(DeviceType.XYStageDevice);
         for (int i=0; i<stages2D.size(); i++) {

            String deviceName = stages2D.get(i);

            double [] x1 = new double[1];
            double [] y1 = new double[1];

            core_.getXYPosition(deviceName,x1,y1);

            StopCalThread stopThread = new StopCalThread(); 
            CalThread calThread = new CalThread();

            stopThread.setPara(calThread, this, deviceName, x1, y1);
            calThread.setPara(stopThread, this, deviceName, x1, y1);

            stopThread.start();
            calThread.start();
         }
      } catch (Exception e) {
         handleError(e.getMessage());
      }

   }   

   class StopCalThread extends Thread{
      double [] x1;
      double [] y1;
      String deviceName;
      MMDialog d;
      Thread otherThread;

      public void setPara(Thread calThread, MMDialog d, String deviceName, double [] x1, double [] y1) {
         if ( calThread==null || d ==null || deviceName==null || x1==null || y1==null){
            JOptionPane.showMessageDialog(d, "parent dialog or x1 or y1 is null!"); 
         }
         this.otherThread = calThread;
         this.d = d;
         this.deviceName = deviceName;
         this.x1 = x1;
         this.y1 = y1;
      }
      public void run() {

         try {

            // popup a dialog that says stop the calibration
            Object[] options = { "Stop" };
            int option = JOptionPane.showOptionDialog(d, "Stop calibration?", "Calibration", 
                  JOptionPane.CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                  null, options, options[0]);

            if (option==0) {//stop the calibration 

               otherThread.interrupt();
               otherThread = null;

               if (isInterrupted())return;
               Thread.sleep(50);
               core_.stop(deviceName);
               if (isInterrupted())return;
               boolean busy = core_.deviceBusy(deviceName);
               while (busy){
                  if (isInterrupted())return;
                  core_.stop(deviceName);
                  if (isInterrupted())return;
                  busy = core_.deviceBusy(deviceName);
               }

               Object[] options2 = { "Yes", "No" };
               option = JOptionPane.showOptionDialog(d, "RESUME calibration?", "Calibration", 
                     JOptionPane.OK_OPTION, JOptionPane.QUESTION_MESSAGE,
                     null, options2, options2[0]);

               if (option==1) return; // not to resume, just stop

               core_.home(deviceName);


               StopCalThread sct = new StopCalThread();
               sct.setPara(this, d, deviceName, x1, y1);

               busy = core_.deviceBusy(deviceName);
               if ( busy ) sct.start();

               if (isInterrupted())return;
               busy = core_.deviceBusy(deviceName);
               while (busy){
                  if (isInterrupted())return;
                  Thread.sleep(100);
                  if (isInterrupted())return;
                  busy = core_.deviceBusy(deviceName);
               }

               sct.interrupt();
               sct=null;

               //calibrate_(deviceName, x1, y1);

               double [] x2 = new double[1];
               double [] y2 = new double[1];

               // check if the device busy?
               busy = core_.deviceBusy(deviceName);
               int delay=500; //500 ms 
               int period=60000;//60 sec
               int elapse = 0;
               while (busy && elapse<period){
                  Thread.sleep(delay);
                  busy = core_.deviceBusy(deviceName);
                  elapse+=delay;
               }

               // now the device is not busy

               core_.getXYPosition(deviceName,x2,y2);

               // zero the coordinates
               core_.setOriginXY(deviceName);

               BackThread bt = new BackThread();
               bt.setPara(d);
               bt.start();

               core_.setXYPosition(deviceName, x1[0]-x2[0], y1[0]-y2[0]);               

               busy = core_.deviceBusy(deviceName);

               if (isInterrupted())return;
               busy = core_.deviceBusy(deviceName);
               while (busy){
                  if (isInterrupted())return;
                  Thread.sleep(100);
                  if (isInterrupted())return;
                  busy = core_.deviceBusy(deviceName);
               }

               bt.interrupt();
               bt=null;

            }           
         }catch (InterruptedException e) {}
         catch (Exception e) {
            handleError(e.getMessage());
         }
      }
   }

   class CalThread extends Thread{

      double [] x1;
      double [] y1;
      String deviceName;
      MMDialog d;
      Thread stopThread;

      public void setPara(Thread stopThread, MMDialog d, String deviceName, double [] x1, double [] y1) {
         if ( stopThread==null || d ==null || deviceName==null || x1==null || y1==null){
            JOptionPane.showMessageDialog(d, "parent dialog or x1 or y1 is null!"); 
         }
         this.stopThread = stopThread;
         this.d = d;
         this.deviceName = deviceName;
         this.x1 = x1;
         this.y1 = y1;
      }

      public void run() {

         try {
            core_.home(deviceName);

            // check if the device busy?
            boolean busy = core_.deviceBusy(deviceName);
            int delay=500; //500 ms 
            int period=60000;//60 sec
            int elapse = 0;
            while (busy && elapse<period){
               Thread.sleep(delay);
               busy = core_.deviceBusy(deviceName);
               elapse+=delay;
            }

            stopThread.interrupt();
            stopThread = null;

            //JOptionPane.showMessageDialog(d, "ok 0!"); 

            // now the device is not busy

            double [] x2 = new double[1]; 
            double [] y2 = new double[1];

            core_.getXYPosition(deviceName,x2,y2);

            //JOptionPane.showMessageDialog(d, "ok 1!"); 

            // zero the coordinates
            core_.setOriginXY(deviceName);

            //JOptionPane.showMessageDialog(d, "ok 2!"); 

            BackThread bt = new BackThread();
            bt.setPara(d);
            bt.start();

            core_.setXYPosition(deviceName, x1[0]-x2[0], y1[0]-y2[0]);

            //JOptionPane.showMessageDialog(d, "ok 3!"); 

            busy = core_.deviceBusy(deviceName);

            if (isInterrupted())return;
            busy = core_.deviceBusy(deviceName);
            while (busy){
               if (isInterrupted())return;
               Thread.sleep(100);
               if (isInterrupted())return;
               busy = core_.deviceBusy(deviceName);
            }

            bt.interrupt();
            bt=null;	        	

         } catch (InterruptedException e) {}
         catch (Exception e) {
            handleError(e.getMessage());
         }
      }
   }

   class BackThread extends Thread{

      MMDialog d;

      public void setPara(MMDialog d) {
         if ( d ==null ){
            JOptionPane.showMessageDialog(d, "parent dialog is null!"); 
         }
      }	   
      public void run() {
         JOptionPane.showMessageDialog(d, "Going back to the original position!");		        	
      }
   }      

}
