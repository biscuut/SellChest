package codes.biscuit.voidchest.hooks;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.integration.Econ;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

class FactionsUUIDHook { // Faction bank & Faction leader

    private HookUtils hookUtils;

    FactionsUUIDHook(HookUtils hookUtils) {
        this.hookUtils = hookUtils;
    }

    void addBalance(Location loc, double amount) {
        Faction faction = Board.getInstance().getFactionAt(new FLocation(loc));
        hookUtils.economy.depositPlayer(Bukkit.getOfflinePlayer(faction.getAccountId()), amount);
    }

    OfflinePlayer getFactionLeader(Location loc) {
        return Board.getInstance().getFactionAt(new FLocation(loc)).getFPlayerAdmin().getPlayer();
    }

//    boolean hasFaction(Player p) {
//        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(p);
//        return fPlayer.hasFaction();
//    }

    boolean isPlayerClaim(Location loc) {
        Faction fLocFaction = Board.getInstance().getFactionAt(new FLocation(loc));
        return !fLocFaction.isWilderness() && !fLocFaction.isWarZone() && !fLocFaction.isSafeZone();
    }

    boolean moneyEnabled() {
        return Econ.shouldBeUsed();
    }

//    boolean locationIsFactionClaim(Location loc, Player p) {
//        Faction locFaction = Board.getInstance().getFactionAt(new FLocation(loc));
//        Faction pFaction = FPlayers.getInstance().getByPlayer(p).getFaction();
//        return locFaction.equals(pFaction);
//    }
}
