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

import java.util.HashMap;
import java.util.Objects;

public class DenizenFlagParseSpell extends TargetedSpell implements TargetedEntitySpell {
    private String flagName;
    private String variableName;
    private String parseType;
    private boolean targeted;
    private int slot;
    private String equipmentSlot;


    public DenizenFlagParseSpell(MagicConfig config, String spellName) {
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
        return false;
    }

    @Override
    public boolean castAtEntity(LivingEntity target, float power) {
        return false;
    }

    private void setFlag(Player caster, Player target) {
        Player person = caster;
        if (targeted) {
            person = target;
        }
        NBTItem nbti = new NBTItem(new ItemStack(Material.FEATHER));
        if (slot != 0 && person.getInventory().getItem(slot).getType() != Material.AIR) {
            nbti = new NBTItem(Objects.requireNonNull(person.getInventory().getItem(EquipmentSlot.valueOf(equipmentSlot))));
        }
        if ((!Objects.equals(equipmentSlot, "NONE")) && person.getInventory().getItem(EquipmentSlot.valueOf(equipmentSlot)).getType() != Material.AIR) {
            nbti = new NBTItem(Objects.requireNonNull(person.getInventory().getItem(EquipmentSlot.valueOf(equipmentSlot))));
        }
        try { nbti.getCompound("Denizen").getKeys(); } catch (Exception e) {return;}
        String denizenFlags = nbti.getCompound("Denizen").getString("flags");
        for (String flag : denizenFlags.substring(5, denizenFlags.length() - 1).split(";")){
            if (Objects.equals(flagName, flag.split("=")[0])){
                person.sendMessage("FOUND!");
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
