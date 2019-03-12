package ru.mrekin.sc.launcher.core;

import org.apache.commons.io.FileUtils;
import ru.mrekin.sc.launcher.plugin.NginxUpdateStorageClient;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by MRekin on 31.07.2014.
 */
public class FileDriver implements ISCLogger {

    private ArrayList<Application> appList;

    private static FileDriver instance = null;
    // private String appRoot = "./apps";
    private String appRoot = LauncherConstants.WorkingDirectory + SettingsManager.getInstance().getPropertyByName(LauncherConstants.ApplicationDirectory, "./apps");

    private FileDriver() {
        loadAppsSettings();
        instance = this;
    }

    public static FileDriver getInstance() {
        if (instance != null) {

            return instance;
        } else {
            return new FileDriver();
        }

    }

    public static ArrayList<File> listFiles(String path, String nameContains) throws IOException {

        ArrayList<File> apps = new ArrayList<File>(1);

        File file = new File(path);

        File[] files = file.listFiles();
        if (files != null) {

            for (File f : files) {
                if (f.isDirectory()) {
                    apps.addAll(listFiles(f.getPath(), nameContains));
                } else if (f.getName().contains(nameContains) && f.isFile()) {
                    apps.add(f);
                }
            }
        }
        return apps;
    }

    public CopyOnWriteArrayList<Application> getAppList() {
        return new CopyOnWriteArrayList(this.appList);
    }

    public void loadAppsSettings() {
        appList = new ArrayList<Application>(1);
        ArrayList<File> apps = new ArrayList<File>(1);
        File file = new File(appRoot);

        log(file.getAbsolutePath());
        if (!file.exists() || !file.isDirectory()) {
            try {

                boolean f = file.mkdir();
            } catch (Exception ioe) {
                log("Can't create apps directory: " + ioe.getLocalizedMessage());
            }

        } else {
            try {
                //TODO !!! will not work with old version. Need to do something
                apps = listFiles(file.getPath(), "app.properties");
                for (File f : apps) {

                    String propertyFile = f.getParent() + "/" + LauncherConstants.PropertiesFileName;
                    File pf = new File(propertyFile);
                    if (!pf.exists() || pf.isDirectory()) {

                        try {
                            JarFile jar = new JarFile(f);
                            Attributes attr = jar.getManifest().getMainAttributes();
                            Application apl = new Application();
                            String appName = attr.getValue("appName");
                            String appTitle = attr.getValue("appTitle");
                            String appVersion = attr.getValue("appVersion");
                            if (appName == null) appName = "";
                            if (appTitle == null) appTitle = "";
                            if (appVersion == null) appVersion = "";
                            apl.setAppName(appName);
                            //apl.setAppTitle(appTitle);
                            apl.setAppVersion(appVersion);
                            apl.setAppPath(f.getPath());
                            apl.setInstalled(true);
                            appList.add(apl);
                            jar.close();
                        } catch (IOException ioe) {
                            log("Can't open file: " + f.getAbsolutePath() + ", error: " + ioe.getLocalizedMessage());
                        }
                    } else {
                        Properties attr = new Properties();
                        FileInputStream fis = new FileInputStream(propertyFile);
                        attr.load(fis);
                        fis.close();
                        Application apl = new Application();
                        String appName = attr.getProperty(LauncherConstants.ApplicationName, "");
                        String appVersion = attr.getProperty(LauncherConstants.ApplicationVersion, "");
                        String appType = attr.getProperty(LauncherConstants.ApplicationType, "");
                        String appExecFile = attr.getProperty(LauncherConstants.ApplicationExecFile, "");

                        apl.setAppName(appName);
                        apl.setAppVersion(appVersion);
                        apl.setExecFile(appExecFile);
                        apl.setAppPath(f.getParentFile().getName());
                        apl.setAppType(appType);
                        apl.setInstalled(true);
                        appList.add(apl);
                    }
                }


            } catch (IOException ioe) {
                log("Can't list files: " + ioe.getLocalizedMessage());
            }
        }


    }

    public FileOutputStream installApp(String name, String version) {
        try {
            String appDir = appRoot + "/" + name;
            String appName = name + "-" + version + ".jar";
            File appD = new File(appRoot + "/" + name);
            if (!appD.exists() || !appD.isDirectory()) {
                try {

                    boolean f = appD.mkdirs();
                } catch (Exception ioe) {
                    log("Can't create app directory: " + ioe.getLocalizedMessage());
                }
            }

            return new FileOutputStream(appDir + "/" + appName);
        } catch (Exception e) {
            e.printStackTrace();
            return null;

        }
    }

    public boolean installFile(String folder, String appPath, String version, String fileName, InputStream is) {
        try {
            String appDir = "";
            if ("".equals(folder) || folder == null) {
                appDir = appRoot + "/" + appPath;
            } else {
                appDir = folder + "/" + appPath + "/" + version;
            }

            fileName = appDir + "/" + fileName;
            File file = new File(fileName);
            if (!file.exists() || !file.isFile()) {
                boolean b = file.mkdirs();
                b = file.delete();
            }
            FileOutputStream fos = new FileOutputStream(file);
            int c = 0;

            byte[] array = new byte[1048576];
            while ((c = is.read(array)) != -1) {
                fos.write(array, 0, c);
            }
            fos.close();
            is.close();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean deleteFile(String path) {

        File f = new File(path);

        try {
            if (!f.exists()) {
                return false;
            }
            if (f.isDirectory()) {
                // FileUtils.forceDeleteOnExit(f);
                FileUtils.deleteDirectory(f);
            } else {
                if (f.isFile()) {
                    FileUtils.forceDeleteOnExit(f);
                }
            }
        } catch (IOException ioe) {
            log(ioe.getLocalizedMessage());
            return false;
        }
        return true;
    }

    public static Class findClassByInterface(JarFile jar, URL jarURL, Class iface) {
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
    public static ArrayList<Class<?>> findClassByInterface(Class iface) {
        ArrayList<Class<?>> result = new ArrayList<>();
        Class[] loadedClasses = null;
        try {
            loadedClasses = getAllClassesFromPackage("ru.mrekin");
            System.out.println(String.valueOf(loadedClasses));
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
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
            System.out.println(ioe.getLocalizedMessage());
        }
        for (File f : jars) {
            try {
                URL jarURL = f.toURI().toURL();
                JarFile jf = new JarFile(f);
                Class cl = findClassByInterface(jf, jarURL, iface);
                if (cl == null) {
                    continue;
                } else {
                    result.add(cl);
                }

            } catch (Exception e) {
                System.out.println(e.getLocalizedMessage());
            }

        }
        return result;
    }


    public static Class[] getAllClassesFromPackage(final String packageName) throws ClassNotFoundException, IOException {
        //ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        //ClassLoader classLoader = AutoUpdaterV2.class.getClassLoader();
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        //TODO hack for loading class. Need to resolve this problem
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<File>();

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
            System.out.println("Dirs: " + resource.getFile());
        }
        ArrayList<Class> classes = new ArrayList<Class>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        System.out.println(String.valueOf(classes));
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
        System.out.println(directory.getAbsolutePath());
        List<Class<?>> classes = new ArrayList<Class<?>>();
        File[] files = null;
        if (!directory.exists()) {

            classes.add(NginxUpdateStorageClient.class);
            System.out.println(String.valueOf(classes));
            return classes;

        } else {
            files = directory.listFiles();
        }
        System.out.println("Directory: " + directory.length());

        System.out.println("Classes: " + files.length + ", " + String.valueOf(files));
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
