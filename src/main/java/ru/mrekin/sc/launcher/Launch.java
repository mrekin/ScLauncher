package ru.mrekin.sc.launcher;

import ru.mrekin.sc.launcher.core.AppManager;
import ru.mrekin.sc.launcher.core.LauncherConstants;
import ru.mrekin.sc.launcher.core.SettingsManager;
import ru.mrekin.sc.launcher.gui.LauncherGui;
import ru.mrekin.sc.launcher.gui.TrayPopup;
import ru.mrekin.sc.launcher.tools.ApplicationTools;
import ru.mrekin.sc.launcher.update.AutoUpdater;

import javax.swing.*;

/**
 * Created by MRekin on 30.07.2014.
 */
public class Launch {

    public static void main(String[] args) {
        try {

        } catch (Exception e) {
            e.printStackTrace();
        }
        boolean isStart = true;
        if (args.length > 0) {
            //execute tools
            isStart = ApplicationTools.execute(args);
        }
        if (isStart) {
            //launch
            SettingsManager.updateLocalSettings();
            SettingsManager.getInstance();
            ApplicationTools.prepareToStart();
            AppManager.getInstance();

            //Checking if need to update launcher
            if ("true".equals(SettingsManager.getInstance().getPropertyByName(LauncherConstants.AutoUpdaterEnabled, "true"))) {
                String version = AutoUpdater.checkForUpdates();
                if (version != null) {
                    AutoUpdater.update(version);
                }
            }


            //Launching launcher :)
            LauncherGui gui = new LauncherGui();


        }
    }

}
