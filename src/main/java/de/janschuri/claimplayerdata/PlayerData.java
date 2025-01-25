package de.janschuri.claimplayerdata;

import de.tr7zw.nbtapi.*;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.nio.file.LinkOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class PlayerData {

    private final UUID uuid;
    private List<ItemStack> inventoryItems = new ArrayList<>();
    private List<ItemStack> enderchestItems = new ArrayList<>();
    private int xp = 0;

    public PlayerData(
            UUID uuid
    ) {
        this.uuid = uuid;

        NBTFile playerNbtFile = getPlayerNbtFile();

        if (playerNbtFile == null) {
            return;
        }

        NBTCompoundList inventory = playerNbtFile.getCompoundList("Inventory");

        if (inventory != null) {
            inventory.stream().forEach(nbtCompound -> {
                ItemStack item = NBT.itemStackFromNBT(nbtCompound);

                inventoryItems.add(item);
            });
        }

        NBTCompoundList ender = playerNbtFile.getCompoundList("EnderItems");

        if (ender != null) {
            ender.stream().forEach(nbtCompound -> {
                ItemStack item = NBT.itemStackFromNBT(nbtCompound);

                enderchestItems.add(item);
            });
        }


        xp = playerNbtFile.getInteger("XpTotal");
    }


    boolean isEmpty() {
        return inventoryItems.isEmpty() && inventoryItems.isEmpty() && xp == 0;
    }

    public List<ItemStack> getInventoryItems() {
        return inventoryItems;
    }

    public List<ItemStack> getEnderchestItems() {
        return enderchestItems;
    }

    public int getXp() {
        return xp;
    }

    public void clearInventory() {
        inventoryItems.clear();

        if (isEmpty()) {
            delete();
        } else {
            removeKey("Inventory");
        }
    }

    public void clearEnderchest() {
        enderchestItems.clear();

        if (isEmpty()) {
            delete();
        } else {
            removeKey("EnderItems");
        }
    }

    public void clearXp() {
        xp = 0;

        if (isEmpty()) {
            delete();
        } else {
            removeKey("XpTotal");
        }
    }

    public void removeKey(String key) {
        try {
            NBTFile playerNbtFile = getPlayerNbtFile();

            if (playerNbtFile == null) {
                Bukkit.getLogger().warning("Cannot remove key " + key + " because player data file is null.");
                return;
            }

            playerNbtFile.removeKey(key);

            playerNbtFile.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void delete() {
        File playerDataDir = new File(ClaimPlayerData.getInstance().getDataFolder(), "playerdata");

        File playerDataFile = new File(playerDataDir, uuid + ".dat");
        File playerDataFileOld = new File(playerDataDir, uuid + ".dat_old");

        playerDataFile.delete();
        playerDataFileOld.delete();

        ClaimPlayerData.removePlayerData(uuid);
    }

    private NBTFile getPlayerNbtFile() {
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
