///////////////////////////////////////////////////////////////////////////////
//FILE:          ConfiguratorDlg.java
//PROJECT:       Micro-Manager
//SUBSYSTEM:     mmstudio
//-----------------------------------------------------------------------------
//
// AUTHOR:       Nenad Amodaj, nenad@amodaj.com, December 2, 2006
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
package org.micromanager.conf;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;

import mmcorej.CMMCore;

import org.micromanager.utils.ReportingUtils;

/**
 * Configuration Wizard main panel.
 * Based on the dialog frame to be activated as part of the
 * MMStudio
 */
public class ConfiguratorDlg extends JDialog {

    private static final long serialVersionUID = 1L;
    private JLabel pagesLabel_;
    private JButton backButton_;
    private JButton nextButton_;
    private PagePanel pages_[];
    private int curPage_ = 0;
    private MicroscopeModel microModel_;
    private CMMCore core_;
    private Preferences prefs_;
    private static final String APP_NAME = "Configurator";
    private JLabel titleLabel_;
    private JEditorPane helpTextPane_;
    private String defaultPath_;
    private JCheckBox sendCheck_;
    private boolean sendConfig_;

    /**
     * Create the application
     */
    public ConfiguratorDlg(CMMCore core, String defFile) {
        super();
        core_ = core;
        defaultPath_ = defFile;
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setModal(true);
        initialize();
    }

