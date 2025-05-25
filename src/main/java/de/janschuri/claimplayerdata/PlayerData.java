package de.janschuri.claimplayerdata;

import de.tr7zw.nbtapi.*;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static de.janschuri.claimplayerdata.ClaimPlayerData.getClaimedPlayerData;
import static de.janschuri.claimplayerdata.ClaimPlayerData.getPlayerNbtFile;

public class PlayerData {

    private final UUID uuid;
    private JSONObject claimedData;
    private List<ItemStack> inventoryItems = new ArrayList<>();
    private List<ItemStack> enderchestItems = new ArrayList<>();
    private int xpTotal = 0;

    public PlayerData(
            UUID uuid
    ) {
        this.uuid = uuid;

        NBTFile playerNbtFile = getPlayerNbtFile(uuid);
        claimedData = getClaimedPlayerData(uuid);

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

        NBTCompoundList enderItems = playerNbtFile.getCompoundList("EnderItems");

        if (enderItems != null) {
            enderItems.stream().forEach(nbtCompound -> {
                ItemStack item = NBT.itemStackFromNBT(nbtCompound);

                enderchestItems.add(item);
            });
        }


        xpTotal = playerNbtFile.getInteger("XpTotal");
    }


    boolean isEmpty() {
        return inventoryItems.isEmpty() && inventoryItems.isEmpty() && xpTotal == 0;
    }

    public List<ItemStack> getInventoryItems() {
        return inventoryItems;
    }

    public List<ItemStack> getEnderchestItems() {
        return enderchestItems;
    }

    public int getXpTotal() {
        return xpTotal;
    }

    public void clearInventory() {
        ClaimPlayerData.playerClaimedData(uuid, "Inventory");
    }

    public void clearEnderchest() {
        ClaimPlayerData.playerClaimedData(uuid, "EnderItems");
    }

    public void clearXp() {
        ClaimPlayerData.playerClaimedData(uuid, "XpTotal");
    }

    public boolean hasClaimed(String type) {
        if (claimedData == null) {
            return false;
        }

        if (claimedData.get("claimed_playerdata") == null) {
            return false;
        }

        List<String> claimedPlayerData = (List<String>) claimedData.get("claimed_playerdata");

        return claimedPlayerData.contains(type);
    }
}
