package codes.biscuit.sellchest.hooks;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.integration.Econ;
import com.massivecraft.factions.struct.Role;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

class FactionsUUIDHook {

    private HookUtils hookUtils;

    FactionsUUIDHook(HookUtils hookUtils) {
        this.hookUtils = hookUtils;
    }

    @SuppressWarnings("deprecation")
    void addBalance(Location loc, double amount) {
        Faction faction = Board.getInstance().getFactionAt(new FLocation(loc));
        hookUtils.getEconomy().depositPlayer(Bukkit.getOfflinePlayer(faction.getAccountId()), amount);
    }

    OfflinePlayer getFactionLeader(Location loc) {
        return Board.getInstance().getFactionAt(new FLocation(loc)).getFPlayerAdmin().getPlayer();
    }

    boolean factionIsSame(Location loc, Player p) {
        if (Board.getInstance().getFactionAt(new FLocation(loc)).isWilderness()) return true;
        return FPlayers.getInstance().getByPlayer(p).getFaction().equals(Board.getInstance().getFactionAt(new FLocation(loc)));
    }

    boolean isPlayerClaim(Location loc) {
        Faction fLocFaction = Board.getInstance().getFactionAt(new FLocation(loc));
        return !fLocFaction.isWilderness() && !fLocFaction.isWarZone() && !fLocFaction.isSafeZone();
    }

    boolean isWilderness(Location loc) {
        return Board.getInstance().getFactionAt(new FLocation(loc)).isWilderness();
    }

    boolean moneyEnabled() {
        return Econ.shouldBeUsed();
    }

    boolean checkRole(Player p, String role) {
        Role playerRole = FPlayers.getInstance().getByPlayer(p).getRole();
        if (playerRole == null) {
            return false;
        }
        Role adminRole;
        try {
            adminRole = Role.valueOf("ADMIN");
        } catch (Exception ex) {
            try {
                adminRole = Role.valueOf("LEADER");
            } catch (Exception ex2) {
                return false;
            }
        }
        Role coLeader = null;
        try {
            coLeader = Role.valueOf("COLEADER");
        } catch (Exception ignored) {}
        switch (role) {
            case "leader": case "admin":
                if (playerRole.equals(adminRole)) {
                    return true;
                }
                break;
            case "coleader": case "co-leader":
                if (playerRole.equals(coLeader) || playerRole.equals(adminRole)) {
                    return true;
                }
                break;
            case "moderator":
                if (playerRole.equals(Role.MODERATOR) || playerRole.equals(coLeader) || playerRole.equals(adminRole)) {
                    return true;
                }
                break;
            case "member": case "normal":
                if (playerRole.equals(Role.NORMAL) || playerRole.equals(Role.MODERATOR) || playerRole.equals(coLeader) || playerRole.equals(adminRole)) {
                    return true;
                }
                break;
            case "recruit": case "any":
                return true;
        }
        return false;
    }
}
