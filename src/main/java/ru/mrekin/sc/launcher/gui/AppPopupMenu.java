package ru.mrekin.sc.launcher.gui;

import ru.mrekin.sc.launcher.core.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;

/**
 * Created by MRekin on 03.08.2014.
 */
public class AppPopupMenu extends JPopupMenu {

    private AppManager appManager;
    private Application application;

    private static void log(String msg) {
        SCLogger.getInstance().log(MethodHandles.lookup().lookupClass().getName(), "INFO", msg);
    }

    public AppPopupMenu(final LauncherGui gui, Application application, AppManager appMan) {

        this.appManager = AppManager.getInstance();
        this.application = application;


        JMenuItem updateItem = new JMenuItem();
        updateItem.setText("Update");
        try {
            if (application.getSourcePlugin() != null && !"".equals(application.getSourcePlugin()))
                updateItem.setEnabled(PluginManager.getInstance().getPluginByName(application.getSourcePlugin()).getPluginObj().checkConnection());
        } catch (Exception e) {
            log(e.getLocalizedMessage());
        }
        updateItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                class run implements Runnable {
                    public void run() {

                        //appManager.deleteApplication(AppPopupMenu.this.application.getAppPath());
                        appManager.updateApplication(AppPopupMenu.this.application.getAppPath());
                        //iform.setVisible(false);
                        //iform = null;
                        //gui.init();
                        gui.launch();

                    }
                }

                new run().run();

            }
        });


        JMenu installItem = new JMenu("Install");
        try {
            if (application.getSourcePlugin() != null && !"".equals(application.getSourcePlugin()))
                installItem.setEnabled(PluginManager.getInstance().getPluginByName(application.getSourcePlugin()).getPluginObj().checkConnection());
        } catch (Exception e) {
            log(e.getLocalizedMessage());
        }


        if (this.application.getAppVersions() != null) {
            for (String ver : this.application.getAppVersions()) {

                JMenuItem mi = new JMenuItem(ver);
                mi.addActionListener(new ActionListener() {
                    public void actionPerformed(final ActionEvent e) {

                        class run implements Runnable {
                            public void run() {
                                AppInstallForm iform = new AppInstallForm();
                                appManager.deleteApplication(AppPopupMenu.this.application.getAppName());
                                appManager.installApplication(AppPopupMenu.this.application.getAppName(), ((JMenuItem) e.getSource()).getText());
                                iform.setVisible(false);
                                iform = null;
                                //gui.init();
                                gui.launch();
                            }
                        }

                        new run().run();

                    }
                });
                installItem.add(mi);
            }
        }

        JMenuItem deleteItem = new JMenuItem();
        deleteItem.setText("Delete");

        deleteItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                class run implements Runnable {
                    public void run() {

                        appManager.deleteApplication(AppPopupMenu.this.application.getAppPath());
                        gui.launch();
                    }
                }

                new run().run();
            }
        });

        JMenuItem viewFolderItem = new JMenuItem();
        viewFolderItem.setText("Open app folder");

        viewFolderItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                class run implements Runnable {
                    public void run() {
                        java.io.File file = new File(SettingsManager.getInstance().getPropertyByName(LauncherConstants.ApplicationDirectory) + AppPopupMenu.this.application.getAppPath());
                        try {
                            Desktop.getDesktop().open(file);
                        } catch (IOException ioe) {
                            log(ioe.getLocalizedMessage());
                        }
                    }
                }

                new run().run();
            }
        });
        if (!AppPopupMenu.this.application.isInstalled()) {
            viewFolderItem.setEnabled(false);
        }

        this.add(updateItem);
        this.add(installItem);
        this.add(deleteItem);
        this.addSeparator();
        this.add(viewFolderItem);

    }
}
