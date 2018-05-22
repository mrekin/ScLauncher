package ru.mrekin.sc.launcher;

import ru.mrekin.sc.launcher.core.*;
import ru.mrekin.sc.launcher.gui.LauncherGui;
import ru.mrekin.sc.launcher.tools.ApplicationTools;
import ru.mrekin.sc.launcher.update.AutoUpdater;

/**
 * Created by MRekin on 30.07.2014.
 */
public class Launch {


    public static void main(String[] args) {
        //SetUp logging
        SCLogger.getInstance();

        if (ApplicationTools.isAlreadyRunning()) {
            System.exit(0);
        }

        boolean isStart = true;
        if (args.length > 0) {
            //execute tools
            isStart = ApplicationTools.execute(args);
        }
//      LauncherGui.getInstance();
        if (isStart) {
            //launch

            SettingsManager.updateLocalSettings();
            SettingsManager.getInstance();
            //LauncherGui.getInstance();
            LauncherGui.getInstance().launch();

            Thread th = new Thread() {
                @Override
                public void run() {
                    PluginManager.getInstance().checkNewPluginVersions();
                    ApplicationTools.prepareToStart();
                    AppManager.getInstance();
                }
            };
            th.start();


            //PluginManager.getInstance().checkNewPluginVersions();

 /*           SettingsManager.updateLocalSettings();
            SettingsManager.getInstance();
            ApplicationTools.prepareToStart();
            AppManager.getInstance();
            PluginManager.getInstance().checkNewPluginVersions();
*/
            //Checking if need to update launcher
            if ("true".equals(SettingsManager.getInstance().getPropertyByName(LauncherConstants.AutoUpdaterEnabled, "true"))) {
                String version = AutoUpdater.checkForUpdates();
                if (version != null) {
                    AutoUpdater.update(version);
                }
            }

            AppManager.getInstance().loadLocalAppInfo();
            //Launching launcher :)


            AppManager.getInstance().updateAppList();

        }
    }


}
