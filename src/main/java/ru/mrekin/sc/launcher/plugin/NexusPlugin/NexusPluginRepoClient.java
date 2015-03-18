package ru.mrekin.sc.launcher.plugin.NexusPlugin;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import ru.mrekin.sc.launcher.plugin.IPluginRepoClient;
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
 * Created by xam on 21.01.2015.
 */
public class NexusPluginRepoClient implements IPluginRepoClient {

    ArrayList<Plugin> pluginsList = null;
    private static NexusPluginRepoClient instance = null;

    private String user, pass, serverURL;
    private String name = "NexusRepo";
    private String version = "1.0.0";

    public NexusPluginRepoClient() {

    }

    public void connect(String user, String pass, String serverURL) throws Exception {
        this.user = user;
        this.pass = pass;
        this.serverURL = serverURL;
        loadPluginList(serverURL);
    }


    private ArrayList<NexusXmlResources> getNexusXmlResources(String user, String pass, String serverURL) throws Exception {

        ArrayList<NexusXmlResources> resources = new ArrayList<NexusXmlResources>();
        HttpURLConnection connection = null;
        URL serverAddress;
        serverAddress = new URL(serverURL);
        //set up out communications stuff
        connection = null;
        //Set up the initial connection
        connection = (HttpURLConnection) serverAddress.openConnection();
        connection.setRequestMethod("GET");
        connection.setDoOutput(true);
        connection.setReadTimeout(10000);
        connection.connect();
        //ByteArrayInputStream bais = (ByteArrayInputStream)connection.getInputStream();
        InputStream bais = connection.getInputStream();

        //Get the DOM Builder Factory
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setIgnoringComments(true);
        factory.setIgnoringElementContentWhitespace(true);
        //Get the DOM Builder
        DocumentBuilder builder = factory.newDocumentBuilder();

        //Load and Parse the XML document
        //document contains the complete XML as a Tree.
        Document document = builder.parse(bais);
        NodeList contentItems = document.getElementsByTagName("content-item");
        if (contentItems != null && contentItems.getLength() > 0) {
            for (int i = 0; i < contentItems.getLength(); i++) {
                Element contentItem = (Element) contentItems.item(i);

                NexusXmlResources nexusXmlResources = new NexusXmlResources();
                NodeList items = contentItem.getElementsByTagName("text");
                if (items != null && items.getLength() == 1) {
                    nexusXmlResources.setText(items.item(0).getTextContent());
                }
                items = contentItem.getElementsByTagName("leaf");
                if (items != null && items.getLength() == 1) {
                    nexusXmlResources.setLeaf(items.item(0).getTextContent());
                }
                items = contentItem.getElementsByTagName("lastModified");
                if (items != null && items.getLength() == 1) {
                    nexusXmlResources.setLastModified(items.item(0).getTextContent());
                }
                items = contentItem.getElementsByTagName("resourceURI");
                if (items != null && items.getLength() == 1) {
                    nexusXmlResources.setResourceURI(items.item(0).getTextContent());
                }
                resources.add(nexusXmlResources);
            }
        }
        connection.disconnect();
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
        properties.setProperty("URL", "http://sclauncher.ru/nexus/service/local/repositories/releases/content/ru/mrekin/sc/scl-plugins");
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

        ArrayList<NexusXmlResources> pluginsList = getNexusXmlResources(user, pass, URL);
        for (NexusXmlResources pl : pluginsList) {
            if (pl.getLeaf().equals(Boolean.FALSE.toString())) {
                Plugin plugin = new Plugin();
                plugin.setInstalled(false);
                plugin.setPluginName(pl.getText());
                plugin.setPluginSimpleName(pl.getText());
                ArrayList<NexusXmlResources> versionList = getNexusXmlResources(user, pass, pl.getResourceURI());
                HashMap<String, String> pluginVersions = new HashMap<String, String>();
                for (NexusXmlResources ver : versionList) {
                    if (ver.getLeaf().equals(Boolean.FALSE.toString())) {
                        //HashMap<String,String> version = new HashMap<String, String>();
                        ArrayList<NexusXmlResources> jarList = getNexusXmlResources(user, pass, ver.getResourceURI());
                        for (NexusXmlResources jar : jarList) {
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
