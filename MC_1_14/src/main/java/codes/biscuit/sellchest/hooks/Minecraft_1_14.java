package codes.biscuit.sellchest.hooks;

import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

public class Minecraft_1_14 implements MinecraftAbstraction {

    public boolean isCancelled(PlayerInteractEvent e) {
        return e.useItemInHand() == Event.Result.DENY;
    }
}
