package ru.mrekin.sc.launcher.gui;

/**
 * Created by MRekin on 20.10.2016.
 */

import ru.mrekin.sc.launcher.plugin.INotificationService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.net.URL;

public class TrayPopup implements INotificationService {

    private static final String APPLICATION_NAME = "SClauncher";

    public static TrayIcon getTrayIcon() {
        return trayIcon;
    }

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

        URL imageURL = TrayPopup.class.getClassLoader().getResource("icon.png");

        Image icon = Toolkit.getDefaultToolkit().getImage(imageURL);
        System.out.println("New tray icon creation!");
        trayIcon = new TrayIcon(icon, APPLICATION_NAME, trayMenu);
        trayIcon.setImageAutoSize(true);

        item.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        trayMenu.add(item);

        SystemTray tray = SystemTray.getSystemTray();
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            e.printStackTrace();
        }

        /*trayIcon.displayMessage(APPLICATION_NAME, "SCLauncher started!",
                TrayIcon.MessageType.INFO);
                */
    }

    public void displayMsg(String message) {

        TrayPopup.displayMessage(message);
    }

    public static void displayMessage(String message) {
        TrayPopup.message = message;
        Thread th = new Thread() {
            MouseListener tml = null;

            @Override
            public void run() {
                if (trayIcon == null) {
                    createGUI();
                }
                trayIcon.displayMessage(APPLICATION_NAME, TrayPopup.message, TrayIcon.MessageType.INFO);

            }
        };
        //SwingUtilities.invokeLater(th);
        th.start();
    }


    public static void displayMessage(final String message, MouseListener ml) {
        TrayPopup.message = message;
        final MouseListener fml = ml;

        Thread th = new Thread() {
            MouseListener tml = null;

            @Override
            public void run() {
                if (trayIcon == null) {
                    createGUI();
                }
                if (tml != null) {
                    trayIcon.addMouseListener(tml);
                }
                if (message != null) {
                    trayIcon.displayMessage(APPLICATION_NAME, TrayPopup.message, TrayIcon.MessageType.INFO);
                }
            }

            public Thread setMouseListener(MouseListener ml) {
                tml = ml;
                return this;
            }
        }.setMouseListener(ml);

        //SwingUtilities.invokeLater(th);
        th.start();
    }
}