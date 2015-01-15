package ru.mrekin.sc.launcher.gui;

//import ru.mrekin.sc.launcher.SvnClient;

import ru.mrekin.sc.launcher.core.AppManager;
import ru.mrekin.sc.launcher.core.Application;
import ru.mrekin.sc.launcher.core.FileDriver;

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


    private AppManager appManager;
    ArrayList<Application> appList;
    //, svnAppList, localApps;
    FileDriver fileDriver;
    //    SvnClient svnClient;
    JPanel mainPanel, statusPanel;
    BufferedImage mainIcon;
    ImageIcon redIcon, greenIcon;
    static LauncherGui instance;

    public LauncherGui() {
        super("SC launcher");
        setLocationByPlatform(true);
        //  setResizable(false);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
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
            mainIcon = ImageIO.read(getClass().getClassLoader().getResource("icon.png"));
            greenIcon = new ImageIcon(ImageIO.read(getClass().getClassLoader().getResource("green.png")));
            redIcon = new ImageIcon(ImageIO.read(getClass().getClassLoader().getResource("red.png")));

        } catch (
                IOException ioe
                )

        {
            ioe.printStackTrace();
        }


        appManager = AppManager.getInstance();
        appManager.init();
//        fileDriver = appManager.getFileDriver();
        appList = appManager.getAppList();
//        svnClient = appManager.getSvnClient();
//        svnAppList = appManager.getSvnAppList();
/*
        if (svnAppList == null) {
            JDialog msg = new JDialog();
            msg.setTitle("Can't connect to SVN");

            msg.setVisible(true);
            try {
                wait(1000);
            } catch (InterruptedException ie) {

            }
            System.exit(1);

        }
  */      //temp is a diff
        //      localApps = (ArrayList<Application>) appList.clone();
        //      localApps.removeAll(svnAppList);

//        setSize(new Dimension(300, 45 + 55 * (svnAppList.size() + localApps.size())));
        setSize(new Dimension(300, 45 + 55 * appList.size()));


    }

    public void launch() {
        //TODO Need to add menu panel with settings, plugin list, about page,tool for prepare apps for publishing, may be Help menu
        String appLocalVersionDef = "Need to install";
        //setContentPane(new Container());
        this.getContentPane().removeAll();
//        mainPanel = new JPanel(new GridLayout(svnAppList.size() + localApps.size(), 2));
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
            if(localApp.isInstalled()) {
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


            label.setComponentPopupMenu(new AppPopupMenu(this, localApp, appManager));
            if (localApp.isInstalled()&&localApp.getAppLastVersion().equals(appLocalVersion)) {
                label.setForeground(Color.GREEN);
                button.setEnabled(true);
            } else if (localApp.isInstalled()&&!localApp.getAppLastVersion().equals(appLocalVersion)) {
                label.setForeground(Color.RED);
                button.setEnabled(true);
            } else if (!localApp.isInstalled()) {
                label.setForeground(Color.LIGHT_GRAY);
                button.setEnabled(false);
            }


            JComboBox box = new JComboBox();
            box.setBorder(new TitledBorder("Avaliable ver."));
            box.setSize(new Dimension(40, 45));


            for (String ver : localApp.getAppVersions()) {
                box.addItem(ver);

            }

            box.setSelectedIndex(box.getItemCount() - 1);

            mainPanel.add(button);
            mainPanel.add(label);
            //    add(box);

        }
        //TODO need to move this logic to AppManager, GUI must work only with one common app list
 /*       for (Application svnApp : svnAppList) {
            JButtonEx button = new JButtonEx();
            String appTitle = "";
            String appLocalVersion = "Need to install";
            String localPath = "";

            for (Application app : appList) {
                if (app.equals(svnApp)) {
                    //appTitle = app.getAppTitle();
                    appLocalVersion = app.getAppVersion();
                    localPath = app.getRunCommand();
                    break;
                }
            }

            button.setPreferredSize(new Dimension(100, 30));
            button.setText(("".equals(appTitle) ? svnApp.getAppName() : appTitle));
            button.setBorder(new EtchedBorder());
            button.setToolTipText((String) svnApp.getAppName());
            button.setApp(localPath);


            JLabel label = new JLabel();
            label.setMaximumSize(new Dimension(40, 25));
            label.setText(appLocalVersion);
            label.setBorder(new TitledBorder("Version"));
            label.setHorizontalAlignment(SwingConstants.CENTER);


            label.setComponentPopupMenu(new AppPopupMenu(this, svnApp, appManager));
            if (svnApp.getAppLastVersion().equals(appLocalVersion)) {
                label.setForeground(Color.GREEN);
                button.setEnabled(true);
            } else if (!svnApp.getAppLastVersion().equals(appLocalVersion) && !appLocalVersionDef.equals(appLocalVersion)) {
                label.setForeground(Color.RED);
                button.setEnabled(true);
            } else if (appLocalVersionDef.equals(appLocalVersion)) {
                label.setForeground(Color.LIGHT_GRAY);
                button.setEnabled(false);
            }


            JComboBox box = new JComboBox();
            box.setBorder(new TitledBorder("Avaliable ver."));
            box.setSize(new Dimension(40, 45));


            for (String ver : svnApp.getAppVersions()) {
                box.addItem(ver);

            }

            box.setSelectedIndex(box.getItemCount() - 1);

            mainPanel.add(button);
            mainPanel.add(label);
            //    add(box);

        }
*/
        // setResizable(false);

        mainPanel.setVisible(true);
        getContentPane().add(mainPanel, BorderLayout.NORTH);

        statusPanel.setMaximumSize(new Dimension(0, 20));

        statusPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        //TODO implements plugin status logic
        statusPanel.setToolTipText("Status");
        JLabel lbl = new JLabel();
        JLabel lbl2 = new JLabel();
        //if (appManager.getSvnClient().checkSvnConnection()) {

        if (true) {
            lbl2.setIcon(greenIcon);
        } else {
            lbl2.setIcon(redIcon);
        }
        lbl.setText("Svn:");
        lbl.setMaximumSize(new Dimension(20, 20));

        statusPanel.add(lbl, BorderLayout.EAST);
        statusPanel.add(lbl2, BorderLayout.EAST);

        getContentPane().add(statusPanel, BorderLayout.SOUTH);
        setVisible(true);


        setIconImage(mainIcon);

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
