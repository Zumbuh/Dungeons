package net.jagsnet.minecraft.plugins.dungeons.scheduledtasks;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import net.jagsnet.minecraft.plugins.dungeons.Dungeons;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static net.jagsnet.minecraft.plugins.dungeons.configs.Configs.*;
import static net.jagsnet.minecraft.plugins.dungeons.otherStuff.Utils.sendMessage;
import static org.bukkit.Bukkit.getServer;

public class Generator {
    int straights = 0;
    int curves = 0;
    int chunkSize = 0;
    int hatch = 0;
    int worms = 0;
    int finishedWorms = 0;
    int maxAge = 0;
    boolean flat = false;
    int breaks = 0;
    int minX;
    int minY;
    int minZ;
    int maxX;
    int maxY;
    int maxZ;
    Player p;
    String pack;

    int family = 0;
    int moves = 0;

    // ------------------------------------------------------
    // ---------------- 3D maze generator -------------------
    // ------------------------------------------------------
    public void vGenCommand(Location l, String type, Player player) {
        pack = type;
        p = player;
        if (p != null) {
            if (Dungeons.isGenerating()) {
                sendMessage(p, "A dungeon is currently generating, please wait for it to finish before starting generation of another.");
                return;
            }
            if (existsDungeonConf(p.getUniqueId().toString())) {
                sendMessage(p, "You already have a dungeon. Either remove it using the /dungeons delete command or wait for it to expire in x hours.");
                return;
            }
            if (!existsPackConf(pack)) {
                sendMessage(p, "The pack specified does not exist.");
                return;
            }
            Dungeons.setGenerating(true);
            sendMessage(p, "Generation started!");
            setupGenConf(p.getUniqueId().toString());
            loadGenConf(p.getUniqueId().toString());
        } else {
            String loc = Math.floor(l.getX()) + Math.floor(l.getY()) + Math.floor(l.getZ()) + l.getWorld().getName();
            setupGenConf(loc);
            loadGenConf(loc);
        }
        loadPackConf(pack);
        int boundx = getPackConf().getInt("sizex");
        int boundy = getPackConf().getInt("sizey");
        int boundz = getPackConf().getInt("sizez");
        straights = getPackConf().getInt("straights");
        curves = getPackConf().getInt("curves");
        chunkSize = getPackConf().getInt("chunkSize");
        hatch = getPackConf().getInt("hatch");
        flat = getPackConf().getBoolean("flat");
        maxAge = getPackConf().getInt("maxAge");
        breaks = getPackConf().getInt("ogres");
        family = getPackConf().getInt("family");
        worms = 1;
        finishedWorms = 0;
        int x = l.getChunk().getX();
        int z = l.getChunk().getZ();
        getGenConf().set("start", x + "-" + z);
        getGenConf().set("end", (x+boundx) + "-" + (z+boundz));
        getGenConf().set("code", ThreadLocalRandom.current().nextInt(100000, 999999));
        getGenConf().set("pack", pack);
        getGenConf().set("chunkSize", chunkSize);
        saveGenConf();
        minX = x-1;
        minY = 0;
        minZ = z-1;
        maxX = x+boundx+1;
        maxY = boundy+1;
        maxZ = z+boundz+1;
        String[] walls = {"B", "B", "B", "B", "B", "B", "Z"};
        String wayBack = "Z";
        vMazeGen(x, 1, z, walls, wayBack, maxAge, 1);
        return;
    }

