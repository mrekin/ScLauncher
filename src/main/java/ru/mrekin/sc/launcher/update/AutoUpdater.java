package ru.mrekin.sc.launcher.update;

import ru.mrekin.sc.launcher.core.LauncherConstants;
import ru.mrekin.sc.launcher.core.SCLogger;
import ru.mrekin.sc.launcher.core.SettingsManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.lang.invoke.MethodHandles;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.jar.JarFile;

/**
 * Created by MRekin on 25.08.2014.
 */
public class AutoUpdater {

    private static JTextArea ta = null;
    //private static String serverURL = "http://bssdev:8888/SCLauncher/";
    private static String serverURL, appURL;

    private static void log3(String msg){
        SCLogger.getInstance().log(MethodHandles.lookup().lookupClass().getName(),"INFO",msg);
    }

    public static String checkForUpdates() {

        if ("false".equals(SettingsManager.getInstance().getPropertyByName(LauncherConstants.AutoUpdaterDevMode, "false"))) {
            serverURL = SettingsManager.getInstance().getPropertyByName(LauncherConstants.AutoUpdaterServerURL);
        } else {
            serverURL = SettingsManager.getInstance().getPropertyByName(LauncherConstants.AutoUpdaterServerURL) + "devmode/";
        }
        appURL = serverURL + LauncherConstants.SettingsFileName;
        String version = null;

        HttpURLConnection connection = null;
        OutputStreamWriter wr = null;
        BufferedReader rd = null;
        StringBuilder sb = null;
        String line = null;
        String currentVersion = null;
        String serverVersion = null;


        URL serverAddress = null;

        try {
            serverAddress = new URL(appURL);
            //set up out communications stuff
            connection = null;

            //Set up the initial connection
            connection = (HttpURLConnection) serverAddress.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setReadTimeout(1000);

            connection.connect();
            int responce = connection.getResponseCode();
            if (responce != 200) {
                //Cant connect
                log("Update server returns: " + responce);
                return null;
            }

            //read the result from the server
            Properties props = new Properties();
            props.load(connection.getInputStream());
            if (props.size() == 0) {
                //no settings
                return null;
            }

            serverVersion = props.getProperty("Application.version");
            // System.out.println(sb.toString());


            currentVersion = SettingsManager.getInstance().getPropertyByName("Application.version");


        } catch (MalformedURLException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //close the connection, set all objects to null
            connection.disconnect();
            rd = null;
            // sb = null;
            wr = null;
            connection = null;
        }

        if (currentVersion != null && currentVersion.equals(serverVersion)) {
            //no new version
            return null;
        } else if (currentVersion == null) {
            log("Current version is null");
            return null;
        } else {
            return serverVersion;
        }
    }

    public static void update(String version) {
        //loadSettings();
        String name = AutoUpdater.class.getCanonicalName().replace(".", "/") + ".class";
        String name1 = AutoUpdater.class.getCanonicalName().replace(".", "/") + "$1.class";
        String name2 = AutoUpdater.class.getCanonicalName().replace(".", "/") + "$1MyThread.class";
        String name3 = SettingsManager.class.getCanonicalName().replace(".", "/") + ".class";
        String packagePath = AutoUpdater.class.getPackage().getName().replace(".", "/");
        String packageName = AutoUpdater.class.getPackage().getName();
        //String pluginsDir = "plugins";
        String pluginsDir = SettingsManager.getInstance().getPropertyByName(LauncherConstants.PluginDirectory, "plugins/");
        String pluginName = "AutoUpdater";
        String pluginDir = "./" + pluginsDir + pluginName + "/";
        String fullPath = pluginDir + name;
        String fullPath1 = pluginDir + name1;
        String fullPath2 = pluginDir + name2;
        String fullPath3 = pluginDir + name3;
        URL pr = AutoUpdater.class.getClassLoader().getResource(name);
        try {
            /*
            File plugin = new File(pluginDir + packagePath);
            if (!plugin.exists() || !plugin.isDirectory()) {
                boolean b = plugin.mkdirs();

            }
            */
            installUpdaterClass(name, fullPath);
            installUpdaterClass(name1, fullPath1);
            installUpdaterClass(name2, fullPath2);
            installUpdaterClass(name3, fullPath3);

            String appName = SettingsManager.getInstance().getPropertyByName("Application.name", "sc-launcher");
            String appURL = serverURL + appName + "-" + version + ".jar";
            String command = "java -cp " + pluginDir + " " + packageName + "." + AutoUpdater.class.getSimpleName() + " " + version + " " + appURL + " " + appName;
            log(command);
            Runtime.getRuntime().exec(command);

        } catch (Exception use) {
            use.printStackTrace();
        }

        System.exit(0);
    }

