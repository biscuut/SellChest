package codes.biscuit.voidchest.events;

import codes.biscuit.voidchest.VoidChest;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerEvents implements Listener {

    private VoidChest main;

    public PlayerEvents(VoidChest main) {
        this.main = main;
    }

    @EventHandler
    public void onVoidChestPlace(PlayerInteractEvent e) {
        if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && e.getItem() != null && main.getUtils().isVoidChest(e.getItem())) {
            Block newBlock = e.getClickedBlock().getRelative(e.getBlockFace());
            Block[] surroundingBlocks = {newBlock.getRelative(BlockFace.NORTH),
                    newBlock.getRelative(BlockFace.EAST),
                    newBlock.getRelative(BlockFace.SOUTH),
                    newBlock.getRelative(BlockFace.WEST)};
            e.setCancelled(true);
            for (Block currentBlock : surroundingBlocks) {
                if (currentBlock.getType().equals(Material.CHEST) && !main.getLocations().containsKey(currentBlock.getLocation())) {
                    if (!main.getConfigUtils().getMessageChestBeside().equals("")) e.getPlayer().sendMessage(main.getConfigUtils().getMessageChestBeside());
                    return;
                }
            }
            newBlock.setType(Material.CHEST);
            if (e.getPlayer().getGameMode().equals(GameMode.SURVIVAL) || e.getPlayer().getGameMode().equals(GameMode.ADVENTURE)) {
                ItemStack removeItem = e.getItem();
                removeItem.setAmount(e.getItem().getAmount()-1);
                e.getPlayer().setItemInHand(removeItem);
            }
            main.getUtils().addConfigLocation(newBlock.getLocation(), e.getPlayer());
            if (!main.getConfigUtils().getMessagePlace().equals("")) e.getPlayer().sendMessage(main.getConfigUtils().getMessagePlace());
        }
    }

    @EventHandler
    public void onChestPlace(BlockPlaceEvent e) {
        if (e.getBlock().getType().equals(Material.CHEST)) { // TODO && is not voidchest necessary?
            Block[] surroundingBlocks = {e.getBlock().getRelative(BlockFace.NORTH),
                    e.getBlock().getRelative(BlockFace.EAST),
                    e.getBlock().getRelative(BlockFace.SOUTH),
                    e.getBlock().getRelative(BlockFace.WEST)};
            for (Block currentBlock : surroundingBlocks) {
                if (currentBlock.getType().equals(Material.CHEST) && main.getLocations().containsKey(currentBlock.getLocation())) {
                    if (!main.getConfigUtils().getMessageChestBeside().equals("")) e.getPlayer().sendMessage(main.getConfigUtils().getMessageChestBeside());
                    e.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onVoidChestDamage(BlockDamageEvent e) {
        if (e.getBlock().getType().equals(Material.CHEST) && main.getLocations().containsKey(e.getBlock().getLocation())) {
            e.setCancelled(true);
            Player p = e.getPlayer();
            OfflinePlayer offlineP = main.getLocations().get(e.getBlock().getLocation());
            if (offlineP.getPlayer().getUniqueId().equals(p.getUniqueId())) {
                if (p.getInventory().firstEmpty() != -1) {
                    p.sendMessage(main.getConfigUtils().getMessageRemove());
                    main.getLocations().remove(e.getBlock().getLocation());
                    e.getBlock().setType(Material.AIR);
                    p.getInventory().addItem(main.getUtils().getVoidChestItemStack(1));
                    main.getUtils().removeConfigLocation(e.getBlock().getLocation(), e.getPlayer());
                } else {
                    p.sendMessage(main.getConfigUtils().getMessageNoSpace());
                }
            } else {
                p.sendMessage(main.getConfigUtils().getMessageNotOwner());
            }
        }
    }

    @EventHandler
    public void onVoidChestBreak(BlockBreakEvent e) {
        if (e.getBlock().getType().equals(Material.CHEST) && main.getLocations().containsKey(e.getBlock().getLocation())) {
            e.setCancelled(true);
            Player p = e.getPlayer();
            OfflinePlayer offlineP = main.getLocations().get(e.getBlock().getLocation());
            if (offlineP.getPlayer().getUniqueId().equals(p.getUniqueId())) {
                if (p.getInventory().firstEmpty() != -1) {
                    p.sendMessage(main.getConfigUtils().getMessageRemove());
                    main.getLocations().remove(e.getBlock().getLocation());
                    e.getBlock().setType(Material.AIR);
                    p.getInventory().addItem(main.getUtils().getVoidChestItemStack(1));
                    main.getUtils().removeConfigLocation(e.getBlock().getLocation(), e.getPlayer());
                } else {
                    p.sendMessage(main.getConfigUtils().getMessageNoSpace());
                }
            } else {
                p.sendMessage(main.getConfigUtils().getMessageNotOwner());
            }
        }
    }
}
