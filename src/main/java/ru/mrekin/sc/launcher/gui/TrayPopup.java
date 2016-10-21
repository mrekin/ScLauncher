package ru.mrekin.sc.launcher.gui;

/**
 * Created by MRekin on 20.10.2016.
 */

import java.awt.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.*;

public class TrayPopup {

    private static final String APPLICATION_NAME = "SClauncher";
    private static TrayIcon trayIcon = null;
    private static String message;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                createGUI();
            }
        });
    }

    public static void createGUI() {
        setTrayIcon();
    }

    private static void setTrayIcon() {
        if (!SystemTray.isSupported()) {
            return;
        }

        PopupMenu trayMenu = new PopupMenu();
        MenuItem item = new MenuItem("Exit");
        item.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        trayMenu.add(item);


        URL imageURL = TrayPopup.class.getClassLoader().getResource("icon.png");

        Image icon = Toolkit.getDefaultToolkit().getImage(imageURL);
        trayIcon = new TrayIcon(icon, APPLICATION_NAME, trayMenu);
        trayIcon.setImageAutoSize(true);

        SystemTray tray = SystemTray.getSystemTray();
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            e.printStackTrace();
        }

        trayIcon.displayMessage(APPLICATION_NAME, "SCLauncher started!",
                TrayIcon.MessageType.INFO);
    }

    public static void displayMessage(String message) {
  /*      TrayPopup.message = message;
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                if (trayIcon == null) {
                    createGUI();
                }
                trayIcon.displayMessage(APPLICATION_NAME, TrayPopup.message, TrayIcon.MessageType.INFO);
            }
        });

*/
        if (trayIcon == null) {
            createGUI();
        }
        trayIcon.displayMessage(APPLICATION_NAME, message, TrayIcon.MessageType.INFO);
    }
}