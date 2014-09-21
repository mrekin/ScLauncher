package ru.mrekin.sc.launcher;


import ru.mrekin.sc.launcher.core.Application;
import ru.mrekin.sc.launcher.core.LauncherConstants;
import ru.mrekin.sc.launcher.core.SettingsManager;
import ru.mrekin.sc.launcher.plugin.RemoteStorageClient;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNWCUtil;
import org.tmatesoft.svn.core.wc2.SvnOperationFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Created by MRekin on 30.07.2014.
 * Will be changed on SVNPlugin later
 */
public class SvnClient implements RemoteStorageClient {

    SVNRepository svnRepository;
    private SvnOperationFactory svnOperationFactory = new SvnOperationFactory();
    private SVNClientManager svnClientManager = SVNClientManager.newInstance();
    private ArrayList<Application> svnAppList;
    private String rootPath = SettingsManager.getPropertyByName("SVNRepoURL");

    public SvnClient() {
        try {
            if ("".equals(rootPath) || rootPath == null) {
                //log();
                svnAppList = new ArrayList<Application>(1);
                return;
            }
            DAVRepositoryFactory.setup();
            SVNURL svnurl = SVNURL.parseURIEncoded(rootPath);
            ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(SettingsManager.getPropertyByName("SVNUser"), SettingsManager.getPropertyByName("SVNPassword"));
            svnClientManager.setAuthenticationManager(authManager);
            svnRepository = svnClientManager.createRepository(svnurl, true);
            svnRepository.setLocation(svnurl, true);
            SVNProperties svnProperties = new SVNProperties();

            ArrayList<SVNDirEntry> svnDirEntries = new ArrayList<SVNDirEntry>(1);

            //long l = svnRepository.getLatestRevision();
            svnDirEntries = (ArrayList<SVNDirEntry>) svnRepository.getDir("", -1, svnProperties, svnDirEntries);
            svnAppList = new ArrayList<Application>(svnDirEntries.size());
            for (SVNDirEntry de : svnDirEntries) {
                Application svnApp = new Application();
                svnApp.setAppName(de.getName());
                SVNProperties p = svnProperties.getRegularProperties();
                p.put("kind", "dir");
                ArrayList<SVNDirEntry> versions = (ArrayList<SVNDirEntry>) svnRepository.getDir(de.getName(), -1, svnProperties, new ArrayList<SVNDirEntry>());
                ArrayList<String> vers = new ArrayList<String>();
                for (SVNDirEntry v : versions) {
                    //System.out.println(v.getKind().toString());
                    if (v.getKind().toString().equals("dir")) {
                        vers.add(v.getName());
                        try {
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            svnRepository.getFile(de.getName() + "/" + v.getName() + "/" + LauncherConstants.PropertiesFileName, -1, svnProperties, baos);
                            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                            Properties appProps = new Properties();
                            appProps.load(bais);
                            svnApp.setAppName(appProps.getProperty(LauncherConstants.ApplicationName, ""));
                            svnApp.setAppPath(de.getName());
                            bais.close();
                            baos.close();
                        } catch (IOException ioe) {
                            System.out.println(ioe.getLocalizedMessage());
                        } catch (SVNException se) {
                            System.out.println(se.getLocalizedMessage());
                        }


                    }
                }
                svnApp.setAppVersions(vers);


                svnAppList.add(svnApp);
            }

        } catch (SVNException svne) {
            svnAppList = new ArrayList<Application>(1);
            //svne.printStackTrace();

        }
    }

    public ArrayList<Application> getAppList() {
        return svnAppList;
    }

    public void setAppList(ArrayList<Application> svnAppList) {
        this.svnAppList = svnAppList;
    }

    public boolean disconnect() {
        return true;
    }

    public boolean connect() {
        return true;
    }

    public boolean loadProperties(Properties props) {

        return true;
    }

    public boolean getApp(String appPath, String version, FileOutputStream fos) {
        long revision = -1;
        try {
            revision = svnRepository.getFile(appPath.concat("/").concat(version).concat("/" + appPath + "-" + version + ".jar"), -1, null, fos);

            fos.close();
            fos = null;

        } catch (SVNException se) {
            se.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return true;
    }

    public boolean checkSvnConnection() {
        try {
            svnRepository.getLatestRevision();
        } catch (SVNException svne) {
            return false;
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public String getPluginName() {
        return "Svn Client";
    }

    public String getPluginVersion() {
        return "1.0";
    }

    public Properties getFiles(String appName, String version) {

        Properties files = new Properties();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[]{});
        //New files - via property files
        try {
            svnRepository.getFile(appName.concat("/").concat(version).concat("/" + LauncherConstants.FilesListFileName), -1, null, baos);
            bais = new ByteArrayInputStream(baos.toByteArray());
            files.load(bais);
        } catch (SVNException se) {
            se.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                bais.close();
                baos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //Old version - via name
        return files;
    }

    public InputStream getFile(String appName, String version, String fileName) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[]{});
        //New files - via property files
        try {
            svnRepository.getFile(appName.concat("/").concat(version).concat("/" + fileName), -1, null, baos);
            bais = new ByteArrayInputStream(baos.toByteArray());
        } catch (SVNException se) {
            se.printStackTrace();
        } finally {
            try {
                bais.close();
                baos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return bais;
    }
}
