package codes.biscuit.sellchest.hooks;

import codes.biscuit.sellchest.SellChest;
import com.sun.org.apache.regexp.internal.RE;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;

import java.util.HashMap;
import java.util.Map;

public class HookUtils {

    private SellChest main;
    private Economy economy;
    private Map<Hooks, Object> enabledHooks = new HashMap<>();
    private Map<OfflinePlayer, Double> moneyHistory = new HashMap<>();

    public HookUtils(SellChest main) {
        this.main = main;
        economy = main.getServer().getServicesManager().getRegistration(Economy.class).getProvider();
        PluginManager pm = main.getServer().getPluginManager();
        if (pm.getPlugin("MassiveCore") != null &&
                pm.getPlugin("Factions") != null) {
            main.getLogger().info("Hooked into MassiveCore factions");
            enabledHooks.put(Hooks.MASSIVECOREFACTIONS, new MassiveCoreHook(this));
        } else if (pm.getPlugin("Factions") != null) {
            main.getLogger().info("Hooked into FactionsUUID/SavageFactions");
            enabledHooks.put(Hooks.FACTIONSUUID, new FactionsUUIDHook(this));
        }
        if (pm.getPlugin("Essentials") != null) {
            main.getLogger().info("Hooked into Essentials");
            enabledHooks.put(Hooks.ESSENTIALS, new EssentialsHook(pm.getPlugin("Essentials")));
        }
        if (pm.getPlugin("ShopGUIPlus") != null) {
            main.getLogger().info("Hooked into ShopGUI+");
            enabledHooks.put(Hooks.SHOPGUIPLUS, new ShopGUIPlusHook());
        }
        if (pm.getPlugin("ASkyBlock") != null) {
            main.getLogger().info("Hooked into ASkyBlock");
            enabledHooks.put(Hooks.ASKYBLOCK, new ASkyblockHook());
        }
        if (pm.getPlugin("PlotSquared") != null) {
            main.getLogger().info("Hooked into PlotSquared");
            enabledHooks.put(Hooks.PLOTSQUARED, new PlotSquaredHook());
        }
        if (main.getMinecraftVersion() >= 14) {
            enabledHooks.put(Hooks.MINECRAFT_1_14, new Minecraft_1_14());
        }
    }

    public double getValue(ItemStack sellItem, Player p) {
        if (main.getConfigValues().essentialsHookEnabled() && enabledHooks.containsKey(Hooks.ESSENTIALS)) {
            return ((EssentialsHook)enabledHooks.get(Hooks.ESSENTIALS)).getSellPrice(sellItem);
        } else if (main.getConfigValues().shopGUIPlusHookEnabled() && enabledHooks.containsKey(Hooks.SHOPGUIPLUS)) {
            if (p == null) {
                if (main.getConfigValues().workaroundEnabled()) {
                    p = main.getServer().getOnlinePlayers().stream().findAny().orElse(null);
                    if (p != null) {
                        return ((ShopGUIPlusHook) enabledHooks.get(Hooks.SHOPGUIPLUS)).getSellPrice(p, sellItem);
                    }
                }
            } else {
                return ((ShopGUIPlusHook) enabledHooks.get(Hooks.SHOPGUIPLUS)).getSellPrice(p, sellItem);
            }
        }
        String itemString = sellItem.getType().toString().toLowerCase();
        return main.getConfigValues().getConfigPrice(itemString) * sellItem.getAmount();
    }

    public boolean isCancelled(PlayerInteractEvent e) {
        if (enabledHooks.containsKey(Hooks.MINECRAFT_1_14)) {
            Minecraft_1_14 minecraftHook = ((Minecraft_1_14)enabledHooks.get(Hooks.MINECRAFT_1_14));
            return minecraftHook.isCancelled(e);
        } else {
            return e.isCancelled();
        }
    }

