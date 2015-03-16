package ru.mrekin.sc.launcher.gui;

//import ru.mrekin.sc.launcher.SvnClient;

import ru.mrekin.sc.launcher.core.AppManager;
import ru.mrekin.sc.launcher.core.Application;
import ru.mrekin.sc.launcher.core.PluginManager;
import ru.mrekin.sc.launcher.plugin.Plugin;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by MRekin on 30.07.2014.
 */
public class LauncherGui extends JFrame {


    static LauncherGui instance;
    ArrayList<Application> appList;
    JPanel mainPanel, statusPanel;
    BufferedImage mainIcon;
    ImageIcon redIcon, greenIcon;
    JMenuBar menuBar;
    JMenu pluginMenu, settingsMenu, helpMenu;
    JMenuItem pluginRepoMenuItem, pluginSettingsMenuItem, settingMenuItem;
    PluginRepoForm pluginRepoForm;
    SettingsForm settingsForm;

    public LauncherGui() {
        super("SC launcher");
        //setLocationByPlatform(true);
        //  setResizable(false);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        init();
        launch();
        instance = this;
    }

    public static LauncherGui getInstance() {
        if (instance == null) {
            return new LauncherGui();
        } else {
            return instance;
        }
    }

    public void init() {

        try

        {
            //TODO need to create new icon :)
            //TODO need to create resource manager
            mainIcon = ImageIO.read(getClass().getClassLoader().getResource("icon.png"));
            greenIcon = new ImageIcon(ImageIO.read(getClass().getClassLoader().getResource("green.png")));
            redIcon = new ImageIcon(ImageIO.read(getClass().getClassLoader().getResource("red.png")));

        } catch (
                IOException ioe
                )

        {
            ioe.printStackTrace();
        }


//        fileDriver = appManager.getFileDriver();


        menuBar = new JMenuBar();
        menuBar.setToolTipText("menu");
        //menuBar.setMaximumSize(new Dimension(0, 25));
        menuBar.setSize(new Dimension(0, 25));
        //menuBar.setMinimumSize(new Dimension(0, 20));

        //this.getContentPane().setMaximumSize(new Dimension(300,menuBar.getHeight()+ 55 * appList.size()));
        //setMinimumSize(new Dimension(300, menuBar.getHeight() + 55 * appList.size()));
        pluginMenu = new JMenu("Plugins");
        pluginMenu.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));

        pluginRepoMenuItem = new JMenuItem("Plugin repositories");
        pluginRepoMenuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (pluginRepoForm == null) {
                    pluginRepoForm = new PluginRepoForm();
                } else if (!pluginRepoForm.isVisible()) {
                    pluginRepoForm = new PluginRepoForm();
                }
                pluginRepoForm.setEnabled(true);
            }
        });

        pluginSettingsMenuItem = new JMenuItem("Plugin settings");
        settingsMenu = new JMenu("Settings");
        settingMenuItem = new JMenuItem("Settings");

        settingMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (settingsForm == null) {
                    settingsForm = new SettingsForm();
                } else if (!settingsForm.isVisible()) {
                    settingsForm = new SettingsForm();
                }
                settingsForm.setEnabled(true);
            }
        });

    }

    public void launch() {
        //TODO Need to add menu panel with settings, plugin list, about page,tool for prepare apps for publishing, may be Help menu
        String appLocalVersionDef = "Need to install";
        //setContentPane(new Container());
        PluginManager.getInstance().loadInstalledPlugins();
        AppManager.getInstance().loadLocalAppInfo();
        appList = AppManager.getInstance().getAppList();
        this.getContentPane().removeAll();
//        mainPanel = new JPanel(new GridLayout(svnAppList.size() + localApps.size(), 2));
        // MenuBar start

//        menuBar.setSize(new Dimension(200, 20));


        pluginMenu.add(pluginRepoMenuItem);
        pluginMenu.add(pluginSettingsMenuItem);


        settingsMenu.add(settingMenuItem);

        //TODO remove this after implementing logic
        // settingsMenu.setEnabled(false);
        pluginSettingsMenuItem.setEnabled(false);

        menuBar.add(pluginMenu);
        menuBar.add(settingsMenu);
        setJMenuBar(menuBar);
// MenuBar end
        mainPanel = new JPanel(new GridLayout(appList.size(), 2));
        mainPanel.setBorder(new LineBorder(Color.GRAY));


        statusPanel = new JPanel();
        //setVisible(true);
        // setVisible(false);
        for (Application localApp : appList) {
            JButtonEx button = new JButtonEx();
            //String appTitle = "";
            String appLocalVersion = appLocalVersionDef;
            String localPath = "";


            //appTitle = localApp.getAppTitle();
            if (localApp.isInstalled()) {
                appLocalVersion = localApp.getAppVersion();
            }
            localPath = localApp.getRunCommand();


            button.setPreferredSize(new Dimension(100, 30));
            button.setText(localApp.getAppName());
            //button.setBorder(new TitledBorder(""));
            button.setBorder(new EtchedBorder());
            button.setToolTipText((String) localApp.getAppName());
            button.setApp(localPath);


            JLabel label = new JLabel();
            label.setMaximumSize(new Dimension(40, 25));
            label.setText(appLocalVersion);
            label.setBorder(new TitledBorder("Version"));
            label.setHorizontalAlignment(SwingConstants.CENTER);


            label.setComponentPopupMenu(new AppPopupMenu(this, localApp, AppManager.getInstance()));
            if (localApp.isInstalled() && localApp.getAppLastVersion().equals(appLocalVersion)) {
                label.setForeground(Color.GREEN);
                button.setEnabled(true);
            } else if (localApp.isInstalled() && !localApp.getAppLastVersion().equals(appLocalVersion)) {
                label.setForeground(Color.RED);
                button.setEnabled(true);
            } else if (!localApp.isInstalled()) {
                label.setForeground(Color.LIGHT_GRAY);
                button.setEnabled(false);
            }


            JComboBox box = new JComboBox();
            box.setBorder(new TitledBorder("Avaliable ver."));
            box.setSize(new Dimension(40, 45));
            box.setMinimumSize(new Dimension(40, 45));


            for (String ver : localApp.getAppVersions()) {
                box.addItem(ver);

            }

            box.setSelectedIndex(box.getItemCount() - 1);

            mainPanel.add(button);
            mainPanel.add(label);
            //    add(box);


        }

        mainPanel.setVisible(true);
        getContentPane().add(mainPanel, BorderLayout.NORTH);

        statusPanel.setMaximumSize(new Dimension(0, 20));

        statusPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        //TODO implements plugin status logic
        //statusPanel.setToolTipText("Status");
        JLabel lbl = null;
        JLabel lbl2 = null;
        //if (appManager.getSvnClient().checkSvnConnection()) {
        lbl = new JLabel();
        lbl2 = new JLabel();
        lbl.setText("Connection status:");
        lbl.setMaximumSize(new Dimension(20, 20));
        boolean connStatus = false;
        String connStatusFull = "";
        for (Plugin pl : PluginManager.getInstance().getPlugins()) {
            connStatusFull = connStatusFull.concat(pl.getPluginSimpleName());
            connStatusFull = connStatusFull.concat(": ");
            try {
                if (pl.getPluginObj().checkConnection()) {
                    connStatus = true;
                    connStatusFull = connStatusFull.concat("connected");
                } else {
                    connStatusFull = connStatusFull.concat("disconnected");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            connStatusFull = connStatusFull.concat("; ");
        }
        if (connStatus) {
            lbl2.setIcon(greenIcon);
        } else {
            lbl2.setIcon(redIcon);
        }
        statusPanel.setToolTipText(connStatusFull);
        statusPanel.add(lbl, BorderLayout.EAST);
        statusPanel.add(lbl2, BorderLayout.EAST);

        getContentPane().add(statusPanel, BorderLayout.SOUTH);
        setVisible(true);


        setIconImage(mainIcon);
        pack();


    }

    private class JButtonEx extends JButton {
        private String app = "";

        public JButtonEx() {
            super();
            addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {

                        //This is the simple way to launch apps.
                        Runtime.getRuntime().exec(app);

                        //JarFile jar = new JarFile(app);
                        //String mainClass = (String)jar.getManifest().getMainAttributes().get("Main-Class");
                        //read this http://stackoverflow.com/questions/60764/how-should-i-load-jars-dynamically-at-runtime
                        //Jar plugins not need there - > moved to plugin manager

                    } catch (Exception ioe) {
                        ioe.printStackTrace();
                    }
                }
            });
        }

        public String getApp() {
            return app;
        }

        public void setApp(String app) {
            this.app = app;
        }

    }


//JarFile


}