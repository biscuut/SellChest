package codes.biscuit.voidchest.events;

import codes.biscuit.voidchest.VoidChest;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Chest;

import java.util.Map;

public class PlayerEvents implements Listener {

    private VoidChest main;

    public PlayerEvents(VoidChest main) {
        this.main = main;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onVoidChestPlace(PlayerInteractEvent e) {
        if (!e.isCancelled()) {
            if (e.getPlayer().hasPermission("voidchest.place")) {
                if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && e.getItem() != null && main.getUtils().isVoidChest(e.getItem())) {
                    e.setCancelled(true);
                    Block newBlock;
                    if (e.getClickedBlock().getType().equals(Material.LONG_GRASS)) {
                        newBlock = e.getClickedBlock(); // Blocks place directly on grass (remind me if i'm missing more blocks)
                    } else {
                        newBlock = e.getClickedBlock().getRelative(e.getBlockFace());
                    }
                    Block[] surroundingBlocks = {newBlock.getRelative(BlockFace.NORTH),
                            newBlock.getRelative(BlockFace.EAST),
                            newBlock.getRelative(BlockFace.SOUTH),
                            newBlock.getRelative(BlockFace.WEST)};
                    for (Block currentBlock : surroundingBlocks) {
                        if (currentBlock.getType().equals(Material.CHEST) && !main.getUtils().getChestLocations().containsKey(currentBlock.getLocation())) {
                            if (!main.getConfigUtils().getMessageVoidChestBeside().equals("")) e.getPlayer().sendMessage(main.getConfigUtils().getMessageVoidChestBeside());
                            return;
                        }
                    }
                    newBlock.setType(Material.CHEST);
                    BlockState state = newBlock.getState();
                    Chest chest = new Chest(main.getUtils().getOppositeDirection(e.getPlayer()));
                    state.setData(chest);
                    state.update();
                    e.getPlayer().getWorld().playSound(e.getPlayer().getLocation(), Sound.STEP_WOOD, 1, 1);
                    if (e.getPlayer().getGameMode().equals(GameMode.SURVIVAL) || e.getPlayer().getGameMode().equals(GameMode.ADVENTURE)) {
                        ItemStack removeItem = e.getItem();
                        removeItem.setAmount(e.getItem().getAmount() - 1);
                        e.getPlayer().setItemInHand(removeItem);
                    }
                    main.getUtils().addConfigLocation(newBlock.getLocation(), e.getPlayer());
                    if (!main.getConfigUtils().getMessagePlace().equals("")) e.getPlayer().sendMessage(main.getConfigUtils().getMessagePlace());
                }
            } else {
                if (!main.getConfigUtils().getNoPermissionPlaceMessage().equals("")) e.getPlayer().sendMessage(main.getConfigUtils().getNoPermissionPlaceMessage());
            }
        }
    }

    @EventHandler
    public void onChestPlace(BlockPlaceEvent e) {
        if (e.getBlock().getType().equals(Material.CHEST)) { // and is not a voidchest (above is cancelled actually... hmm)
            Block[] surroundingBlocks = {e.getBlock().getRelative(BlockFace.NORTH),
                    e.getBlock().getRelative(BlockFace.EAST),
                    e.getBlock().getRelative(BlockFace.SOUTH),
                    e.getBlock().getRelative(BlockFace.WEST)};
            for (Block currentBlock : surroundingBlocks) {
                if (currentBlock.getType().equals(Material.CHEST) && main.getUtils().getChestLocations().containsKey(currentBlock.getLocation())) {
                    if (!main.getConfigUtils().getMessageChestBeside().equals("")) e.getPlayer().sendMessage(main.getConfigUtils().getMessageChestBeside());
                    e.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onVoidChestBreak(BlockBreakEvent e) {
        Block b = e.getBlock();
        if (b.getType().equals(Material.CHEST) &&
                main.getUtils().getChestLocations().containsKey(b.getLocation())) {
            e.setCancelled(true);
            breakVoidChest(e.getPlayer(), b);
        }
    }

    @EventHandler
    public void onVoidChestDamage(BlockDamageEvent e) {
        Block b = e.getBlock();
        if (main.getConfigUtils().sneakToBreak() && b.getType().equals(Material.CHEST) &&
                main.getUtils().getChestLocations().containsKey(b.getLocation()) && e.getPlayer().isSneaking()) {
            e.setCancelled(true);
            breakVoidChest(e.getPlayer(), b);
        }
    }

    private void breakVoidChest(Player p, Block b) {
        // OfflinePlayer offlineP = main.getUtils().getChestLocations().get(b.getLocation()); // This is the owner TODO: remove this if works
        if (main.getHookUtils().canBreakChest(b.getLocation(), p)) {
            if (main.getHookUtils().isMinimumFaction(p)) {
                if (main.getConfigUtils().breakIntoInventory()) {
                    if (main.getConfigUtils().breakDontDropIfFull()) {
                        if (p.getInventory().firstEmpty() == -1) {
                            if (!main.getConfigUtils().getMessageNoSpace().equals(""))
                                p.sendMessage(main.getConfigUtils().getMessageNoSpace());
                            return;
                        }
                    }
                    main.getUtils().getChestLocations().remove(b.getLocation());
                    b.setType(Material.AIR);
                    main.getUtils().removeConfigLocation(b.getLocation(), p);
                    Map excessItems = p.getInventory().addItem(main.getUtils().getVoidChestItemStack(1));
                    for (Object excessItem : excessItems.values()) {
                        int itemCount = ((ItemStack) excessItem).getAmount();
                        while (itemCount > 64) {
                            ((ItemStack) excessItem).setAmount(64);
                            p.getWorld().dropItemNaturally(p.getLocation(), (ItemStack) excessItem);
                            itemCount = itemCount - 64;
                        }
                        if (itemCount > 0) {
                            ((ItemStack) excessItem).setAmount(itemCount);
                            p.getWorld().dropItemNaturally(p.getLocation(), (ItemStack) excessItem);
                        }
                    }
                } else {
                    main.getUtils().getChestLocations().remove(b.getLocation());
                    b.setType(Material.AIR);
                    main.getUtils().removeConfigLocation(b.getLocation(), p);
                    p.getWorld().dropItemNaturally(b.getLocation(), main.getUtils().getVoidChestItemStack(1));
                }
                if (!main.getConfigUtils().getMessageRemove().equals(""))
                    p.sendMessage(main.getConfigUtils().getMessageRemove());
            } else {
                if (!main.getConfigUtils().getNotMinimumFactionMessage().equals("")) p.sendMessage(main.getConfigUtils().getNotMinimumFactionMessage());
            }
        } else {
            if (!main.getConfigUtils().getMessageNotOwner().equals("")) p.sendMessage(main.getConfigUtils().getMessageNotOwner());
        }
    }
}
