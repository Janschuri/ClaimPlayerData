package de.janschuri.claimplayerdata;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClaimCommand implements CommandExecutor, TabCompleter {

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

            PlayerData playerData = new PlayerData(player.getUniqueId());

            if (playerData.isEmpty()) {
                player.sendMessage(getMessage("no_unclaimed_data"));
                return true;
            }


            switch (arg) {
                case "inv":
                    if (!player.hasPermission("claimplayerdata.claim.inv")) {
                        player.sendMessage(getMessage("no_permission"));
                        return true;
                    }

                    if (playerData.hasClaimed("Inventory")) {
                        player.sendMessage(getMessage("inventory_already_claimed"));
                        return true;
                    }

                    List<ItemStack> inventoryItems = playerData.getInventoryItems();

                    if (inventoryItems.isEmpty()) {
                        player.sendMessage(getMessage("inventory_no_unclaimed"));
                        return true;
                    }


                    if (!confirm) {
                        sendConfirmMessage(player, "inv", inventoryItems);

                        return true;
                    }

                    dropItems(player, inventoryItems);

                    playerData.clearInventory();
                    player.sendMessage(getMessage("inventory_claimed"));
                    break;
                case "end":
                    if (!player.hasPermission("claimplayerdata.claim.end")) {
                        player.sendMessage(getMessage("no_permission"));
                        return true;
                    }

                    if (playerData.hasClaimed("EnderItems")) {
                        player.sendMessage(getMessage("ender_items_already_claimed"));
                        return true;
                    }

                    List<ItemStack> enderchestItems = playerData.getEnderchestItems();

                    if (enderchestItems.isEmpty()) {
                        player.sendMessage(getMessage("ender_items_no_unclaimed"));
                        return true;
                    }

                    if (!confirm) {
                        sendConfirmMessage(player, "end", enderchestItems);

                        return true;
                    }

                    dropItems(player, enderchestItems);

                    playerData.clearEnderchest();
                    player.sendMessage(getMessage("ender_items_claimed"));
                    break;
                case "xp":
                    if (!player.hasPermission("claimplayerdata.claim.xp")) {
                        player.sendMessage(getMessage("no_permission"));
                        return true;
                    }

                    if (playerData.hasClaimed("XpTotal")) {
                        player.sendMessage(getMessage("xp_total_already_claimed"));
                        return true;
                    }

                    if (playerData.getXpTotal() == 0) {
                        player.sendMessage(getMessage("xp_total_no_unclaimed"));
                        return true;
                    }

                    if (!confirm) {
                        sendConfirmMessage(player, "xp", Component.text(
                                getMessage("xp_total_claim_confirm")
                                        .replace("%xp%", String.valueOf(playerData.getXpTotal()))
                                )
                        );

                        return true;
                    }


                    player.giveExp(playerData.getXpTotal());
                    Bukkit.getLogger().info(player.getName() + " got " + playerData.getXpTotal() + " xp");

                    playerData.clearXp();
                    player.sendMessage(getMessage("xp_total_claimed"));
                    break;
                default:
                    player.sendMessage(getMessage("wrong_usage"));
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
                message = getMessage("inventory_claim_confirm");
                break;
            case "end":
                message = getMessage("ender_items_claim_confirm");
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
        return ClaimPlayerData.getMessage(key);
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

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        List<String> completions = new ArrayList<>();

        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;

            if (strings.length == 1) {
                if (player.hasPermission("claimplayerdata.claim.inv")) {
                    if ("inv".startsWith(strings[0])) {
                        completions.add("inv");
                    }
                }

                if (player.hasPermission("claimplayerdata.claim.end")) {
                    if ("end".startsWith(strings[0])) {
                        completions.add("end");
                    }
                }

                if (player.hasPermission("claimplayerdata.claim.xp")) {
                    if ("xp".startsWith(strings[0])) {
                        completions.add("xp");
                    }
                }
            }
        }

        return completions;
    }
}
