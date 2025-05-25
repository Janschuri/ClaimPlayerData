package de.janschuri.claimplayerdata;

import de.tr7zw.nbtapi.NBTFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public final class ClaimPlayerData extends JavaPlugin {

    private static ClaimPlayerData instance;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        File claimedPlayerDataFile = new File(getDataFolder(), "claimed_playerdata.json");
        if (!claimedPlayerDataFile.exists()) {
            saveResource("claimed_playerdata.json", false);
        }

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

    public static void reload() {
        getInstance().saveDefaultConfig();
        getInstance().reloadConfig();
    }

    public static String getMessage(String key) {
        return instance.getConfig().getString("messages."+key, "Message not found:" + key);
    }

    public static JSONObject getClaimedPlayerData(UUID uuid) {
        JSONParser parser = new JSONParser();

        try (FileReader reader = new FileReader(ClaimPlayerData.getInstance().getDataFolder() + "/claimed_playerdata.json")) {
            // Parse the JSON file
            Object obj = parser.parse(reader);
            JSONArray jsonArray = (JSONArray) obj;

            // Search for the object with the matching UUID
            for (Object jsonObject : jsonArray) {
                JSONObject json = (JSONObject) jsonObject;
                if (uuid.toString().equals(json.get("uuid"))) {
                    return json;
                }
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static JSONObject playerClaimedData(UUID uuid, String type) {
        JSONParser parser = new JSONParser();
        JSONArray jsonArray;

        File claimedPlayerDataFile = new File(getInstance().getDataFolder(), "claimed_playerdata.json");
        if (!claimedPlayerDataFile.exists()) {
            getInstance().saveResource("claimed_playerdata.json", false);
        }

        try (FileReader reader = new FileReader(ClaimPlayerData.getInstance().getDataFolder() + "/claimed_playerdata.json")) {
            Object obj = parser.parse(reader);
            jsonArray = (JSONArray) obj;
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            return null;
        }

        JSONObject targetObject = null;
        for (Object jsonObject : jsonArray) {
            JSONObject json = (JSONObject) jsonObject;
            if (uuid.toString().equals(json.get("uuid"))) {
                targetObject = json;
                break;
            }
        }

        if (targetObject == null) {
            targetObject = new JSONObject();
            targetObject.put("uuid", uuid.toString());
            targetObject.put("claimed_playerdata", new JSONArray());
            jsonArray.add(targetObject);
        }

        JSONArray claimedPlayerData = (JSONArray) targetObject.get("claimed_playerdata");
        if (!claimedPlayerData.contains(type)) {
            claimedPlayerData.add(type);
        }

        try (FileWriter writer = new FileWriter(ClaimPlayerData.getInstance().getDataFolder() + "/claimed_playerdata.json")) {
            writer.write(jsonArray.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return targetObject;
    }

    public static NBTFile getPlayerNbtFile(UUID uuid) {
        File playerDataDir = new File(ClaimPlayerData.getInstance().getDataFolder(), "playerdata");

        File playerDataFile = new File(playerDataDir, uuid + ".dat");

        if (!playerDataFile.exists()) {
            return null;
        }

        try {
            return new NBTFile(playerDataFile);
        } catch (IOException e) {
            return null;
        }
    }
}
