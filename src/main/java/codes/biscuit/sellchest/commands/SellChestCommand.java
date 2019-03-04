package codes.biscuit.sellchest.commands;

import codes.biscuit.sellchest.SellChest;
import codes.biscuit.sellchest.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class SellChestCommand implements TabExecutor {

    private SellChest main;

    public SellChestCommand(SellChest main) {
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
        if (sender.hasPermission("sellchest.admin")) {
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
                                            sender.sendMessage(Utils.color("&cThis isn't a valid amount (integer)!"));
                                            return false;
                                        }
                                    }
                                    if (main.getConfigValues().commandDontDropIfFull()) {
                                        if (giveAmount < 65) {
                                            if (p.getInventory().firstEmpty() == -1) {
                                                sender.sendMessage(Utils.color("&cThis player doesn't have any empty slots in their inventory!"));
                                                return true;
                                            }
                                        } else {
                                            sender.sendMessage(Utils.color("&cYou can only give 64 sellchests at a time!"));
                                            return true;
                                        }
                                    }
                                    Map excessItems = p.getInventory().addItem(main.getUtils().getSellChestItemStack(giveAmount));
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
                                    sender.sendMessage(Utils.color("&aGave " + p.getName() + " " + giveAmount + " sellchest(s)!"));
                                    if (!main.getConfigValues().getMessageReceive(giveAmount).equals("")) p.sendMessage(main.getConfigValues().getMessageReceive(giveAmount));
                                } else {
                                    sender.sendMessage(Utils.color("&cThis player doesn't have an empty slot in their inventory!"));
                                }
                            } else {
                                sender.sendMessage(Utils.color("&cThis player isn't online!"));
                            }
                        } else {
                            sender.sendMessage(Utils.color("&cPlease specify a player!"));
                        }
                        break;
                    case "reload":
                        main.reloadConfig();
                        Bukkit.getScheduler().cancelTask(main.getUtils().getSellTimerID());
                        main.getUtils().runSellTimer();
                        Bukkit.getScheduler().cancelTask(main.getUtils().getSaveTimerID());
                        main.getUtils().runSaveTimer();
                        sender.sendMessage(Utils.color("&aReloaded the config! Most values have been instantly updated."));
                        break;
                    case "bypass":
                        if (sender instanceof Player) {
                            Player p = (Player)sender;
                            if (main.getUtils().getBypassPlayers().contains(p)) {
                                main.getUtils().getBypassPlayers().remove(p);
                                sender.sendMessage(Utils.color("&cYou are no longer bypassing!"));
                            } else {
                                main.getUtils().getBypassPlayers().add(p);
                                sender.sendMessage(Utils.color("&aYou are now bypassing most checks!"));
                            }
                        } else {
                            sender.sendMessage(Utils.color("&cYou can only bypass as a player ingame!."));
                        }
                        break;
                    default:
                        sender.sendMessage(Utils.color("&cInvalid argument!"));
                        break;
                }
            } else {
                sender.sendMessage(Utils.color("&7&m--------------&7[&6&l SellChest &7]&7&m--------------"));
                sender.sendMessage(Utils.color("&6● /sc give <player> [amount]  &7- Give a player a sellchest"));
                sender.sendMessage(Utils.color("&6● /sc bypass &7- Bypass all restrictions for placing/breaking sellchests"));
                sender.sendMessage(Utils.color("&6● /sc reload &7- Reload the config"));
                sender.sendMessage(Utils.color("&7&ov" + main.getDescription().getVersion() + " by Biscut"));
                sender.sendMessage(Utils.color("&7&m-----------------------------------------"));
            }
        } else {
            if (!main.getConfigValues().getNoPermissionCommand().equals("")) sender.sendMessage(main.getConfigValues().getNoPermissionCommand());
        }
        return false;
    }
}
