package ru.iamdvz.magika.instant;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.spells.InstantSpell;
import com.nisovin.magicspells.util.MagicConfig;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;

public class VanishHandItem extends InstantSpell {

    private int vanishDuration;
    private int customModelData;
    private String itemType;

    public VanishHandItem(MagicConfig config, String spellName) {
        super(config, spellName);
        vanishDuration = getConfigInt("vanish-duration", 20);
        customModelData = getConfigInt("custom-model-data", 1);
        itemType = getConfigString("item-type", "feather").toUpperCase();

        try {
            Material.valueOf(itemType);
        } catch (Exception e) {
            MagicSpells.error("VanishHandItem '" + internalName + "' has an invalid item-type ("+itemType+")");
        }

        if (vanishDuration <= 0) {
            MagicSpells.error("VanishHandItem '" + internalName + "' has vanish-duration below zero! ("+vanishDuration+")");
            vanishDuration = 20;
        }
    }
    @Override
    public PostCastAction castSpell(LivingEntity caster, SpellCastState state, float power, String[] args) {
        Player player = (Player) caster;
        if (player.getEquipment().getItemInMainHand().getType() == Material.AIR) {
            return null;
        }
        ItemStack handItemCopy = player.getEquipment().getItemInMainHand().clone();
        ItemStack handItem = player.getEquipment().getItemInMainHand();
        int slotNum = 0;
        for (int i = 0; i<9; i++) {
            if (Objects.equals(player.getInventory().getItem(i), handItemCopy)) {
                slotNum = i;
            }
        }
        ItemMeta handItemM = handItem.getItemMeta();
        handItemM.setDisplayName(" ");
        handItemM.setCustomModelData(customModelData);
        handItem.setType(Material.valueOf(itemType));
        handItem.setAmount(1);
        handItem.setItemMeta(handItemM);

        int finalSlotNum = slotNum;
        Bukkit.getScheduler().runTaskLater(MagicSpells.getInstance(), new Runnable() {
            @Override
            public void run() {
                player.getInventory().setItem(finalSlotNum, handItemCopy);
            }
        }, vanishDuration);

        return null;
    }
}
