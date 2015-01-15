package ru.mrekin.sc.launcher.plugin;

import ru.mrekin.sc.launcher.core.FileDriver;
import ru.mrekin.sc.launcher.core.LauncherConstants;
import ru.mrekin.sc.launcher.core.SettingsManager;
import sun.net.www.protocol.jar.Handler;
import sun.net.www.protocol.jar.JarURLConnection;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by MRekin on 02.09.2014.
 */
public class PluginManager {

    private String pluginDir = "";
    private ArrayList<Plugin> installedPlugins = new ArrayList<Plugin>(1);
    private ArrayList<Plugin> avaliabledPlugins = new ArrayList<Plugin>(1);
    private static PluginManager instance;

    private PluginManager() {
        loadProperties();
        instance = this;
    }

    public static PluginManager getInstance() {
        if (instance != null) {

            return instance;
        } else {
            return new PluginManager();
        }
    }

    public void loadInstalledPlugins() {
        //TODO need to change logic -> scl must find class implementing rsc without manifest
        String mainClass = "";
        String pluginName = "";
        String pluginVersion = "";
        try {
            ArrayList<File> jars = FileDriver.listFiles(pluginDir, ".jar");
            for (File f : jars) {
                try {
                    URL jarURL = f.toURI().toURL();
                    JarFile jf = new JarFile(f);
                    //oldVersion
                    //mainClass = jf.getManifest().getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);
                    // mainClass = "ru/mrekin/sc/launcher/plugin/SvnClient.class";
                    //mainClass = findClassByInterface(jf, jarURL,IRemoteStorageClient.class);
                    //URLClassLoader classLoader = new URLClassLoader(new URL[]{jarURL});


                    Class cl = findClassByInterface(jf, jarURL, IRemoteStorageClient.class);
                    if (!IRemoteStorageClient.class.isAssignableFrom(cl)) {
                        System.out.println("Plugin: " + f.toURI().toURL() + ", main class \'" + mainClass + "\' must implement " + IRemoteStorageClient.class.getCanonicalName());
                        continue;
                    }

                    IRemoteStorageClient instance = (IRemoteStorageClient) cl.newInstance();
                    Plugin plugin = new Plugin();
                    try {
                        pluginName = "".equals(instance.getPluginName()) ? cl.getSimpleName() : instance.getPluginName();
                        pluginVersion = "".equals(instance.getPluginVersion()) ? "-1" : instance.getPluginVersion();
                    } catch (AbstractMethodError ame) {
                        //TODO need to implement central logging logic (at first time it can be simple sout, but in one place).
                        System.out.println("Local plugin loading AbstractMethodError: " + ame.getMessage());
                    }
                    plugin.setPluginName(pluginName);
                    plugin.setPluginVersion(pluginVersion);
                    plugin.setPluginObj(instance);
                    plugin.setPluginPath(f.toURI().toURL());
                    plugin.setInstalled(true);
                    installedPlugins.add(plugin);


                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();

                }

            }

        } catch (IOException ioe) {
            System.out.println(ioe.getLocalizedMessage());
        }

    }

    public void loadAvaliablePlugins() {
        //TODO need to move all file names to constans class
        String serverURL = SettingsManager.getPropertyByName(LauncherConstants.PluginRepoServerURL) + "plugin.list";
        HttpURLConnection connection = null;
        URL serverAddress;

        try {
            serverAddress = new URL(serverURL);
            //set up out communications stuff
            connection = null;

            //Set up the initial connection
            connection = (HttpURLConnection) serverAddress.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setReadTimeout(10000);

            connection.connect();

            //read the result from the server

            Properties props = new Properties();
            props.load(connection.getInputStream());

            for (Map.Entry e : props.entrySet()) {

                if ("true".equals(e.getValue().toString())) {
                    //1. Read settings from settings file (at least jar name) 2. Load plugin info (version, description)
                    //2. Store plugin path info for further install process
                    Properties settings = new Properties();
                    serverAddress = new URL(SettingsManager.getPropertyByName(LauncherConstants.PluginRepoServerURL) + e.getKey() + "/" + LauncherConstants.SettingsFileName);
                    connection = (HttpURLConnection) serverAddress.openConnection();
                    connection.connect();
                    settings.load(connection.getInputStream());
                    JarURLConnection j = new JarURLConnection(new URL(serverURL + e.getKey() + "/"), new Handler());


                }
            }


        } catch (MalformedURLException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //close the connection, set all objects to null
            if (connection != null) {
                connection.disconnect();
            }

        }


    }

    public void loadProperties() {
        pluginDir = SettingsManager.getPropertyByName(LauncherConstants.PluginDirectory, "plugin/");
    }

    public ArrayList<Plugin> getPlugins() {
        return installedPlugins;
    }

    public Plugin getPluginByName(String name) {
        for (Plugin pl : installedPlugins) {
            if (name.equals(pl.getPluginName())) {
                return pl;
            }
        }
        return null;
    }

    private Class findClassByInterface(JarFile jar, URL jarURL, Class iface) {
        Enumeration<JarEntry> entries = jar.entries();
        URLClassLoader classLoader = new URLClassLoader(new URL[]{jarURL});

        Class cl = null;
        while (entries.hasMoreElements()) {
            JarEntry nextElement = entries.nextElement();
            String element = nextElement.getName().replace("/", ".");
            if (element.endsWith(".class")) {
                try {
                    cl = classLoader.loadClass(element.replace(".class", ""));
                    //cl = classLoader.loadClass(element);
                } catch (ClassNotFoundException cnfe) {
                    System.out.println("Entry " + element + " not a class");
                }
                if (cl != null && iface.isAssignableFrom(cl)) {
                    return cl;
                }
            }
        }


        return null;
    }

}
