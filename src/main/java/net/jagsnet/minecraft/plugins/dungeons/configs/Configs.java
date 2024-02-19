package net.jagsnet.minecraft.plugins.dungeons.configs;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class Configs {
    /*
    Player dungeon configs
    */
    public static void setupDungeonConf(String configName){
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
    private static File dungeonFile;
    private static FileConfiguration dungeonFileConf;
    public static FileConfiguration getDungeonConf(){
        return dungeonFileConf;
    }
    public static void loadDungeonConf(String name){
        dungeonFile = new File(Bukkit.getServer().getPluginManager().getPlugin("Dungeons").getDataFolder(), "/dungeons/" + name + ".yml");
        dungeonFileConf = YamlConfiguration.loadConfiguration(dungeonFile);
    }

    public static FileConfiguration loadGetDungeonConf(String name){
        File file = new File(Bukkit.getServer().getPluginManager().getPlugin("Dungeons").getDataFolder(), "/dungeons/" + name + ".yml");
        return YamlConfiguration.loadConfiguration(file);
    }
    public static void saveDungeonConf(){
        try{
            dungeonFileConf.save(dungeonFile);
        }catch (IOException e){
            System.out.println("Couldn't save file");
        }
    }
    public static boolean existsDungeonConf(String filename) {
        File file = new File(Bukkit.getServer().getPluginManager().getPlugin("Dungeons").getDataFolder(), "/dungeons/" + filename + ".yml");
        if (file.exists()) {
            return true;
        }
        return false;
    }
    public static boolean delDungeonConf(String filename) {
        File file = new File(Bukkit.getServer().getPluginManager().getPlugin("Dungeons").getDataFolder(), "/dungeons/" + filename + ".yml");
        if (file.delete()) {
            return true;
        }
        return false;
    }



    /*
    Dungeon pack configs
    */
    public static void setupPackConf(String filename){
        File file;
        FileConfiguration customFile;
        file = new File(Bukkit.getServer().getPluginManager().getPlugin("Dungeons").getDataFolder(), "/packs/" + filename + "/" + filename + ".yml");
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
    private static File packFile;
    private static FileConfiguration packFileConf;
    public static FileConfiguration getPackConf(){
        return packFileConf;
    }
    public static void loadPackConf(String filename){
        packFile = new File(Bukkit.getServer().getPluginManager().getPlugin("Dungeons").getDataFolder(), "/packs/" + filename + "/" + filename + ".yml");
        packFileConf = YamlConfiguration.loadConfiguration(packFile);
    }

    public static FileConfiguration loadGetPackConf(String filename){
        File file = new File(Bukkit.getServer().getPluginManager().getPlugin("Dungeons").getDataFolder(), "/packs/" + filename + "/" + filename + ".yml");
        return YamlConfiguration.loadConfiguration(file);
    }
    public static void savePackConf(){
        try{
            packFileConf.save(packFile);
        }catch (IOException e){
            System.out.println("Couldn't save file");
        }
    }
    public static boolean existsPackConf(String filename) {
        File file = new File(Bukkit.getServer().getPluginManager().getPlugin("Dungeons").getDataFolder(),  "/packs/" + filename + "/" + filename + ".yml");
        if (file.exists()) {
            return true;
        }
        try {
            System.out.println(file.getCanonicalPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
