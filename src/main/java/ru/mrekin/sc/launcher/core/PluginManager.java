package ru.mrekin.sc.launcher.core;

import ru.mrekin.sc.launcher.gui.PluginRepoForm;
import ru.mrekin.sc.launcher.gui.TrayPopup;
import ru.mrekin.sc.launcher.plugin.INotificationClient;
import ru.mrekin.sc.launcher.plugin.IRemoteStorageClient;
import ru.mrekin.sc.launcher.plugin.Plugin;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by MRekin on 02.09.2014.
 */
public class PluginManager {

    private static PluginManager instance;
    private static String pluginDir = "";
    private ArrayList<Plugin> installedPlugins = new ArrayList<Plugin>(1);
    private ArrayList<Plugin> avaliabledPlugins = new ArrayList<Plugin>(1);
    private boolean avaliablePluginsLoaded = false;

    private PluginManager() {
        instance = this;
        loadProperties();
        loadInstalledPlugins();

//        LauncherGui.getInstance().launch();
    }

    private static void log(String msg){
        SCLogger.getInstance().log(PluginManager.class.getName(),"INFO",msg);
    }

    public static PluginManager getInstance() {
        if (instance != null) {

            return instance;
        } else {
            return new PluginManager();
        }
    }

    public void load() {
        this.installedPlugins = new ArrayList<Plugin>(1);
        this.avaliabledPlugins = new ArrayList<Plugin>(1);
        loadInstalledPlugins();
        loadAvaliablePlugins();
        //loadAvaliablePlugins();

    }

