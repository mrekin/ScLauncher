package ru.mrekin.sc.launcher.update;

import ru.mrekin.sc.launcher.core.*;
import ru.mrekin.sc.launcher.plugin.IUpdateStorageClient;
import ru.mrekin.sc.launcher.plugin.NginxUpdateStorageClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.CopyOption;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarFile;

/**
 * Created by MRekin on 25.08.2014.
 */
public class AutoUpdaterV2 {

    private static JTextArea ta = null;
    //private static String serverURL = "http://bssdev:8888/SCLauncher/";
    private static String serverURL, appURL, serverVersion, currentVersion;
    private static ArrayList<String> serverURLs = new ArrayList<>();
    private static ArrayList<IUpdateStorageClient> usc = new ArrayList<>();
    private static ArrayList<String> versions = new ArrayList<>();
    private static AutoUpdaterV2 instance = null;


    public static AutoUpdaterV2 getInstance(){
        if(instance==null){
            instance = new AutoUpdaterV2();
        }
        return instance;
    }

    public static String checkForUpdates() {
        log("Checking for new versions");
        if ("false".equals(SettingsManager.getInstance().getPropertyByName(LauncherConstants.AutoUpdaterDevMode, "false"))) {
            serverURL = SettingsManager.getInstance().getPropertyByName(LauncherConstants.AutoUpdaterServerURL);
        } else {
            serverURL = SettingsManager.getInstance().getPropertyByName(LauncherConstants.AutoUpdaterServerURL) + "devmode/";
        }

        ArrayList<Class<?>> cls = findClassByInterface(IUpdateStorageClient.class);
        log("Found UpdateStorageClients: "+ cls.size());
        for (Class cl : cls) {
            try {
                IUpdateStorageClient instance = ((IUpdateStorageClient) cl.newInstance());

                Properties props = instance.getDefaultProperties();
                props.setProperty("appName",SettingsManager.getInstance().getPropertyByName("Application.name", "sc-launcher"));
                instance.loadProperties(props);
                try {
                    instance.connect();
                }catch (Exception e){
                    log(e.getLocalizedMessage());
                    log(e.getCause().toString());
                    continue;
                }
                ArrayList<String> vs = instance.getVersionsList();
                vs.removeAll(versions);
                versions.addAll(vs);
                usc.add(instance);
            } catch (Exception e) {
                log(e.getLocalizedMessage());
                log("Something goes wrong..");
            }
        }
        try {
            log("Avaliable versions: "+ String.valueOf(versions));
            serverVersion = IUpdateStorageClient.getLatestVersion(versions);
            // System.out.println(sb.toString());
        } catch (Exception e) {
            log(e.getLocalizedMessage());
        }
        currentVersion = SettingsManager.getInstance().getPropertyByName("Application.version");

        if (PluginManager.compareVersions(currentVersion, serverVersion) == -1) {
            return serverVersion;
        } else {
            return null;
        }

    }

    private InputStream getFISbyVersion(String version){
        InputStream is =null;
        if(usc==null||usc.size()==0){
            log("");
        }
        for(IUpdateStorageClient ius : usc){
            try {
                if (ius.getVersionsList().contains(version)) {
                    is = ius.getFile(version);
                    break;
                }
            }catch (Exception e){
                log(e.getLocalizedMessage());
            }
        }
        return is;
    }

    public void update(String version) {
        //loadSettings();
        String appName = SettingsManager.getInstance().getPropertyByName("Application.name", "sc-launcher");
        String fileName = appName + "-" + version + ".jar";
        FileDriver.getInstance().installFile("update",appName,version,fileName, getFISbyVersion(version));

        String name = AutoUpdaterV2.class.getCanonicalName().replace(".", "/") + ".class";
        String packagePath = AutoUpdaterV2.class.getPackage().getName().replace(".", "/");
        String packageName = AutoUpdaterV2.class.getPackage().getName();
        //String pluginsDir = "plugins";
        String pluginsDir = SettingsManager.getInstance().getPropertyByName(LauncherConstants.PluginDirectory, "plugins/");
        String pluginName = "AutoUpdater";
        String pluginDir = "./" + pluginsDir + pluginName + "/";
        String fullPath = pluginDir + name;
        URL pr = AutoUpdaterV2.class.getClassLoader().getResource(name);
        try {
            /*
            File plugin = new File(pluginDir + packagePath);
            if (!plugin.exists() || !plugin.isDirectory()) {
                boolean b = plugin.mkdirs();

            }
            */
            installUpdaterClass(name, fullPath);

            String appURL = serverURL + appName + "-" + version + ".jar";
            String command = "java -cp " + pluginDir + " " + packageName + "." + AutoUpdaterV2.class.getSimpleName() + " " + version + " " + appName;
            log(command);
            Runtime.getRuntime().exec(command);

        } catch (Exception use) {
            use.printStackTrace();
        }

        System.exit(0);
    }

