package codes.biscuit.sellchest.hooks;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.Worth;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;

class EssentialsHook {

    private Essentials essentials;

    EssentialsHook(Plugin essentials) {
        this.essentials = (Essentials)essentials;
    }

    double getSellPrice(ItemStack sellItem) {
//        BigDecimal price = essentials.getWorth().getPrice(sellItem);
        try {
            Worth worth = essentials.getWorth();
            BigDecimal price;
            try { // EssentialsX 2.15 & below
                Method getPrice = worth.getClass().getMethod("getPrice", ItemStack.class);
                price = (BigDecimal)getPrice.invoke(worth, sellItem);
            } catch (NoSuchMethodException ex) {
                try { // EssentialsX 2.16
                    Method getPrice = worth.getClass().getMethod("getPrice", IEssentials.class, ItemStack.class);
                    IEssentials iEssentials = (IEssentials)Bukkit.getServer().getPluginManager().getPlugin("Essentials");
                    price = (BigDecimal) getPrice.invoke(worth, iEssentials, sellItem);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex2) {
                    return 0;
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                return 0;
            }
            return price.doubleValue() * sellItem.getAmount();
        } catch (NullPointerException ex) {
            return 0;
        }
    }
}
