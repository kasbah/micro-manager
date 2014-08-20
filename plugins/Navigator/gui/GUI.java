package gui;


import acq.CustomAcqEngine;
import tables.PropertyControlTableModel;
import java.awt.Component;
import java.text.DecimalFormat;
import java.util.prefs.Preferences;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import mmcorej.CMMCore;
import org.micromanager.api.ScriptInterface;
import org.micromanager.utils.PropertyValueCellEditor;
import org.micromanager.utils.PropertyValueCellRenderer;
import org.micromanager.utils.ReportingUtils;
import tables.SimpleChannelTableModel;


/**
 *
 * @author Henry
 */
public class GUI extends javax.swing.JFrame {

   private static final DecimalFormat TWO_DECIMAL_FORMAT = new DecimalFormat("0.00");
   private static final int SLIDER_TICKS = 100000;
   
   private ScriptInterface mmAPI_;
   private CMMCore core_;
   private CustomAcqEngine eng_;
   private Preferences prefs_;
   private String zName_, xyName_;
   private double zMin_, zMax_;
           
           
   public GUI(Preferences prefs, ScriptInterface mmapi, String version) {
      prefs_ = prefs;
      mmAPI_ = mmapi;
      core_ = mmapi.getMMCore();
      this.setTitle("5D Navigator " + version);
      initComponents();
      moreInitialization();
      refresh();
      this.setVisible(true);
      eng_ = new CustomAcqEngine(mmAPI_.getMMCore());
      updatePropertiesTable();
   }
   
   public void updatePropertiesTable() {
      ((PropertyControlTableModel) (propertyControlTable_.getModel())).updateStoredProps();
      ((AbstractTableModel) propertyControlTable_.getModel()).fireTableDataChanged();

      //autofit columns
      propertyControlTable_.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
      TableColumn col1 = propertyControlTable_.getColumnModel().getColumn(0);
      int preferredWidth = col1.getMinWidth();
      for (int row = 0; row < propertyControlTable_.getRowCount(); row++) {
         TableCellRenderer cellRenderer = propertyControlTable_.getCellRenderer(row, 0);
         Component c = propertyControlTable_.prepareRenderer(cellRenderer, row, 0);
         int width = c.getPreferredSize().width + propertyControlTable_.getIntercellSpacing().width;
         preferredWidth = Math.max(preferredWidth, width);
      }
      col1.setPreferredWidth(preferredWidth);       
      TableColumn col2 = propertyControlTable_.getColumnModel().getColumn(1);
      col2.setPreferredWidth(propertyControlTable_.getWidth() - preferredWidth - 
              (customPropsScrollPane_.getVerticalScrollBar().isVisible() ? customPropsScrollPane_.getVerticalScrollBar().getWidth() : 0) );
   }
   
   private void moreInitialization() {
      zSlider_.setMaximum(SLIDER_TICKS);
      //TODO: delete and replace with actual values
      zMin_ = 0;
      zMax_ = 50;
      zName_ = core_.getFocusDevice();
      xyName_ = core_.getXYStageDevice();
      try {
         setZSliderPosition(core_.getPosition(zName_));
      } catch (Exception e) {
         ReportingUtils.showError("Couldn't get focus position from core");
      }
      zSliderAdjusted(null);
      
      
   }
     
