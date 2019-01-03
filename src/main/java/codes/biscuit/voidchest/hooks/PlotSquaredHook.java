package codes.biscuit.voidchest.hooks;

import com.intellectualcrafters.plot.api.PlotAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

class PlotSquaredHook {

    private PlotAPI plotAPI = new PlotAPI();

    OfflinePlayer getPlotOwner(Location loc) {
        for (UUID randomOwner : plotAPI.getPlot(loc).getOwners()) {
            return Bukkit.getOfflinePlayer(randomOwner);
        }
        return null;
    }

    boolean isPlot(Location loc) {
        return plotAPI.getPlot(loc) != null;
    }
}