    private static void installUpdaterClass(String pathInJar, String pathInPluginDirectory) {
        try {
            URL pr = AutoUpdaterV2.class.getClassLoader().getResource(pathInJar);
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

        if (args.length != 2) {
            log("Usage: <version> <AppName>");
            System.exit(1);
        }
        String version = args[0];
        String appName = args[1];
        String newFileName = appName+"-"+version+".jar";

        System.out.println("New launcher version avaliable: " + args[0]);
        File file = new File(".");
        File newFile = new File("update/"+appName+"/"+version+"/"+newFileName);

        System.out.println("Launcher directory: " + file.getAbsolutePath());
        System.out.println("Searching jar files..");


        if (newFile!=null&&newFile.exists()&&!newFile.isDirectory()) {
            if(newFile.length()==0){
                return;
            }
        }else{
            log("Update not found in update directory");
            return;
        }

        System.out.println("Installing new version: "+version);

        try {
            java.nio.file.Files.move(new File(appName+".jar").toPath(),new File(appName+".jar.bak").toPath(), StandardCopyOption.REPLACE_EXISTING);
            java.nio.file.Files.move(newFile.toPath(), new File(appName+".jar").toPath(), StandardCopyOption.REPLACE_EXISTING);

            Runtime.getRuntime().exec("java -jar " + appName + ".jar --forceSettings");
        }catch (IOException ioe){
            System.out.println(ioe.getLocalizedMessage());
        }
        System.exit(0);
    }


    private static void log(String text) {
        SCLogger.getInstance().log(AutoUpdaterV2.class.getName(),"INFO",text);
    }


    /**
     * Returns all classes implementing interfece from Classloader and plugin directory
     *
     * @param iface
     * @return
     */
    private static ArrayList<Class<?>> findClassByInterface(Class iface) {
        ArrayList<Class<?>> result = new ArrayList<>();
        Class[] loadedClasses = null;
        try {
            loadedClasses = getAllClassesFromPackage("ru.mrekin");
            log(String.valueOf(loadedClasses));
        } catch (Exception e) {
            log(e.getLocalizedMessage());
        }
        for (Class c : loadedClasses) {
            if (iface.isAssignableFrom(c))
                result.add(c);
        }

        String pluginDir = SettingsManager.getInstance().getPropertyByName(LauncherConstants.PluginDirectory, "plugin/");
        ArrayList<File> jars = new ArrayList<>();
        try {
            jars = FileDriver.listFiles(pluginDir, ".jar");
        } catch (IOException ioe) {
            log(ioe.getLocalizedMessage());
        }
        for (File f : jars) {
            try {
                URL jarURL = f.toURI().toURL();
                JarFile jf = new JarFile(f);
                Class cl = PluginRepoManager.findClassByInterface(jf, jarURL, iface);
                if (cl == null) {
                    continue;
                } else {
                    result.add(cl);
                }

            } catch (Exception e) {
                log(e.getLocalizedMessage());
            }

        }
        return result;
    }


    public static Class[] getAllClassesFromPackage(final String packageName) throws ClassNotFoundException, IOException {
        //ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        //ClassLoader classLoader = AutoUpdaterV2.class.getClassLoader();
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        //TODO hack for loading class. Need to resolve this problem
        NginxUpdateStorageClient ccc = new NginxUpdateStorageClient();
        classLoader.loadClass(NginxUpdateStorageClient.class.getCanonicalName());
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<File>();
        
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
            log("Dirs: "+resource.getFile());
        }
        ArrayList<Class> classes = new ArrayList<Class>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        log(String.valueOf(classes));
        return classes.toArray(new Class[classes.size()]);
    }

    /**
     * Find file in package.
     *
     * @param directory
     * @param packageName
     * @return
     * @throws ClassNotFoundException
     */
    public static List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
        log(directory.getAbsolutePath());
        List<Class<?>> classes = new ArrayList<Class<?>>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        log("Classes: "+ String.valueOf(files));
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }
}
