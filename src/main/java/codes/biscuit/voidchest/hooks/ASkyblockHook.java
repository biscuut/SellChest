package codes.biscuit.voidchest.hooks;

import com.wasteofplastic.askyblock.ASkyBlockAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

class ASkyblockHook {
    OfflinePlayer getIslandOwner(Location loc) {
        return Bukkit.getOfflinePlayer(ASkyBlockAPI.getInstance().getOwner(loc));
    }

    boolean isIsland(Location loc) {
        return ASkyBlockAPI.getInstance().getIslandAt(loc) != null;
    }
}
