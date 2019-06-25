package codes.biscuit.sellchest.hooks;

import com.massivecraft.factions.Rel;
import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MConf;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.massivecore.money.Money;
import com.massivecraft.massivecore.ps.PS;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

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

    @SuppressWarnings("deprecation")
    void addBalance(Location loc, double amount) {
        if (isPlayerClaim(loc)) {
            Faction faction = BoardColl.get().getFactionAt(PS.valueOf(loc));
            hookUtils.getEconomy().depositPlayer(Money.accountId(faction), amount);
        }
    }

    boolean factionIsSame(Location loc, Player p) {
        if (BoardColl.get().getFactionAt(PS.valueOf(loc)).isNone()) return true;
        return MPlayer.get(p).getFaction().equals(BoardColl.get().getFactionAt(PS.valueOf(loc)));
    }

    boolean isPlayerClaim(Location loc) {
        Faction faction = BoardColl.get().getFactionAt(PS.valueOf(loc));
        return faction.isNormal() && !faction.getId().equals("safezone") && !faction.getId().equals("warzone");
    }

    boolean isWilderness(Location loc) {
        return BoardColl.get().getFactionAt(PS.valueOf(loc)).isNone();
    }

    boolean checkRole(Player p, String role) {
        Rel playerRole = MPlayer.get(p).getRole();
        switch (role) {
            case "leader": case "admin":
                if (playerRole.equals(Rel.LEADER)) {
                    return true;
                }
                break;
            case "officer":
                if (playerRole.equals(Rel.OFFICER) || playerRole.equals(Rel.LEADER)) {
                    return true;
                }
                break;
            case "member": case "normal":
                if (playerRole.equals(Rel.MEMBER) || playerRole.equals(Rel.OFFICER) || playerRole.equals(Rel.LEADER)) {
                    return true;
                }
                break;
            case "recruit": case "any":
                return true;
        }
        return false;
    }
}
