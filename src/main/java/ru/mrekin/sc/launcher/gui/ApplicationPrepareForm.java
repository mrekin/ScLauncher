package ru.mrekin.sc.launcher.gui;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import ru.mrekin.sc.launcher.core.AppManager;
import ru.mrekin.sc.launcher.core.LauncherConstants;
import ru.mrekin.sc.launcher.core.SCLogger;
import ru.mrekin.sc.launcher.core.SettingsManager;
import ru.mrekin.sc.launcher.tools.ApplicationPrepare;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
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
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by xam on 04.02.2015.
 */
public class ApplicationPrepareForm extends JFrame {

    //List<Plugin> plugins;

    JScrollPane jspane;
    JFileChooser fc;
    JTextArea ta;
    ApplicationPrepareForm instance;
    File selectedFolder;

    public ApplicationPrepareForm() {
        init();
        launch();
    }

    private static void log(String msg) {
        SCLogger.getInstance().log(MethodHandles.lookup().lookupClass().getName(), "INFO", msg);
    }

    private void init() {
        //setResizable(false);
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
        setTitle(LauncherConstants.AppPrepareFormTitle);


        fc = new JFileChooser();
        fc.setCurrentDirectory(new java.io.File("."));
        fc.setDialogTitle("Select folder containing app");
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setAcceptAllFileFilterUsed(false);

        ta = new JTextArea();
        ta.setLineWrap(true);
        DefaultCaret caret = (DefaultCaret) ta.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        ta.setEditable(false);
        ta.setAutoscrolls(true);
        ta.setPreferredSize(new Dimension(500, 200));
        ta.setVisible(true);
        ta.setText("Select folder contianing application.");
        ta.setBackground(this.getBackground());

        setAlwaysOnTop(true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(0, 1));

        final JButton prepButton = new JButton("Start");
        //JButton updateButton = new JButton("Update");
        //final JButton deleteButton = new JButton("Delete");

        prepButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                ta.append("\nGenerating cfg files..");
                ta.append("\n+++" + LauncherConstants.ReliseNotesFileName);
                ta.append("\n+++" + LauncherConstants.PropertiesFileName);
                ApplicationPrepare.appPrepare(selectedFolder.getAbsolutePath());
                ta.append("\n Done.");
                ta.append("\nPls don't forget to fill " + LauncherConstants.ReliseNotesFileName + " and " + LauncherConstants.PropertiesFileName);
                ta.append("\nAfter that you must add app folder to repository");
                ta.append("\nAs example, for SVN pls commit app to {repoURL}/{app}/{version}");
            }
        });

        prepButton.setEnabled(false);

        final JButton saveButton = new JButton("Select folder..");
        //JButton updateButton = new JButton("Update");
        //final JButton deleteButton = new JButton("Delete");

        saveButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                instance.setAlwaysOnTop(false);
                if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    log("getSelectedDir() : " + fc.getSelectedFile());
                    ta.append("\n Folder selected: " + fc.getSelectedFile());
                    selectedFolder = fc.getSelectedFile();
                    instance.setAlwaysOnTop(true);
                    if (!"".equals(selectedFolder) && selectedFolder != null) {
                        prepButton.setEnabled(true);
                    }
                } else {

                    log("No Selection");
                    instance.setAlwaysOnTop(true);
                    ta.append("\n Folder not selected.");
                    selectedFolder = fc.getSelectedFile();
                    if (!"".equals(selectedFolder) && selectedFolder != null) {
                        ta.append("\n Current selected folder: " + selectedFolder);
                        prepButton.setEnabled(true);
                    } else {
                        prepButton.setEnabled(false);
                    }
                }
            }
        });


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
        buttonPanel.add(prepButton);
        //buttonPanel.add(deleteButton);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.weighty = 1;

        JPanel east = new JPanel(new GridBagLayout());
        east.add(buttonPanel, gbc);


        add(east, BorderLayout.EAST);

        setLocationRelativeTo(null);
    }

    private void launch() {
        //remove(tablePanel);
        //tablePanel = new JPanel();
        jspane = new JScrollPane(ta);
        jspane.setBorder(new LineBorder(Color.green));
        jspane.setSize(ta.getSize());
        //jspane.setPreferredSize(table.getMaximumSize());
        //jspane.size
        //tablePanel.add(jspane);
        //tablePanel.setBorder(new LineBorder(Color.red));
        add(jspane, BorderLayout.WEST);
        pack();
        setVisible(true);
    }


}
