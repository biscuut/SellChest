package codes.biscuit.sellchest.hooks;

import org.bukkit.event.player.PlayerInteractEvent;

public interface MinecraftAbstraction {

    boolean isCancelled(PlayerInteractEvent e);
}