    /**
     * Initialize the contents of the frame
     */
    private void initialize() {
        prefs_ = Preferences.userNodeForPackage(this.getClass());

        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent arg0) {
                onCloseWindow();
            }
        });
        setResizable(false);
        getContentPane().setLayout(null);
        setTitle("Hardware Configuration Wizard");
        setBounds(50, 100, 602, 529);

        sendCheck_ = new JCheckBox();
        sendCheck_.setBounds(5, 462, 275, 23);
        sendCheck_.setFont(new Font("", Font.PLAIN, 10));
        sendCheck_.setSelected(true);
        sendConfig_ = false;
        sendCheck_.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                sendConfig_ = sendCheck_.isSelected();
            }
        });
        sendCheck_.setText("Send configuration to Micro-manager.org");

        //getContentPane().add(sendCheck_);


        final JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(9, 320, 578, 136);
        getContentPane().add(scrollPane);

        helpTextPane_ = new JEditorPane();
        scrollPane.setViewportView(helpTextPane_);
        helpTextPane_.setEditable(false);
        //helpTextPane_.setBorder(new LineBorder(Color.black, 1, false));

        helpTextPane_.setContentType("text/html; charset=ISO-8859-1");

        nextButton_ = new JButton();
        nextButton_.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                if (curPage_ == pages_.length - 1) {
                    onCloseWindow();
                } else {
                    setPage(curPage_ + 1);
                }
            }
        });
        nextButton_.setText("Next >");
        nextButton_.setBounds(494, 462, 93, 23);
        getContentPane().add(nextButton_);
        getRootPane().setDefaultButton(nextButton_);

        backButton_ = new JButton();
        backButton_.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                setPage(curPage_ - 1);
            }
        });
        backButton_.setText("< Back");
        backButton_.setBounds(395, 462, 93, 23);
        getContentPane().add(backButton_);

        pagesLabel_ = new JLabel();
        pagesLabel_.setBorder(new LineBorder(Color.black, 1, false));
        pagesLabel_.setBounds(9, 28, 578, 286);
        getContentPane().add(pagesLabel_);

        // add page panels
        pages_ = new PagePanel[9];

        int pageNumber = 0;
        pages_[pageNumber++] = new IntroPage(prefs_);
        pages_[pageNumber++] = new DevicesPage(prefs_);
        pages_[pageNumber++] = new EditPropertiesPage(prefs_);
        pages_[pageNumber++] = new ComPortsPage(prefs_);
        pages_[pageNumber++] = new RolesPage(prefs_);
        pages_[pageNumber++] = new DelayPage(prefs_);
        pages_[pageNumber++] = new SynchroPage(prefs_);
        pages_[pageNumber++] = new LabelsPage(prefs_);
        pages_[pageNumber++] = new FinishPage(prefs_);

        microModel_ = new MicroscopeModel();
        microModel_.loadAvailableDeviceList(core_);
        microModel_.setFileName(defaultPath_);
        microModel_.scanComPorts(core_);
        Rectangle r = pagesLabel_.getBounds();

        titleLabel_ = new JLabel();
        titleLabel_.setText("Title");
        titleLabel_.setBounds(9, 4, 578, 21);
        getContentPane().add(titleLabel_);
        for (int i = 0; i < pages_.length; i++) {
            pages_[i].setModel(microModel_, core_);
            pages_[i].loadSettings();
            pages_[i].setBounds(r);
            pages_[i].setTitle("Step " + (i + 1) + " of " + pages_.length + ": " + pages_[i].getTitle());
            pages_[i].setParentDialog(this);
        }
        setPage(0);

    }

    public String BoundaryString() {
        String possibleCharacters = "+-0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        int length = 36;
        StringBuffer workingBuffer = new StringBuffer(length);
        String preAmble = "--Micro-ManagerReporter";
        workingBuffer.append(preAmble);
        length -= preAmble.length();
        for (int i = 0; i < length; i++) {
            int ioff = (int) (0.5 + Math.random() * possibleCharacters.length());
            workingBuffer.append(possibleCharacters.charAt(ioff));
        }

        return workingBuffer.toString();
    }

    public void upload(URL url, List<File> files) throws Exception {
        final String Boundary = new String(BoundaryString());
        HttpURLConnection anURLConnection = (HttpURLConnection) url.openConnection();
        anURLConnection.setDoOutput(true);
        anURLConnection.setDoInput(true);
        anURLConnection.setUseCaches(false);
        anURLConnection.setChunkedStreamingMode(1024);
        anURLConnection.setRequestMethod("POST");
        anURLConnection.setRequestProperty("Connection", "keep-alive");
        anURLConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + Boundary);

        DataOutputStream httpOut = new DataOutputStream(anURLConnection.getOutputStream());

        for (int i = 0; i < 1 /*files.size()*/; i++) {
            File f = files.get(i);
            String str = "--" + Boundary + "\r\n"
                    + "Content-Disposition: form-data; name=\"file\"; filename=\"" + f.getName() + "\"\r\n"
                    //+ "Content-Type: application/octet-stream\r\n"
                    + "Content-Type: text/plain\r\n\r\n";

            httpOut.write(str.getBytes());

            FileInputStream uploadFileReader = new FileInputStream(f);
            int numBytesToRead = 1024;
            int availableBytesToRead;
            while ((availableBytesToRead = uploadFileReader.available()) > 0) {
                byte[] bufferBytesRead;
                bufferBytesRead = availableBytesToRead >= numBytesToRead ? new byte[numBytesToRead]
                        : new byte[availableBytesToRead];
                uploadFileReader.read(bufferBytesRead);
                httpOut.write(bufferBytesRead);
                httpOut.flush();
            }

        }


        httpOut.write(("\n\r\n--" + Boundary + "\r\n").getBytes());
        httpOut.write(("Content-Disposition: form-data; name=\"submit\"\r\n\r\nSubmit\r\n--" + Boundary + "--\r\n").getBytes());
        httpOut.flush();
        httpOut.close();

        // read & parse the response
        InputStream is = anURLConnection.getInputStream();
        StringBuilder response = new StringBuilder();
        byte[] respBuffer = new byte[4096];
        while (is.read(respBuffer) >= 0) {
            response.append(new String(respBuffer).trim());
        }
        is.close();
        System.out.println(response.toString());
    }

    private void setPage(int i) {
        // try to exit the current page

        if (i > 0) {
            if (!pages_[curPage_].exitPage(curPage_ < i ? true : false)) {
                return;
            }
        }

        int newPage = 0;
        if (i < 0) {
            newPage = 0;
        } else if (i >= pages_.length) {
            newPage = pages_.length - 1;
        } else {
            newPage = i;
        }

        // try to enter the new page
        if (!pages_[newPage].enterPage(curPage_ > newPage ? true : false)) {
            return;
        }

        // everything OK so we can proceed with swapping pages
        getContentPane().remove(pages_[curPage_]);
        curPage_ = newPage;

        getContentPane().add(pages_[curPage_]);
        // Java 2.0 specific, uncomment once we go for Java 2
        //frame.getContentPane().setComponentZOrder(pages_[curPage_], 0);
        getContentPane().repaint();
        pages_[curPage_].refresh();

        if (curPage_ == 0) {
            backButton_.setEnabled(false);
        } else {
            backButton_.setEnabled(true);
        }

        if (curPage_ == pages_.length - 1) {
            nextButton_.setText("Exit");
        } else {
            nextButton_.setText("Next >");
        }

        titleLabel_.setText(pages_[curPage_].getTitle());

        // By default, load plain text help
        helpTextPane_.setContentType("text/plain");
        helpTextPane_.setText(pages_[curPage_].getHelpText());

        // Try to load html help text
        try {
            File curDir = new File(".");
            String helpFileName = pages_[curPage_].getHelpFileName();
            if (helpFileName == null) {
                return;
            }
            URL htmlURL = ConfiguratorDlg.class.getResource(helpFileName);
            String helpText = readStream(ConfiguratorDlg.class.getResourceAsStream(helpFileName));
            helpTextPane_.setContentType("text/html; charset=ISO-8859-1");
            helpTextPane_.setText(helpText);

        } catch (MalformedURLException e1) {
            ReportingUtils.showError(e1);
        } catch (IOException e) {
            ReportingUtils.showError(e);
        }
    }

    private void onCloseWindow() {
        for (int i = 0; i < pages_.length; i++) {
            pages_[i].saveSettings();
        }

        if (microModel_.isModified()) {
            int result = JOptionPane.showConfirmDialog(this,
                    "Save changes to the configuration file?\nIf you press YES you will get a chance to change the file name.",
                    APP_NAME, JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.INFORMATION_MESSAGE);

            switch (result) {
                case JOptionPane.YES_OPTION:
                    saveConfiguration();
                    break;
                case JOptionPane.NO_OPTION:
                    break;
                case JOptionPane.CANCEL_OPTION:
                    return;
            }
        }
        if (sendConfig_) {
            try {
                List<File> list = new ArrayList<File>();
                list.add(new File("/x.cfg"));
                list.add(new File("/x2.cfg"));
                URL url = new URL("http://localhost/~karlhoover/upload_file.php");
                upload(url, list);


            } catch (java.net.UnknownHostException e) {
                ReportingUtils.logError(e, "config posting");
            } catch (MalformedURLException e) {
                ReportingUtils.logError(e);
            } catch (IOException e) {
                ReportingUtils.logError(e);
            } catch (SecurityException e) {
                ReportingUtils.logError(e, "");
            } catch (Exception e) {
                ReportingUtils.logError(e);
            }
        }

        dispose();
    }

    private void saveConfiguration() {
        JFileChooser fc = new JFileChooser();
        boolean saveFile = true;
        File f;

        do {
            fc.setSelectedFile(new File(microModel_.getFileName()));
            int retVal = fc.showSaveDialog(this);
            if (retVal == JFileChooser.APPROVE_OPTION) {
                f = fc.getSelectedFile();

                // check if file already exists
                if (f.exists()) {
                    int sel = JOptionPane.showConfirmDialog(this,
                            "Overwrite " + f.getName(),
                            "File Save",
                            JOptionPane.YES_NO_OPTION);

                    if (sel == JOptionPane.YES_OPTION) {
                        saveFile = true;
                    } else {
                        saveFile = false;
                    }
                }
            } else {
                return;
            }
        } while (saveFile == false);

        try {
            microModel_.saveToFile(f.getAbsolutePath());
        } catch (MMConfigFileException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    public String getFileName() {
        return microModel_.getFileName();
    }

    /**
     * Read string out of stream
     */
    private static String readStream(InputStream is) throws IOException {
        StringBuffer bf = new StringBuffer();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        //Note: not exactly the original
        while ((line = br.readLine()) != null) {
            bf.append(line);
            bf.append("\n");
        }
        return bf.toString();
    }
}
