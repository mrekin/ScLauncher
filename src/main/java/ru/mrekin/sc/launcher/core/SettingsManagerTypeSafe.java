package ru.mrekin.sc.launcher.core;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import ru.mrekin.sc.launcher.plugin.Plugin;
import ru.mrekin.sc.launcher.plugin.RepoPlugin;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Properties;

public class SettingsManagerTypeSafe implements ISettingsManager{


    private Config config = null;

    @Override
    public String getPropertyByName(String name) {
        return config.getString(name);
    }

    @Override
    public String getPropertyByName(String name, String defValue)
    {
        return config.getString(name).isEmpty()?defValue:config.getString(name);
    }

    @Override
    public String[] getPropertiesArrayByName(String name, String defValue) {
        return (String[])config.getStringList(name).toArray();
    }

    @Override
    public String[] getPropertiesArrayByName(String name) {
        return new String[0];
    }

    @Override
    public boolean updateLocalSettings() {
        return false;
    }

    @Override
    public void loadLauncherSettings() {

    }

    @Override
    public void loadLauncherProperties() {
        config = ConfigFactory.parseFile(new File(LauncherConstants.SettingsFileName3));
        if(config==null){
            config = ConfigFactory.empty();
        }
        Iterator<String> keys = SettingsManager.getInstance().getXmlConfiguration().getKeys();
        while(keys.hasNext()) {
            String key = keys.next();
            Object value = SettingsManager.getInstance().getXmlConfiguration().getProperty(key);
            System.out.println(key+" : "+value.toString());
            if(key.contains("@force")){
                key ="_Force." + key.replaceAll("\\[.*\\]","");

            }else{

            }
            setProperty(key,String.valueOf(value));

          //  Object o =config.getObject("AutoUpdaterEnabled");
             Object o1 = config.getAnyRef("Plugins.scl_mq_notif_plugin.MQNotifPlugin");
            // Object o2 =config.getObjectList("AutoUpdaterEnabled");
            System.out.println("".toString());
        }
    }

    @Override
    public void loadPluginProperties() {
        try {
            Config temp = null;
            for(RepoPlugin pl:PluginRepoManager.getInstance().getRepoPlugins()){
                Properties p = pl.getPluginObj().getDefaultProperties();
                temp = ConfigFactory.parseProperties(p);
                String name ="RepoPlugins." + pl.getPluginName().replace(" ","_");
                config = config.withFallback(temp.atPath(name));
            }
            for(Plugin pl:PluginManager.getInstance().getPlugins()){
                Properties p = pl.getPluginObj().getDefaultProperties();
                temp = ConfigFactory.parseProperties(p);
                String name = "Plugins." + pl.getPluginName().replace(" ","_").replace("\"","");
                config = config.withFallback(temp.atPath(name));
            }
        } catch (Exception e) {
            log(e.getLocalizedMessage());
        }
    }

    public void setProperty(String key, String value){
        try {
            config = config.withFallback(ConfigFactory.parseString(key + ":" + value
                    .replace("[", "")
                    .replace("]", "")));
        }catch (Exception e){
            log(e.getLocalizedMessage());
        }
    }

    public  static void main(String[] args){
        SettingsFactory.getInstance().loadLauncherProperties();
        SettingsFactory.getInstance().loadPluginProperties();
        SettingsFactory.getInstance().save();
    }

    @Override
    public void save(){
        ConfigRenderOptions options = ConfigRenderOptions.defaults()
                .setJson(false)
                .setFormatted(true)
                .setOriginComments(false);
        String prp = config.root().render(options);
        log("\n"+prp,"DEBUG");
        FileDriver.getInstance().installFile(".","","",LauncherConstants.SettingsFileName3,new ByteArrayInputStream(prp.getBytes(StandardCharsets.UTF_8)));
    }
}
