package ru.mrekin.sc.launcher.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mrekin.sc.launcher.core.*;
import ru.mrekin.sc.launcher.gui.TrayPopup;
import ru.mrekin.sc.launcher.plugin.Plugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.util.ArrayList;

/**
 * Created by xam on 24.09.2014.
 */
public class ApplicationTools {
    final static Logger logger = LoggerFactory.getLogger(ApplicationTools.class);
    static ArrayList<Command> commands = new ArrayList<Command>();
    static Command cmd = new Command();

    private static void log(String msg) {
        SCLogger.getInstance().log(AppManager.class.getName(), "INFO", msg);
    }

    public static boolean execute(String[] args) {
        log("Starting execute command");
        ArrayList<String> arguments = new ArrayList<String>(1);


        if (args.length > 0) {

            for (int i = 0; i < args.length; i++) {
                if (args[i].startsWith("--")) {
/*                    if(!"".equals(cmd.command) && cmd.command != null){
                        commands.add(cmd);
                    }
  */
                    cmd = new Command();
                    commands.add(cmd);
                    cmd.command = args[i];
                    log("Command: " + cmd.command);
                } else {
                    cmd.commandArgs.add(args[i]);
                }


            }
        } else {
            return false;
        }

// Prepare directories for SCL usage

        for (Command cmd : commands) {
            // prepare application for SClauncher
            if ("--prepare".equals(cmd.command)) {
                log("--prepare found");
                if (arguments.size() == 0) {
                    log("No arguments found");
                    return true;
                }
                if ("app".equals(arguments.get(0)) || ("application".equals(arguments.get(0)))) {
                    log("Start prepeare application");
                    ApplicationPrepare.appPrepare("./");
                    return false;
                }

                // force update settings (on SCLauncher update usually)
            } else if ("--forceSettings".equals(cmd.command)) {
                System.setProperty("ru.mrekin.sc.sclauncher.forceSettings", "true");
                //     return true;
                // delete plugin on restart
            } else if ("--deletePlugin".equals(cmd.command)) {
                if (cmd.commandArgs.size() == 0 || cmd.commandArgs.get(0) == null) {
                    log("Plugin name for delete not specified!");
                    return true;
                }
                if (PluginManager.remove(cmd.commandArgs.get(0))) {
                    log("Plugin " + cmd.commandArgs.get(0) + " removed");
                }
                //       return true;
            } else if ("--installPlugin".equals(cmd.command)) {
                if (cmd.commandArgs.size() == 0 || cmd.commandArgs.get(0) == null) {
                    log("Plugin name for install not specified!");
                    return true;
                }
                PluginManager.getInstance().getAllPlugins();
                Plugin pl = PluginManager.getInstance().getPluginByName(cmd.commandArgs.get(0));
                PluginManager.getInstance().install(pl, cmd.commandArgs.get(1));
                //     return true;
            }

        }

        return true;
    }

    public static void prepareToStart() {
        log("Method: prepareToStart");
        installMandatoryPlugins();
    }

    public static void removeMarkedPlugins() {

    }

    public static void installMandatoryPlugins() {
        log("Method: installMandatoryPlugins");
        boolean installed = false;
        boolean avaliable = false;
        String[] mandatoryPluginsArray = SettingsManager.getInstance().getPropertiesArrayByName(LauncherConstants.MandatoryPlugins);
        for (String mPlugin : mandatoryPluginsArray) {
            log("[Mandatory plugin installer] Starting to check plugin: " + mPlugin);
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
                    if (plugin.getLatestVersion().equals("-1")) {
                        log("[Mandatory plugin installer] Latest version unknown. Possible repository not reachable.");
                    }
                    if (plugin.isInstalled() && PluginManager.compareVersions(plugin.getPluginVersion(), plugin.getLatestVersion()) < 0) {
                        log("[Mandatory plugin installer] Plugin " + plugin.getPluginSimpleName() + "is intalled. Break.");
                        break;
                    }
                }
            }
            if (installed) {
                continue;
            }
            if (!PluginManager.getInstance().isAvaliablePluginsLoaded()) {
                log("[Mandatory plugin installer] Begin of loadAvaliablePlugins");
                PluginManager.getInstance().loadAvaliablePlugins();
                log("[Mandatory plugin installer] End of loadAvaliablePlugins");
            }
            for (Plugin plugin : PluginManager.getInstance().getAllPlugins()) {
                if (!plugin.isInstalled() && plugin.getPluginSimpleName().equals(plName)) {
                    plVersion = "".equals(plVersion) ? plugin.getLatestVersion() : plVersion;
                    log("[Mandatory plugin installer] Plugin " + plName + " not installed. Installing version " + plVersion);
                    PluginManager.getInstance().install(plugin, plVersion);
                    log("[Mandatory plugin installer] Plugin " + plName + ":" + plVersion + " installed");
                    TrayPopup.displayMessage("Mandatory plugin " + plName + " installed\n It may require restart.");
                    avaliable = true;
                    break;
                }
            }

            if (!avaliable) {
                log("[Mandatory plugin installer] Plugin '" + plName + "' not avaliable. Please check plugin name or repository connections.");
            }

        }

    }

    static class Command {
        public String command;
        public ArrayList<String> commandArgs = new ArrayList<String>();
    }

    public static boolean isAlreadyRunning() {
        try {
            final RandomAccessFile file = new RandomAccessFile(new File("lock"), "rw");
            final FileLock lock = file.getChannel().tryLock();
            System.out.println("Got the lock? " + (null != lock));
            if (null == lock) {
                return true;
            }
        } catch (FileNotFoundException e) {
            return false;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
