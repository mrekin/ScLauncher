package ru.mrekin.sc.launcher.core;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by MRekin on 01.08.2014.
 */
public class Application {

    private String appName = "";
    private String appVersion = "";
    private String appTitle = "";
    private String appPath = "";
    private ArrayList<String> appVersions = new ArrayList<String>(1);


    public Application() {
        super();

    }

    public String getAppName() {
        return this.appName;
    }

    ;

    public void setAppName(String name) {
        this.appName = name;
    }

    ;

    public String getAppVersion() {
        return this.appVersion;
    }

    ;

    public void setAppVersion(String version) {
        this.appVersion = version;
    }

    public ArrayList<String> getAppVersions() {
        return this.appVersions;
    }

    ;

    public void setAppVersions(ArrayList<String> versions) {
        this.appVersions = versions;
    }

    ;

    public String getAppLastVersion() {
        if (this.appVersions != null && this.appVersions.size() != 0) {
            Collections.sort(this.appVersions);

            return this.appVersions.get(this.appVersions.size() - 1);
        } else {
            return "-1";
        }
    }

    ;

    public String getAppPath() {
        return this.appPath;
    }

    ;

    public void setAppPath(String path) {
        this.appPath = path;
    }

    ;

    public String getAppTitle() {
        return this.appTitle;
    }

    ;

    public void setAppTitle(String title) {
        this.appTitle = title;
    }

    ;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Application)) {
            return false;
        } else {
            return getAppName().equals(((Application) o).getAppName());
        }
    }
}