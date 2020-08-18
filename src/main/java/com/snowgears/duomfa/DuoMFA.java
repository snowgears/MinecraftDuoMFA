package com.snowgears.duomfa;

import com.snowgears.duomfa.duojavaclient.AuthClient;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONObject;

import java.io.File;
import java.util.logging.Logger;

public class DuoMFA extends JavaPlugin {

    private static final Logger log = Logger.getLogger("Minecraft");
    private static DuoMFA plugin;

    private PlayerListener playerListener;
    private AuthClient duoAuthClient;

    private String duoIntegrationKey = "";
    private String duoSecretKey = "";
    private String duoAPIHostname = "";

    private YamlConfiguration config;

    public static DuoMFA getPlugin() {
        return plugin;
    }

    @Override
    public void onEnable() {
        plugin = this;

        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            UtilMethods.copy(getResource("config.yml"), configFile);
        }
        config = YamlConfiguration.loadConfiguration(configFile);

        playerListener = new PlayerListener(this);

        duoIntegrationKey = config.getString("ikey");
        duoSecretKey = config.getString("skey");
        duoAPIHostname = config.getString("apihost");

        duoAuthClient = new AuthClient(duoIntegrationKey, duoSecretKey, duoAPIHostname);

        getServer().getPluginManager().registerEvents(playerListener, this);
    }

    @Override
    public void onDisable(){

    }

    public void reload(){
        HandlerList.unregisterAll(playerListener);
        onEnable();
    }

    public PlayerListener getPlayerListener() {
        return playerListener;
    }

    public AuthClient getDuoAuthClient() {
        return duoAuthClient;
    }
}