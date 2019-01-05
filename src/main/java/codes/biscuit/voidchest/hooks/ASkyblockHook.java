package codes.biscuit.voidchest.hooks;

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

    // getIslandOwnedBy also includes members- so no, you're not stupid future self.
    boolean islandIsSame(Location loc, Player p) {
        return ASkyBlockAPI.getInstance().getIslandOwnedBy(p.getUniqueId()).equals(ASkyBlockAPI.getInstance().getIslandAt(loc));
    }
}
