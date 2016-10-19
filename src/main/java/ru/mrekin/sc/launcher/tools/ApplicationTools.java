package ru.mrekin.sc.launcher.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mrekin.sc.launcher.core.LauncherConstants;
import ru.mrekin.sc.launcher.core.PluginManager;
import ru.mrekin.sc.launcher.core.SettingsManager;
import ru.mrekin.sc.launcher.plugin.Plugin;

import java.util.ArrayList;

/**
 * Created by xam on 24.09.2014.
 */
public class ApplicationTools {
    final static Logger logger = LoggerFactory.getLogger(ApplicationTools.class);

    public static boolean execute(String[] args) {
        String command = "";
        logger.trace("Starting execute command");
        ArrayList<String> arguments = new ArrayList<String>(1);
        if (args.length > 0) {
            command = args[0];
            logger.trace("Command: " + command);
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
            logger.trace("--prepare found");
            if (arguments.size() == 0) {
                logger.trace("No arguments found");
                return true;
            }
            if ("app".equals(arguments.get(0)) || ("application".equals(arguments.get(0)))) {
                logger.trace("Start prepeare application");
                ApplicationPrepare.appPrepare("./");
                return false;
            }
        } else if ("--forceSettings".equals(command)) {
            System.setProperty("ru.mrekin.sc.sclauncher.forceSettings", "true");
            return true;
        }

        return true;
    }

    public static void prepareToStart() {
        logger.trace("Method: prepareToStart");
        installMandatoryPlugins();
    }

    public static void removeMarkedPlugins() {

    }

    public static void installMandatoryPlugins() {
        logger.trace("Method: installMandatoryPlugins");
        boolean installed = false;
        boolean avaliable = false;
        String[] mandatoryPluginsArray = SettingsManager.getInstance().getPropertiesArrayByName(LauncherConstants.MandatoryPlugins);
        for (String mPlugin : mandatoryPluginsArray) {
            logger.trace("Starting to check plugin: " + mPlugin);
            String[] tempArray = mPlugin.split(":");
            String plName = "", plVersion = "";
            if (tempArray != null && tempArray.length > 0 && tempArray[0] != null) {
                plName = tempArray[0];
            }
            if (tempArray != null && tempArray.length > 1 && tempArray[1] != null) {
                plVersion = tempArray[1];
            }
            installed = false;
            avaliable = false;
            for (Plugin plugin : PluginManager.getInstance().getAllPlugins()) {
                if (plugin.isInstalled() && plugin.getPluginSimpleName().equals(plName)) {
                    installed = plugin.isInstalled();
                    logger.trace("Plugin " + plugin.getPluginSimpleName() + "is intalled. Break.");
                    break;
                }
            }
            if (installed) {
                continue;
            }
            if (!PluginManager.getInstance().isAvaliablePluginsLoaded()) {
                logger.trace("Begin of loadAvaliablePlugins");
                PluginManager.getInstance().loadAvaliablePlugins();
                logger.trace("End of loadAvaliablePlugins");
            }
            for (Plugin plugin : PluginManager.getInstance().getAllPlugins()) {
                if (!plugin.isInstalled() && plugin.getPluginSimpleName().equals(plName)) {
                    plVersion = "".equals(plVersion) ? plugin.getLatestVersion() : plVersion;
                    logger.trace("Plugin " + plName + " not installed. Installing version " + plVersion);
                    PluginManager.getInstance().install(plugin, plVersion);
                    logger.trace("Plugin " + plName + ":" + plVersion + " installed");
                    avaliable = true;
                    break;
                }
            }

            if (!avaliable) {
                logger.warn("Plugin '" + plName + "' not avaliable. Please check plugin name or repository connections.");
            }

        }

    }
}
