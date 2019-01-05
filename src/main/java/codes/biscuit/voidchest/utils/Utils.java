package codes.biscuit.voidchest.utils;

import codes.biscuit.voidchest.VoidChest;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {

    private VoidChest main;
    private int sellTimerID;
    private int saveTimerID;
    private Map<Location, OfflinePlayer> chestLocations = new HashMap<>();

    public Utils(VoidChest main) {
        this.main = main;
    }

    public boolean isVoidChest(ItemStack item) {
        return (item.hasItemMeta() && item.getItemMeta().getDisplayName().equals(main.getConfigUtils().getChestName()) && item.getItemMeta().hasLore() && item.getItemMeta().getLore().equals(main.getConfigUtils().getChestLore()));
    }

    public ItemStack getVoidChestItemStack(int amount) {
        ItemStack voidChest = new ItemStack(main.getConfigUtils().getItemMaterial(), amount, main.getConfigUtils().getItemDamage());
        ItemMeta voidChestMeta = voidChest.getItemMeta();
        voidChestMeta.setDisplayName(main.getConfigUtils().getChestName());
        voidChestMeta.setLore(main.getConfigUtils().getChestLore());
        voidChest.setItemMeta(voidChestMeta);
        if (main.getConfigUtils().chestShouldGlow()) addGlow(voidChest);
        return voidChest;
    }

    public void addConfigLocation(Location loc, OfflinePlayer offlineP) {
        chestLocations.put(loc, offlineP);
        String serializedLoc = loc.getWorld().getName() + "|" + loc.getBlockX() + "|" + loc.getBlockY() + "|" + loc.getBlockZ();
        String uuid = offlineP.getUniqueId().toString();
        List<String> locationList;
        if (main.getConfigUtils().getLocationsConfig().isSet("locations." + uuid)) {
            locationList = main.getConfigUtils().getLocationsConfig().getStringList("locations." + uuid);
        } else {
            locationList = new ArrayList<>();
        }
        locationList.add(serializedLoc);
        main.getConfigUtils().getLocationsConfig().set("locations." + uuid, locationList);
    }

    public void removeConfigLocation(Location loc, OfflinePlayer offlineP) {
        chestLocations.remove(loc);
        String serializedLoc = loc.getWorld().getName() + "|" + loc.getBlockX() + "|" + loc.getBlockY() + "|" + loc.getBlockZ();
        String uuid = offlineP.getUniqueId().toString();
        List<String> locationList = main.getConfigUtils().getLocationsConfig().getStringList("locations." + uuid);
        locationList.remove(serializedLoc);
        if (locationList.isEmpty()) {
            main.getConfigUtils().getLocationsConfig().set("locations." + uuid, null);
        } else {
            main.getConfigUtils().getLocationsConfig().set("locations." + uuid, locationList);
        }
    }

    public void runSellTimer() {
        sellTimerID = Bukkit.getScheduler().scheduleSyncRepeatingTask(main, () -> {
            for (Map.Entry<Location, OfflinePlayer> locationEntry : chestLocations.entrySet()) {
                OfflinePlayer offlineP = locationEntry.getValue();
                Location loc = locationEntry.getKey();
                if (loc.getBlock().getType().equals(Material.CHEST)) {
                    Chest voidChest = (Chest)loc.getBlock().getState();
                    Inventory voidChestInventory = voidChest.getInventory();
                    for (ItemStack item : voidChestInventory) {
                        if (item != null) {
                            double sellPrice;
                            if (offlineP.isOnline()) {
                                Player p = offlineP.getPlayer();
                                sellPrice = main.getHookUtils().getValue(item, p);
                            } else {
                                sellPrice = main.getHookUtils().getValue(item, null);
                            }
                            if (sellPrice > 0) {
                                main.getHookUtils().giveMoney(offlineP, sellPrice, loc);
                                voidChestInventory.setItem(voidChestInventory.first(item), new ItemStack(Material.AIR));
                                loc.getBlock().getState().update(); // If you don't update the chest after removing, double chests will sell double
                            }
                        }
                    }
                    if (main.getConfigUtils().removeUnsellableItems()) {
                        voidChest.getBlockInventory().clear();
                    }
                }
            }
        }, main.getConfigUtils().getSellInterval(), main.getConfigUtils().getSellInterval());
    }

    public int getSellTimerID() {
        return sellTimerID;
    }

    public void runSaveTimer() {
        saveTimerID = Bukkit.getScheduler().scheduleSyncRepeatingTask(main, () -> {
            try {
                main.getConfigUtils().getLocationsConfig().save(main.getConfigUtils().getLocationsFile());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }, main.getConfigUtils().getSaveInterval(), main.getConfigUtils().getSaveInterval());
    }

    public int getSaveTimerID() {
        return saveTimerID;
    }

    private void addGlow(ItemStack item) {
        item.addUnsafeEnchantment(Enchantment.LUCK, 1);
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
    }

    public Map<Location, OfflinePlayer> getChestLocations() {
        return this.chestLocations;
    }

    public BlockFace getOppositeDirection(Player player) {
        double rotation = (player.getLocation().getYaw() - 90) % 360;
        if (rotation < 0) {
            rotation += 360.0;
        }
        if (rotation >= 45 && rotation < 135) {
            return BlockFace.SOUTH;
        } else if (rotation >= 135 && rotation < 225) {
            return BlockFace.WEST;
        } else if (rotation >= 225 && rotation < 315) {
            return BlockFace.NORTH;
        } else if (rotation >= 315 || rotation < 45) {
            return BlockFace.EAST;
        } else {
            return null;
        }
    }
}
