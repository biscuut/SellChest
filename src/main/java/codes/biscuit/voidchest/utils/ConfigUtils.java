package codes.biscuit.voidchest.utils;

import codes.biscuit.voidchest.VoidChest;
import codes.biscuit.voidchest.hooks.HookUtils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class ConfigUtils {

    private VoidChest main;
    private File locationsFile;
    private YamlConfiguration locationsConfig;

    public ConfigUtils(VoidChest main) {
        locationsFile = new File("plugins/VoidChest/locations.yml");
        this.main = main;
    }

    public void setupVoidChests() {
        if (!locationsFile.exists()) {
            try {
                boolean outcome = locationsFile.createNewFile();
                if (!outcome) {
                    throw new IOException("The file already exists. (impossible?)");
                }
                locationsConfig = YamlConfiguration.loadConfiguration(this.locationsFile);
                locationsConfig.createSection("locations");
                locationsConfig.save(this.locationsFile);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            main.getLogger().info("Files not found, generated configs!");
        } else {
            locationsConfig = YamlConfiguration.loadConfiguration(this.locationsFile);
            for (String key : locationsConfig.getConfigurationSection("locations").getKeys(false)) {
                UUID uuid = UUID.fromString(key);
                OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
                for (String location : locationsConfig.getStringList("locations." + key)) {
                    String[] splitLocation = location.split(Pattern.quote("|"));
                    World world = Bukkit.getWorld(splitLocation[0]);
                    Double x = Double.parseDouble(splitLocation[1]);
                    Double y = Double.parseDouble(splitLocation[2]);
                    Double z = Double.parseDouble(splitLocation[3]);
                    Location loc = new Location(world, Location.locToBlock(x), Location.locToBlock(y), Location.locToBlock(z));
                    main.getUtils().getChestLocations().put(loc, p);
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

    public String getNotMinimumFactionMessage() {
        return ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("messages.not-minimum-faction"));
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

    public boolean sneakToBreak() {
        return main.getConfig().getBoolean("sneak-punch-to-break");
    }

    public boolean commandDontDropIfFull() {
        return !main.getConfig().getBoolean("command-drop-if-full");
    }

    public boolean breakDontDropIfFull() {
        return !main.getConfig().getBoolean("break-drop-if-full");
    }

    public boolean breakIntoInventory() {
        return main.getConfig().getBoolean("break-give-to-inventory");
    }

    public boolean anyoneCanBreak() {
        return main.getConfig().getBoolean("anyone-can-break");
    }

    public String minimumFactionsRank() {
        return main.getConfig().getString("minimum-factions-rank-break").toLowerCase();
    }
}
