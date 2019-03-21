package ru.mrekin.sc.launcher.gui;

import net.miginfocom.swing.MigLayout;
import ru.mrekin.sc.launcher.core.ISCLogger;
import ru.mrekin.sc.launcher.core.LauncherConstants;
import ru.mrekin.sc.launcher.core.SettingsManager;
import ru.mrekin.sc.launcher.update.AutoUpdaterV2;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class UpdateForm extends JDialog implements ISCLogger {
    private JPanel contentPane;
    private JButton buttonOK = new JButton("OK");
    private JButton buttonCancel = new JButton("Cancel");
    private JLabel currVersionText = new JLabel();
    private JLabel availableVerText = new JLabel("Available version: ");
    private JComboBox<String> versionsCBox = new JComboBox<>();
    private boolean result = false;

    public UpdateForm() {

        setTitle("Update");
        contentPane = new JPanel((new MigLayout()));

        BufferedImage
                mainIcon = null;
        try {

            mainIcon = ImageIO.read(getClass().getClassLoader().getResource("icon.png"));
        } catch (IOException ioe) {
            log(ioe.getLocalizedMessage());
        }

        setLocationRelativeTo(LauncherGui.getInstance());
        setIconImage(mainIcon);

        setContentPane(contentPane);
        setModal(true);
        setModalityType(ModalityType.APPLICATION_MODAL);
        currVersionText.setText("Current version: "+ SettingsManager.getInstance().getPropertyByName(LauncherConstants.ApplicationVersion));
        ArrayList<String> vers = AutoUpdaterV2.getInstance().getVersions();
        Collections.sort(vers, Collections.reverseOrder());
        for(String ver: vers){
            versionsCBox.addItem(ver);
        }

        contentPane.add(currVersionText, "wrap");
        contentPane.add(availableVerText, "split 2");
        contentPane.add(versionsCBox, "gapbefore push, wrap");
        contentPane.add(buttonOK,"split 2");
        contentPane.add(buttonCancel,"gapleft push");

        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        pack();
    }


    private void onOK() {
        AutoUpdaterV2.getInstance().update(versionsCBox.getSelectedItem().toString());
        // add your code here
        dispose();
    }

    private void onCancel() {
        result = false;
        // add your code here if necessary
        dispose();
    }

    public boolean launch() {
        pack();
        setVisible(true);
        return result;
    }

}
