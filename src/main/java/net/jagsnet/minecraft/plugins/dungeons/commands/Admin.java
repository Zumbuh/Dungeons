package net.jagsnet.minecraft.plugins.dungeons.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import static net.jagsnet.minecraft.plugins.dungeons.otherStuff.Utils.sendMessage;

public class Admin implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof org.bukkit.entity.Player)) {
            System.out.println("Needs to be run by a player.");
            return true;
        }
        org.bukkit.entity.Player p = (org.bukkit.entity.Player) sender;
        if (args.length < 1) {
            sendMessage(p, "Not a valid dungeons command. Use //dungeons help to see available commands for you.");
            return true;
        }

        // ------------------------------------------------------
        // --------------- Paste schematics ---------------------
        // ------------------------------------------------------
        if (args[0].equalsIgnoreCase("generate")) {
            if (!p.hasPermission("dungeons.admin")) {
                sendMessage(p, "You do not have permission to use this command. If you believe you do require permission please contact your nearest admin.");
                return true;
            }



            //vPaste(getGenConf().getString("x" + X + "y" + Y + "z" + Z).split(""), X, Y, Z, pack, flat, chunkSize);

        }
        return true;
    }
}
