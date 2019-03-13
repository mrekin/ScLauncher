package ru.mrekin.sc.launcher.core;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import ru.mrekin.sc.launcher.plugin.Plugin;
import ru.mrekin.sc.launcher.plugin.RepoPlugin;

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
        config = ConfigFactory.parseResources(LauncherConstants.PropertiesFileName);
        if(config==null){
            config = ConfigFactory.empty();
        }
    }

    @Override
    public void loadPluginProperties() {
        try {
            Config temp = null;
            for(RepoPlugin pl:PluginRepoManager.getInstance().getRepoPlugins()){
                Properties p = pl.getPluginObj().getDefaultProperties();
                temp = ConfigFactory.parseProperties(p);
                String name = pl.getPluginName().replace(" ","_");
                config = config.withFallback(temp.atPath(name));
            }
            for(Plugin pl:PluginManager.getInstance().getPlugins()){
                Properties p = pl.getPluginObj().getDefaultProperties();
                temp = ConfigFactory.parseProperties(p);
                String name = pl.getPluginName().replace(" ","_");
                config = config.withFallback(temp.atPath(name));
            }
        } catch (Exception e) {
            log(e.getLocalizedMessage());
        }
    }

    public  static void main(String[] args){
        SettingsFactory.getInstance().loadLauncherSettings();
        SettingsFactory.getInstance().loadPluginProperties();
        SettingsFactory.getInstance().save();
    }

    @Override
    public void save(){
        System.out.println(config.root().render(ConfigRenderOptions.defaults()));
    }
}
