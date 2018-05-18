package ru.mrekin.sc.launcher.gui;

import ru.mrekin.sc.launcher.core.AppManager;
import ru.mrekin.sc.launcher.core.Application;
import ru.mrekin.sc.launcher.core.PluginManager;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by MRekin on 03.08.2014.
 */
public class AppPopupMenu extends JPopupMenu {

    private AppManager appManager;
    private Application application;

    public AppPopupMenu(final LauncherGui gui, Application application, AppManager appMan) {

        this.appManager = AppManager.getInstance();
        this.application = application;


        JMenuItem updateItem = new JMenuItem();
        updateItem.setText("Update");
        try {
            updateItem.setEnabled(PluginManager.getInstance().getPluginByName(application.getSourcePlugin()).getPluginObj().checkConnection());
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
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
            installItem.setEnabled(PluginManager.getInstance().getPluginByName(application.getSourcePlugin()).getPluginObj().checkConnection());
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
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


        this.add(updateItem);
        this.addSeparator();
        this.add(installItem);
        this.addSeparator();
        this.add(deleteItem);

    }
}
