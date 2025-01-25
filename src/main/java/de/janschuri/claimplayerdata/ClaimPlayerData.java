package de.janschuri.claimplayerdata;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class ClaimPlayerData extends JavaPlugin {

    private static ClaimPlayerData instance;

    private static Map<UUID, PlayerData> playerDataMap = new HashMap<>();

    @Override
    public void onEnable() {
        // register command
        instance = this;
        saveDefaultConfig();

        getCommand("claimplayerdata").setExecutor(new ClaimCommand());
        getServer().getPluginManager().registerEvents(new JoinListener(), this);

        //create dir for plugin

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
        if (playerDataMap.containsKey(uuid)) {
            return playerDataMap.get(uuid);
        } else {
            PlayerData playerData = new PlayerData(uuid);
            if (!playerData.isEmpty()) {
                playerDataMap.put(uuid, playerData);
                return playerData;
            } else {
                return null;
            }
        }
    }

    public static void reload() {
        getInstance().saveDefaultConfig();
        getInstance().reloadConfig();
    }

    public static void addPlayerData(UUID uuid, PlayerData playerData) {
        playerDataMap.put(uuid, playerData);
    }

    public static void removePlayerData(UUID uuid) {
        playerDataMap.remove(uuid);
    }

    public static String getMessage(String key) {
        return instance.getConfig().getString("messages."+key, "Message not found:" + key);
    }
}
