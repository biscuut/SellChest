package codes.biscuit.voidchest.events;

import codes.biscuit.voidchest.VoidChest;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.ArrayList;

public class OtherEvents implements Listener {

    private VoidChest main;

    public OtherEvents(VoidChest main) {
        this.main = main;
    }

    @EventHandler
    public void onVoidChestEntityExplosion(EntityExplodeEvent e) {
        for (Block block : new ArrayList<>(e.blockList())) {
            if (main.getUtils().getChestLocations().containsKey(block.getLocation())) {
                e.blockList().remove(block);
            }
        }
    }

    @EventHandler
    public void onVoidChestBlockExplosion(BlockExplodeEvent e) {
        for (Block block : new ArrayList<>(e.blockList())) {
            if (main.getUtils().getChestLocations().containsKey(block.getLocation())) {
                e.blockList().remove(block);
            }
        }
    }

    @EventHandler
    public void onVoidChestBurn(BlockBurnEvent e) {
        if (main.getUtils().getChestLocations().containsKey(e.getBlock().getLocation())) {
            e.setCancelled(true);
        }
    }
}
