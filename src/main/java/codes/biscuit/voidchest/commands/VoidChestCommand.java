package codes.biscuit.voidchest.commands;

import codes.biscuit.voidchest.VoidChest;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class VoidChestCommand implements TabExecutor {

    private VoidChest main;

    public VoidChestCommand(VoidChest main) {
        this.main = main;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) {
            List<String> arguments = new ArrayList<>(Arrays.asList("give", "reload"));
            for (String arg : Arrays.asList("give", "reload")) {
                if (!arg.startsWith(args[0].toLowerCase())) {
                    arguments.remove(arg);
                }
            }
            return arguments;
        }
        return null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("voidchest.admin")) {
            if (args.length > 0) {
                switch (args[0].toLowerCase()) {
                    case "give":
                        if (args.length > 1) {
                            Player p = Bukkit.getPlayerExact(args[1]);
                            if (p != null) {
                                if (p.getInventory().firstEmpty() != -1) {
                                    int giveAmount = 1;
                                    if (args.length > 2) {
                                        try {
                                            giveAmount = Integer.parseInt(args[2]);
                                        } catch (NumberFormatException ex) {
                                            sender.sendMessage(ChatColor.RED + "This isn't a valid amount (integer)!");
                                            return false;
                                        }
                                    }
                                    if (main.getConfigUtils().commandDontDropIfFull()) {
                                        if (giveAmount < 65) {
                                            if (p.getInventory().firstEmpty() == -1) {
                                                sender.sendMessage(ChatColor.RED + "This player doesn't have any empty slots in their inventory!");
                                                return true;
                                            }
                                        } else {
                                            sender.sendMessage(ChatColor.RED + "You can only give 64 voidchests at a time!");
                                            return true;
                                        }
                                    }
                                    Map excessItems = p.getInventory().addItem(main.getUtils().getVoidChestItemStack(giveAmount));
                                    for (Object excessItem : excessItems.values()) {
                                        int itemCount = ((ItemStack) excessItem).getAmount();
                                        while (itemCount > 64) {
                                            ((ItemStack) excessItem).setAmount(64);
                                            p.getWorld().dropItemNaturally(p.getLocation(), (ItemStack) excessItem);
                                            itemCount = itemCount - 64;
                                        }
                                        if (itemCount > 0) {
                                            ((ItemStack) excessItem).setAmount(itemCount);
                                            p.getWorld().dropItemNaturally(p.getLocation(), (ItemStack) excessItem);
                                        }
                                    }
                                    sender.sendMessage(ChatColor.GREEN + "Gave " + p.getName() + " " + giveAmount + " voidchest(s)!");
                                    if (!main.getConfigUtils().getMessageReceive(giveAmount).equals("")) p.sendMessage(main.getConfigUtils().getMessageReceive(giveAmount));
                                } else {
                                    sender.sendMessage(ChatColor.RED + "This player doesn't have an empty slot in their inventory!");
                                }
                            } else {
                                sender.sendMessage(ChatColor.RED + "This player isn't online!");
                            }
                        } else {
                            sender.sendMessage(ChatColor.RED + "Please specify a player!");
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
                        sender.sendMessage(ChatColor.RED + "Invalid argument!");
                        break;
                }
            } else { //TODO bypass
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
