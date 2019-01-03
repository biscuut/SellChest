package codes.biscuit.voidchest.utils;

import codes.biscuit.voidchest.hooks.HookUtils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import codes.biscuit.voidchest.VoidChest;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ConfigUtils {

    private VoidChest main;
    private File locationsFile;
    private YamlConfiguration locationsConfig;

    public ConfigUtils(VoidChest main) {
        locationsFile = new File("plugins/VoidChest/locations.yml");
        this.main = main;
    }

    public void setupVoidChests() {
        if (!this.locationsFile.exists()) {
            try {
                this.locationsFile.createNewFile();
                locationsConfig = YamlConfiguration.loadConfiguration(this.locationsFile);
                locationsConfig.createSection("locations");
                locationsConfig.save(this.locationsFile);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            Bukkit.getLogger().info("[VoidChest] Files not found, generated configs!");
        } else {
            locationsConfig = YamlConfiguration.loadConfiguration(this.locationsFile);
            Set<String> locationKeys = locationsConfig.getConfigurationSection("locations").getKeys(false);
            for (String key : locationKeys) {
                UUID uuid = UUID.fromString(key);
                OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
                List<String> locationList = locationsConfig.getStringList("locations." + key);
                for (String location : locationList) {
                    String[] splitLocation = location.split("!");
                    World world = Bukkit.getWorld(splitLocation[0]);
                    Double x = Double.parseDouble(splitLocation[1]);
                    Double y = Double.parseDouble(splitLocation[2]);
                    Double z = Double.parseDouble(splitLocation[3]);
                    Location loc = new Location(world, Location.locToBlock(x), Location.locToBlock(y), Location.locToBlock(z));
                    main.getLocations().put(loc, p);
                }
            }
        }
        main.getUtils().runSellTimer();
        main.getUtils().runSaveTimer();
    }

    String getChestName() {
        return ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("item.name"));
    }

    long getSellInterval() {
        long ticks = Math.round(main.getConfig().getDouble("sell-interval") * 20);
        if (ticks >= 1) {
            return ticks;
        } else {
            return 1;
        }
    }

    long getSaveInterval() {
        long ticks = Math.round(main.getConfig().getDouble("save-interval") * 20);
        if (ticks >= 1) {
            return ticks;
        } else {
            return 1;
        }
    }

    boolean removeUnsellableItems() {
        return main.getConfig().getBoolean("remove-unsellable-items");
    }

    public String getMessageVoidChestBeside() {
        return ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("messages.voidchest-beside-chest"));
    }

    public String getMessageChestBeside() {
        return ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("messages.chest-beside-voidchest"));
    }

    public String getMessageRemove() {
        return ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("messages.voidchest-removed"));
    }

    public String getMessageNoSpace() {
        return ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("messages.voidchest-remove-nospace"));
    }

    public String getMessageNotOwner() {
        return ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("messages.voidchest-remove-notowner"));
    }

    public String getMessagePlace() {
        return ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("messages.voidchest-place"));
    }

    public String getNoPermissionPlaceMessage() {
        return ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("messages.no-permission-place"));
    }

    public String getNoPermissionCommand() {
        return ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("messages.no-permission-command"));
    }


    public String getMessageReceive(int giveAmount) {
        String message = ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("messages.voidchest-receive"));
        return message.replace("{amount}", String.valueOf(giveAmount));
    }

    List<String> getChestLore() {
        List<String> lore = main.getConfig().getStringList("item.lore");
        for (int i = 0; i < lore.size(); i++) {
            lore.set(i, ChatColor.translateAlternateColorCodes('&', lore.get(i)));
        }
        return lore;
    }

    public YamlConfiguration getLocationsConfig() {
        return this.locationsConfig;
    }

    public File getLocationsFile() {
        return this.locationsFile;
    }

    boolean chestShouldGlow() {
        return main.getConfig().getBoolean("item.glow");
    }

    Material getItemMaterial() {
        String rawMaterial = main.getConfig().getString("item.material");
        Material mat;
        if (rawMaterial.contains(":")) {
            String[] materialSplit = rawMaterial.split(":");
            try {
                mat = Material.valueOf(materialSplit[0]);
            } catch (IllegalArgumentException ex) {
                mat = Material.CHEST;
                main.getLogger().severe("Your chest item material is invalid!");
            }
        } else {
            try {
                mat = Material.valueOf(rawMaterial);
            } catch (IllegalArgumentException ex) {
                mat = Material.CHEST;
                main.getLogger().severe("Your chest item material is invalid!");
            }
        }
        return mat;
    }

    short getItemDamage() {
        String rawDamage = main.getConfig().getString("item.material");
        if (rawDamage.contains(":")) {
            String[] materialSplit = rawDamage.split(":");
            short damage;
            try {
                damage = Short.valueOf(materialSplit[1]);
            } catch (IllegalArgumentException ex) {
                damage = 1;
                main.getLogger().severe("Your chest item damage is invalid!");
            }
            return damage;
        } else {
            return 0;
        }
    }

    public boolean essentialsHookEnabled() {
        return main.getConfig().getBoolean("hooks.essentials");
    }

    public boolean shopGUIPlusHookEnabled() {
        return main.getConfig().getBoolean("hooks.shopguiplus");
    }

    public boolean askyblockHookEnabled() {
        return main.getConfig().getBoolean("hooks.askyblock");
    }

    public boolean factionsHookEnabled() {
        return main.getConfig().getBoolean("hooks.factions");
    }

    public boolean plotSquaredHookEnabled() {
        return main.getConfig().getBoolean("hooks.plotsquared");
    }

    public HookUtils.MoneyRecipient getMoneyRecipient() {
        String rawRecipient = main.getConfig().getString("money-recipient");
        try {
            return HookUtils.MoneyRecipient.valueOf(rawRecipient);
        } catch (Exception ex) {
            return HookUtils.MoneyRecipient.PLAYER;
        }
    }

    public double getConfigPrice(String item) {
        return main.getConfig().getDouble("prices."+item);
    }
}
