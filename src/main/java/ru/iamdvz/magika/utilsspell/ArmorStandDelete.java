package ru.iamdvz.magika.utilsspell;

import com.nisovin.magicspells.spells.InstantSpell;
import com.nisovin.magicspells.util.MagicConfig;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class ArmorStandDelete extends InstantSpell {

    public ArmorStandDelete(MagicConfig config, String spellName) {
        super(config, spellName);
        for (World world : Bukkit.getServer().getWorlds()) {
            for (Entity entity : world.getLivingEntities()) {
                if (entity != null && entity.getScoreboardTags().contains("MS_ARMOR_STAND")) {
                    entity.remove();
                }
            }
        }

    }

    @Override
    public PostCastAction castSpell(LivingEntity caster, SpellCastState state, float power, String[] args) {
        return null;
    }
}