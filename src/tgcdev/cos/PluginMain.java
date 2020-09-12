package tgcdev.cos;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class PluginMain extends JavaPlugin implements Listener {
    private final static Map<HumanEntity, Inventory> viewers = new HashMap<>();

    @Override
    public void onDisable() {
        for (HumanEntity player:viewers.keySet()) player.closeInventory();
    }

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this,this);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (viewers.containsKey(event.getPlayer())) event.getPlayer().closeInventory();
    }

    @EventHandler
    public void onCloseInventory(InventoryCloseEvent event) {
        if (viewers.containsKey(event.getPlayer())) {
            Player player = (Player) event.getPlayer();
            ItemStack stack = player.getInventory().getItemInMainHand();
            ItemMeta meta = stack.getItemMeta();
            if (!(meta instanceof BlockStateMeta)) return;
            BlockStateMeta stateMeta = (BlockStateMeta) meta;
            if (!(stateMeta.getBlockState() instanceof ShulkerBox)) return;
            ShulkerBox box = (ShulkerBox) stateMeta.getBlockState();
            closeBox(player,box);
        }
    }

    public boolean isShulkerBox(ItemStack stack) {
        if (stack == null) return false;
        return stack.getType().name().contains("SHULKER_BOX");
    }

    public ShulkerBox getShulkerBox(ItemStack stack) {
        if (stack == null) return null;
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return null;
        if (!(meta instanceof BlockStateMeta)) return null;
        BlockStateMeta stateMeta = (BlockStateMeta) meta;
        if (!stack.hasItemMeta()) return null;
        if (!(stateMeta.getBlockState() instanceof ShulkerBox)) return null;
        return (ShulkerBox) stateMeta.getBlockState();
    }

    public void openBox(Player player,@Nullable ShulkerBox box) {
        Inventory inventory = Bukkit.createInventory(player,27,"Shulker Box");
        if (box != null) for (int i = 0;i < 27;i++) inventory.setItem(i,box.getInventory().getItem(i));
        player.openInventory(inventory);
        viewers.put(player,inventory);
    }

    public void closeBox(Player player,ShulkerBox box) {
        Inventory inv = viewers.get(player);
        for (int i = 0;i < 27;i++) {
            ItemStack item = inv.getItem(i);
            box.getSnapshotInventory().setItem(i,item);
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        BlockStateMeta meta = (BlockStateMeta) item.getItemMeta();
        meta.setBlockState(box);
        player.getInventory().getItemInMainHand().setItemMeta(meta);
    }

    @EventHandler
    public void onClickShulkerBox(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR) return;
        ItemStack stack = event.getItem();
        if (!isShulkerBox(stack)) return;
        event.setCancelled(true);
        openBox(event.getPlayer(),getShulkerBox(stack));
    }

    @EventHandler
    public void onClickItem(InventoryClickEvent event) {
        if (event.getSlotType() == InventoryType.SlotType.OUTSIDE) return;
        if (!isShulkerBox(event.getClickedInventory().getItem(event.getSlot()))) return;
        event.setCancelled(true);
    }
}
