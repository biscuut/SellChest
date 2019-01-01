package codes.biscuit.voidchest;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import codes.biscuit.voidchest.events.*;
import codes.biscuit.voidchest.hooks.ShopGUIPlusHook;
import codes.biscuit.voidchest.utils.*;
import codes.biscuit.voidchest.commands.*;

import java.io.IOException;
import java.util.HashMap;

public class VoidChest extends JavaPlugin {

    private HashMap<Location, OfflinePlayer> voidChests = new HashMap<>();
    private ConfigUtils configUtils;
    private Utils utils;
    private ShopGUIPlusHook shopGUIHook = null;
    public Economy economy = null;

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        getCommand("voidchest").setExecutor(new VoidChestCommand(this));
        this.configUtils = new ConfigUtils(this);
        this.utils = new Utils(this);
        Bukkit.getPluginManager().registerEvents(new PlayerEvents(this), this);
        Bukkit.getPluginManager().registerEvents(new OtherEvents(this), this);
        setupEconomy();
        if (Bukkit.getPluginManager().getPlugin("ShopGUIPlus") != null) {
            shopGUIHook = new ShopGUIPlusHook();
            getLogger().info("ShopGUI+ hook enabled!");
        }
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

    private void setupEconomy()
    {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            this.economy = economyProvider.getProvider();
        }
    }

    public ConfigUtils getConfigUtils() {
        return this.configUtils;
    }

    public Utils getUtils() {
        return this.utils;
    }

    public HashMap<Location, OfflinePlayer> getLocations() {
        return this.voidChests;
    }

    public ShopGUIPlusHook getShopGUIHook() {
        return this.shopGUIHook;
    }
}
