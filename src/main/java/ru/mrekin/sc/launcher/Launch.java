package ru.mrekin.sc.launcher;

import ru.mrekin.sc.launcher.core.AppManager;
import ru.mrekin.sc.launcher.core.LauncherConstants;
import ru.mrekin.sc.launcher.core.PluginManager;
import ru.mrekin.sc.launcher.core.SettingsManager;
import ru.mrekin.sc.launcher.gui.LauncherGui;
import ru.mrekin.sc.launcher.tools.ApplicationTools;
import ru.mrekin.sc.launcher.update.AutoUpdater;

/**
 * Created by MRekin on 30.07.2014.
 */
public class Launch {

    public static void main(String[] args) {

        boolean isStart = true;
        if (args.length > 0) {
            //execute tools
            isStart = ApplicationTools.execute(args);
        }
//      LauncherGui.getInstance();
        if (isStart) {
            //launch
         Thread th =  new Thread(){
                @Override
                public void run() {
                    SettingsManager.updateLocalSettings();
                    SettingsManager.getInstance();
                    ApplicationTools.prepareToStart();
                    AppManager.getInstance();
                    PluginManager.getInstance().checkNewPluginVersions();

                }
            };
            th.start();

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


            //Launching launcher :)
            LauncherGui.getInstance();
            LauncherGui.getInstance().launch();

        }
    }

}
