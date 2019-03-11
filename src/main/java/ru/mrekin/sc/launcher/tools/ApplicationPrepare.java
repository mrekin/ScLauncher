package ru.mrekin.sc.launcher.tools;


import ru.mrekin.sc.launcher.core.FileDriver;
import ru.mrekin.sc.launcher.core.LauncherConstants;
import ru.mrekin.sc.launcher.core.SCLogger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;

/**
 * Created by xam on 20.09.2014.
 */
public class ApplicationPrepare {
    //TODO need to create GUI for this. Now is is simple tool for listing files and prepare some configs.

    private static void log(String msg) {
        SCLogger.getInstance().log(MethodHandles.lookup().lookupClass().getName(), "INFO", msg);
    }

    /**
     * @param dir
     */
    public static void appPrepare(String dir) {
        appPrepare(dir, "", "", "", "");
    }

    /**
     * @param dir
     * @param appName
     * @param appVersion
     * @param appType
     */
    public static void appPrepare(String dir, String appName, String appVersion, String appType, String execPath) {

        if (dir.lastIndexOf("\\") == dir.length() - 1) {
            dir = dir.substring(0, dir.length() - 1);
        }
        ArrayList<File> files = new ArrayList<File>();
        StringBuffer sb = new StringBuffer();
        File directory = new File(dir);
        if (!directory.isDirectory()) {
            log("Invalid application directory path");
        }
        try {


            File list;
            FileOutputStream fos;

            list = new File(directory.getAbsolutePath() + "/" + LauncherConstants.PropertiesFileName);
            if (!list.isFile() && !list.exists()) {
                sb = new StringBuffer();
                sb.append(LauncherConstants.ApplicationName + " = " + appName + "\n");
                sb.append(LauncherConstants.ApplicationVersion + " = " + appVersion + "\n");
                sb.append(LauncherConstants.ApplicationType + " = " + appType + "\n");
                sb.append(LauncherConstants.ApplicationExecFile + " = " + execPath);

                fos = (new FileOutputStream(list));
                fos.write(sb.toString().getBytes());
                fos.close();
            }
            sb = new StringBuffer();
            list = new File(directory.getAbsolutePath() + "/" + LauncherConstants.ReliseNotesFileName);
            list.createNewFile();

            files = FileDriver.listFiles(directory.getAbsolutePath(), "");
            for (File f : files) {
                //sb.append(f.getTotalSpace());

                sb.append(f.getAbsolutePath().replace(dir, "").replaceFirst("\\\\", "").replaceAll("\\\\", "/") + "\n");

            }

            list = new File(directory.getAbsolutePath() + "/files.list");
            //list.createNewFile();

            fos = (new FileOutputStream(list));
            fos.write(sb.toString().getBytes());
            fos.close();

            log("Pls don't forget to fill " + LauncherConstants.ReliseNotesFileName + " and " + LauncherConstants.PropertiesFileName);

        } catch (IOException ioe) {
            log(ioe.getLocalizedMessage());
        }

    }


    public static void pluginDirPrepare(String dir) {

    }

    public static void updateDirPrepare(String dir) {

    }


}
