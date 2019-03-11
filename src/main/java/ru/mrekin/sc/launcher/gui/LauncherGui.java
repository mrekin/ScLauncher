package ru.mrekin.sc.launcher.gui;

//import ru.mrekin.sc.launcher.SvnClient;

import ru.mrekin.sc.launcher.core.*;
import ru.mrekin.sc.launcher.plugin.Plugin;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
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
    JMenu pluginMenu, settingsMenu, toolsMenu;
    JMenuItem pluginRepoMenuItem, pluginSettingsMenuItem, settingMenuItem, viewLogMenuItem, appPrepareMenuItem;
    //PluginRepoForm pluginRepoForm;
    SettingsForm settingsForm;
    ApplicationPrepareForm applicationPrepareForm;
    ApplicationPrepareLinkForm applicationPrepareLinkForm;

    private static void log(String msg) {
        SCLogger.getInstance().log(MethodHandles.lookup().lookupClass().getName(), "INFO", msg);
    }

    private LauncherGui() {
        super("SC launcher " + SettingsManager.getInstance().getPropertyByName("Application.version"));
        log("New gui!");
        instance = this;
        //setLocationByPlatform(true);
        //  setResizable(false);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        instance.setDropTarget(AppDropTarget.getInstance());
        TrayPopup.createGUI();

        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (getState() == Frame.ICONIFIED) {
                    setVisible(true);
                    setEnabled(true);
                    setState(Frame.NORMAL);
                } else {
                    setVisible(false);
                    setEnabled(false);
                    setState(Frame.ICONIFIED);
                }
            }
        };
        TrayPopup.getTrayIcon().addMouseListener(ma);
        this.addWindowStateListener(new WindowStateListener() {
            public void windowStateChanged(WindowEvent e) {
                if (e.getNewState() == Frame.ICONIFIED) {
                    setVisible(false);
                    setEnabled(false);
                    TrayPopup.displayMessage("SCLauncher still running");
                } else {
                    setVisible(true);
                    setEnabled(true);
                }
            }
        });


        setLocationRelativeTo(null);
        init();
        //launch();

    }

    public static LauncherGui getInstance() {
        if (instance == null) {
            return new LauncherGui();
        } else {
            return instance;
        }
    }

    public void init() {

        try {
            //TODO need to create new icon :)
            //TODO need to create resource manager
            mainIcon = ImageIO.read(getClass().getClassLoader().getResource("icon.png"));
            greenIcon = new ImageIcon(ImageIO.read(getClass().getClassLoader().getResource("green.png")));
            redIcon = new ImageIcon(ImageIO.read(getClass().getClassLoader().getResource("red.png")));

            PluginManager.getInstance().loadInstalledPlugins();

        } catch (
                IOException ioe
        ) {
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
                if (!PluginRepoForm.getInstance().isVisible()) {
                    PluginRepoForm.getInstance().setVisible(true);
                    ;
                }
                PluginRepoForm.getInstance().setEnabled(true);

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


        if (SettingsManager.getInstance().getPropertyByName(LauncherConstants.MenuExtendedMode, "false").equals("true")) {
            toolsMenu = new JMenu("Tools");

            appPrepareMenuItem = new JMenuItem("Prepare application");
            appPrepareMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (applicationPrepareForm == null) {
                        applicationPrepareForm = new ApplicationPrepareForm();
                    } else if (!applicationPrepareForm.isVisible()) {
                        applicationPrepareForm = new ApplicationPrepareForm();
                    }
                    applicationPrepareForm.setEnabled(true);

                }
            });


            viewLogMenuItem = new JMenuItem("View logs");
            viewLogMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    java.io.File file = new File(SCLogger.getLogFileName());
                    try {
                        Desktop.getDesktop().open(file);
                    } catch (IOException ioe) {
                        log(ioe.getLocalizedMessage());
                    }
                }
            });

            toolsMenu.add(appPrepareMenuItem);
            toolsMenu.add(viewLogMenuItem);
        }
        // FORMING MENUS
        pluginMenu.add(pluginRepoMenuItem);
        pluginMenu.add(pluginSettingsMenuItem);

        settingsMenu.add(settingMenuItem);


        pluginSettingsMenuItem.setEnabled(false);

        //FORMING MENU BAR
        menuBar.add(pluginMenu);
        menuBar.add(settingsMenu);
        if (SettingsManager.getInstance().getPropertyByName(LauncherConstants.MenuExtendedMode, "false").equals("true")) {
            menuBar.add(toolsMenu);
        }
        setJMenuBar(menuBar);

    }

    synchronized public void launch() {
        //TODO Need to add menu panel about page,tool for prepare apps for publishing, may be Help menu
        log("Launching gui..");
        String appLocalVersionDef = "Need to install";

        //setContentPane(new Container());


        appList = AppManager.getInstance().getAppList();
        this.getContentPane().removeAll();


        mainPanel = new JPanel(new GridLayout(appList.size(), 2));
        mainPanel.setBorder(new LineBorder(Color.GRAY));


        statusPanel = new JPanel();
        for (Application localApp : appList) {
            JButtonEx button = new JButtonEx();
            String appLocalVersion = appLocalVersionDef;
            String localPath = "";

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
            button.setEnabled(localApp.isInstalled());


            JLabel label = new JLabel();
            label.setMaximumSize(new Dimension(40, 25));
            label.setText(appLocalVersion);
            label.setBorder(new TitledBorder("Version"));
            label.setHorizontalAlignment(SwingConstants.CENTER);


            label.setComponentPopupMenu(new AppPopupMenu(this, localApp, AppManager.getInstance()));
            if (localApp.isInstalled() && localApp.getAppLastVersion().equals(appLocalVersion)) {
                label.setForeground(Color.GREEN);
            } else if (localApp.isInstalled() && localApp.getSourcePlugin().isEmpty()) {
                label.setForeground(Color.ORANGE);
            } else if (localApp.isInstalled() && !localApp.getAppLastVersion().equals(appLocalVersion)) {
                label.setForeground(Color.RED);
                button.setEnabled(true);
            } else if (!localApp.isInstalled()) {
                label.setForeground(Color.LIGHT_GRAY);
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
        }

        mainPanel.setVisible(true);
        getContentPane().add(mainPanel, BorderLayout.NORTH);

        statusPanel.setMaximumSize(new Dimension(0, 20));

        statusPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        JLabel lbl = null;
        JLabel lbl2 = null;
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
                if (pl.getPluginObj() != null && pl.getPluginObj().checkConnection()) {
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
        setEnabled(true);
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
                        File file = new File(app);
                        Desktop.getDesktop().open(file);
                        //
                    } catch (IllegalArgumentException iae) {
                        log("Get exception: " + iae.getLocalizedMessage() + ". Open with exec().");
                        try {
                            Runtime.getRuntime().exec(app);
                        } catch (IOException ioe) {
                            log(ioe.getLocalizedMessage());
                        }
                    } catch (Exception ex) {
                        log(ex.getLocalizedMessage());
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
}
