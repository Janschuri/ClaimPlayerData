package de.janschuri.claimplayerdata;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {


    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = ClaimPlayerData.getPlayerData(player.getUniqueId());

        if (playerData != null && ClaimPlayerData.getInstance().getConfig().getBoolean("notify_on_join")) {
            if (playerData.getInventoryItems().size() > 0 && player.hasPermission("claimplayerdata.claim.inv")) {
                player.sendMessage(ClaimPlayerData.getMessage("unclaimed_data"));
                return;
            }

            if (playerData.getXp() > 0 && player.hasPermission("claimplayerdata.claim.xp")) {
                player.sendMessage(ClaimPlayerData.getMessage("unclaimed_data"));
                return;
            }

            if (playerData.getEnderchestItems().size() > 0 && player.hasPermission("claimplayerdata.claim.ender")) {
                player.sendMessage(ClaimPlayerData.getMessage("unclaimed_data"));
                return;
            }
        }
    }
}
