package codes.biscuit.sellchest.utils;

import codes.biscuit.sellchest.SellChest;
import codes.biscuit.sellchest.hooks.HookUtils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

public class ConfigValues {

    private SellChest main;
    private File locationsFile;
    private YamlConfiguration locationsConfig;

    public ConfigValues(SellChest main) {
        locationsFile = new File("plugins/SellChest/locations.yml");
        this.main = main;
    }

    public void setupSellChests() {
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
                    double x = Double.parseDouble(splitLocation[1]);
                    double y = Double.parseDouble(splitLocation[2]);
                    double z = Double.parseDouble(splitLocation[3]);
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
        long ticks = Math.round(main.getConfig().getDouble("sell-time") * 20);
        if (ticks >= 1) {
            return ticks;
        } else {
            return 1;
        }
    }

    long getSaveInterval() {
        long ticks = Math.round(main.getConfig().getDouble("save-time") * 20);
        if (ticks >= 1) {
            return ticks;
        } else {
            return 1;
        }
    }

    boolean removeUnsellableItems() {
        return main.getConfig().getBoolean("remove-unsellable-items");
    }

    public String getMessageReceive(int giveAmount) {
        return getMessage(Message.RECEIVED).replace("{amount}", String.valueOf(giveAmount));
    }

    String getReachedLimitMessage(int limit) {
        return getMessage(Message.REACHED_LIMIT).replace("{limit}", String.valueOf(limit));
    }


    List<String> getChestLore() {
        return main.getUtils().colorLore(main.getConfig().getStringList("item.lore"));
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

    ItemStack getItem() {
        return main.getUtils().itemFromString(main.getConfig().getString("item.material"));
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

    public boolean workaroundEnabled() {
        return main.getConfig().getBoolean("shopguiplus-workaround");
    }

    double getConfigVersion() {
        return main.getConfig().getDouble("config-version");
    }

    public boolean sendUpdateMessages() {
        return main.getConfig().getBoolean("update-messages");
    }

    Map<String, Integer> getChestLimits() {
        Map<String, Integer> limits = new HashMap<>();
        for (String limit : main.getConfig().getConfigurationSection("chest-limits").getKeys(false)) {
            limits.put(limit, main.getConfig().getInt("chest-limits."+limit));
        }
        if (!limits.containsKey("default")) {
            limits.put("default", 0);
        }
        return limits;
    }

    String getMessage(Message message) {
        return Utils.color(main.getConfig().getString(message.getPath()));
    }

    public enum Message {
        SELLCHEST_BESIDE("sellchest-beside-chest"),
        CHEST_BESIDE("chest-beside-sellchest"),
        REMOVED("sellchest-removed"),
        RECEIVED("sellchest-receive"),
        NO_SPACE("sellchest-remove-nospace"),
        NOT_OWNER("sellchest-remove-notowner"),
        PLACE("sellchest-place"),
        NO_PERMISSION_PLACE("no-permission-place"),
        NO_PERMISSION_COMMAND("no-permission-command"),
        NOT_MINIMUM_FACTION("not-minimum-faction"),
        REACHED_LIMIT("reached-limit");

        private String path;

        Message(String path) {
            this.path = "messages."+path;
        }

        public String getPath() {
            return path;
        }
    }
}
