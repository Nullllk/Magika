package ru.iamdvz.magika;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import ru.iamdvz.magika.listeners.InventoryListener;

public final class Magika extends JavaPlugin {
    private ProtocolManager protocolManager;
    private static Magika instance;
    public static Plugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new InventoryListener(), this);
        ProtocolManager manager = ProtocolLibrary.getProtocolManager();

    }

    //@Override
    //public void onDisable() {
        // Plugin shutdown logic
    //}
}
