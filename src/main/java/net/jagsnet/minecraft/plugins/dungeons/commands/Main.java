package net.jagsnet.minecraft.plugins.dungeons.commands;

import net.jagsnet.minecraft.plugins.dungeons.scheduledtasks.Generator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import static net.jagsnet.minecraft.plugins.dungeons.configs.Configs.*;
import static net.jagsnet.minecraft.plugins.dungeons.otherStuff.Utils.sendMessage;

public class Main implements CommandExecutor {

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
        // --------------- Generate a vDungeon ------------------
        // ------------------------------------------------------
        if (args[0].equalsIgnoreCase("generate")) {
            if (!p.hasPermission("dungeons.use")) {
                sendMessage(p, "You do not have permission to use this command. If you believe you do require permission please contact your nearest admin.");
                return true;
            }
            if (args.length < 2) {
                sendMessage(p, "You need to specify a dungeon pack.");
                return true;
            }
            args[1] = args[1].toLowerCase();
            Generator g = new Generator();
            g.vGenCommand(p.getLocation(), args[1], p);
            return true;
        }

        // ------------------------------------------------------
        // ---------------- Delete a dungeon --------------------
        // ------------------------------------------------------
        if (args[0].equalsIgnoreCase("delete")) {
            if (!p.hasPermission("dungeons.use")) {
                sendMessage(p, "You do not have permission to use this command. If you believe you do require permission please contact your nearest admin.");
                return true;
            }
            delCommand(p);
            return true;
        }

        // ------------------------------------------------------
        // ----------- Get the code for your dungeon ------------
        // ------------------------------------------------------
        if (args[0].equalsIgnoreCase("getcode")) {
            if (!p.hasPermission("dungeons.use")) {
                sendMessage(p, "You do not have permission to use this command. If you believe you do require permission please contact your nearest admin.");
                return true;
            }
            if (!existsDungeonConf(p.getUniqueId().toString())) {
                sendMessage(p, "You do not seem to have a dungeon.");
                return true;
            }
            loadDungeonConf(p.getUniqueId().toString());
            sendMessage(p, "Your dungeon code is " + getDungeonConf().get("code"));
            return true;
        }

        // ------------------------------------------------------
        // ------------------- Join a dungeon -------------------
        // ------------------------------------------------------
        if (args[0].equalsIgnoreCase("join")) {
            if (!p.hasPermission("dungeons.use")) {
                sendMessage(p, "You do not have permission to use this command. If you believe you do require permission please contact your nearest admin.");
                return true;
            }
            if (existsDungeonConf(args[1])) {
                loadDungeonConf(Bukkit.getPlayer(args[1]).getUniqueId().toString());
            } else {
                if (!Bukkit.getPlayer(args[1]).isOnline()) {
                    sendMessage(p, "The player whos dungeon you are joining needs to be online.");
                    return true;
                }
                if (!existsDungeonConf(Bukkit.getPlayer(args[1]).getUniqueId().toString())) {
                    sendMessage(p, "This dungeon does not seem to exist.");
                    return true;
                }
                loadDungeonConf(Bukkit.getPlayer(args[1]).getUniqueId().toString());
                if (!getDungeonConf().get("code").equals(args[2])) {
                    sendMessage(p, "The code supplied was not correct");
                    return true;
                }
            }
            String[] start = getDungeonConf().getString("start").split("-");
            p.teleport(new Location(Bukkit.getWorld(getDungeonConf().getString("pack")), Integer.getInteger(start[0]) * 16 + 8, 66, Integer.getInteger(start[1]) * 16 + 8));
            return true;
        }

        sendMessage(p, "Not a valid dungeons command. Use /dungeons help to see available commands for you.");
        return true;
    }

    // ------------------------------------------------------
    // ------------ Player command functions ----------------
    // ------------------------------------------------------
    public void delCommand(org.bukkit.entity.Player p) {
        String filename = p.getUniqueId().toString();
        if (existsDungeonConf(filename)) {
            if (delDungeonConf(filename)) {
                sendMessage(p, "Your dungeon has been removed.");
                return;
            }
            sendMessage(p, "We couldn't delete your dungeon for some reason. Contact your nearest admin for assistance.");
            return;
        }
        sendMessage(p, "You do not have a dungeon to delete.");
        return;
    }
}
