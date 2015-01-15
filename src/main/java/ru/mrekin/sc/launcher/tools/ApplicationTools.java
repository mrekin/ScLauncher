package ru.mrekin.sc.launcher.tools;

import java.io.File;
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
}
