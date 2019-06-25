package codes.biscuit.sellchest;

import codes.biscuit.sellchest.commands.SellChestCommand;
import codes.biscuit.sellchest.hooks.HookUtils;
import codes.biscuit.sellchest.hooks.MetricsLite;
import codes.biscuit.sellchest.listeners.PlayerListener;
import codes.biscuit.sellchest.utils.ConfigValues;
import codes.biscuit.sellchest.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.regex.Pattern;

public class SellChest extends JavaPlugin {

    private ConfigValues configValues;
    private Utils utils;
    private HookUtils hookUtils;
    private int minecraftVersion = -1;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getCommand("sellchest").setExecutor(new SellChestCommand(this));
        configValues = new ConfigValues(this);
        utils = new Utils(this);
        utils.updateConfig(this);
        hookUtils =  new HookUtils(this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        configValues.setupSellChests();
        new MetricsLite(this);
        if (minecraftVersion == -1) {
            minecraftVersion = Integer.valueOf(Bukkit.getBukkitVersion().split(Pattern.quote("-"))[0].split(Pattern.quote("."))[1]);
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("Saved sellchest locations!");
        utils.saveChestLocations();
    }

    public ConfigValues getConfigValues() {
        return this.configValues;
    }

    public Utils getUtils() {
        return this.utils;
    }

    public HookUtils getHookUtils() {
        return hookUtils;
    }

    public int getMinecraftVersion() {
        return minecraftVersion;
    }
}
