package ru.mrekin.sc.launcher.gui;

import net.miginfocom.swing.MigLayout;
import ru.mrekin.sc.launcher.core.*;
import ru.mrekin.sc.launcher.tools.ApplicationPrepare;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;

/**
 * Created by xam on 04.02.2015.
 */
public class ApplicationPrepareLinkForm extends JFrame implements ISCLogger {

    //List<Plugin> plugins;

    JPanel jspane;
    //JFileChooser fc;
    ApplicationPrepareLinkForm instance;
    File selectedFile;
    JLabel mainText;
    JLabel filePath;
    JTextField appName, appVersion, appType;
    String appNameStr, appVersionStr, appTypeStr, appPathStr;
    JButton saveButton, cancelButton/*, selectButton*/;
    ShellLinkEx link = null;

    public ApplicationPrepareLinkForm() {
        init();
        launch();
    }

    /**
     * @param link
     */
    public ApplicationPrepareLinkForm(ShellLinkEx link) {
        try {
            this.link = link;
            appNameStr = link.getIdList().getLast().getName();
            appVersionStr = "unknown";
            appPathStr = link.resolveTarget();
            appTypeStr = link.getIdList().getLast().getName().endsWith(".jar") ? "java" : "win";
        } catch (Exception e) {
            log(e.toString());
        }
        init();
        launch();
    }

    /*
        private static void log(String msg) {
            SCLogger.getInstance().log(MethodHandles.lookup().lookupClass().getName(), "INFO", msg);
        }

    */
    private void init() {
        //setResizable(false);
        log("Init starting");
        instance = this;
        getContentPane().setLayout(new BorderLayout());
        BufferedImage
                mainIcon = null;
        try {

            mainIcon = ImageIO.read(getClass().getClassLoader().getResource("icon.png"));
        } catch (IOException ioe) {
            log(ioe.getLocalizedMessage());
        }
        setIconImage(mainIcon);
        setTitle(LauncherConstants.AppPrepareLinkFormTitle);

        setAlwaysOnTop(true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        /*  File chooser*/
        /*log("Creating fileChooser");
        fc = new JFileChooser();
        fc.setCurrentDirectory(new File("."));
        fc.setDialogTitle("Select folder containing app");
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setAcceptAllFileFilterUsed(false);
        */
        /*File path*/
        log("Creating fields");
        filePath = new JLabel(appPathStr);
        appName = new JTextField(appNameStr);
        appName.setPreferredSize(new Dimension(200, 0));
        appVersion = new JTextField(appVersionStr);
        appVersion.setPreferredSize(new Dimension(200, 0));
        appType = new JTextField(appTypeStr);
        appType.setPreferredSize(new Dimension(200, 0));

        /* Main text*/
        mainText = new JLabel("Please check App params and confirm!");


        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(0, 1));

        saveButton = new JButton("Save");

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ShellLinkEx s = null;
                try {
                    s = new ShellLinkEx();
                    s.setTarget(link.resolveTarget());
                } catch (Exception ee) {
                    log(ee.toString());
                }
                AppManager.getInstance().createAppLink(s, appName.getText(), appVersion.getText(), appType.getText());
                instance.dispose();
            }
        });
        cancelButton = new JButton("Cancel");

        cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                instance.dispose();
            }
        });

        cancelButton.setEnabled(true);
        /*
        selectButton = new JButton("Select file..");
        //selectButton.setPreferredSize(new Dimension(0,200));
        //JButton updateButton = new JButton("Update");
        //final JButton deleteButton = new JButton("Delete");

        selectButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                instance.setAlwaysOnTop(false);
                if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    selectedFile = fc.getSelectedFile();
                    log("getSelectedFile() : " + selectedFile);
                    filePath.setText(selectedFile.getAbsolutePath());

                    instance.setAlwaysOnTop(true);
                    if(!"".equals(selectedFile.getAbsolutePath()) && selectedFile!=null) {
                        selectButton.setEnabled(true);
                    }
                } else {

                    log("No Selection");
                    instance.setAlwaysOnTop(true);
                    if(!"".equals(selectedFile.getAbsolutePath()) && selectedFile!=null) {
                        selectButton.setEnabled(true);
                    }else{
                        selectButton.setEnabled(false);
                    }
                }
            }
        });


        */
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                //LauncherGui.getInstance().init();
                AppManager.getInstance().updateAppList();
                //LauncherGui.getInstance().launch();
                log("Window closed");
                super.windowClosed(e);
            }
        });


        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        //buttonPanel.add(deleteButton);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.weighty = 1;

        JPanel east = new JPanel(new GridBagLayout());
        east.add(buttonPanel, gbc);
        east.setMaximumSize(new Dimension(0, 200));


        add(east, BorderLayout.EAST);

        setLocationRelativeTo(null);
        log("Init finished");
    }

    private void launch() {
        log("Launching frame");

        MigLayout ml = new MigLayout(
                "wrap 2", // Layout Constraints
                "[:100:]5[]", // Column constraints
                "[][]");
        //remove(tablePanel);
        //tablePanel = new JPanel();
        jspane = new JPanel();
        jspane.setLayout(ml);
        jspane.add(mainText, "span");

        jspane.add(new JLabel("Target file:"));
        jspane.add(filePath);

        jspane.add(new JLabel("App name:"));
        jspane.add(appName);

        jspane.add(new JLabel("App version:"));
        jspane.add(appVersion);

        jspane.add(new JLabel("App type:"));
        jspane.add(appType);

        jspane.setBorder(new LineBorder(Color.green));
        //jspane.setPreferredSize(table.getMaximumSize());
        //jspane.size
        //tablePanel.add(jspane);
        //tablePanel.setBorder(new LineBorder(Color.red));
        add(jspane, BorderLayout.WEST);
        pack();
        setVisible(true);
        log("Launched");
    }

    /**
     * @param name
     * @param c
     * @return
     */
    private JPanel getComponentWithLabel(String name, JComponent c) {
        SpringLayout sl = new SpringLayout();

        JLabel l = new JLabel(name);
        l.setMinimumSize(new Dimension(200, 0));

        sl.putConstraint(SpringLayout.WEST, c,
                5,
                SpringLayout.EAST, l);
        JPanel filech = new JPanel(sl);
        filech.add(l);
        filech.add(c);
        return filech;
    }


}
