package ru.mrekin.sc.launcher.plugin;

import ru.mrekin.sc.launcher.core.FileDriver;
import ru.mrekin.sc.launcher.core.LauncherConstants;
import ru.mrekin.sc.launcher.core.SettingsManager;
import ru.mrekin.sc.launcher.plugin.NexusPlugin.NexusPluginRepoClient;

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
public class PluginRepoManager {

    private static PluginRepoManager instance;
    private String pluginDir = "";
    private ArrayList<RepoPlugin> installedPlugins = new ArrayList<RepoPlugin>(1);
//    private ArrayList<RepoPlugin> avaliabledPlugins = new ArrayList<RepoPlugin>(1);

    private PluginRepoManager() {
        loadProperties();
        loadInstalledRepoPlugins();
        instance = this;
    }

    public static PluginRepoManager getInstance() {
        if (instance != null) {

            return instance;
        } else {
            return new PluginRepoManager();
        }
    }

    public void loadInstalledRepoPlugins() {
        //TODO need to change logic -> scl must find class implementing rsc without manifest
        String mainClass = "";
        String pluginName = "";
        String pluginVersion = "";
        try {

            //Loading builInPlugins
            RepoPlugin plugin = getLoadedPlugin(NexusPluginRepoClient.class);
            installedPlugins.add(plugin);

            //LoadingIntalledPlugins
            ArrayList<File> jars = FileDriver.listFiles(pluginDir, ".jar");
            for (File f : jars) {
                try {
                    URL jarURL = f.toURI().toURL();
                    JarFile jf = new JarFile(f);
                    Class cl = findClassByInterface(jf, jarURL, IPluginRepoClient.class);
                    if (cl == null) {
                        continue;
                    }
                    if (!IPluginRepoClient.class.isAssignableFrom(cl)) {
                        System.out.println("Plugin: " + f.toURI().toURL() + ", main class \'" + mainClass + "\' must implement " + IPluginRepoClient.class.getCanonicalName());
                        continue;
                    }

                    plugin = getLoadedPlugin(cl);
                    plugin.setPluginPath(f.toURI().toURL());
                    installedPlugins.add(plugin);

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException ioe) {
            System.out.println(ioe.getLocalizedMessage());
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
        }
    }

    private RepoPlugin getLoadedPlugin(Class cl) throws IllegalAccessException, InstantiationException {
        RepoPlugin plugin = new RepoPlugin();
        String pluginName = "";
        String pluginVersion = "";
        IPluginRepoClient instance = (IPluginRepoClient) cl.newInstance();
        try {
            pluginName = "".equals(instance.getRepoPluginName()) ? cl.getSimpleName() : instance.getRepoPluginName();
            pluginVersion = "".equals(instance.getRepoPluginVersion()) ? "-1" : instance.getRepoPluginVersion();
        } catch (AbstractMethodError ame) {
            //TODO need to implement central logging logic (at first time it can be simple sout, but in one place).
            System.out.println("Local plugin loading AbstractMethodError: " + ame.getMessage());
        }
        plugin.setPluginName(pluginName);
        plugin.setPluginVersion(pluginVersion);
        plugin.setPluginObj(instance);
        plugin.setInstalled(true);
        return plugin;
    }

    public ArrayList<Plugin> getAvaliablePlugins() {
        // GET plugins from repositories
        //Get all repoClients
        //Get plugins from each repository
        //Merge plugins and versions

        if (installedPlugins == null || installedPlugins.size() == 0) {
            System.out.println("No plugin avaliable. Please install repository client");
            return null;
        }
        ArrayList<Plugin> plugins = new ArrayList<Plugin>();
        Properties props;

        for (RepoPlugin repoPlugin : installedPlugins) {
            //TODO for all repositories from settings
            //TODO need to set setting for plugin from XMLSettingsManager
            try {
                props = repoPlugin.getPluginObj().getDefaultProperties();
                repoPlugin.getPluginObj().connect(props.getProperty("user"), props.getProperty("pass"), props.getProperty("URL"));
                plugins.addAll((ArrayList<Plugin>) repoPlugin.getPluginObj().getPluginsList());
            } catch (Exception e) {
                System.out.println("RepoClient connecting: " + e.getLocalizedMessage());
            }

        }
        return plugins;
    }

    public void loadProperties() {
        //TODO Need to load settings from XMLSettingsManager
        pluginDir = SettingsManager.getInstance().getPropertyByName2(LauncherConstants.PluginDirectory, "plugin/");
    }

    public ArrayList<RepoPlugin> getRepoPlugins() {
        return installedPlugins;
    }

    public RepoPlugin getPluginByName(String name) {
        for (RepoPlugin pl : installedPlugins) {
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

    public void install(Plugin plugin, String version) {
        Properties props = null;
        for (RepoPlugin repoPlugin : installedPlugins) {
            //TODO for all repositories from settings
            //TODO need to set setting for plugin from XMLSettingsManager
            String url = plugin.getPluginVersions().get(version);
            try {
                props = repoPlugin.getPluginObj().getDefaultProperties();
                repoPlugin.getPluginObj().connect(props.getProperty("user"), props.getProperty("pass"), props.getProperty("URL"));
                FileDriver.getInstance().installFile("./plugins", plugin.getPluginSimpleName(), version, plugin.getPluginSimpleName() + ".jar", repoPlugin.getPluginObj().getPluginIS(plugin.getPluginSimpleName(), version));

            } catch (Exception e) {
                System.out.println("RepoClient connecting: " + e.getLocalizedMessage());
            }

        }
    }
}
