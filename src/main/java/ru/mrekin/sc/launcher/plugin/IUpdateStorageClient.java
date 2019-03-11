package ru.mrekin.sc.launcher.plugin;

import com.github.sardine.model.Prop;
import ru.mrekin.sc.launcher.core.ISCLogger;
import ru.mrekin.sc.launcher.core.PluginManager;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Created by MRekin on 06.03.2019.
 */
public interface IUpdateStorageClient {

    public void loadProperties(Properties props);

    public boolean connect() throws Exception;

    public boolean disconnect() throws Exception;

    /**
     * Returns SCLauncher avaliable versions. Method retrieve versions list for storage on each inkove.
     *
     * @return
     * @throws Exception
     */
    public ArrayList<String> getVersionsList() throws Exception;

    public String getPluginName();

    public String getPluginVersion();

    /**
     * Returns InputStream for given SClauncher version
     *
     * @param version
     * @return
     * @throws Exception
     */
    public InputStream getFile(String version) throws Exception;

    /**
     * Returns releaseNotes for given SCLauncher version
     *
     * @param version
     * @return
     * @throws Exception
     */
    public String getReleaseNotes(String version) throws Exception;

    /**
     * Returns latest version
     *
     * @return
     */
    default public String getLatestVersion() throws Exception {
        return getLatestVersion(getVersionsList());
    }

    public static String getLatestVersion(ArrayList<String> versions) throws Exception {
        String latestVersion = null;
        try {
            for (String ver : versions) {
                //if ver > latestVersion
                if (PluginManager.compareVersions(latestVersion, ver) == -1) {
                    latestVersion = ver;
                }
            }
        } catch (Exception e) {
            throw (e);
        }
        return latestVersion;
    }

    /**
     * Returns default plugin properties (like user, password, serverURL)
     *
     * @return
     */
    public Properties getDefaultProperties();
}