    synchronized public void loadInstalledPlugins() {
        //TODO need to change logic -> scl must find class implementing rsc without manifest
        //
        String mainClass = "";
        String pluginName = "";
        String pluginVersion = "";
        ArrayList<Plugin> alreadyInstalled = (ArrayList<Plugin>) installedPlugins.clone();
        log("Already installed: "+ alreadyInstalled.size());
        installedPlugins = new ArrayList<Plugin>(1);
        boolean installed = false;
        try {
            ArrayList<File> jars = FileDriver.listFiles(pluginDir, ".jar");

            for (File f : jars) {

                try {
                    URL jarURL = f.toURI().toURL();
                    installed = false;
                    for (Plugin p : installedPlugins) {
                        if (p.getPluginPath() != null && p.getPluginPath().equals(jarURL)) {
                            installed = true;
                            //
                            if (p.getPluginObj() != null) {
                                p.getPluginObj().disconnect();
                            } else {
                                p.setPluginObj(p.getPluginObj().getClass().newInstance());
                            }
                            boolean is = p.getPluginObj().connect();
                            //installedPlugins.add(p);
                            break;
                        }
                    }
                    if (installed) {
                        continue;
                    }

                    JarFile jf = new JarFile(f);


                    Class cl = findClassByInterface(jf, jarURL, IRemoteStorageClient.class);

                    if (cl != null && IRemoteStorageClient.class.isAssignableFrom(cl)) {

                        jf.close();
                        Plugin plugin = new Plugin();
                        IRemoteStorageClient instance = null;
                        try {
                            instance = (IRemoteStorageClient) cl.newInstance();
                            pluginName = "".equals(instance.getPluginName()) ? cl.getSimpleName() : instance.getPluginName();
                            pluginVersion = "".equals(instance.getPluginVersion()) ? "-1" : instance.getPluginVersion();
                        } catch (AbstractMethodError ame) {
                            //TODO need to implement central logging logic (at first time it can be simple sout, but in one place).
                            log("Local plugin loading AbstractMethodError: " + ame.getMessage());
                        } catch (IncompatibleClassChangeError ice) {
                            log("Plugin manager: can't load plugin " + f.getAbsolutePath());
                            log(ice.getLocalizedMessage());
                            continue;
                        }
                        if (cl != null && INotificationClient.class.isAssignableFrom(cl)) {
                            ((INotificationClient) instance).setMessageService(new TrayPopup());
                            Properties props = ((INotificationClient) instance).getDefaultProperties();
                            for (Object key : props.keySet()) {
                                if (!SettingsManager.getInstance().getXmlConfiguration().containsKey((String) key)) {
                                    SettingsManager.getInstance().getXmlConfiguration().setProperty((String) key, props.getProperty((String) key));
                                } else {
                                    props.setProperty((String) key, (String) SettingsManager.getInstance().getXmlConfiguration().getProperty((String) key));
                                }
                            }
                            SettingsManager.getInstance().save();
                            ((INotificationClient) instance).loadProperties(props);
                        }
                        plugin.setPluginName(pluginName);
                        plugin.setPluginVersion(pluginVersion);
                        plugin.setPluginObj(instance);
                        plugin.setPluginPath(f.toURI().toURL());
                        plugin.setInstalled(true);
                        plugin.setPluginSimpleName(f.getName().replace(".jar", ""));
                        plugin.setRepository("");
                        installedPlugins.add(plugin);
                        continue;
                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();

                }

            }


//
            log("Loaded installed plugins: "+installedPlugins.size());
        } catch (IOException ioe) {
            log(ioe.getLocalizedMessage());
        }
        /*     };
         */
    }

    public void loadAvaliablePlugins() {
        //TODO need to move all file names to constans class //
        this.avaliabledPlugins = PluginRepoManager.getInstance().getAvaliablePlugins2();
        avaliablePluginsLoaded = true;
    }

    public static void loadProperties() {
        pluginDir = SettingsManager.getInstance().getPropertyByName(LauncherConstants.PluginDirectory, "plugin/");
    }

    public boolean isAvaliablePluginsLoaded() {
        return avaliablePluginsLoaded;
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

    private static Class findClassByInterface(JarFile jar, URL jarURL, Class iface) {
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
                    log("Entry " + element + " not a class");
                }
                if (cl != null && iface.isAssignableFrom(cl)) {

                    return cl;
                }
            }
        }
        return null;
    }

    public ArrayList<Plugin> getAvaliabledPlugins() {
        return avaliabledPlugins;
    }

    public ArrayList<Plugin> getAllPlugins() {
        ArrayList<Plugin> tmp = (ArrayList<Plugin>) installedPlugins.clone();
        for (Plugin pl : avaliabledPlugins) {
            if (!installedPlugins.contains(pl)) {
                tmp.add(pl);
            }
        }

        return tmp;
    }

    public void checkNewPluginVersions() {
        if (!avaliablePluginsLoaded) {
            loadAvaliablePlugins();
        }
        ArrayList<Plugin> plugins = getAllPlugins();
        StringBuffer sb = new StringBuffer();
        for (Plugin pl : plugins) {
            if (!pl.isInstalled()) {
                continue;
            }
            if (compareVersions(pl.getPluginVersion(), pl.getLatestVersion()) == -1) {
                if (sb.length() != 0) {
                    sb.append("\n");
                }
                log(pl.getPluginVersion() + " " + pl.getLatestVersion());
                sb.append("New version of " + pl.getPluginSimpleName() + " plugin avaliable!");
            }
        }
        if (sb.length() != 0) {
            MouseAdapter ma = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    PluginRepoForm.getInstance().setEnabled(true);
                    PluginRepoForm.getInstance().setVisible(true);
                }
            };
            TrayPopup.displayMessage(sb.toString(), ma);
        }
    }

    public void install(Plugin plugin, String version) {
        log("Intalling plugin " + plugin.getPluginName() + ", " + version);
        PluginRepoManager.getInstance().install(plugin, version);
        loadInstalledPlugins();
    }

    //TODO need to delete plugin with restarting scLauncher. No way to unload classes and release jar file.
    public void update(Plugin plugin, final String targetVersion) {
        TrayPopup.displayMessage("SCLauncher will restart to remove old version of plugin");
        final String plName = plugin.getPluginSimpleName();
        new Thread() {
            @Override
            public void run() {
                try {
                    sleep(3000);

                    String command = "java -jar " + SettingsManager.getInstance().getPropertyByName("Application.name", "sc-launcher") + ".jar --deletePlugin " + plName + " --installPlugin " + plName + " " + targetVersion;
                    log(command);
                    Runtime.getRuntime().exec(command);
                } catch (Exception e) {
                    log(e.getLocalizedMessage());
                }
                System.exit(0);
            }
        }.run();

    }

    public static boolean remove(String pluginName) {
        //  installedPlugins.remove(plugin);
        try {
            File f = getPluginFile(pluginName);
            if (f != null) {
                return f.delete();
            } else {
                log("Plugin for remove not found");
            }
        } catch (Exception e) {
            log(e.getLocalizedMessage());
        }
        return false;
    }

    public void removeWithRestart(Plugin plugin) {
        TrayPopup.displayMessage("SCLauncher will restart to remove plugin");
        final String plName = plugin.getPluginSimpleName();
        new Thread() {
            @Override
            public void run() {
                try {
                    sleep(3000);

                    String command = "java -jar " + SettingsManager.getInstance().getPropertyByName("Application.name", "sc-launcher") + ".jar --deletePlugin " + plName;
                    log(command);
                    Runtime.getRuntime().exec(command);
                } catch (Exception e) {
                    log(e.getLocalizedMessage());
                }
                System.exit(0);
            }
        }.run();
    }

    public static int compareVersions(String v1, String v2) {
        try {
            String[] components1 = v1.split("\\.");
            String[] components2 = v2.split("\\.");
            int length = Math.min(components1.length, components2.length);
            for (int i = 0; i < length; i++) {
                //int result = new Integer(components1[i]).compareTo(Integer.parseInt(components2[i]));
                int result = (new Integer(components1[i]) < Integer.parseInt(components2[i])) ? -1 : ((new Integer(components1[i]) == Integer.parseInt(components2[i])) ? 0 : 1);
                //new Integer(components1[i]).compareTo(Integer.parseInt(components2[i]));
                if (result != 0) {
                    return result;
                }
            }
            return components1.length < components2.length ? -1 : (components1.length == components2.length ? 0 : 1);
        } catch (Exception e) {
            log(e.getLocalizedMessage());
            return 1;
        }
    }

    private static File getPluginFile(String pluginName) {
        loadProperties();
        try {
            ArrayList<File> jars = FileDriver.listFiles(pluginDir, ".jar");
            for (File f : jars) {
                try {
/*                    URL jarURL = f.toURI().toURL();
                    JarFile jf = new JarFile(f);
                    Class cl = findClassByInterface(jf, jarURL, IRemoteStorageClient.class);
                    jf.close();
                    if (!IRemoteStorageClient.class.isAssignableFrom(cl)) {
                        System.out.println("Plugin: " + f.toURI().toURL() + ", main class \'" + IRemoteStorageClient.class + "\' must implement " + IRemoteStorageClient.class.getCanonicalName());
                        continue;
                    }
                    String clPluginName = cl.getSimpleName();
                    if (pluginName.equals(clPluginName)) {
                        return f;
                    }*/

                    if (f.getCanonicalPath().contains(pluginName + ".jar")) {
                        return f;
                    }

                } catch (AbstractMethodError ame) {
                    //TODO need to implement central logging logic (at first time it can be simple sout, but in one place).
                    log("Local plugin loading AbstractMethodError: " + ame.getMessage());
                } catch (IncompatibleClassChangeError ice) {
                    log("Plugin manager: can't get plugin " + f.getAbsolutePath());
                    log(ice.getLocalizedMessage());
                    continue;
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();

        }
        return null;
    }

}
