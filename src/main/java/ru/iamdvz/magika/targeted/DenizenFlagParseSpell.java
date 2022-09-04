package ru.iamdvz.magika.targeted;

import com.denizenscript.denizen.objects.ItemTag;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.spells.TargetedEntitySpell;
import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.util.MagicConfig;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class DenizenFlagParseSpell extends TargetedSpell implements TargetedEntitySpell {
    private final String flagName;
    private final String variableName;
    private final String parseType;
    private final boolean targeted;
    private final int slot;
    private final String equipmentSlot;


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
    public void initializeVariables() {
        super.initializeVariables();

        if (MagicSpells.getVariableManager().getVariable(variableName) == null) {
            MagicSpells.error("DenizenFlagParseSpell '" + internalName + "' has an invalid variable-name defined! ("+variableName+")");
        }
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
        ItemTag nbtItem = new ItemTag(new ItemStack(Material.FEATHER));
        if ((!Objects.equals(equipmentSlot, "NONE")) && person.getInventory().getItem(EquipmentSlot.valueOf(equipmentSlot)).getType() != Material.AIR) {
            nbtItem = new ItemTag(Objects.requireNonNull(person.getInventory().getItem(EquipmentSlot.valueOf(equipmentSlot))));
        }
        else {
            if (slot > 0 && Objects.requireNonNull(person.getInventory().getItem(slot)).getType() != Material.AIR) {
                nbtItem = new ItemTag(Objects.requireNonNull(person.getInventory().getItem(EquipmentSlot.valueOf(equipmentSlot))));
            }
        }
        if (nbtItem.getFlagTracker().getFlagMap().keys().contains(flagName)){
            switch (parseType.toUpperCase()) {
                case "SET":
                    MagicSpells.getVariableManager().set(variableName, person, String.valueOf(nbtItem.getFlagTracker().getFlagValue(flagName)));
                    break;
                case "ADD":
                    MagicSpells.getVariableManager().set(variableName, person, MagicSpells.getVariableManager().getValue(variableName, person)+Double.parseDouble(String.valueOf(nbtItem.getFlagTracker().getFlagValue(flagName))));
                    break;
            }
        }
    }
}
