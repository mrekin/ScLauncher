package ru.mrekin.sc.launcher.gui;

import ru.mrekin.sc.launcher.core.AppManager;
import ru.mrekin.sc.launcher.core.Application;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by MRekin on 03.08.2014.
 */
public class AppPopupMenu extends JPopupMenu {

    private AppManager appManager;
    private Application svnApp;

    public AppPopupMenu(final LauncherGui gui, Application svnAp, AppManager appMan) {

        this.appManager = AppManager.getInstance();
        this.svnApp = svnAp;


        JMenuItem updateItem = new JMenuItem();
        updateItem.setText("Update");

        updateItem.setEnabled(appManager.getSvnClient().checkSvnConnection());

        updateItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                class run implements Runnable {
                    public void run() {

                        appManager.deleteApplication(svnApp.getAppPath());
                        appManager.updateApplication(svnApp.getAppPath());
                        //iform.setVisible(false);
                        //iform = null;
                        gui.init();
                        gui.launch();
                    }
                }

                new run().run();
            }
        });


        JMenu installItem = new JMenu("Install");
        installItem.setEnabled(appManager.getSvnClient().checkSvnConnection());
        if (svnApp.getAppVersions() != null) {
            for (String ver : svnApp.getAppVersions()) {

                JMenuItem mi = new JMenuItem(ver);
                mi.addActionListener(new ActionListener() {
                    public void actionPerformed(final ActionEvent e) {

                        class run implements Runnable {
                            public void run() {
                                AppInstallForm iform = new AppInstallForm();
                                appManager.deleteApplication(svnApp.getAppPath());
                                appManager.installApplication(svnApp.getAppPath(), ((JMenuItem) e.getSource()).getText());
                                iform.setVisible(false);
                                iform = null;
                                gui.init();
                                gui.launch();
                            }
                        }

                        new run().run();

                    }
                });
                installItem.add(mi);
            }
        }
        this.add(updateItem);
        this.addSeparator();
        this.add(installItem);

    }
}
