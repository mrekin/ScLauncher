package ru.mrekin.sc.launcher.tools;

import ru.mrekin.sc.launcher.core.LauncherConstants;
import ru.mrekin.sc.launcher.core.PluginManager;
import ru.mrekin.sc.launcher.core.SettingsManager;
import ru.mrekin.sc.launcher.plugin.Plugin;

import java.util.ArrayList;

/**
 * Created by xam on 24.09.2014.
 */
public class ApplicationTools {

    public static boolean execute(String[] args) {
        String command = "";
        ArrayList<String> arguments = new ArrayList<String>(1);
        if (args.length > 0) {
            command = args[0];
        } else {
            return false;
        }
        if (args.length > 1) {
            for (int i = 1; i < args.length; i++) {
                arguments.add(args[i]);
            }
        }
// Prepare directories for SCL usage
        if ("--prepare".equals(command)) {
            if (arguments.size() == 0) {
                return false;
            }
            if ("app".equals(arguments.get(0)) || ("application".equals(arguments.get(0)))) {
                ApplicationPrepare.appPrepare("./");
            }
        }

        return true;
    }

    public static void prepareToStart() {
        installMandatoryPlugins();
    }

    public static void removeMarkedPlugins() {

    }

    public static void installMandatoryPlugins() {
        boolean installed = false;
        String mandatoryPlugins = SettingsManager.getInstance().getPropertyByName(LauncherConstants.MandatoryPlugins);
        String[] mandatoryPluginsArray = mandatoryPlugins.split(",");
        for (String mPlugin : mandatoryPluginsArray) {
            String[] tempArray = mPlugin.split(":");
            String plName = "", plVersion = "";
            if (tempArray != null && tempArray.length > 0 && tempArray[0] != null) {
                plName = tempArray[0];
            }
            if (tempArray != null && tempArray.length > 1 && tempArray[1] != null) {
                plVersion = tempArray[1];
            }
            installed = false;
            for (Plugin plugin : PluginManager.getInstance().getAllPlugins()) {
                if (plugin.isInstalled() && plugin.getPluginSimpleName().equals(plName)) {
                    installed = plugin.isInstalled();
                    break;
                }
            }
            if (installed) {
                break;
            }
            if (!PluginManager.getInstance().isAvaliablePluginsLoaded()) {
                PluginManager.getInstance().loadAvaliablePlugins();
            }
            for (Plugin plugin : PluginManager.getInstance().getAllPlugins()) {
                if (!plugin.isInstalled() && plugin.getPluginSimpleName().equals(plName)) {
                    plVersion = "".equals(plVersion) ? plugin.getLatestVersion() : plVersion;
                    PluginManager.getInstance().install(plugin, plVersion);
                    break;
                }
            }

        }

    }
}
