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

    public void openBox(Player player,ShulkerBox box) {
        Inventory inventory = Bukkit.createInventory(player,27,"Shulker Box");
        for (int i = 0;i < 27;i++) inventory.setItem(i,box.getInventory().getItem(i));
        player.openInventory(inventory);
    }

    public void closeBox(Player player,ShulkerBox box) {
        Inventory inv = viewers.get(player);
        for (int i = 0;i < 27;i++) {
            ItemStack item = inv.getItem(i);
            if (getShulkerBox(item) != null) box.getInventory().setItem(i,new ItemStack(Material.AIR));
            box.getInventory().setItem(i,item);
        }
    }

    @EventHandler
    public void onClickShulkerBox(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR) return;
        ItemStack stack = event.getItem();
        ShulkerBox box = getShulkerBox(stack);
        if (box == null) return;
        event.setCancelled(true);
        openBox(event.getPlayer(),box);
    }

    @EventHandler
    public void onClickItem(InventoryClickEvent event) {
        if (event.getSlotType() == InventoryType.SlotType.OUTSIDE) return;
        if (getShulkerBox(event.getClickedInventory().getItem(event.getSlot())) == null) return;
        event.setCancelled(true);
    }
}
