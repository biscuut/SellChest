package codes.biscuit.sellchest;

import codes.biscuit.sellchest.commands.SellChestCommand;
import codes.biscuit.sellchest.hooks.HookUtils;
import codes.biscuit.sellchest.hooks.MetricsLite;
import codes.biscuit.sellchest.listeners.PlayerListener;
import codes.biscuit.sellchest.utils.ConfigValues;
import codes.biscuit.sellchest.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class SellChest extends JavaPlugin {

    private ConfigValues configValues;
    private Utils utils;
    private HookUtils hookUtils;

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
}
