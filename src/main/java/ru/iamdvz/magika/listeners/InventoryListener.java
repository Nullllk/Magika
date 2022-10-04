package ru.iamdvz.magika.listeners;

import com.nisovin.magicspells.spells.passive.DropItemListener;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import ru.iamdvz.magika.instant.VanishHandItem;

public class InventoryListener implements Listener {

    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        if (event.getItemDrop().getItemStack().getType() == Material.valueOf(VanishHandItem.getType())
            && event.getItemDrop().getItemStack().getItemMeta().getCustomModelData() == VanishHandItem.getCustomModelData()) {
            event.setCancelled(true);
        }
    }
}