    public void vMazeGen(int x, int y, int z, String[] walls, String wayBack, int age, int wormNum) {
        moves++;
//        System.out.println(walls[0] + walls[1] + walls[2] + walls[3] + walls[4] + walls[5] + " | Worm " + wormNum);
//        System.out.println("Worms = " + worms + " | hatch = " + hatch + " | Finished worms = " + finishedWorms + " | Worm " + wormNum);

        BukkitScheduler scheduler = getServer().getScheduler();
        if (age <= 0) {
            finishedWorms++;
            if (worms <= finishedWorms) {
                saveGenConf();
                System.out.println("- - - - - - - - - - - - - - - - - - - - - - - - - - -");
                System.out.println("MAZE GENERATION FINISHED. MAZE PASTING BEGUN");
                System.out.println("- - - - - - - - - - - - - - - - - - - - - - - - - - -");
                breakWalls(breaks);
            }
            return;
        }

        if (vCantMove(x, y, z)) {

            // vPaste(walls, x, y, z, pack);

            switch (wayBack) {
                case "Z":

                    finishedWorms++;
                    if (worms <= finishedWorms) {
                        saveGenConf();
                        System.out.println("- - - - - - - - - - - - - - - - - - - - - - - - - - -");
                        System.out.println("MAZE GENERATION FINISHED. MAZE PASTING BEGUN");
                        System.out.println("- - - - - - - - - - - - - - - - - - - - - - - - - - -");
                        breakWalls(breaks);
                    }

                    return;
                case "N":
//                    System.out.println("Going Back North | Worm " + wormNum);
                    String[] finalWalls1 = vGetNorth(x, y, z);
                    String back1 = finalWalls1[6];
                    scheduler.scheduleSyncDelayedTask(Dungeons.getInstance(), () -> vMazeGen(x, y, z - 1, finalWalls1, back1, age - 1, wormNum), worms - finishedWorms);
                    return;
                case "E":
//                    System.out.println("Going Back East | Worm " + wormNum);
                    String[] finalWalls2 = vGetEast(x, y, z);
                    String back2 = finalWalls2[6];
                    scheduler.scheduleSyncDelayedTask(Dungeons.getInstance(), () -> vMazeGen(x + 1, y, z, finalWalls2, back2, age - 1, wormNum), worms - finishedWorms);
                    return;
                case "S":
//                    System.out.println("Going Back South | Worm " + wormNum);
                    String[] finalWalls3 = vGetSouth(x, y, z);
                    String back3 = finalWalls3[6];
                    scheduler.scheduleSyncDelayedTask(Dungeons.getInstance(), () -> vMazeGen(x, y, z + 1, finalWalls3, back3, age - 1, wormNum), worms - finishedWorms);
                    return;
                case "W":
//                    System.out.println("Going Back West | Worm " + wormNum);
                    String[] finalWalls4 = vGetWest(x, y, z);
                    String back4 = finalWalls4[6];
                    scheduler.scheduleSyncDelayedTask(Dungeons.getInstance(), () -> vMazeGen(x - 1, y, z, finalWalls4, back4, age - 1, wormNum), worms - finishedWorms);
                    return;
                case "U":
//                    System.out.println("Going Back Up | Worm " + wormNum);
                    String[] finalWalls5 = vGetUp(x, y, z);
                    String back5 = finalWalls5[6];
                    scheduler.scheduleSyncDelayedTask(Dungeons.getInstance(), () -> vMazeGen(x, y + 1, z, finalWalls5, back5, age - 1, wormNum), worms - finishedWorms);
                    return;
                case "D":
//                    System.out.println("Going Back Down | Worm " + wormNum);
                    String[] finalWalls6 = vGetDown(x, y, z);
                    String back6 = finalWalls6[6];
                    scheduler.scheduleSyncDelayedTask(Dungeons.getInstance(), () -> vMazeGen(x, y - 1, z, finalWalls6, back6, age - 1, wormNum), worms - finishedWorms);
                    return;
                default: return;
            }
        }

        ArrayList<String> freeLoc = new ArrayList<>();
        if (vGetNorth(x, y, z) == null) {
            freeLoc.add(freeLoc.size(), "N");
            if (straights > 0 && wayBack.equals("S")) {
                for (int i = 0; i < straights; i++) {
                    freeLoc.add(freeLoc.size(), "N");
                }
            }
            if (curves > 0) {
                if (wayBack.equals("W")) {
                    for (int i = 0; i < curves; i++) {
                        freeLoc.add(freeLoc.size(), "N");
                    }
                }
                if (wayBack.equals("E")) {
                    for (int i = 0; i < curves; i++) {
                        freeLoc.add(freeLoc.size(), "N");
                    }
                }
            }
        }

        if (vGetEast(x, y, z) == null) {
            freeLoc.add(freeLoc.size(), "E");
            if (straights > 0 && wayBack.equals("W")) {
                for (int i = 0; i < straights; i++) {
                    freeLoc.add(freeLoc.size(), "E");
                }
            }
            if (curves > 0) {
                if (wayBack.equals("N")) {
                    for (int i = 0; i < curves; i++) {
                        freeLoc.add(freeLoc.size(), "E");
                    }
                }
                if (wayBack.equals("S")) {
                    for (int i = 0; i < curves; i++) {
                        freeLoc.add(freeLoc.size(), "E");
                    }
                }
            }
        }

        if (vGetSouth(x, y, z) == null) {
            freeLoc.add(freeLoc.size(), "S");
            if (straights > 0 && wayBack.equals("N")) {
                for (int i = 0; i < straights; i++) {
                    freeLoc.add(freeLoc.size(), "S");
                }
            }
            if (curves > 0) {
                if (wayBack.equals("W")) {
                    for (int i = 0; i < curves; i++) {
                        freeLoc.add(freeLoc.size(), "S");
                    }
                }
                if (wayBack.equals("E")) {
                    for (int i = 0; i < curves; i++) {
                        freeLoc.add(freeLoc.size(), "S");
                    }
                }
            }
        }

        if (vGetWest(x, y, z) == null) {
            freeLoc.add(freeLoc.size(), "W");
            if (straights > 0 && wayBack.equals("E")) {
                for (int i = 0; i < straights; i++) {
                    freeLoc.add(freeLoc.size(), "W");
                }
            }
            if (curves > 0) {
                if (wayBack.equals("N")) {
                    for (int i = 0; i < curves; i++) {
                        freeLoc.add(freeLoc.size(), "W");
                    }
                }
                if (wayBack.equals("S")) {
                    for (int i = 0; i < curves; i++) {
                        freeLoc.add(freeLoc.size(), "W");
                    }
                }
            }
        }

        if (vGetUp(x, y, z) == null) {
            freeLoc.add(freeLoc.size(), "U");
        }
        if (vGetDown(x, y, z) == null) {
            freeLoc.add(freeLoc.size(), "D");
        }

        int newWorms = 1;
        if (freeLoc.size() > 1 && worms < family) {
            int random = ThreadLocalRandom.current().nextInt(0, 100);
            if (random <= hatch) {
                newWorms++;
            }
        }

        for (int i = 1; i <= newWorms; i++) {
            Random generator = new Random();
            int freeLocLoc = generator.nextInt(freeLoc.size());
            String direction = freeLoc.get(freeLocLoc);

            ArrayList<String> freeLoc2 = new ArrayList<>();
            if (newWorms == 2) {
                for (String s : freeLoc) {
                    if (!s.equals(direction)) {
                        freeLoc2.add(freeLoc2.size(), s);
                    }
                }
                freeLoc = freeLoc2;
            }

            if (i == 2) {
                worms++;
            }
            switch (direction) {
                case "N":
                    walls[0] = "A";
                    if (i == 2) {
//                        System.out.println("Going North | Worm " + worms);
                        vSaveChunk(x, y, z, walls, wayBack, worms);
                        vSaveChunk(x, y, z - 1, new String[]{"B", "B", "A", "B", "B", "B"}, "S", worms);
                        scheduler.scheduleSyncDelayedTask(Dungeons.getInstance(), () -> vMazeGen(x, y, z - 1, new String[]{"B", "B", "A", "B", "B", "B", "S"}, "S", maxAge, worms), worms - finishedWorms);
                    } else {
//                        System.out.println("Going North | Worm " + wormNum);
                        vSaveChunk(x, y, z, walls, wayBack, wormNum);
                        vSaveChunk(x, y, z - 1, new String[]{"B", "B", "A", "B", "B", "B"}, "S", wormNum);
                        scheduler.scheduleSyncDelayedTask(Dungeons.getInstance(), () -> vMazeGen(x, y, z - 1, new String[]{"B", "B", "A", "B", "B", "B", "S"}, "S", age - 1, wormNum), worms - finishedWorms);
                    }
                    break;
                case "E":
                    walls[1] = "A";
                    if (i == 2) {
//                        System.out.println("Going North | Worm " + worms);
                        vSaveChunk(x, y, z, walls, wayBack, worms);
                        vSaveChunk(x + 1, y, z, new String[]{"B", "B", "B", "A", "B", "B"}, "W", worms);
                        scheduler.scheduleSyncDelayedTask(Dungeons.getInstance(), () -> vMazeGen(x + 1, y, z, new String[]{"B", "B", "B", "A", "B", "B", "W"}, "W", maxAge, worms), worms - finishedWorms);
                    } else {
//                        System.out.println("Going North | Worm " + wormNum);
                        vSaveChunk(x, y, z, walls, wayBack, wormNum);
                        vSaveChunk(x + 1, y, z, new String[]{"B", "B", "B", "A", "B", "B"}, "W", wormNum);
                        scheduler.scheduleSyncDelayedTask(Dungeons.getInstance(), () -> vMazeGen(x + 1, y, z, new String[]{"B", "B", "B", "A", "B", "B", "W"}, "W", age - 1, wormNum), worms - finishedWorms);
                    }
                    break;
                case "S":
                    walls[2] = "A";
                    if (i == 2) {
//                        System.out.println("Going North | Worm " + worms);
                        vSaveChunk(x, y, z, walls, wayBack, worms);
                        vSaveChunk(x, y, z + 1, new String[]{"A", "B", "B", "B", "B", "B"}, "N", worms);
                        scheduler.scheduleSyncDelayedTask(Dungeons.getInstance(), () -> vMazeGen(x, y, z + 1, new String[]{"A", "B", "B", "B", "B", "B", "N"}, "N", maxAge, worms), worms - finishedWorms);
                    } else {
//                        System.out.println("Going North | Worm " + wormNum);
                        vSaveChunk(x, y, z, walls, wayBack, wormNum);
                        vSaveChunk(x, y, z + 1, new String[]{"A", "B", "B", "B", "B", "B"}, "N", wormNum);
                        scheduler.scheduleSyncDelayedTask(Dungeons.getInstance(), () -> vMazeGen(x, y, z + 1, new String[]{"A", "B", "B", "B", "B", "B", "N"}, "N", age - 1, wormNum), worms - finishedWorms);
                    }
                    break;
                case "W":
                    walls[3] = "A";
                    if (i == 2) {
//                        System.out.println("Going North | Worm " + worms);
                        vSaveChunk(x, y, z, walls, wayBack, worms);
                        vSaveChunk(x - 1, y, z, new String[]{"B", "A", "B", "B", "B", "B"}, "E", worms);
                        scheduler.scheduleSyncDelayedTask(Dungeons.getInstance(), () -> vMazeGen(x - 1, y, z, new String[]{"B", "A", "B", "B", "B", "B", "E"}, "E", maxAge, worms), worms - finishedWorms);
                    } else {
//                        System.out.println("Going North | Worm " + wormNum);
                        vSaveChunk(x, y, z, walls, wayBack, wormNum);
                        vSaveChunk(x - 1, y, z, new String[]{"B", "A", "B", "B", "B", "B"}, "E", wormNum);
                        scheduler.scheduleSyncDelayedTask(Dungeons.getInstance(), () -> vMazeGen(x - 1, y, z, new String[]{"B", "A", "B", "B", "B", "B", "E"}, "E", age - 1, wormNum), worms - finishedWorms);
                    }
                    break;
                case "U":
                    walls[4] = "A";
                    if (i == 2) {
//                        System.out.println("Going North | Worm " + worms);
                        vSaveChunk(x, y, z, walls, wayBack, worms);
                        vSaveChunk(x, y + 1, z, new String[]{"B", "B", "B", "B", "B", "A"}, "D", worms);
                        scheduler.scheduleSyncDelayedTask(Dungeons.getInstance(), () -> vMazeGen(x, y + 1, z, new String[]{"B", "B", "B", "B", "B", "A", "D"}, "D", maxAge, worms), worms - finishedWorms);
                    } else {
//                        System.out.println("Going North | Worm " + wormNum);
                        vSaveChunk(x, y, z, walls, wayBack, wormNum);
                        vSaveChunk(x, y + 1, z, new String[]{"B", "B", "B", "B", "B", "A"}, "D", wormNum);
                        scheduler.scheduleSyncDelayedTask(Dungeons.getInstance(), () -> vMazeGen(x, y + 1, z, new String[]{"B", "B", "B", "B", "B", "A", "D"}, "D", age - 1, wormNum), worms - finishedWorms);
                    }
                    break;
                case "D":
                    walls[5] = "A";
                    if (i == 2) {
//                        System.out.println("Going North | Worm " + worms);
                        vSaveChunk(x, y, z, walls, wayBack, worms);
                        vSaveChunk(x, y - 1, z, new String[]{"B", "B", "B", "B", "A", "B"}, "U", worms);
                        scheduler.scheduleSyncDelayedTask(Dungeons.getInstance(), () -> vMazeGen(x, y - 1, z, new String[]{"B", "B", "B", "B", "A", "B", "U"}, "U", maxAge, worms), worms - finishedWorms);
                    } else {
//                        System.out.println("Going North | Worm " + wormNum);
                        vSaveChunk(x, y, z, walls, wayBack, wormNum);
                        vSaveChunk(x, y - 1, z, new String[]{"B", "B", "B", "B", "A", "B"}, "U", wormNum);
                        scheduler.scheduleSyncDelayedTask(Dungeons.getInstance(), () -> vMazeGen(x, y - 1, z, new String[]{"B", "B", "B", "B", "A", "B", "U"}, "U", age - 1, wormNum), worms - finishedWorms);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public boolean vCantMove(int x, int y, int z) {
        return vGetNorth(x, y, z) != null &&
                vGetEast(x, y, z) != null &&
                vGetSouth(x, y, z) != null &&
                vGetWest(x, y, z) != null &&
                vGetUp(x, y, z) != null &&
                vGetDown(x, y, z) != null;
    }

    public String[] vGetNorth(int x, int y, int z) {
        return vGet(x, y, z - 1);
    }
    public String[] vGetEast(int x, int y, int z) {
        return vGet(x + 1, y, z);
    }
    public String[] vGetSouth(int x, int y, int z) {
//        z = z + 1;
        return vGet(x, y, z + 1);
    }
    public String[] vGetWest(int x, int y, int z) {
//        x = x - 1;
        return vGet(x - 1, y , z);
    }
    public String[] vGetUp(int x, int y, int z) {
//        y = y + 1;
        return vGet(x, y + 1, z);
    }
    public String[] vGetDown(int x, int y, int z) {
//        y = y - 1;
        return vGet(x, y -1, z);
    }

    public String[] vGet(int x, int y, int z) {
        if (minX >= x || maxX <= x || minY >= y || maxY <= y || minZ >= z || maxZ <= z) {
            return new String[]{"B", "B", "B", "B", "B", "B"};
        }
        if (getGenConf().getString("x" + x + "y" + y + "z" + z) == null) return null;
        return getGenConf().getString("x" + x + "y" + y + "z" + z).split("");
    }
    public void vSaveChunk(int x, int y, int z, String[] walls, String wayBack, int worm) {
//        System.out.println("Saving " + "x" + x + "y" + y + "z" + z + "." + walls[0] + walls[1] + walls[2] + walls[3] + walls[4] + walls[5] + wayBack + " | Worm " + worm);
        getGenConf().set("x" + x + "y" + y + "z" + z, walls[0] + walls[1] + walls[2] + walls[3] + walls[4] + walls[5] + wayBack);
//        if (worm > 0) {
//            getGenConf().set(worm + "." + "x" + x + "y" + y + "z" + z, walls[0] + walls[1] + walls[2] + walls[3] + walls[4] + walls[5] + wayBack + " | " + moves);
//        }
        saveGenConf();
    }
    public void breakWalls(int ogres) {
        if (ogres > 0) {
            ogres = ogres - 1;

            int x = ThreadLocalRandom.current().nextInt(minX + 2, maxX - 1);
            int z = ThreadLocalRandom.current().nextInt(minZ + 2, maxZ - 1);

            int y = 1;

            if (vGet(x, y, z) != null) {
                String[] a;

                a = vGetNorth(x, y, z);
                a = a != null ? new String[]{a[0], a[1], "A", a[3], a[4], a[5]} : new String[]{"B", "B", "A", "B", "B", "B"};
                vSaveChunk(x, y, z - 1, a, "O", 0);

                a = vGetEast(x, y, z);
                a = a != null ? new String[]{a[0], a[1], a[2], "A", a[4], a[5]} : new String[]{"B", "B", "B", "A", "B", "B"};
                vSaveChunk(x + 1, y, z, a, "O", 0);

                a = vGetSouth(x, y, z);
                a = a != null ? new String[]{"A", a[1], a[2], a[3], a[4], a[5]} : new String[]{"A", "B", "B", "B", "B", "B"};
                vSaveChunk(x, y, z + 1, a, "O", 0);

                a = vGetWest(x, y, z);
                a = a != null ? new String[]{a[0], "A", a[2], a[3], a[4], a[5]} : new String[]{"B", "A", "B", "B", "B", "B"};
                vSaveChunk(x - 1, y, z, a, "O", 0);

                if (!flat) {
                    y = ThreadLocalRandom.current().nextInt(minY + 2, maxY - 1);

                    vSaveChunk(x, y, z, new String[]{"A", "A", "A", "A", "A", "A"}, "O", 0);

                    a = vGetUp(x, y, z);
                    a = a != null ? new String[]{a[1], a[1], a[2], a[3], a[4], "A"} : new String[]{"B", "B", "B", "B", "B", "A"};
                    vSaveChunk(x, y + 1, z, a, "O", 0);

                    a = vGetDown(x, y, z);
                    a = a != null ? new String[]{a[1], a[1], a[2], a[3], "A", a[5]} : new String[]{"B", "B", "B", "B", "A", "B"};
                    vSaveChunk(x, y - 1, z, a, "O", 0);
                } else {
                    vSaveChunk(x, y, z, new String[]{"A", "A", "A", "A", "B", "B"}, "O", 0);
                }
            }

            breaks = ogres;
            BukkitScheduler scheduler = getServer().getScheduler();
            scheduler.scheduleSyncDelayedTask(Dungeons.getInstance(), () -> breakWalls(breaks), 1L);
            return;
        }
        pasteLoop(minX, minY, minZ);
    }

    public void pasteLoop(int X, int Y, int Z) {

        if (getGenConf().getString("x" + X + "y" + Y + "z" + Z) != null) {
            vPaste(getGenConf().getString("x" + X + "y" + Y + "z" + Z).split(""), X, Y, Z, pack, flat, chunkSize);
        }

        if (X <= maxX) {
            X++;
        } else if (Y <= maxY) {
            X = minX;
            Y++;
        } else if (Z <= maxZ) {
            X = minX;
            Y = minY;
            Z++;
        } else {
            if (p != null) {
                if (p.isOnline()) {
                    sendMessage(p, "Ding! Your dungeon is ready to go!");
                    sendMessage(p, "Your dungeon code is " + getGenConf().get("code"));
                    sendMessage(p, "Give that code to those who you want to join you.");
                }
            }
            Dungeons.setGenerating(false);
            return;
        }

        int finalX = X;
        int finalY = Y;
        int finalZ = Z;

        BukkitScheduler scheduler = getServer().getScheduler();
        scheduler.scheduleSyncDelayedTask(Dungeons.getInstance(), () -> pasteLoop(finalX, finalY, finalZ), 1L);
    }

    public void vPaste(String[] walls, int X, int Y, int Z, String pack, boolean flat, int chunkSize) {
        File[] fileList = new File(Bukkit.getServer().getPluginManager().getPlugin("Dungeons").getDataFolder() + "/packs/" + pack + "/schematics/").listFiles();
        ArrayList<String> fileNames = new ArrayList<String>();

        for (int i = 0; i < fileList.length; i++) {
            fileNames.add(i, fileList[i].getName());
        }
        Clipboard clipboard = null;
        File file = null;
        String chunkVar;
        if (flat) {
            chunkVar = walls[0] + walls[1] + walls[2] + walls[3];
        } else {
            chunkVar = walls[0] + walls[1] + walls[2] + walls[3] + walls[4] + walls[5];
        }
//        System.out.println("Attempting to find a schematic for " + chunkVar);
        ArrayList<File> pickFrom = new ArrayList<File>();
        for (int i = 0; i < fileNames.size(); i++) {
            String currentFile = fileNames.get(i);
            if (currentFile.contains(chunkVar)) {
                pickFrom.add(pickFrom.size(), fileList[i]);
            }
        }
        int ok = 1;
        if (pickFrom.size() == 0) {
            ok = 0;
        } else if (pickFrom.size() == 1) {
            file = pickFrom.get(0);
        } else {
            int randomNum = ThreadLocalRandom.current().nextInt(0, pickFrom.size());
            file = pickFrom.get(randomNum);
        }

        getGenConf().set("x" + X + "y" + Y + "z" + Z, file.getName().split("\\.")[0]);
        saveGenConf();

        World world = Bukkit.getWorld(pack);
        com.sk89q.worldedit.world.World world2 = BukkitAdapter.adapt(world);

        if (ok == 1) {
//            System.out.println("Pasting " + file.getName() + " at x" + X * chunkSize + " Y" + (10 + (chunkSize*Y)) + " z" + Z * chunkSize);
            ClipboardFormat format = ClipboardFormats.findByFile(file);
            try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
                clipboard = reader.read();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world2, -1)) {
                Operation operation = new ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .to(BlockVector3.at(X * chunkSize, Y * chunkSize, Z * chunkSize))
                        .ignoreAirBlocks(true)
                        .build();
                Operations.complete(operation);
            } catch (WorldEditException e) {
                e.printStackTrace();
            }
        }
    }

    public void setupGenConf(String configName){
        File file;
        FileConfiguration customFile;
        file = new File(Bukkit.getServer().getPluginManager().getPlugin("Dungeons").getDataFolder(), "/dungeons/" + configName + ".yml");
        if (!file.exists()){
            try{
                file.createNewFile();
            }catch (IOException e){
            }
        }
        customFile = YamlConfiguration.loadConfiguration(file);
        try{
            customFile.save(file);
        }catch (IOException e){
            System.out.println("Couldn't save file");
        }
    }
    private File dungeonGenFile;
    private FileConfiguration dungeonFileGenConf;
    public FileConfiguration getGenConf(){
        return dungeonFileGenConf;
    }
    public void loadGenConf(String name){
        dungeonGenFile = new File(Bukkit.getServer().getPluginManager().getPlugin("Dungeons").getDataFolder(), "/dungeons/" + name + ".yml");
        dungeonFileGenConf = YamlConfiguration.loadConfiguration(dungeonGenFile);
    }
    public void saveGenConf(){
        try{
            dungeonFileGenConf.save(dungeonGenFile);
        }catch (IOException e){
            System.out.println("Couldn't save file");
        }
    }
}
