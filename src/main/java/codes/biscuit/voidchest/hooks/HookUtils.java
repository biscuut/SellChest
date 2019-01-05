package codes.biscuit.voidchest.hooks;

import codes.biscuit.voidchest.VoidChest;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;

import java.util.HashMap;
import java.util.Map;

public class HookUtils {

    private VoidChest main;
    Economy economy;
    private Map<Hooks, Object> enabledHooks = new HashMap<>();

    public HookUtils(VoidChest main) {
        this.main = main;
        economy = main.getServer().getServicesManager().getRegistration(Economy.class).getProvider();
        PluginManager pm = main.getServer().getPluginManager();
        if (pm.getPlugin("MassiveCore") != null &&
                pm.getPlugin("Factions") != null) {
            main.getLogger().info("Hooked into MassiveCore factions");
            enabledHooks.put(Hooks.MASSIVECOREFACTIONS, new MassiveCoreHook(this));
        } else if ( pm.getPlugin("Factions") != null) {
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
    }

    public double getValue(ItemStack sellItem, Player p) {
        if (main.getConfigUtils().essentialsHookEnabled()) {
            return ((EssentialsHook)enabledHooks.get(Hooks.ESSENTIALS)).getSellPrice(sellItem);
        } else if (main.getConfigUtils().shopGUIPlusHookEnabled()) {
            return ((ShopGUIPlusHook)enabledHooks.get(Hooks.SHOPGUIPLUS)).getSellPrice(p, sellItem);
        } else {
            String itemString = sellItem.getType().toString().toLowerCase();
            return main.getConfigUtils().getConfigPrice(itemString) * sellItem.getAmount();
        }
    }

    public void giveMoney(OfflinePlayer p, double amount, Location loc) {
        MoneyRecipient mr = main.getConfigUtils().getMoneyRecipient();
        FactionsUUIDHook factionsUUIDHook = ((FactionsUUIDHook)enabledHooks.get(Hooks.FACTIONSUUID));
        MassiveCoreHook massiveCoreHook = ((MassiveCoreHook)enabledHooks.get(Hooks.MASSIVECOREFACTIONS));
        ASkyblockHook aSkyblockHook = ((ASkyblockHook)enabledHooks.get(Hooks.ASKYBLOCK));
        PlotSquaredHook plotSquaredHook = ((PlotSquaredHook)enabledHooks.get(Hooks.PLOTSQUARED));
        if (main.getConfigUtils().factionsHookEnabled()) {
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
        } else if (main.getConfigUtils().askyblockHookEnabled()) {
            if (mr.equals(MoneyRecipient.ISLAND_OWNER) && aSkyblockHook != null && aSkyblockHook.isIsland(loc)) {
                addPlayerMoney(aSkyblockHook.getIslandOwner(loc), amount);
                return;
            }
        } else if (main.getConfigUtils().plotSquaredHookEnabled()) {
            if (mr.equals(MoneyRecipient.PLOT_OWNER) && plotSquaredHook != null && plotSquaredHook.isPlot(loc)) {
                addPlayerMoney(plotSquaredHook.getPlotOwner(loc), amount);
                return;
            }
        }
        addPlayerMoney(p, amount);
    }

    public boolean isMinimumFaction(Player p) {
        MoneyRecipient mr = main.getConfigUtils().getMoneyRecipient();
        FactionsUUIDHook factionsUUIDHook = ((FactionsUUIDHook)enabledHooks.get(Hooks.FACTIONSUUID));
        MassiveCoreHook massiveCoreHook = ((MassiveCoreHook)enabledHooks.get(Hooks.MASSIVECOREFACTIONS));
        if (main.getConfigUtils().factionsHookEnabled()) {
            if (mr.equals(MoneyRecipient.FACTION_BALANCE) || mr.equals(MoneyRecipient.FACTION_LEADER)) {
                if (factionsUUIDHook != null) {
                    return factionsUUIDHook.checkRole(p, main.getConfigUtils().minimumFactionsRank());
                } else if (massiveCoreHook != null) {
                    return massiveCoreHook.checkRole(p, main.getConfigUtils().minimumFactionsRank());
                }
            }
        }
        return true;
    }

    public boolean canBreakChest(Location loc, Player p) {
        if (main.getConfigUtils().anyoneCanBreak()) {
            return true;
        }
        MoneyRecipient mr = main.getConfigUtils().getMoneyRecipient();
        FactionsUUIDHook factionsUUIDHook = ((FactionsUUIDHook)enabledHooks.get(Hooks.FACTIONSUUID));
        MassiveCoreHook massiveCoreHook = ((MassiveCoreHook)enabledHooks.get(Hooks.MASSIVECOREFACTIONS));
        ASkyblockHook aSkyblockHook = ((ASkyblockHook)enabledHooks.get(Hooks.ASKYBLOCK));
        PlotSquaredHook plotSquaredHook = ((PlotSquaredHook)enabledHooks.get(Hooks.PLOTSQUARED));
        if (main.getConfigUtils().factionsHookEnabled()) {
            if (mr.equals(MoneyRecipient.FACTION_BALANCE) || mr.equals(MoneyRecipient.FACTION_LEADER)) {
                if (factionsUUIDHook != null) {
                    return factionsUUIDHook.factionIsSame(loc, p);
                } else if (massiveCoreHook != null) {
                    return massiveCoreHook.factionIsSame(loc, p);
                }
            }
        } else if (main.getConfigUtils().askyblockHookEnabled()) {
            if (mr.equals(MoneyRecipient.ISLAND_OWNER) && aSkyblockHook != null) {
                return aSkyblockHook.islandIsSame(loc, p);
            }
        } else if (main.getConfigUtils().plotSquaredHookEnabled()) {
            if (mr.equals(MoneyRecipient.PLOT_OWNER) && plotSquaredHook != null) {
                return plotSquaredHook.plotIsSame(loc, p);
            }
        }
        return main.getUtils().getChestLocations().get(loc).equals(p); //TODO Check uuids if thid doesnt work
    }

    private void addPlayerMoney(OfflinePlayer p, double amount) {
        economy.depositPlayer(p, amount);
    }

    enum Hooks {
        MASSIVECOREFACTIONS,
        FACTIONSUUID, // and all forks like SavageFactions
        ESSENTIALS,
        SHOPGUIPLUS,
        ASKYBLOCK,
        PLOTSQUARED
    }

    public enum MoneyRecipient {
        PLAYER,
        FACTION_LEADER,
        FACTION_BALANCE,
        ISLAND_OWNER,
        PLOT_OWNER
    }
}
