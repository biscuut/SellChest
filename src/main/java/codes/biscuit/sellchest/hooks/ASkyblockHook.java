package codes.biscuit.sellchest.hooks;

import com.wasteofplastic.askyblock.ASkyBlockAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

class ASkyblockHook {
    OfflinePlayer getIslandOwner(Location loc) {
        return Bukkit.getOfflinePlayer(ASkyBlockAPI.getInstance().getOwner(loc));
    }

    boolean isIsland(Location loc) {
        return ASkyBlockAPI.getInstance().getIslandAt(loc) != null;
    }

    // Note to self: remember i'm not stupid- getIslandOwnedBy also includes members...
    boolean islandIsSame(Location loc, Player p) {
        return ASkyBlockAPI.getInstance().getIslandOwnedBy(p.getUniqueId()).equals(ASkyBlockAPI.getInstance().getIslandAt(loc));
    }
}
