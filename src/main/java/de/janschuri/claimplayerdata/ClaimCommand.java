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

import java.nio.file.LinkOption;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ClaimCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;

            PlayerData playerData = ClaimPlayerData.getPlayerData(player.getUniqueId());

            if (playerData == null) {
                player.sendMessage("You have no unclaimed player data.");
                return true;
            }

            String arg = "";
            if (strings.length > 0) {
                arg = strings[0];
            }

            boolean confirm = false;

            if (strings.length > 1) {
                confirm = strings[1].equalsIgnoreCase("confirm");
            }

            Bukkit.getLogger().info("Command: " + arg);

            switch (arg) {
                case "inv":
                    List<ItemStack> inventoryItems = playerData.getInventoryItems();

                    if (inventoryItems.isEmpty()) {
                        player.sendMessage("You have no unclaimed inventory.");
                        return true;
                    }


                    if (!confirm) {
                        sendConfirmMessage(player, "inv", inventoryItems);

                        return true;
                    }

                    dropItems(player, inventoryItems);

                    playerData.clearInventory();
                    player.sendMessage("You have claimed your inventory.");
                    break;
                case "end":
                    List<ItemStack> enderchestItems = playerData.getEnderchestItems();

                    if (enderchestItems.isEmpty()) {
                        player.sendMessage("You have no unclaimed enderchest.");
                        return true;
                    }

                    if (!confirm) {
                        sendConfirmMessage(player, "end", enderchestItems);

                        return true;
                    }

                    dropItems(player, enderchestItems);

                    playerData.clearEnderchest();
                    player.sendMessage("You have claimed your enderchest.");
                    break;
                case "xp":
                    if (playerData.getXp() == 0) {
                        player.sendMessage("You have no unclaimed xp.");
                        return true;
                    }

                    if (!confirm) {
                        sendConfirmMessage(player, "xp", Component.text("Do you want to claim your " + playerData.getXp() + " xp?"));

                        return true;
                    }


                    player.giveExp(playerData.getXp());
                    Bukkit.getLogger().info(player.getName() + " got " + playerData.getXp() + " xp");

                    playerData.clearXp();
                    player.sendMessage("You have claimed your xp.");
                    break;
                default:
                    player.sendMessage("Usage: /claimplayerdata <inv|end|xp>");
            }

            return true;
        } else {
            commandSender.sendMessage("You must be a player to use this command.");
            return true;
        }
    }

    private void sendConfirmMessage(Player player, String command, List<ItemStack> items) {
        String message = "";

        switch (command) {
            case "inv":
                message = "You need to have enough space in your inventory to claim your old inventory.";
                break;
            case "end":
                message = "You need to have enough space in your inventory to claim your old enderchest.";
                break;
            default:
                message = "You need to have enough space in your inventory to claim your old items.";
        }

        Component itemsMessage = Component.text("Items: ")
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
                .append(Component.text("Overflowing items will be dropped.")
                        .color(net.kyori.adventure.text.format.NamedTextColor.RED)
                    )
                );

        sendConfirmMessage(player, command, confirmMessage);
    }

    private void sendConfirmMessage(Player player, String command, Component component) {

        Component confirmMessage = component
                .append(Component.text(" [Confirm]").color(net.kyori.adventure.text.format.NamedTextColor.GREEN)
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
