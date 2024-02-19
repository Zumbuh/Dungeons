package net.jagsnet.minecraft.plugins.dungeons.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;

public class LitematicTest implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void inventory1(InventoryEvent e) {
        msgMelon("---- InventoryEvent ----");
        msgMelon(e.getInventory().getHolder().toString());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void inventory2(InventoryMoveItemEvent e) {
        msgMelon("---- InventoryMoveItemEvent ----");
        msgMelon(e.getDestination().getHolder().toString());
        msgMelon(e.getItem().toString());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void inventory3(InventoryPickupItemEvent e) {
        msgMelon("---- InventoryPickupItemEvent ----");
        msgMelon(e.getInventory().getHolder().toString());
        msgMelon(e.getItem().toString());

    }

    public void msgMelon(String t){
        Player p = Bukkit.getPlayer("MelonMarauda");
        p.sendMessage(t);
    }
}
