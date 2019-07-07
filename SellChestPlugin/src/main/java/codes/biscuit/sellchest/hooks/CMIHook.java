package codes.biscuit.sellchest.hooks;

import com.Zrips.CMI.Modules.Worth.WorthItem;
import org.bukkit.inventory.ItemStack;
import com.Zrips.CMI.CMI;

class CMIHook {

    double getSellPrice(ItemStack sellItem) {
        WorthItem worth = CMI.getInstance().getWorthManager().getWorth(sellItem);
        if (worth != null) {
            return worth.getSellPrice() * sellItem.getAmount();
        } else {
            return 0;
        }
    }
}
