package ru.iamdvz.magika.command;

import java.util.List;
import java.util.Collection;

import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.Spellbook;
import com.nisovin.magicspells.util.Util;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.SpellFilter;
import com.nisovin.magicspells.spells.PassiveSpell;
import com.nisovin.magicspells.spells.CommandSpell;
import com.nisovin.magicspells.util.PlayerNameUtils;

// Advanced perm is for listing other player's spells

public class ColoredListSpell extends CommandSpell {

    private final List<String> spellsToHide;

    private final int lineLength = 60;

    private boolean reloadGrantedSpells;
    private boolean onlyShowCastableSpells;

    private String strPrefix;
    private String strNoSpells;
    private String separatorColor;
    private String separator;

    private SpellFilter filter;

    public ColoredListSpell(MagicConfig config, String spellName) {
        super(config, spellName);

        spellsToHide = getConfigStringList("spells-to-hide", null);

        reloadGrantedSpells = getConfigBoolean("reload-granted-spells", true);
        onlyShowCastableSpells = getConfigBoolean("only-show-castable-spells", false);

        strPrefix = getConfigString("str-prefix", "Known spells:");
        strNoSpells = getConfigString("str-no-spells", "You do not know any spells.");

        separatorColor = getConfigString("separator-color", "b");
        separator = getConfigString("separator", ",");

        filter = SpellFilter.fromConfig(config, "spells." + internalName + ".filter");
    }

    @Override
    public PostCastAction castSpell(LivingEntity caster, SpellCastState state, float power, String[] args) {
        if (state == SpellCastState.NORMAL && caster instanceof Player player) {
            Spellbook spellbook = MagicSpells.getSpellbook(player);
            String extra = "";
            if (args != null && args.length > 0 && spellbook.hasAdvancedPerm("list")) {
                Player p = PlayerNameUtils.getPlayer(args[0]);
                if (p != null) {
                    spellbook = MagicSpells.getSpellbook(p);
                    extra = '(' + Util.getStringFromComponent(p.displayName()) + ") ";
                }
            }

            if (reloadGrantedSpells) spellbook.addGrantedSpells();

            if (spellbook.getSpells().isEmpty()) {
                sendMessage(strNoSpells, player, args);
                return PostCastAction.HANDLE_NORMALLY;
            }

            String s = "";
            for (Spell spell : spellbook.getSpells()) {
                if (shouldListSpell(spell, spellbook)) {
                    if (s.isEmpty()) s = spell.getName();
                    else s += "&"+separatorColor + separator + " " + MagicSpells.getTextColor() + spell.getName();
                }
            }

            s = strPrefix + ' ' + extra + s;
            while (s.length() > lineLength) {
                int i = s.substring(0, lineLength).lastIndexOf(' ');
                if (i < 0) break;
                sendMessage(s.substring(0, i), player, args);
                s = s.substring(i + 1);
            }
            if (!s.isEmpty()) sendMessage(s, player, args);
        }
        return PostCastAction.HANDLE_NORMALLY;
    }

    @Override
    public boolean castFromConsole(CommandSender sender, String[] args) {
    //    StringBuilder s = new StringBuilder();

    //    // Get spell list
    //    Collection<Spell> spells = MagicSpells.spells();
    //    if (args != null && args.length > 0) {
    //        Player p = PlayerNameUtils.getPlayer(args[0]);
    //        if (p == null) {
    //            sender.sendMessage("No such player.");
    //            return true;
    //        }
    //        spells = MagicSpells.getSpellbook(p).getSpells();
    //        s.append(p.getName()).append("'s spells: ");
    //    } else s.append("All spells: ");

    //    for (Spell spell : spells) {
    //        s.append(spell.getName());
    //        s.append(' ');
    //    }

    //    sender.sendMessage(s.toString());
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String partial) {
        if (sender instanceof ConsoleCommandSender && !partial.contains(" ")) return tabCompletePlayerName(sender, partial);
        return null;
    }

    private boolean shouldListSpell(Spell spell, Spellbook spellbook) {
        if (spell.isHelperSpell()) return false;
        if (onlyShowCastableSpells && (!spellbook.canCast(spell) || spell instanceof PassiveSpell)) return false;
        if (spellsToHide != null && spellsToHide.contains(spell.getInternalName())) return false;
        return filter.check(spell);
    }

    public List<String> getSpellsToHide() {
        return spellsToHide;
    }

    public int getLineLength() {
        return lineLength;
    }

    public boolean shouldReloadGrantedSpell() {
        return reloadGrantedSpells;
    }

    public void setReloadGrantedSpells(boolean reloadGrantedSpells) {
        this.reloadGrantedSpells = reloadGrantedSpells;
    }

    public boolean shouldOnlyShowCastableSpells() {
        return onlyShowCastableSpells;
    }

    public void setOnlyShowCastableSpells(boolean onlyShowCastableSpells) {
        this.onlyShowCastableSpells = onlyShowCastableSpells;
    }

    public String getStrPrefix() {
        return strPrefix;
    }

    public void setStrPrefix(String strPrefix) {
        this.strPrefix = strPrefix;
    }

    public String getStrNoSpells() {
        return strNoSpells;
    }

    public void setStrNoSpells(String strNoSpells) {
        this.strNoSpells = strNoSpells;
    }

    public SpellFilter getFilter() {
        return filter;
    }

    public void setFilter(SpellFilter filter) {
        this.filter = filter;
    }

}
