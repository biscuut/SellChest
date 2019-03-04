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
import org.bukkit.inventory.InventoryHolder;
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
        boolean bypassing = main.getUtils().getBypassPlayers().contains(e.getPlayer());
        if (!e.isCancelled() || bypassing) {
            if (e.getPlayer().hasPermission("voidchest.place") || bypassing) {
                if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && e.getItem() != null && main.getUtils().isVoidChest(e.getItem())) {
                    e.setCancelled(true);
                    Block newBlock;
                    Material longGrass = null;
                    try {
                        longGrass = Material.valueOf("LONG_GRASS");
                    } catch (IllegalArgumentException ex) {
                        try {
                            longGrass = Material.valueOf("TALL_GRASS");
                        } catch (IllegalArgumentException ignored) {
                        }
                    }
                    if (e.getClickedBlock().getState() instanceof InventoryHolder && !e.getPlayer().isSneaking()) {
                        return;
                    } else if (e.getClickedBlock().getType().equals(longGrass)) {
                        newBlock = e.getClickedBlock(); // Blocks place directly on grass
                    } else {
                        newBlock = e.getClickedBlock().getRelative(e.getBlockFace());
                        if (!newBlock.getType().equals(Material.AIR)) { // For hackers
                            return;
                        }
                    }
                    Block[] surroundingBlocks = {newBlock.getRelative(BlockFace.NORTH),
                            newBlock.getRelative(BlockFace.EAST),
                            newBlock.getRelative(BlockFace.SOUTH),
                            newBlock.getRelative(BlockFace.WEST)};
                    for (Block currentBlock : surroundingBlocks) {
                        if (currentBlock.getType().equals(Material.CHEST) && !main.getUtils().getChestLocations().containsKey(currentBlock.getLocation())) {
                            if (!main.getConfigValues().getMessageVoidChestBeside().equals(""))
                                e.getPlayer().sendMessage(main.getConfigValues().getMessageVoidChestBeside());
                            return;
                        }
                    }
                    newBlock.setType(Material.CHEST);
                    BlockState state = newBlock.getState();
                    Chest chest = new Chest(main.getUtils().getOppositeDirection(e.getPlayer()));
                    state.setData(chest);
                    state.update();
                    Sound digSound = null;
                    try {
                        digSound = Sound.valueOf("DIG_WOOD"); // Sound for 1.8
                    } catch (Exception ex) {
                        try {
                            digSound = Sound.valueOf("BLOCK_WOOD_PLACE"); // 1.9+
                        } catch (Exception ignored) {
                        }
                    }
                    if (digSound != null)
                        e.getPlayer().getWorld().playSound(e.getPlayer().getLocation(), digSound, 1, 0.8F); // Pitch is by ear
                    if (e.getPlayer().getGameMode().equals(GameMode.SURVIVAL) || e.getPlayer().getGameMode().equals(GameMode.ADVENTURE)) {
                        ItemStack removeItem = e.getItem();
                        removeItem.setAmount(e.getItem().getAmount() - 1);
                        e.getPlayer().setItemInHand(removeItem);
                    }
                    main.getUtils().addConfigLocation(newBlock.getLocation(), e.getPlayer());
                    if (!main.getConfigValues().getMessagePlace().equals(""))
                        e.getPlayer().sendMessage(main.getConfigValues().getMessagePlace());
                }
            } else {
                if (!main.getConfigValues().getNoPermissionPlaceMessage().equals(""))
                    e.getPlayer().sendMessage(main.getConfigValues().getNoPermissionPlaceMessage());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChestPlace(BlockPlaceEvent e) {
        if (e.getBlock().getType().equals(Material.CHEST)) {
            Block[] surroundingBlocks = {e.getBlock().getRelative(BlockFace.NORTH),
                    e.getBlock().getRelative(BlockFace.EAST),
                    e.getBlock().getRelative(BlockFace.SOUTH),
                    e.getBlock().getRelative(BlockFace.WEST)};
            for (Block currentBlock : surroundingBlocks) {
                if (currentBlock.getType().equals(Material.CHEST) && main.getUtils().getChestLocations().containsKey(currentBlock.getLocation())) {
                    if (!main.getConfigValues().getMessageChestBeside().equals("")) e.getPlayer().sendMessage(main.getConfigValues().getMessageChestBeside());
                    e.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onVoidChestBreak(BlockBreakEvent e) {
        boolean bypassing = main.getUtils().getBypassPlayers().contains(e.getPlayer());
        if (!e.isCancelled() || bypassing) {
            Block b = e.getBlock();
            if (b.getType().equals(Material.CHEST) &&
                    main.getUtils().getChestLocations().containsKey(b.getLocation())) {
                e.setCancelled(true);
                breakVoidChest(e.getPlayer(), b);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onVoidChestDamage(BlockDamageEvent e) {
        boolean bypassing = main.getUtils().getBypassPlayers().contains(e.getPlayer());
        if (!e.isCancelled() || bypassing) {
            Block b = e.getBlock();
            if (main.getConfigValues().sneakToBreak() && b.getType().equals(Material.CHEST) &&
                    main.getUtils().getChestLocations().containsKey(b.getLocation()) && e.getPlayer().isSneaking()) {
                e.setCancelled(true);
                breakVoidChest(e.getPlayer(), b);
            }
        }
    }

    private void breakVoidChest(Player p, Block b) {
        boolean bypassing = main.getUtils().getBypassPlayers().contains(p);
        if (bypassing || main.getHookUtils().canBreakChest(b.getLocation(), p)) {
            if (bypassing || main.getHookUtils().isMinimumFaction(p, b.getLocation())) {
                if (main.getConfigValues().breakIntoInventory()) {
                    if (main.getConfigValues().breakDontDropIfFull()) {
                        if (p.getInventory().firstEmpty() == -1) {
                            if (!main.getConfigValues().getMessageNoSpace().equals(""))
                                p.sendMessage(main.getConfigValues().getMessageNoSpace());
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
                if (!main.getConfigValues().getMessageRemove().equals(""))
                    p.sendMessage(main.getConfigValues().getMessageRemove());
            } else {
                if (!main.getConfigValues().getNotMinimumFactionMessage().equals("")) p.sendMessage(main.getConfigValues().getNotMinimumFactionMessage());
            }
        } else {
            if (!main.getConfigValues().getMessageNotOwner().equals("")) p.sendMessage(main.getConfigValues().getMessageNotOwner());
        }
    }
}