   private void refresh() {
      //Refresh z stage, xy stage, etc and transmit them to eng?
      
   }

 
   /**
    * This method is called from within the constructor to initialize the form.
    * WARNING: Do NOT modify this code. The content of this method is always
    * regenerated by the Form Editor.
    */
   @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        newExploreWindowButton_ = new javax.swing.JButton();
        zTextField_ = new javax.swing.JTextField();
        zLabel_ = new javax.swing.JLabel();
        zSlider_ = new javax.swing.JSlider();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        timePointsPanel_ = new javax.swing.JPanel();
        jComboBox1 = new javax.swing.JComboBox();
        jLabel6 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jSpinner1 = new javax.swing.JSpinner();
        tiemPointsCheckBox_ = new javax.swing.JCheckBox();
        zStackPanel_ = new javax.swing.JPanel();
        zPanel_ = new javax.swing.JPanel();
        zStackCheckBox_ = new javax.swing.JCheckBox();
        zStartLabel = new javax.swing.JLabel();
        zEndLabel = new javax.swing.JLabel();
        zStepLabel = new javax.swing.JLabel();
        zStepTextField_ = new javax.swing.JTextField();
        zEndTextField_ = new javax.swing.JTextField();
        zStartTextField_ = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jRadioButton1 = new javax.swing.JRadioButton();
        jLabel2 = new javax.swing.JLabel();
        jComboBox3 = new javax.swing.JComboBox();
        jRadioButton2 = new javax.swing.JRadioButton();
        autofocusPanel = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jComboBox4 = new javax.swing.JComboBox();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jPanel6 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        channelsCheckBox_ = new javax.swing.JCheckBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        newChannelButton_ = new javax.swing.JButton();
        removeChannelButton_ = new javax.swing.JButton();
        jComboBox2 = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        customPropsScrollPane_ = new javax.swing.JScrollPane();
        propertyControlTable_ = new javax.swing.JTable();
        configurePropsButton_ = new javax.swing.JButton();

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        newExploreWindowButton_.setText("New explore window");
        newExploreWindowButton_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newExploreWindowButton_ActionPerformed(evt);
            }
        });

        zTextField_.setText("jTextField1");
        zTextField_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zTextField_ActionPerformed(evt);
            }
        });

        zLabel_.setText("Z: ");

        zSlider_.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                zSliderAdjusted(evt);
            }
        });

        timePointsPanel_.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel6.setText("Interval");

        jTextField1.setText("jTextField1");

        jLabel5.setText("Number");

        tiemPointsCheckBox_.setText("Time points");

        javax.swing.GroupLayout timePointsPanel_Layout = new javax.swing.GroupLayout(timePointsPanel_);
        timePointsPanel_.setLayout(timePointsPanel_Layout);
        timePointsPanel_Layout.setHorizontalGroup(
            timePointsPanel_Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tiemPointsCheckBox_)
            .addGroup(timePointsPanel_Layout.createSequentialGroup()
                .addGroup(timePointsPanel_Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel6)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(timePointsPanel_Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
        timePointsPanel_Layout.setVerticalGroup(
            timePointsPanel_Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, timePointsPanel_Layout.createSequentialGroup()
                .addComponent(tiemPointsCheckBox_)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(timePointsPanel_Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(timePointsPanel_Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(19, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(timePointsPanel_, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(517, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(timePointsPanel_, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Time Points", jPanel2);

        zPanel_.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        zStackCheckBox_.setText("Simple Z-stack");

        zStartLabel.setText("Z-start (µm)");

        zEndLabel.setText("Z-end (µm)");

        zStepLabel.setText("Z-step (µm)");

        zStepTextField_.setText("jTextField4");

        zEndTextField_.setText("jTextField3");

        zStartTextField_.setText("jTextField2");

        javax.swing.GroupLayout zPanel_Layout = new javax.swing.GroupLayout(zPanel_);
        zPanel_.setLayout(zPanel_Layout);
        zPanel_Layout.setHorizontalGroup(
            zPanel_Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(zPanel_Layout.createSequentialGroup()
                .addGroup(zPanel_Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(zPanel_Layout.createSequentialGroup()
                        .addComponent(zStartLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(zStartTextField_, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                    .addGroup(zPanel_Layout.createSequentialGroup()
                        .addGroup(zPanel_Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(zPanel_Layout.createSequentialGroup()
                                .addGroup(zPanel_Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(zStepLabel)
                                    .addComponent(zEndLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(zPanel_Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(zStepTextField_, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(zEndTextField_, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(zStackCheckBox_, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        zPanel_Layout.setVerticalGroup(
            zPanel_Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(zPanel_Layout.createSequentialGroup()
                .addComponent(zStackCheckBox_)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(zPanel_Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(zStartTextField_, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(zStartLabel, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(zPanel_Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(zEndLabel)
                    .addComponent(zEndTextField_, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 26, Short.MAX_VALUE)
                .addGroup(zPanel_Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(zStepLabel)
                    .addComponent(zStepTextField_, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jCheckBox1.setText("Surface conrolled Z stack");

        jRadioButton1.setText("Fixed distance from top");

        jLabel2.setText("Top surface:");

        jComboBox3.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jRadioButton2.setText("Use bottom surface");
        jRadioButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jCheckBox1)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jRadioButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jRadioButton2)))
                .addGap(18, 18, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jCheckBox1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRadioButton1)
                    .addComponent(jRadioButton2))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout zStackPanel_Layout = new javax.swing.GroupLayout(zStackPanel_);
        zStackPanel_.setLayout(zStackPanel_Layout);
        zStackPanel_Layout.setHorizontalGroup(
            zStackPanel_Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(zStackPanel_Layout.createSequentialGroup()
                .addComponent(zPanel_, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 203, Short.MAX_VALUE))
        );
        zStackPanel_Layout.setVerticalGroup(
            zStackPanel_Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(zPanel_, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Z-Stack", zStackPanel_);

        jLabel7.setText("Fiducial channel index:");

        jComboBox4.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        javax.swing.GroupLayout autofocusPanelLayout = new javax.swing.GroupLayout(autofocusPanel);
        autofocusPanel.setLayout(autofocusPanelLayout);
        autofocusPanelLayout.setHorizontalGroup(
            autofocusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(autofocusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(450, Short.MAX_VALUE))
        );
        autofocusPanelLayout.setVerticalGroup(
            autofocusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(autofocusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(autofocusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(112, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Cross correlation autofocus", autofocusPanel);

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(jTable2);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 373, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 255, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 143, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Regions/Surfaces", jPanel5);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        channelsCheckBox_.setText("Channels");

        jTable1.setModel(new SimpleChannelTableModel());
        jScrollPane1.setViewportView(jTable1);

        newChannelButton_.setText("New");
        newChannelButton_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newChannelButton_ActionPerformed(evt);
            }
        });

        removeChannelButton_.setText("Remove");

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel3.setText("Channel group:");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(channelsCheckBox_)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(newChannelButton_)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeChannelButton_)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 614, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(channelsCheckBox_)
                    .addComponent(newChannelButton_)
                    .addComponent(removeChannelButton_)
                    .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 11, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Channels", jPanel6);

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 628, Short.MAX_VALUE)
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 143, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Focus varying properties", jPanel7);

        customPropsScrollPane_.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        PropertyControlTableModel model = new PropertyControlTableModel(prefs_);
        propertyControlTable_.setAutoCreateColumnsFromModel(false);
        propertyControlTable_.setModel(model);
        propertyControlTable_.addColumn(new TableColumn(0, 200, new DefaultTableCellRenderer(), null));
        propertyControlTable_.addColumn(new TableColumn(1, 200, new PropertyValueCellRenderer(false), new PropertyValueCellEditor(false)));
        propertyControlTable_.setTableHeader(null);
        propertyControlTable_.setCellSelectionEnabled(false);
        propertyControlTable_.setModel(model);
        customPropsScrollPane_.setViewportView(propertyControlTable_);

        configurePropsButton_.setText("Configure properties");
        configurePropsButton_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configurePropsButton_ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(configurePropsButton_))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jTabbedPane1)
                            .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addGroup(layout.createSequentialGroup()
                                            .addComponent(zLabel_)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(zTextField_, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(zSlider_, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addComponent(customPropsScrollPane_, javax.swing.GroupLayout.DEFAULT_SIZE, 623, Short.MAX_VALUE))
                                    .addComponent(newExploreWindowButton_))))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(zLabel_)
                        .addComponent(zTextField_, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(zSlider_, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(customPropsScrollPane_, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 79, Short.MAX_VALUE)
                .addComponent(newExploreWindowButton_)
                .addGap(37, 37, 37)
                .addComponent(configurePropsButton_)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

   private void newExploreWindowButton_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newExploreWindowButton_ActionPerformed
      new ExploreInitDialog(prefs_, eng_);
   }//GEN-LAST:event_newExploreWindowButton_ActionPerformed

   private void zTextField_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zTextField_ActionPerformed
      double val = Double.parseDouble(zTextField_.getText());
      val = Math.max(Math.min(val,zMax_),zMin_);
      setZSliderPosition(val);
      zSliderAdjusted(null); 
   }//GEN-LAST:event_zTextField_ActionPerformed
 
   private void setZSliderPosition(double pos) {
      int ticks = (int) (((pos - zMin_) / (zMax_ - zMin_)) * SLIDER_TICKS);
      zSlider_.setValue(ticks);
   }
   
   private void zSliderAdjusted(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_zSliderAdjusted
     int ticks = zSlider_.getValue();
      double zVal = zMin_ + (zMax_ - zMin_) * (ticks / (double) SLIDER_TICKS);
      zTextField_.setText(TWO_DECIMAL_FORMAT.format(zVal));
   }//GEN-LAST:event_zSliderAdjusted

   private void configurePropsButton_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configurePropsButton_ActionPerformed
      new PickPropertiesGUI( prefs_, this);
   }//GEN-LAST:event_configurePropsButton_ActionPerformed

   private void newChannelButton_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newChannelButton_ActionPerformed
      // TODO add your handling code here:
   }//GEN-LAST:event_newChannelButton_ActionPerformed

   private void jRadioButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton2ActionPerformed
      // TODO add your handling code here:
   }//GEN-LAST:event_jRadioButton2ActionPerformed

 

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel autofocusPanel;
    private javax.swing.JCheckBox channelsCheckBox_;
    private javax.swing.JButton configurePropsButton_;
    private javax.swing.JScrollPane customPropsScrollPane_;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JComboBox jComboBox2;
    private javax.swing.JComboBox jComboBox3;
    private javax.swing.JComboBox jComboBox4;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSpinner jSpinner1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JButton newChannelButton_;
    private javax.swing.JButton newExploreWindowButton_;
    private javax.swing.JTable propertyControlTable_;
    private javax.swing.JButton removeChannelButton_;
    private javax.swing.JCheckBox tiemPointsCheckBox_;
    private javax.swing.JPanel timePointsPanel_;
    private javax.swing.JLabel zEndLabel;
    private javax.swing.JTextField zEndTextField_;
    private javax.swing.JLabel zLabel_;
    private javax.swing.JPanel zPanel_;
    private javax.swing.JSlider zSlider_;
    private javax.swing.JCheckBox zStackCheckBox_;
    private javax.swing.JPanel zStackPanel_;
    private javax.swing.JLabel zStartLabel;
    private javax.swing.JTextField zStartTextField_;
    private javax.swing.JLabel zStepLabel;
    private javax.swing.JTextField zStepTextField_;
    private javax.swing.JTextField zTextField_;
    // End of variables declaration//GEN-END:variables


}
