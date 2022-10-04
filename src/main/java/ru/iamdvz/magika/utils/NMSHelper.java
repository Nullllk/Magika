package ru.iamdvz.magika.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class NMSHelper {

        public static void sendPacket(Player player, Object packet) throws NoSuchMethodException, NoSuchFieldException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
            Object handle = getHandle(player);
            Object playerConnection = handle.getClass().getField("playerConnection").get(handle);

            playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(playerConnection, packet);
        }

        public static Object getHandle(Player player) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
            return player.getClass().getMethod("getHandle").invoke(player);
        }

        public static Class<?> getNMSClass(String name) throws ClassNotFoundException {
            return Class.forName("net.minecraft.server." + getVersion() + "." + name);
        }

        public static Class<?> getCraftBukkitClass(String name) throws ClassNotFoundException {
            return Class.forName("org.bukkit.craftbukkit." + getVersion() + "." + name);
        }

        public static String getVersion() {
            return Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        }

        private static Method minecraftItemStack = null;
        public static Object toMinecraftItemStack(ItemStack itemStack) throws InvocationTargetException, IllegalAccessException, NoSuchFieldException, ClassNotFoundException, NoSuchMethodException {
            if (minecraftItemStack == null) minecraftItemStack = NMSHelper.getCraftBukkitClass("inventory.CraftItemStack")
                    .getMethod("asNMSCopy", ItemStack.class);
            return minecraftItemStack.invoke(null, itemStack);
        }
}
