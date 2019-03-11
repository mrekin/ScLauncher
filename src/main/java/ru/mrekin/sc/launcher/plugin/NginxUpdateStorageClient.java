package ru.mrekin.sc.launcher.plugin;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.mrekin.sc.launcher.core.ISCLogger;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;

public class NginxUpdateStorageClient implements IUpdateStorageClient, ISCLogger {

    private String user = null;
    private String password = null;
    private String serverURL = null;
    private String appName = null;
    private Connection connection = null;
    private Properties props = null;

    @Override
    public void loadProperties(Properties props) {
        this.props = props;
    }

    @Override
    public boolean connect() throws Exception {
        log("Connecting using: " + props.getProperty("user") + ", " + props.getProperty("serverURL"));
        this.user = props.getProperty("user");
        this.password = props.getProperty("password");
        this.serverURL = props.getProperty("serverURL");
        this.appName = props.getProperty("appName");

        URL serverAddress = new URL(serverURL);

        connection = Jsoup.connect(serverURL);
        return true;
    }

    @Override
    public boolean disconnect() throws Exception {
        if (connection != null) {
            connection = null;
        }
        return true;
    }

    @Override
    public ArrayList<String> getVersionsList() throws Exception {
        ArrayList<String> versions = new ArrayList<>();

        String itemsSelector = "a[href$='/'], a[href$='.jar'], a[href$='.txt']";
        log("Getting data from server: " + serverURL);
        Document doc = Jsoup.connect(serverURL).get();
        Elements links = doc.select(itemsSelector);
        log("Connected. Checking versions");
        for (Element e : links) {
            String version = e.text().replace("/", "");
            if (version.matches("[0-9][0-9\\.]+")) {
                versions.add(version);
                log("   Found: " + version);
            }
        }
        return versions;
    }

    @Override
    public String getPluginName() {
        return "NginxUpdateStorageClient";
    }

    @Override
    public String getPluginVersion() {
        return "1.0.0";
    }

    @Override
    public InputStream getFile(String version) throws Exception {

        return new java.net.URL(serverURL + "/" + version + "/" + appName + "-" + version + ".jar").openStream();
    }

    @Override
    public String getReleaseNotes(String version) throws Exception {
        return null;
    }

    @Override
    public Properties getDefaultProperties() {
        Properties defProps = new Properties();
        defProps.setProperty("user", "");
        defProps.setProperty("password", "");
        defProps.setProperty("serverURL", "http://localhost:80/sclauncher");
        return defProps;
    }

}
