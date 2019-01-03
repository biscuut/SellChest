package codes.biscuit.voidchest.hooks;

import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MConf;
import com.massivecraft.massivecore.money.Money;
import com.massivecraft.massivecore.ps.PS;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

class MassiveCoreHook {

    private HookUtils hookUtils;

    MassiveCoreHook(HookUtils hookUtils) {
        this.hookUtils = hookUtils;
    }

    OfflinePlayer getFactionLeader(Location loc) {
        return BoardColl.get().getFactionAt(PS.valueOf(loc)).getLeader().getPlayer();
}

    boolean moneyEnabled() {
        return MConf.get().econEnabled && Money.enabled();
    }

    void addBalance(Location loc, double amount) { // Am going to try sending to/from null but if doesnt work ya know why
        if (isPlayerClaim(loc)) {
            Faction faction = BoardColl.get().getFactionAt(PS.valueOf(loc));
            hookUtils.economy.depositPlayer(Money.accountId(faction), amount);
        }
    }

//    boolean hasFaction(Player p) {
//        return MPlayer.get(p).hasFaction();
//    }

    boolean isPlayerClaim(Location loc) {
        Faction faction = BoardColl.get().getFactionAt(PS.valueOf(loc));
        return faction.isNormal() && !faction.getId().equals("safezone") && !faction.getId().equals("warzone");
    }

//    boolean locationIsFactionClaim(Location loc, Player p) {
//        Faction locFaction = BoardColl.get().getFactionAt(PS.valueOf(loc));
//        Faction pFaction = MPlayer.get(p).getFaction();
//        return locFaction.equals(pFaction);
//    }
}
