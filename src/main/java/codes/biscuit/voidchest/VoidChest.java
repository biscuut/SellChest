package codes.biscuit.voidchest;

import codes.biscuit.voidchest.commands.VoidChestCommand;
import codes.biscuit.voidchest.events.OtherEvents;
import codes.biscuit.voidchest.events.PlayerEvents;
import codes.biscuit.voidchest.hooks.HookUtils;
import codes.biscuit.voidchest.utils.ConfigUtils;
import codes.biscuit.voidchest.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public class VoidChest extends JavaPlugin {

    private ConfigUtils configUtils;
    private Utils utils;
    private HookUtils hookUtils;

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        getCommand("voidchest").setExecutor(new VoidChestCommand(this));
        this.configUtils = new ConfigUtils(this);
        this.utils = new Utils(this);
        this.hookUtils =  new HookUtils(this);
        Bukkit.getPluginManager().registerEvents(new PlayerEvents(this), this);
        Bukkit.getPluginManager().registerEvents(new OtherEvents(this), this);
        configUtils.setupVoidChests();
    }

    @Override
    public void onDisable() {
        try {
            configUtils.getLocationsConfig().save(configUtils.getLocationsFile());
            getLogger().info("Saved voidchest locations!");
        } catch (IOException ex) {
            ex.printStackTrace();
            getLogger().info("Unable to save voidchest locations!");
        }
    }

    public ConfigUtils getConfigUtils() {
        return this.configUtils;
    }

    public Utils getUtils() {
        return this.utils;
    }

    public HookUtils getHookUtils() {
        return hookUtils;
    }
}
