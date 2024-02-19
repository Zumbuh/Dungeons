package net.jagsnet.minecraft.plugins.dungeons.configs;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class Config {

    private File file;
    private FileConfiguration fileConf;
    private String type;

    public void setupConf(String configName){
        File file;
        FileConfiguration customFile;
        file = new File(Bukkit.getServer().getPluginManager().getPlugin("Dungeons").getDataFolder(), "/" + type + "/" + configName + ".yml");
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

    public FileConfiguration getConf(){
        return fileConf;
    }

    public void setType (String s) {
        type = s;
    }

    public void loadConf(String name){
        file = new File(Bukkit.getServer().getPluginManager().getPlugin("Dungeons").getDataFolder(), "/" + type + "/" + name + ".yml");
        fileConf = YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration loadGetConf(String name){
        File file = new File(Bukkit.getServer().getPluginManager().getPlugin("Dungeons").getDataFolder(), "/" + type + "/" + name + ".yml");
        return YamlConfiguration.loadConfiguration(file);
    }
    public void saveConf(){
        try{
            fileConf.save(file);
        }catch (IOException e){
            System.out.println("Couldn't save file");
        }
    }
    public boolean existsConf(String filename) {
        File file = new File(Bukkit.getServer().getPluginManager().getPlugin("Dungeons").getDataFolder(), "/" + type + "/" + filename + ".yml");
        if (file.exists()) {
            return true;
        }
        return false;
    }
    public boolean delDungeonConf(String filename) {
        File file = new File(Bukkit.getServer().getPluginManager().getPlugin("Dungeons").getDataFolder(), "/" + type + "/" + filename + ".yml");
        if (file.delete()) {
            return true;
        }
        return false;
    }
}
