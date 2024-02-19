package net.jagsnet.minecraft.plugins.dungeons.otherStuff;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class Utils {
    // ------------- Send Formatted Message To Player ------------------
    public static void sendMessage(Player player, String msg){
        String bold = ChatColor.BOLD + "";
        player.sendMessage(ChatColor.RED + bold + "DUNGEONS" + ChatColor.DARK_GRAY + bold + " > " + ChatColor.WHITE + msg);
    }

    public static void debug(String msg){
        Bukkit.getLogger().log(Level.INFO, msg);
    }

    public static void melonDebug(String msg){
        Player p = Bukkit.getPlayer("MelonMarauda");
        if (p.isOnline()) {
            p.sendMessage(msg);
        }
    }

    public static void log(String s){
        //Dungeons.getInstance().getLogger().log(LogLevel.INFO, System.currentTimeMillis() + );
    }
}
