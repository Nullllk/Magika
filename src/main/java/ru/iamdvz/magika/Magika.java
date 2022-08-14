package ru.iamdvz.magika;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class Magika extends JavaPlugin {
    private static Magika instance;

    public static Plugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        Bukkit.getConsoleSender().sendMessage("\n" +
                "§a§l┌─────────────────────────────────────────────────────┐\n" +
                "§a§l│                                                     §a§l│\n" +
                "§a§l│   §b███§1╗   §b███§1╗ §b█████§1╗  §b██████§1╗ §b██§1╗ §b██████§1╗ █████§b╗    §a§l│\n" +
                "§a§l│   §b████§1╗ §b████§1║§b██§1╔══§b██§1╗§b██§1╔════╝ §b██§1║§b█§b█§1╔════╝██§b╔══§1██§b╗   §a§l│\n" +
                "§a§l│   §b██§1╔§b████§1╔§b██§1║§b███████§1║§b██§1║  §b███§1╗§b██§1║§b██§1║     ███████§b║   §a§l│\n" +
                "§a§l│   §b██§1║╚§b██§1╔╝§b██§1║§b██§1╔══§b██§1║§b██§1║   §b█§b█§1║§b██§1║§b██§1║     §1██§b╔══§1██§b║   §a§l│\n" +
                "§a§l│   §b██§1║ ╚═╝ §b██§1║§b██§1║  §b██§1║§1╚§b██████§1╔╝§b██§1║╚§b██████§1╗██§b║  §1██§b║   §a§l│\n" +
                "§a§l│   §1╚═╝     ╚═╝╚═╝  ╚═╝ ╚═════╝ ╚═╝ ╚═════╝§b╚═╝  ╚═╝   §a§l│\n" +
                "§a§l│ §b§lmade by §1§l@§c§liamDvz                                     §a§l│\n" +
                "§a§l└─────────────────────────────────────────────────────┘");
    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
