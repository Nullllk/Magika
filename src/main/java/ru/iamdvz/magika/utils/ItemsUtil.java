package ru.iamdvz.magika.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ItemsUtil {


    public static List<ItemStack> stringListToItemStack(List<String> itemList) {

        List<ItemStack> itemListIS = new ArrayList<>();

        String[] itemArgs;
        ItemStack itemTemp = new ItemStack(Material.FEATHER);
        PotionMeta itemTempPM = (PotionMeta) new ItemStack(Material.POTION).getItemMeta();
        ItemMeta itemTempM = itemTemp.getItemMeta();

        for (String item : itemList) {
            if (item.substring(0, item.indexOf("{")).equalsIgnoreCase("DELAY")) {
                for (int i = 0; i < Integer.parseInt(Objects.requireNonNull(ParserUtil.getFrom(item))); i++) {
                    itemListIS.add(new ItemStack(Material.AIR));
                }
            } else {
                itemArgs = ParserUtil.getParameters(Objects.requireNonNull(ParserUtil.getFrom(item)));
                itemTemp = new ItemStack(Material.valueOf(item.substring(0, item.indexOf("{")).toUpperCase()));
                if (itemTemp.getType() == Material.POTION) {
                    itemTempPM.setColor(ColorUtil.hexToRGBColor(ParserUtil.getParameterFromArr(itemArgs, "color")));
                    itemTempPM.setCustomModelData(Integer.parseInt(Objects.requireNonNull(ParserUtil.getParameterFromArr(itemArgs, "CMD"))));
                    itemTemp.setItemMeta(itemTempPM);
                } else {
                    itemTempM.setCustomModelData(Integer.parseInt(Objects.requireNonNull(ParserUtil.getParameterFromArr(itemArgs, "CMD"))));
                    itemTemp.setItemMeta(itemTempM);
                }
                itemListIS.add(itemTemp);
            }
        }
    return itemListIS;
    }
}
