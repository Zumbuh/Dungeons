package net.jagsnet.minecraft.plugins.dungeons.listeners;

import net.jagsnet.minecraft.plugins.dungeons.configs.Config;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.List;

public class Events {
    public static void event() {
        for (Player p : Bukkit.getOnlinePlayers()) {

            String filename = p.getUniqueId().toString();
            Config dungeon = new Config();
            dungeon.setType("dungeons");
            if (!dungeon.existsConf(filename)) {return;}

//            System.out.println("exists");

            dungeon.loadConf(filename);
            String packName = dungeon.getConf().getString("pack");
            if (!p.getWorld().getName().equals(packName)) {return;}

//            System.out.println("right world");

            int chunkSize = dungeon.getConf().getInt("chunkSize");
            String location =
                    "x" +
                    (int) Math.floor(p.getLocation().getX()/chunkSize) +
                    "y" +
                    (int) Math.floor(p.getLocation().getY()/chunkSize) +
                    "z" +
                    (int) Math.floor(p.getLocation().getZ()/chunkSize);
            String tile = dungeon.getConf().getString(location);

//            System.out.println(location);

            if (tile == null) {return;}

//            System.out.println("tile exists in dungeon");

            Config pack = new Config();
            pack.setType("packs/" + packName);
            pack.loadConf(packName);
            if (pack.getConf().getString(tile) == null) {return;}

//            System.out.println("config exists in pack");

            List<String> eventNames = pack.getConf().getStringList(tile + ".events");

            for (int i = 0; i < eventNames.size(); i++) {
                boolean first = false;
                if (dungeon.getConf().getString(location + "." + eventNames.get(i) + ".lastTriggered") == null) {
                    dungeon.getConf().set(location + "." + eventNames.get(i) + ".lastTriggered", System.currentTimeMillis());
                    first = true;
                }

                if (!first && !(dungeon.getConf().getLong(location + "." + eventNames.get(i) + ".lastTriggered") < System.currentTimeMillis() - pack.getConf().getLong(tile + "." + eventNames.get(i) + ".cooldown"))) {return;}

                dungeon.getConf().set(location + "." + eventNames.get(i) + ".lastTriggered", System.currentTimeMillis());
//                System.out.println("not on cooldown");

                switch (pack.getConf().getString(tile + "." + eventNames.get(i) + ".event")) {
                    case "mob":
//                        System.out.println("spawning mobs");
                        spawnMobs(
                                pack.getConf().getString(location + "." + eventNames.get(i) + ".type"),
                                pack.getConf().getInt(location + "." + eventNames.get(i) + ".count"),
                                new Location(
                                        Bukkit.getWorld(packName),
                                        (int) Math.floor(p.getLocation().getX()/chunkSize) + pack.getConf().getInt(location + "." + eventNames.get(i) + ".offsetx"),
                                        (int) Math.floor(p.getLocation().getY()/chunkSize) + pack.getConf().getInt(location + "." + eventNames.get(i) + ".offsety"),
                                        (int) Math.floor(p.getLocation().getZ()/chunkSize) + pack.getConf().getInt(location + "." + eventNames.get(i) + ".offsetz")
                                )
                        );
                        break;

                    case "sound":
                        spawnSound(
                                pack.getConf().getString(location + "." + eventNames.get(i) + ".type"),
                                pack.getConf().getInt(location + "." + eventNames.get(i) + ".offsetx"),
                                pack.getConf().getInt(location + "." + eventNames.get(i) + ".offsety"),
                                pack.getConf().getInt(location + "." + eventNames.get(i) + ".offsetz"),
                                new Location(
                                        Bukkit.getWorld(packName),
                                        (int) Math.floor(p.getLocation().getX()/chunkSize),
                                        (int) Math.floor(p.getLocation().getY()/chunkSize),
                                        (int) Math.floor(p.getLocation().getZ()/chunkSize)
                                )
                        );
                        break;

                    default: break;
                }
            }
            dungeon.saveConf();
        }
    }

    public static void spawnMobs(String mob, int count, Location l) {
        for (int i = 0; i < count; i++) {
            Entity spawnedEntity = l.getWorld().spawnEntity(l, EntityType.valueOf(mob));
        }
        System.out.println("spawned mobs " + l.getX() + "x " + l.getY() + "y " + l.getZ() + "z " + l.getWorld().getName());
    }

    public static void spawnSound(String sound, int x, int y, int z, Location l) {

    }
}
