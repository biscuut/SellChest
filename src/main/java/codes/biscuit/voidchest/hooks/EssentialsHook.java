package codes.biscuit.voidchest.hooks;

import com.earth2me.essentials.Essentials;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class EssentialsHook {

    private Essentials essentials;

    EssentialsHook(Plugin essentials) {
        this.essentials = (Essentials)essentials;
    }

    double getSellPrice(ItemStack sellItem) {
        return essentials.getWorth().getPrice(sellItem).doubleValue() * sellItem.getAmount(); //TODO is multiplication nessasary?
    }
}
