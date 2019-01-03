package codes.biscuit.voidchest.commands;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import codes.biscuit.voidchest.VoidChest;

public class VoidChestCommand implements CommandExecutor {

    private VoidChest main;

    public VoidChestCommand(VoidChest main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("voidchest.admin")) {
            if (args.length >= 1) {
                switch (args[0]) {
                    case "give":
                        if (args.length >= 2) {
                            Player p = Bukkit.getPlayerExact(args[1]);
                            if (p != null && p.isOnline()) {
                                if (p.getInventory().firstEmpty() != -1) {
                                    int giveAmount = 1;
                                    if (args.length >= 3) {
                                        try {
                                            giveAmount = Integer.parseInt(args[2]);
                                        } catch (NumberFormatException ex) {
                                            sender.sendMessage(ChatColor.RED + "This isn't a valid amount! /vc [give] [player] [amount]");
                                            return false;
                                        }
                                    }
                                    if (giveAmount < 65) {
                                        p.getInventory().addItem(main.getUtils().getVoidChestItemStack(giveAmount));
                                        sender.sendMessage(ChatColor.GREEN + "Gave " + p.getName() + " " + giveAmount + " voidchest!");
                                        p.sendMessage(main.getConfigUtils().getMessageReceive(giveAmount));
                                    } else {
                                        sender.sendMessage(ChatColor.RED + "You can only give 64 voidchests at a time!");
                                    }
                                } else {
                                    sender.sendMessage(ChatColor.RED + "This player doesn't have an empty slot!");
                                }
                            } else {
                                sender.sendMessage(ChatColor.RED + "This player isn't online!");
                            }
                        } else {
                            sender.sendMessage(ChatColor.RED + "You must specify a player! /vc [give] [player] [amount]");
                        }
                        break;
                    case "reload":
                        main.reloadConfig();
                        Bukkit.getScheduler().cancelTask(main.getUtils().getSellTimerID());
                        main.getUtils().runSellTimer();
                        Bukkit.getScheduler().cancelTask(main.getUtils().getSaveTimerID());
                        main.getUtils().runSaveTimer();
                        sender.sendMessage(ChatColor.GREEN + "Reloaded the config! Most values have been instantly updated.");
                        break;
                    default:
                        sender.sendMessage(ChatColor.RED + "Invalid argument! /vc [give|reload]");
                        break;
                }
            } else {
                sender.sendMessage(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "--------------" + ChatColor.GRAY +"[" + ChatColor.GOLD + ChatColor.BOLD + " VoidChest " + ChatColor.GRAY + "]" + ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "--------------");
                sender.sendMessage(ChatColor.GOLD + "● /vc give <player> [amount] " + ChatColor.GRAY + "- Give a player a voidchest");
                sender.sendMessage(ChatColor.GOLD + "● /vc reload " + ChatColor.GRAY + "- Reload the config");
                sender.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC + "v" + main.getDescription().getVersion() + " by Biscut");
                sender.sendMessage(ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "-----------------------------------------");
            }
        } else {
            if (!main.getConfigUtils().getNoPermissionCommand().equals("")) sender.sendMessage(main.getConfigUtils().getNoPermissionCommand());
        }
        return false;
    }
}
