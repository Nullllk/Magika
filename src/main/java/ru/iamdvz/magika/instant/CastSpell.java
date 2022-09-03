package ru.iamdvz.magika.instant;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.spells.InstantSpell;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.config.ConfigData;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class CastSpell extends InstantSpell {

    private String castSpellName;

    public CastSpell(MagicConfig config, String spellName) {
        super(config, spellName);

        castSpellName = getConfigString("spell-name", null);
    }

    @Override
    public PostCastAction castSpell(LivingEntity caster, SpellCastState state, float power, String[] args) {
        //MagicSpells.log(args[Integer.parseInt(castSpellName.split(":")[1])-1]);
        //MagicSpells.log(args.toString());
        //for (String arg: args){
        //    MagicSpells.log(arg);
        //}

        if (state == SpellCastState.NORMAL && caster instanceof Player player) {
            if (castSpellName.split(":")[0].contains("arg")){
                //MagicSpells.log("THAT = " + String.valueOf(Integer.parseInt(castSpellName.split(":")[1])-1));
                castSpellName = args[Integer.parseInt(castSpellName.split(":")[1])-1];
                if (MagicSpells.getSpells().containsKey(castSpellName)){
                    MagicSpells.getSpellByInternalName(castSpellName).cast(caster, power, args);
                }
            }
            }
        return null;
    }
}