    public void giveMoney(OfflinePlayer p, double amount, Location loc) {
        if (main.getConfigValues().getRecentEarningsInterval() > 0) {
            moneyHistory.put(p, moneyHistory.getOrDefault(p, 0D) + amount);
        }
        MoneyRecipient mr = main.getConfigValues().getMoneyRecipient();
        FactionsUUIDHook factionsUUIDHook = ((FactionsUUIDHook)enabledHooks.get(Hooks.FACTIONSUUID));
        MassiveCoreHook massiveCoreHook = ((MassiveCoreHook)enabledHooks.get(Hooks.MASSIVECOREFACTIONS));
        ASkyblockHook aSkyblockHook = ((ASkyblockHook)enabledHooks.get(Hooks.ASKYBLOCK));
        PlotSquaredHook plotSquaredHook = ((PlotSquaredHook)enabledHooks.get(Hooks.PLOTSQUARED));
        if (main.getConfigValues().factionsHookEnabled()) {
            if (mr.equals(MoneyRecipient.FACTION_BALANCE)) {
                if (factionsUUIDHook != null && factionsUUIDHook.moneyEnabled() && factionsUUIDHook.isPlayerClaim(loc)) {
                    factionsUUIDHook.addBalance(loc, amount);
                    return;
                } else if (massiveCoreHook != null && massiveCoreHook.moneyEnabled() && massiveCoreHook.isPlayerClaim(loc)) {
                    massiveCoreHook.addBalance(loc, amount);
                    return;
                }
            } else if (mr.equals(MoneyRecipient.FACTION_LEADER)) {
                if (factionsUUIDHook != null && factionsUUIDHook.isPlayerClaim(loc)) {
                    addPlayerMoney(factionsUUIDHook.getFactionLeader(loc), amount);
                    return;
                } else if (massiveCoreHook != null && massiveCoreHook.isPlayerClaim(loc)) {
                    addPlayerMoney(massiveCoreHook.getFactionLeader(loc), amount);
                    return;
                }
            }
        } else if (main.getConfigValues().askyblockHookEnabled()) {
            if (mr.equals(MoneyRecipient.ISLAND_OWNER) && aSkyblockHook != null && aSkyblockHook.isIsland(loc)) {
                addPlayerMoney(aSkyblockHook.getIslandOwner(loc), amount);
                return;
            }
        } else if (main.getConfigValues().plotSquaredHookEnabled()) {
            if (mr.equals(MoneyRecipient.PLOT_OWNER) && plotSquaredHook != null && plotSquaredHook.isPlot(loc)) {
                addPlayerMoney(plotSquaredHook.getPlotOwner(loc), amount);
                return;
            }
        }
        addPlayerMoney(p, amount);
    }

    public boolean isMinimumFaction(Player p, Location loc) {
        MoneyRecipient mr = main.getConfigValues().getMoneyRecipient();
        FactionsUUIDHook factionsUUIDHook = ((FactionsUUIDHook)enabledHooks.get(Hooks.FACTIONSUUID));
        MassiveCoreHook massiveCoreHook = ((MassiveCoreHook)enabledHooks.get(Hooks.MASSIVECOREFACTIONS));
        if (main.getConfigValues().factionsHookEnabled()) {
            if (mr.equals(MoneyRecipient.FACTION_BALANCE) || mr.equals(MoneyRecipient.FACTION_LEADER)) {
                if (factionsUUIDHook != null) {
                    if (factionsUUIDHook.isWilderness(loc)) return true;
                    return factionsUUIDHook.checkRole(p, main.getConfigValues().minimumFactionsRank());
                } else if (massiveCoreHook != null) {
                    if (massiveCoreHook.isWilderness(loc)) return true;
                    return massiveCoreHook.checkRole(p, main.getConfigValues().minimumFactionsRank());
                }
            }
        }
        return true;
    }

    public boolean canBreakChest(Location loc, Player p) {
        if (main.getConfigValues().anyoneCanBreak()) {
            return true;
        }
        MoneyRecipient mr = main.getConfigValues().getMoneyRecipient();
        FactionsUUIDHook factionsUUIDHook = ((FactionsUUIDHook)enabledHooks.get(Hooks.FACTIONSUUID));
        MassiveCoreHook massiveCoreHook = ((MassiveCoreHook)enabledHooks.get(Hooks.MASSIVECOREFACTIONS));
        ASkyblockHook aSkyblockHook = ((ASkyblockHook)enabledHooks.get(Hooks.ASKYBLOCK));
        PlotSquaredHook plotSquaredHook = ((PlotSquaredHook)enabledHooks.get(Hooks.PLOTSQUARED));
        if (main.getConfigValues().factionsHookEnabled()) {
            if (mr.equals(MoneyRecipient.FACTION_BALANCE) || mr.equals(MoneyRecipient.FACTION_LEADER)) {
                if (factionsUUIDHook != null) {
                    return factionsUUIDHook.factionIsSame(loc, p);
                } else if (massiveCoreHook != null) {
                    return massiveCoreHook.factionIsSame(loc, p);
                }
            }
        } else if (main.getConfigValues().askyblockHookEnabled()) {
            if (mr.equals(MoneyRecipient.ISLAND_OWNER) && aSkyblockHook != null) {
                return aSkyblockHook.islandIsSame(loc, p);
            }
        } else if (main.getConfigValues().plotSquaredHookEnabled()) {
            if (mr.equals(MoneyRecipient.PLOT_OWNER) && plotSquaredHook != null) {
                return plotSquaredHook.plotIsSame(loc, p);
            }
        }
        return main.getUtils().getChestLocations().get(loc).equals(p.getUniqueId());
    }

    private void addPlayerMoney(OfflinePlayer p, double amount) {
        if (economy.hasAccount(p)) {
            economy.depositPlayer(p, amount);
        }
    }

    Economy getEconomy() {
        return economy;
    }

    public Map<OfflinePlayer, Double> getMoneyHistory() {
        return moneyHistory;
    }

    enum Hooks {
        MASSIVECOREFACTIONS,
        FACTIONSUUID, // and all forks like SavageFactions
        ESSENTIALS,
        SHOPGUIPLUS,
        ASKYBLOCK,
        PLOTSQUARED,
        MINECRAFT_1_14
    }

    public enum MoneyRecipient {
        PLAYER,
        FACTION_LEADER,
        FACTION_BALANCE,
        ISLAND_OWNER,
        PLOT_OWNER
    }
}
