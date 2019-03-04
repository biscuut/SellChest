package codes.biscuit.voidchest.utils;

import codes.biscuit.voidchest.VoidChest;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.regex.Pattern;

public class Utils {

    private VoidChest main;
    private int sellTimerID;
    private int saveTimerID;
    private Map<Location, OfflinePlayer> chestLocations = new HashMap<>();
    private Set<OfflinePlayer> bypassPlayers = new HashSet<>();

    public Utils(VoidChest main) {
        this.main = main;
    }

    public static String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public boolean isVoidChest(ItemStack item) {
        return (item.hasItemMeta() && item.getItemMeta().getDisplayName().equals(main.getConfigValues().getChestName()) && item.getItemMeta().hasLore() && item.getItemMeta().getLore().equals(main.getConfigValues().getChestLore()));
    }

    public ItemStack getVoidChestItemStack(int amount) {
        ItemStack voidChest = new ItemStack(main.getConfigValues().getItemMaterial(), amount, main.getConfigValues().getItemDamage());
        ItemMeta voidChestMeta = voidChest.getItemMeta();
        voidChestMeta.setDisplayName(main.getConfigValues().getChestName());
        voidChestMeta.setLore(main.getConfigValues().getChestLore());
        voidChest.setItemMeta(voidChestMeta);
        if (main.getConfigValues().chestShouldGlow()) addGlow(voidChest);
        return voidChest;
    }

    public void addConfigLocation(Location loc, OfflinePlayer offlineP) {
        chestLocations.put(loc, offlineP);
        String serializedLoc = loc.getWorld().getName() + "|" + loc.getBlockX() + "|" + loc.getBlockY() + "|" + loc.getBlockZ();
        String uuid = offlineP.getUniqueId().toString();
        List<String> locationList;
        if (main.getConfigValues().getLocationsConfig().isSet("locations." + uuid)) {
            locationList = main.getConfigValues().getLocationsConfig().getStringList("locations." + uuid);
        } else {
            locationList = new ArrayList<>();
        }
        locationList.add(serializedLoc);
        main.getConfigValues().getLocationsConfig().set("locations." + uuid, locationList);
    }

    public void removeConfigLocation(Location loc, OfflinePlayer offlineP) {
        chestLocations.remove(loc);
        String serializedLoc = loc.getWorld().getName() + "|" + loc.getBlockX() + "|" + loc.getBlockY() + "|" + loc.getBlockZ();
        String uuid = offlineP.getUniqueId().toString();
        List<String> locationList = main.getConfigValues().getLocationsConfig().getStringList("locations." + uuid);
        locationList.remove(serializedLoc);
        if (locationList.isEmpty()) {
            main.getConfigValues().getLocationsConfig().set("locations." + uuid, null);
        } else {
            main.getConfigValues().getLocationsConfig().set("locations." + uuid, locationList);
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
                    if (main.getConfigValues().removeUnsellableItems()) {
                        voidChest.getBlockInventory().clear();
                    }
                }
            }
        }, main.getConfigValues().getSellInterval(), main.getConfigValues().getSellInterval());
    }

    public int getSellTimerID() {
        return sellTimerID;
    }

    public void runSaveTimer() {
        saveTimerID = Bukkit.getScheduler().scheduleSyncRepeatingTask(main, () -> {
            try {
                main.getConfigValues().getLocationsConfig().save(main.getConfigValues().getLocationsFile());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }, main.getConfigValues().getSaveInterval(), main.getConfigValues().getSaveInterval());
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

    public Set<OfflinePlayer> getBypassPlayers() {
        return bypassPlayers;
    }

    public void updateConfig(VoidChest main) { //TODO: Unused for the first version.
        if (main.getConfigValues().getConfigVersion() < 1.0) {
            Map<String, Object> oldValues = new HashMap<>();
            for (String oldKey : main.getConfig().getKeys(true)) {
                oldValues.put(oldKey, main.getConfig().get(oldKey));
            }
            main.saveResource("config.yml", true);
            main.reloadConfig();
            for (String newKey : main.getConfig().getKeys(true)) {
                if (oldValues.containsKey(newKey) && !oldValues.get(newKey).equals(main.getConfig().get(newKey))) {
                    main.getConfig().set(newKey, oldValues.get(newKey));
                }
            }
            main.getConfig().set("config-version", 1.0);
            main.saveConfig();
        }
    }

    public void checkUpdates(Player p) { //TODO: Start using this on the second version
        try {
            URL url = new URL("https://api.spigotmc.org/legacy/update.php?resource=(ID HERE)");
            URLConnection connection = url.openConnection();
            connection.setReadTimeout(5000);
            connection.addRequestProperty("User-Agent", "VoidChest update checker");
            connection.setDoOutput(true);
            final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String newestVersion = reader.readLine();
            reader.close();
            List<Integer> newestVersionNumbers = new ArrayList<>();
            List<Integer> thisVersionNumbers = new ArrayList<>();
            try {
                for (String s : newestVersion.split(Pattern.quote("."))) {
                    newestVersionNumbers.add(Integer.parseInt(s));
                }
                for (String s : main.getDescription().getVersion().split(Pattern.quote("."))) {
                    thisVersionNumbers.add(Integer.parseInt(s));
                }
            } catch (Exception ex) {
                return;
            }
            for (int i = 0; i < 3; i++) {
                if (newestVersionNumbers.get(i) != null && thisVersionNumbers.get(i) != null) {
                    if (newestVersionNumbers.get(i) > thisVersionNumbers.get(i)) {
                        TextComponent newVersion = new TextComponent("A new version of VoidChest, " + newestVersion + " is available. Download it by clicking here.");
                        newVersion.setColor(ChatColor.RED);
                        newVersion.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "(ADD LINK)"));
                        p.spigot().sendMessage(newVersion);
                        break;
                    } else if (thisVersionNumbers.get(i) > newestVersionNumbers.get(i)) {
                        p.sendMessage(ChatColor.RED + "You are running a development version of VoidChest, " + main.getDescription().getVersion() + ". The latest online version is " + newestVersion + ".");
                        break;
                    }
                }
            }
        } catch (Exception ignored) {}
    }
}
