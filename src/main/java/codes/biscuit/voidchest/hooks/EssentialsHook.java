package codes.biscuit.voidchest.hooks;

import com.earth2me.essentials.Essentials;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

class EssentialsHook {

    private Essentials essentials;

    EssentialsHook(Plugin essentials) {
        this.essentials = (Essentials)essentials;
    }

    double getSellPrice(ItemStack sellItem) {
        try {
            return essentials.getWorth().getPrice(sellItem).doubleValue() * sellItem.getAmount();
        } catch (NullPointerException ex) {
            return 0;
        }
    }
}
