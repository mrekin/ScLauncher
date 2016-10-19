package ru.mrekin.sc.launcher.plugin.ApacheListingDirectory;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.NodeList;
import ru.mrekin.sc.launcher.plugin.IPluginRepoClient;
import ru.mrekin.sc.launcher.plugin.NexusPlugin.NexusXmlResources;
import ru.mrekin.sc.launcher.plugin.Plugin;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

/**
 * Created by MRekin on 18.10.2016.
 */
public class ApacheListingDirectoryRepoClient implements IPluginRepoClient {

    ArrayList<Plugin> pluginsList = null;
    private static ApacheListingDirectoryRepoClient instance = null;

    private String user, pass, serverURL;
    private String name = "ApacheDirectoryListRepo";
    private String version = "1.0.0";

    public ApacheListingDirectoryRepoClient() {

    }

    public void connect(String user, String pass, String serverURL) throws Exception {
        this.user = user;
        this.pass = pass;
        this.serverURL = serverURL;
        loadPluginList(serverURL);
    }


    private ArrayList<ApacheListResources> getXmlResources(String user, String pass, String serverURL) throws Exception {

        String itemsSelector = "tr:has(img[src*='folder']), tr:has(a[href$='.jar'])";

        Document doc = Jsoup.connect(serverURL).get();
        Elements folders = doc.select(itemsSelector);
        ArrayList<ApacheListResources> resources = new ArrayList<ApacheListResources>(1);

        //     NodeList contentItems = document.getElementsByTagName("content-item");
        if (folders != null && folders.size() > 0) {
            for (int i = 0; i < folders.size(); i++) {
                Element element = (Element) folders.get(i);

                ApacheListResources apacheListResources = new ApacheListResources();
                Elements items = element.select("a");
                if (items != null && items.size() == 1) {
                    apacheListResources.setText(items.get(0).text().endsWith("/") ? items.get(0).text().replace("/", "") : items.get(0).text());
                }
                items = element.select("a");
                if (items != null && items.size() == 1) {
                    apacheListResources.setLeaf(items.get(0).text().endsWith("/") ? "false" : "true");
                }
                items = element.select("td:nth-child(3)");
                if (items != null && items.size() == 1) {
                    apacheListResources.setLastModified(items.get(0).text());
                }
                items = element.select("a");
                if (items != null && items.size() == 1) {
                    apacheListResources.setResourceURI(items.get(0).attr("abs:href"));
                }

                resources.add(apacheListResources);
            }
        }
        return resources;
    }

    public List<Plugin> getPluginsList() {
        return pluginsList;
    }

    public InputStream getPluginIS(String pluginSimpleName, String version) throws Exception {

        URL serverAddress = null;
        for (Plugin pl : pluginsList) {
            if (pl.getPluginSimpleName().equals(pluginSimpleName) || pl.getPluginVersions().containsKey(version)) {
                serverAddress = new URL(pl.getPluginVersions().get(version));
                break;
            }
        }
        if (serverAddress == null) {
            System.out.println("No such plugin :" + pluginSimpleName);
            return null;
        }
        HttpURLConnection connection = null;
        //set up out communications stuff
        //Set up the initial connection
        connection = (HttpURLConnection) serverAddress.openConnection();
        connection.setRequestMethod("GET");
        connection.setDoOutput(true);
        connection.setReadTimeout(100000);
        connection.connect();
        //ByteArrayInputStream bais = (ByteArrayInputStream)connection.getInputStream();
        return connection.getInputStream();
    }

    @Override
    public Properties getDefaultProperties() {
        Properties properties = new Properties();
        properties.setProperty("user", "");
        properties.setProperty("pass", "");
        //properties.setProperty("URL", "http://192.168.1.104:8080/nexus/service/local/repositories/releases/content/ru/mrekin/sc/scl-plugins");
        properties.setProperty("URL", "http://sclauncher.ru/ftprepo/ru/mrekin/sc/scl-plugins/");
        return properties;
    }

    @Override
    public String getRepoPluginName() {
        return name;
    }

    @Override
    public String getRepoPluginVersion() {
        return version;
    }


    private void loadPluginList(String URL) throws Exception {
        ArrayList<Plugin> plugins = new ArrayList<Plugin>();

    /*  Repo structure means
        pluginA -   1.0.0 -     jar
                    1.1.0   -    jar
        pluginB -   1.0.0 -     jar
                    1.1.0   -    jar


        1. Load list of plugins (parse XML at URL
        2. For each plugin get versions
        3. For each version get jar URI
*/

        ArrayList<ApacheListResources> pluginsList = getXmlResources(user, pass, URL);
        for (ApacheListResources pl : pluginsList) {
            if (pl.getLeaf().equals(Boolean.FALSE.toString())) {
                Plugin plugin = new Plugin();
                plugin.setInstalled(false);
                plugin.setPluginName(pl.getText());
                plugin.setPluginSimpleName(pl.getText());
                ArrayList<ApacheListResources> versionList = getXmlResources(user, pass, pl.getResourceURI());
                HashMap<String, String> pluginVersions = new HashMap<String, String>();
                for (ApacheListResources ver : versionList) {
                    if (ver.getLeaf().equals(Boolean.FALSE.toString())) {
                        //HashMap<String,String> version = new HashMap<String, String>();
                        ArrayList<ApacheListResources> jarList = getXmlResources(user, pass, ver.getResourceURI());
                        for (ApacheListResources jar : jarList) {
                            if (jar.getText().endsWith(".jar") && jar.getLeaf().equals(Boolean.TRUE.toString())) {
                                pluginVersions.put(ver.getText(), jar.getResourceURI());
                            }
                        }

                    }
                    plugin.setPluginVersions(pluginVersions);

                }
                plugins.add(plugin);
            }
        }
        this.pluginsList = plugins;
    }
}
