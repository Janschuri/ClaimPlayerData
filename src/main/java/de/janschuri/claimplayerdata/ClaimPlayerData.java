package de.janschuri.claimplayerdata;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class ClaimPlayerData extends JavaPlugin {

    private static ClaimPlayerData instance;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        getCommand("claimplayerdata").setExecutor(new ClaimCommand());
        getServer().getPluginManager().registerEvents(new JoinListener(), this);


        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static ClaimPlayerData getInstance() {
        return instance;
    }

    public static PlayerData getPlayerData(UUID uuid) {
            PlayerData playerData = new PlayerData(uuid);
            if (playerData.isEmpty()) {
                return null;
            }

            return playerData;
    }

    public static void reload() {
        getInstance().saveDefaultConfig();
        getInstance().reloadConfig();
    }

    public static String getMessage(String key) {
        return instance.getConfig().getString("messages."+key, "Message not found:" + key);
    }
}