    private static void installUpdaterClass(String pathInJar, String pathInPluginDirectory) {
        try {
            URL pr = AutoUpdater.class.getClassLoader().getResource(pathInJar);
            InputStream bis = pr.openStream();
            File plugin = new File(pathInPluginDirectory);
            if (!plugin.exists() || !plugin.isFile()) {
                boolean b = plugin.mkdirs();
                b = plugin.delete();
            }
            FileOutputStream fos = new FileOutputStream(pathInPluginDirectory);
            int c;
            while ((c = bis.read()) != -1) {
                fos.write(c);
            }
            bis.close();
            fos.close();
        } catch (IOException ioe) {
            log(ioe.getLocalizedMessage());
        }
    }

    public static void main(final String[] args) {

        if (args.length != 3) {
            log("Usage: <version> <AppURL> <AppName>");
            System.exit(1);
        }

        JFrame frame = new JFrame();
        frame.setSize(new Dimension(600, 330));
        frame.setResizable(false);
        frame.setTitle("Updater");
        frame.setLocationByPlatform(true);
        JPanel panel = new JPanel();

        ta = new JTextArea();
        JScrollPane jsp = new JScrollPane(ta);

        jsp.setPreferredSize(new Dimension(560, 250));
        jsp.setMaximumSize(new Dimension(560, 250));
        jsp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        //ta.setPreferredSize(new Dimension(560, 240));
        //ta.setMinimumSize(new Dimension(560, 240));
        ta.setSize(new Dimension(560, 240));
        JButton button = new JButton("Launch");
        button.setEnabled(false);
        button.setSize(new Dimension(50, 20));
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    Runtime.getRuntime().exec("java -jar " + args[2] + ".jar --forceSettings");
                    System.exit(0);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                    log(ioe.getLocalizedMessage());
                }

            }
        });
        ta.setLineWrap(true);
        // jsp.add(ta);
        panel.add(jsp);
        panel.add(button);
        frame.add(panel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        //frame.getContentPane().add(ta);
        //frame.getContentPane().add(button);

        frame.setVisible(true);

        log("New launcher version avaliable: " + args[0]);
        File file = new File(".");

        log("Launcher directory: " + file.getAbsolutePath());
        log("Searching jar files..");

        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                if (f.getName().endsWith(".jar")) {
                    log("===>  " + f.getName());
                    log("Removing..");
                    f.delete();
                    log("Done.");
                }
            }
        }
        log("Downloading new version..");
        log("Application URL: " + args[1]);

        HttpURLConnection connection = null;
        BufferedReader rd = null;
        StringBuilder sb = null;

        URL serverAddress = null;

        try {


            serverAddress = new URL(args[1]);
            //set up out communications stuff
            connection = null;

            //Set up the initial connection
            connection = (HttpURLConnection) serverAddress.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setReadTimeout(10000);

            connection.connect();

            //read the result from the server
            InputStream is = connection.getInputStream();
            //rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            //FileWriter fw = new FileWriter(args[2]+".jar");
            FileOutputStream fos = new FileOutputStream(args[2] + ".jar");


            int c = 0;
            log("Please wait..");
            log2("Downloaded: ");
            String text = ta.getText();
            int i = 0;


            class MyThread extends Thread {
                String text = "";
                boolean run = true;
                int i = 0;

                public void run() {
                    while (run) {
                        ta.setText(text);
                        log(i / 1024 + " KBytes");
                        //ta.updateUI();
                        try {
                            sleep(600);
                        } catch (Exception e) {
                            log(e.getLocalizedMessage());
                        }
                    }
                }

                public void setText(String text) {
                    this.text = text;
                }

                public void setI(int i) {
                    this.i = i;
                }

                public void stopT() {
                    run = false;
                }
            }


            MyThread th = new MyThread();
            th.setText(text);
            th.start();

            byte[] array = new byte[1024];
            while ((c = is.read(array)) != -1) {
                i = i + c;
                fos.write(array, 0, c);
                th.setI(i);
            }

            ta.setText(text);
            log(i / 1024 + " KBytes");

            //th.sleep(600);
            th.stopT();
            fos.close();
            is.close();
            log("Done.");
            JarFile jar = new JarFile(args[2] + ".jar");
            InputStream jis = jar.getInputStream(jar.getEntry(LauncherConstants.ReliseNotesFileName));
            StringWriter sw = new StringWriter();
            while ((c = jis.read()) != -1) {
                sw.write(c);
            }
            log(sw.toString());
            jis.close();
            sw.close();
            log("Press button to Launch or close updater");
            ta.setCaretPosition(ta.getDocument().getLength());
            button.setEnabled(true);


        } catch (MalformedURLException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            log(e.getLocalizedMessage());
        } finally {
            //close the connection, set all objects to null
            connection.disconnect();
            rd = null;

            // sb = null;

            connection = null;
        }
    }

    private static void log(String text) {
        log3(text);
        if (ta != null) {
            ta.append(text + "\n");
        }
    }

    private static void log2(String text) {
        log3(text);
        if (ta != null) {
            ta.append(text);
        }
    }
}
