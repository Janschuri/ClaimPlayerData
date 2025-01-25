package de.janschuri.claimplayerdata;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class ClaimCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;

            String arg = "";
            if (strings.length > 0) {
                arg = strings[0];
            }

            boolean confirm = false;

            if (strings.length > 1) {
                confirm = strings[1].equalsIgnoreCase("confirm");
            }

            if (arg.equalsIgnoreCase("reload") && player.hasPermission("claimplayerdata.reload")) {
                    ClaimPlayerData.reload();
                    player.sendMessage(getMessage("reload"));
                    return true;
            }

            PlayerData playerData = ClaimPlayerData.getPlayerData(player.getUniqueId());

            if (playerData == null) {
                player.sendMessage(getMessage("no_unclaimed_data"));
                return true;
            }


            switch (arg) {
                case "inv":
                    if (!player.hasPermission("claimplayerdata.claim.inv")) {
                        player.sendMessage(getMessage("no_permission"));
                        return true;
                    }

                    List<ItemStack> inventoryItems = playerData.getInventoryItems();

                    if (inventoryItems.isEmpty()) {
                        player.sendMessage(getMessage("no_unclaimed_inventory"));
                        return true;
                    }


                    if (!confirm) {
                        sendConfirmMessage(player, "inv", inventoryItems);

                        return true;
                    }

                    dropItems(player, inventoryItems);

                    playerData.clearInventory();
                    player.sendMessage(getMessage("claimed_inventory"));
                    break;
                case "end":
                    if (!player.hasPermission("claimplayerdata.claim.end")) {
                        player.sendMessage(getMessage("no_permission"));
                        return true;
                    }

                    List<ItemStack> enderchestItems = playerData.getEnderchestItems();

                    if (enderchestItems.isEmpty()) {
                        player.sendMessage(getMessage("no_unclaimed_enderchest"));
                        return true;
                    }

                    if (!confirm) {
                        sendConfirmMessage(player, "end", enderchestItems);

                        return true;
                    }

                    dropItems(player, enderchestItems);

                    playerData.clearEnderchest();
                    player.sendMessage(getMessage("claimed_enderchest"));
                    break;
                case "xp":
                    if (!player.hasPermission("claimplayerdata.claim.xp")) {
                        player.sendMessage(getMessage("no_permission"));
                        return true;
                    }

                    if (playerData.getXp() == 0) {
                        player.sendMessage(getMessage("no_unclaimed_xp"));
                        return true;
                    }

                    if (!confirm) {
                        sendConfirmMessage(player, "xp", Component.text(
                                getMessage("claim_xp_confirm")
                                        .replace("%xp%", String.valueOf(playerData.getXp()))
                                )
                        );

                        return true;
                    }


                    player.giveExp(playerData.getXp());
                    Bukkit.getLogger().info(player.getName() + " got " + playerData.getXp() + " xp");

                    playerData.clearXp();
                    player.sendMessage(getMessage("claimed_xp"));
                    break;
                default:
                    player.sendMessage("Usage: /claimplayerdata <inv|end|xp>");
            }

            return true;
        } else {
            commandSender.sendMessage(getMessage("no_console"));
            return true;
        }
    }

    private void sendConfirmMessage(Player player, String command, List<ItemStack> items) {
        String message = "";

        switch (command) {
            case "inv":
                message = getMessage("claim_inventory_confirm");
                break;
            case "end":
                message = getMessage("claim_enderchest_confirm");
                break;
        }

        Component itemsMessage = Component.text(getMessage("items_list"))
                .append(Component.newline());

        for (ItemStack item : items) {
            itemsMessage = itemsMessage.append(Component.text(" - " + item.getType().name())
                    .append(Component.text(" x" + item.getAmount())
                            .color(net.kyori.adventure.text.format.NamedTextColor.GRAY)
                    )
                    .append(Component.newline())
            );
        }

        Component confirmMessage = Component.text(message)
                .append(Component.text(" (" + items.size() + " items)")
                        .color(net.kyori.adventure.text.format.NamedTextColor.GRAY)
                        .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(
                                itemsMessage
                        )
                )
                .append(Component.newline())
                .append(Component.text(getMessage("overflow_info"))
                        .color(net.kyori.adventure.text.format.NamedTextColor.RED)
                    )
                );

        sendConfirmMessage(player, command, confirmMessage);
    }

    private String getMessage(String key) {
        return ClaimPlayerData.getMessages(key);
    }

    private void sendConfirmMessage(Player player, String command, Component component) {

        Component confirmMessage = component
                .append(Component.text(" " + getMessage("confirm_button")).color(net.kyori.adventure.text.format.NamedTextColor.GREEN)
                        .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/claimplayerdata " + command + " confirm"))
                );

        player.sendMessage(confirmMessage);
    }

    private void dropItems(Player player, List<ItemStack> items) {
        World world = player.getWorld();
        for (ItemStack item : items) {
            Bukkit.getLogger().info(player.getName() + " got " + item.getType().name());

            Map<Integer, ItemStack> remaining = player.getInventory().addItem(item);

            if (!remaining.isEmpty()) {
                for (ItemStack remainingItem : remaining.values()) {
                    world.dropItem(player.getLocation(), remainingItem);
                }
            }
        }
    }
}
