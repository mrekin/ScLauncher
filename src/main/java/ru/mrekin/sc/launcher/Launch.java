package ru.mrekin.sc.launcher;

import ru.mrekin.sc.launcher.core.AppManager;
import ru.mrekin.sc.launcher.core.LauncherConstants;
import ru.mrekin.sc.launcher.core.SettingsManager;
import ru.mrekin.sc.launcher.gui.LauncherGui;
import ru.mrekin.sc.launcher.plugin.PluginManager;
import ru.mrekin.sc.launcher.tools.ApplicationTools;
import ru.mrekin.sc.launcher.update.AutoUpdater;

/**
 * Created by MRekin on 30.07.2014.
 */
public class Launch {

    public static void main(String[] args) {

        if(args.length>0){
            //execute tools
            ApplicationTools.execute(args);
        }else {
            //launch
            SettingsManager.updateLocalSettings();
            SettingsManager.getInstance();
            PluginManager.getInstance();
            AppManager.getInstance();

            //Checking if need to update launcher
            if ("true".equals(SettingsManager.getPropertyByName(LauncherConstants.AutoUpdaterEnabled, "true"))) {
                String version = AutoUpdater.checkForUpdates();
                if (version != null) {
                    AutoUpdater.update(version);
                }
            }

            //Loading plugins
            PluginManager pm = PluginManager.getInstance();
            pm.loadProperties();
            pm.loadInstalledPlugins();
            pm.loadAvaliablePlugins();

            //Launching launcher :)
            LauncherGui gui = new LauncherGui();
        }
    }

}
