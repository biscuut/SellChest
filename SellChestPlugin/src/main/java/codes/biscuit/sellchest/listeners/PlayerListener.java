package codes.biscuit.sellchest.listeners;

import codes.biscuit.sellchest.SellChest;
import codes.biscuit.sellchest.utils.ConfigValues;
import codes.biscuit.sellchest.utils.ReflectionUtils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Chest;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class PlayerListener implements Listener {

    private SellChest main;

    public PlayerListener(SellChest main) {
        this.main = main;
    }

    @SuppressWarnings("ConstantConditions")
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onVoidChestPlace(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        boolean bypassing = main.getUtils().getBypassPlayers().contains(p);
        if (!main.getHookUtils().isCancelled(e) || bypassing) {
            if (p.hasPermission("sellchest.place") || bypassing) {
                if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && e.getItem() != null && main.getUtils().isVoidChest(e.getItem())) {
                    if (bypassing || !main.getUtils().reachedLimit(p)) {
                        e.setCancelled(true);
                        Block newBlock;
                        Set<Material> placeOver = EnumSet.of(Material.AIR, Material.SNOW, Material.LAVA, Material.WATER);
                        try {
                            placeOver.add(Material.valueOf("LONG_GRASS"));
                        } catch (IllegalArgumentException ex) {
                            try {
                                placeOver.add(Material.valueOf("TALL_GRASS"));
                            } catch (IllegalArgumentException ignored) {}
                        }
                        try {
                            placeOver.add(Material.valueOf("STATIONARY_WATER"));
                            placeOver.add(Material.valueOf("STATIONARY_LAVA"));
                        } catch (IllegalArgumentException ignored) {}
                        if (e.getClickedBlock().getState() instanceof InventoryHolder && !p.isSneaking()) {
                            return;
                        } else if (placeOver.contains(e.getClickedBlock().getType())) {
                            newBlock = e.getClickedBlock(); // Blocks place directly on grass
                        } else {
                            newBlock = e.getClickedBlock().getRelative(e.getBlockFace());
                            if (!newBlock.getType().equals(Material.AIR)) { // For hackers/glitched block placement
                                return;
                            }
                        }
                        Block[] surroundingBlocks = {newBlock.getRelative(BlockFace.NORTH),
                                newBlock.getRelative(BlockFace.EAST),
                                newBlock.getRelative(BlockFace.SOUTH),
                                newBlock.getRelative(BlockFace.WEST)};
                        for (Block currentBlock : surroundingBlocks) {
                            if (currentBlock.getType().equals(Material.CHEST) && !main.getUtils().getChestLocations().containsKey(currentBlock.getLocation())) {
                                main.getUtils().sendMessage(p, ConfigValues.Message.SELLCHEST_BESIDE);
                                return;
                            }
                        }
                        newBlock.setType(Material.CHEST);
                        BlockState state = newBlock.getState();
                        Chest chest = new Chest(main.getUtils().getOppositeDirection(p));
                        state.setData(chest);
                        state.update();
                        state = newBlock.getState();
                        try {
                            if (ReflectionUtils.getVersion().contains("1_8") || ReflectionUtils.getVersion().contains("1_9") || ReflectionUtils.getVersion().contains("1_10") || ReflectionUtils.getVersion().contains("1_11")) {
                                Method getTileEntity = ReflectionUtils.getMethod(state.getClass(), "getTileEntity");//newBlock.getState().getClass().getDeclaredMethod("getTileEntity");
                                if (!getTileEntity.isAccessible()) {
                                    getTileEntity.setAccessible(true);
                                }
                                Object tileEntityChest = getTileEntity.invoke(newBlock.getState());
                                Method setDisplayName = ReflectionUtils.getMethod(tileEntityChest.getClass(), "a", String.class); //tileEntityChest.getClass().getMethod("a", String.class);
                                setDisplayName.invoke(tileEntityChest, main.getConfigValues().getChestTitle());
                            } else if (ReflectionUtils.getVersion().contains("1_12")) {
                                Field tileEntity = ReflectionUtils.getField(state.getClass().getSuperclass().getSuperclass().getSuperclass(), "tileEntity");//newBlock.getState().getClass().getSuperclass().getSuperclass().getSuperclass().getDeclaredField("tileEntity");
                                tileEntity.setAccessible(true);
                                Object tileEntityObject = tileEntity.get(newBlock.getState());
                                Field customName = ReflectionUtils.getField(tileEntityObject.getClass().getSuperclass(), "o");//tileEntityObject.getClass().getSuperclass().getDeclaredField("o");
                                customName.setAccessible(true);
                                customName.set(tileEntityObject, main.getConfigValues().getChestTitle());
                            } else if (ReflectionUtils.getVersion().contains("1_13")) {
                                Method getTileEntity = ReflectionUtils.getMethod(state.getClass().getSuperclass().getSuperclass().getSuperclass(), "getTileEntity");//newBlock.getState().getClass().getDeclaredMethod("getTileEntity");
                                getTileEntity.setAccessible(true);
                                Object tileEntityChest = getTileEntity.invoke(newBlock.getState());
                                Method setDisplayName = ReflectionUtils.getMethod(tileEntityChest.getClass().getSuperclass(), "setCustomName", ReflectionUtils.getNMSClass("IChatBaseComponent")); //tileEntityChest.getClass().getMethod("setCustomName", ReflectionUtils.getNMSClass("IChatBaseComponent"));
                                Constructor newChatComponentText = ReflectionUtils.getConstructor(ReflectionUtils.getNMSClass("ChatComponentText"), String.class); //ReflectionUtils.getNMSClass("ChatComponentText").getConstructor(String.class);
                                Object chatComponentText = newChatComponentText.newInstance(main.getConfigValues().getChestTitle());
                                setDisplayName.invoke(tileEntityChest, chatComponentText);
                            } else if (ReflectionUtils.getVersion().contains("1_14")) {
                                World world = newBlock.getWorld();
                                Method getWorldHandle = ReflectionUtils.getMethod(world.getClass(), "getHandle");
                                Object nmsWorld = getWorldHandle.invoke(world);
                                Method getTileEntity = ReflectionUtils.getMethod(nmsWorld.getClass().getSuperclass(), "getTileEntity", ReflectionUtils.getNMSClass("BlockPosition"));
                                getTileEntity.setAccessible(true);
                                Method getPosition = ReflectionUtils.getMethod(newBlock.getClass(), "getPosition");
                                Object blockPos = getPosition.invoke(newBlock);
                                Object tileEntityChest = getTileEntity.invoke(nmsWorld, blockPos);
                                Field customName = ReflectionUtils.getField(tileEntityChest.getClass().getSuperclass().getSuperclass(), "customName");
                                Constructor newChatComponentText = ReflectionUtils.getConstructor(ReflectionUtils.getNMSClass("ChatComponentText"), String.class); //ReflectionUtils.getNMSClass("ChatComponentText").getConstructor(String.class);
                                Object chatComponentText = newChatComponentText.newInstance(main.getConfigValues().getChestTitle());
                                customName.set(tileEntityChest, chatComponentText);
                            }
                        } catch (IllegalAccessException | InvocationTargetException | NullPointerException | InstantiationException ex) {
                            ex.printStackTrace();
                        }
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
                            p.getWorld().playSound(p.getLocation(), digSound, 1, 0.8F); // Pitch is by ear
                        if (p.getGameMode().equals(GameMode.SURVIVAL) || p.getGameMode().equals(GameMode.ADVENTURE)) {
                            ItemStack removeItem = e.getItem();
                            removeItem.setAmount(e.getItem().getAmount() - 1);
                            p.setItemInHand(removeItem);
                        }
                        main.getUtils().getChestLocations().put(newBlock.getLocation(), e.getPlayer().getUniqueId());
                        main.getUtils().sendMessage(p, ConfigValues.Message.PLACE);
                    } else {
                        e.setCancelled(true);
                    }
                }
            } else {
                main.getUtils().sendMessage(p, ConfigValues.Message.NO_PERMISSION_PLACE);
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
                    main.getUtils().sendMessage(e.getPlayer(), ConfigValues.Message.CHEST_BESIDE);
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
                            main.getUtils().sendMessage(p, ConfigValues.Message.NO_SPACE);
                            return;
                        }
                    }
                    main.getUtils().getChestLocations().remove(b.getLocation());
                    b.setType(Material.AIR);
                    Map excessItems = p.getInventory().addItem(main.getUtils().getSellChestItemStack(1));
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
                    p.getWorld().dropItemNaturally(b.getLocation(), main.getUtils().getSellChestItemStack(1));
                }
                main.getUtils().sendMessage(p, ConfigValues.Message.REMOVED);
            } else {
                main.getUtils().sendMessage(p, ConfigValues.Message.NOT_MINIMUM_FACTION);
            }
        } else {
            main.getUtils().sendMessage(p, ConfigValues.Message.NOT_OWNER);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (main.getConfigValues().sendUpdateMessages() && e.getPlayer().isOp()) {
            main.getUtils().checkUpdates(e.getPlayer());
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVoidChestEntityExplosion(EntityExplodeEvent e) {
        if (main.getConfigValues().unbreakableNaturally()) {
            for (Block block : new ArrayList<>(e.blockList())) {
                if (main.getUtils().getChestLocations().containsKey(block.getLocation())) {
                    e.blockList().remove(block);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVoidChestBlockExplosion(BlockExplodeEvent e) {
        if (main.getConfigValues().unbreakableNaturally()) {
            for (Block block : new ArrayList<>(e.blockList())) {
                if (main.getUtils().getChestLocations().containsKey(block.getLocation())) {
                    e.blockList().remove(block);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVoidChestBurn(BlockBurnEvent e) {
        if (main.getConfigValues().unbreakableNaturally()) {
            if (main.getUtils().getChestLocations().containsKey(e.getBlock().getLocation())) {
                e.setCancelled(true);
            }
        }
    }
}
