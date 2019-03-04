package codes.biscuit.sellchest;

import codes.biscuit.sellchest.commands.SellChestCommand;
import codes.biscuit.sellchest.events.OtherEvents;
import codes.biscuit.sellchest.events.PlayerEvents;
import codes.biscuit.sellchest.hooks.HookUtils;
import codes.biscuit.sellchest.utils.ConfigValues;
import codes.biscuit.sellchest.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public class SellChest extends JavaPlugin {

    private ConfigValues configValues;
    private Utils utils;
    private HookUtils hookUtils;

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        getCommand("sellchest").setExecutor(new SellChestCommand(this));
        this.configValues = new ConfigValues(this);
        this.utils = new Utils(this);
        this.hookUtils =  new HookUtils(this);
        Bukkit.getPluginManager().registerEvents(new PlayerEvents(this), this);
        Bukkit.getPluginManager().registerEvents(new OtherEvents(this), this);
        configValues.setupSellChests();
    }

    @Override
    public void onDisable() {
        try {
            configValues.getLocationsConfig().save(configValues.getLocationsFile());
            getLogger().info("Saved sellchest locations!");
        } catch (IOException ex) {
            ex.printStackTrace();
            getLogger().info("Unable to save sellchest locations!");
        }
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
