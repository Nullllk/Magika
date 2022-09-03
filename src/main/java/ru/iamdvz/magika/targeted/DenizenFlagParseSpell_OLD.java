package ru.iamdvz.magika.targeted;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.spells.TargetedEntitySpell;
import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.util.MagicConfig;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class DenizenFlagParseSpell_OLD extends TargetedSpell implements TargetedEntitySpell {
    private final String flagName;
    private final String variableName;
    private final String parseType;
    private final boolean targeted;
    private final int slot;
    private final String equipmentSlot;


    public DenizenFlagParseSpell_OLD(MagicConfig config, String spellName) {
        super(config, spellName);
        flagName = getConfigString("flag-name", null);
        variableName = getConfigString("variable-name", flagName);
        parseType = getConfigString("parse-type", "SET");
        targeted = getConfigBoolean("targeted", false);
        slot = getConfigInt("slot", 0);
        equipmentSlot = getConfigString("equipment-slot", "NONE").toUpperCase();
    }

    @Override
    public PostCastAction castSpell(LivingEntity caster, SpellCastState state, float power, String[] args) {
        if (state == SpellCastState.NORMAL && caster instanceof Player player) {
            setFlag((Player) caster, null);
        }
        return PostCastAction.HANDLE_NORMALLY;
    }

    @Override
    public boolean castAtEntity(LivingEntity caster, LivingEntity target, float power) {
        if (caster instanceof Player player1 && target instanceof Player player2) {
            setFlag((Player) caster, (Player) target);
        }
        return true;
    }

    @Override
    public boolean castAtEntity(LivingEntity target, float power) {
        if (target instanceof Player player) {
            setFlag((Player) target, (Player) target);
        }
        return true;
    }

    private void setFlag(Player caster, Player target) {
        Player person = caster;
        if (targeted) {
            person = target;
        }
        NBTItem nbtItem = new NBTItem(new ItemStack(Material.FEATHER));
        if (slot != 0 && Objects.requireNonNull(person.getInventory().getItem(slot)).getType() != Material.AIR) {
            nbtItem = new NBTItem(Objects.requireNonNull(person.getInventory().getItem(EquipmentSlot.valueOf(equipmentSlot))));
        }
        if ((!Objects.equals(equipmentSlot, "NONE")) && person.getInventory().getItem(EquipmentSlot.valueOf(equipmentSlot)).getType() != Material.AIR) {
            nbtItem = new NBTItem(Objects.requireNonNull(person.getInventory().getItem(EquipmentSlot.valueOf(equipmentSlot))));
        }
        try { nbtItem.getCompound("Denizen").getKeys(); } catch (Exception e) {return;}
        String denizenFlags = nbtItem.getCompound("Denizen").getString("flags");
        for (String flag : denizenFlags.substring(5, denizenFlags.length() - 1).split(";")){
            if (Objects.equals(flagName, flag.split("=")[0])){
                switch (parseType.toUpperCase()) {
                    case "SET":
                        MagicSpells.getVariableManager().set(variableName, person, Double.parseDouble(flag.split("=")[1]));
                    case "ADD":
                        MagicSpells.getVariableManager().set(variableName, person, MagicSpells.getVariableManager().getValue(variableName, person)+Double.parseDouble(flag.split("=")[1]));
                }
            }
        }
    }
}
