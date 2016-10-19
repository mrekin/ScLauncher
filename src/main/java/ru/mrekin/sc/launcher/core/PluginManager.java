package ru.mrekin.sc.launcher.core;

import ru.mrekin.sc.launcher.plugin.IRemoteStorageClient;
import ru.mrekin.sc.launcher.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by MRekin on 02.09.2014.
 */
public class PluginManager {

    private static PluginManager instance;
    private String pluginDir = "";
    private ArrayList<Plugin> installedPlugins = new ArrayList<Plugin>(1);
    private ArrayList<Plugin> avaliabledPlugins = new ArrayList<Plugin>(1);
    private boolean avaliablePluginsLoaded = false;

    private PluginManager() {
        loadProperties();
        load();
        instance = this;
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
        //loadAvaliablePlugins();
    }

    public void loadInstalledPlugins() {
        //TODO need to change logic -> scl must find class implementing rsc without manifest
        String mainClass = "";
        String pluginName = "";
        String pluginVersion = "";
        ArrayList<Plugin> tempList = (ArrayList<Plugin>) installedPlugins.clone();
        installedPlugins = new ArrayList<Plugin>(1);
        boolean installed = false;
        try {
            ArrayList<File> jars = FileDriver.listFiles(pluginDir, ".jar");
            for (File f : jars) {
                try {
                    URL jarURL = f.toURI().toURL();
                    installed = false;
                    for (Plugin p : tempList) {
                        if (p.getPluginPath() != null && p.getPluginPath().equals(jarURL)) {
                            installed = true;
                            //
                            p.getPluginObj().disconnect();
                            p.setPluginObj(p.getPluginObj().getClass().newInstance());
                            p.getPluginObj().connect();
                            installedPlugins.add(p);
                            break;
                        }
                    }
                    if (installed) {
                        continue;
                    }

                    JarFile jf = new JarFile(f);

                    Class cl = findClassByInterface(jf, jarURL, IRemoteStorageClient.class);
                    jf.close();
                    if (!IRemoteStorageClient.class.isAssignableFrom(cl)) {
                        System.out.println("Plugin: " + f.toURI().toURL() + ", main class \'" + mainClass + "\' must implement " + IRemoteStorageClient.class.getCanonicalName());
                        continue;
                    }
                    Plugin plugin = new Plugin();
                    IRemoteStorageClient instance = null;
                    try {
                        instance = (IRemoteStorageClient) cl.newInstance();


                        pluginName = "".equals(instance.getPluginName()) ? cl.getSimpleName() : instance.getPluginName();
                        pluginVersion = "".equals(instance.getPluginVersion()) ? "-1" : instance.getPluginVersion();
                    } catch (AbstractMethodError ame) {
                        //TODO need to implement central logging logic (at first time it can be simple sout, but in one place).
                        System.out.println("Local plugin loading AbstractMethodError: " + ame.getMessage());
                    } catch (IncompatibleClassChangeError ice) {
                        System.out.println("Plugin manager: can't load plugin " + f.getAbsolutePath());
                        System.out.println(ice.getLocalizedMessage());
                        continue;
                    }
                    plugin.setPluginName(pluginName);
                    plugin.setPluginVersion(pluginVersion);
                    plugin.setPluginObj(instance);
                    plugin.setPluginPath(f.toURI().toURL());
                    plugin.setInstalled(true);
                    plugin.setPluginSimpleName(f.getName().replace(".jar", ""));
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
        //TODO need to move all file names to constans class //
        this.avaliabledPlugins = PluginRepoManager.getInstance().getAvaliablePlugins2();
        avaliablePluginsLoaded = true;
    }

    public void loadProperties() {
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

    public ArrayList<Plugin> getAvaliabledPlugins() {
        return avaliabledPlugins;
    }

    public ArrayList<Plugin> getAllPlugins() {
        for (Plugin pl : avaliabledPlugins) {
            if (!installedPlugins.contains(pl)) {
                installedPlugins.add(pl);
            }
        }

        return installedPlugins;
    }

    public void install(Plugin plugin, String version) {
        System.out.println("Intalling plugin " + plugin.getPluginName() + ", " + version);
        PluginRepoManager.getInstance().install(plugin, version);
        loadInstalledPlugins();
    }

    //TODO need to delete plugin with restarting scLauncher. No way to unload classes and release jar file.
    public void remove(Plugin plugin) {
        installedPlugins.remove(plugin);
        try {
            plugin.getPluginObj().disconnect();
            plugin = null;
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
        }
    }
}
