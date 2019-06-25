package codes.biscuit.sellchest.hooks;

import com.intellectualcrafters.plot.api.PlotAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

class PlotSquaredHook {

    private PlotAPI plotAPI = new PlotAPI();

    OfflinePlayer getPlotOwner(Location loc) {
        Set<UUID> owners = plotAPI.getPlot(loc).getOwners();
        if (owners.isEmpty()) {
            return null;
        } else {
            return Bukkit.getOfflinePlayer(owners.stream().findFirst().get());
        }
    }

    boolean isPlot(Location loc) {
        return plotAPI.getPlot(loc) != null;
    }

    boolean plotIsSame(Location loc, Player p) {
        return plotAPI.getPlot(p).equals(plotAPI.getPlot(loc));
    }
}
