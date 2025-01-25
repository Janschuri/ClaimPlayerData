package de.janschuri.claimplayerdata;

import de.tr7zw.nbtapi.*;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.nbtapi.iface.ReadableNBT;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
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

    public static String getMessages(String key) {
        return instance.getConfig().getString("messages."+key, "Message not found:" + key);
    }
}
