package codes.biscuit.voidchest.hooks;

import net.brcdev.shopgui.ShopGuiPlusApi;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;


public class ShopGUIPlusHook {
    public double getSellPrice(Player p, ItemStack item) {
        double price = ShopGuiPlusApi.getItemStackPriceSell(p, item);
        if (price == -1.0) {
            return 0.0;
        } else {
            return price;
        }
        Essent
    }
}
