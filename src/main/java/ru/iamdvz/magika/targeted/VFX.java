package ru.iamdvz.magika.targeted;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.spells.TargetedLocationSpell;
import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.util.MagicConfig;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import ru.iamdvz.magika.utils.colorUtil;

import java.util.ArrayList;
import java.util.List;

public class VFX extends TargetedSpell implements TargetedLocationSpell {
    private List<ItemStack> itemListIS = new ArrayList<>();
    private Vector headRotation;
    private int maxDuration;
    private List<String> itemList;
    private Vector relativeOffset;
    private boolean teleportToCaster;
    private boolean orientWhenTeleportToCaster;

    public VFX(MagicConfig config, String spellName) {
        super(config, spellName);
        itemList = getConfigStringList("items-list", null);
        headRotation = getConfigVector("head-rotation", "0,0,0");
        relativeOffset = getConfigVector("relative-offset", "0,0,0");
        teleportToCaster = getConfigBoolean("teleport-to-caster", false);
        orientWhenTeleportToCaster = getConfigBoolean("orient-when-teleport", false);
        maxDuration = getConfigInt("max-duration", itemList.size());

        for (String item : itemList){
            ItemStack itemTemp = new ItemStack(Material.valueOf(item.split(":")[0].toUpperCase()));
            ItemMeta itemTempM = itemTemp.getItemMeta();
            itemTempM.setCustomModelData(Integer.valueOf(item.split(":")[1].split(",")[0]));
            itemTemp.setItemMeta(itemTempM);
            itemListIS.add(itemTemp);
        }
        itemList = null;
    }

    @Override
    public PostCastAction castSpell(LivingEntity caster, SpellCastState state, float power, String[] args) {
        playSpellEffects(EffectPosition.CASTER, caster);
        if (teleportToCaster){
            armorstandSpawn(caster.getLocation(), (Player) caster);
        }
        return PostCastAction.HANDLE_NORMALLY;
    }

    @Override
    public boolean castAtLocation(LivingEntity caster, Location target, float power) {
        armorstandSpawn(target, (Player) caster);
        return false;
    }
    // не используется в тмуспе
    @Override
    public boolean castAtLocation(Location target, float power) {
        armorstandSpawn(target, null);
        return false;
    }

    private boolean armorstandSpawn(Location target, Player player){
        double toRadian = Math.PI/180;
        Location armorStandLocation = new Location(target.getWorld(),
                target.getX() + relativeOffset.getX()*Math.cos((target.getYaw()+90) * toRadian) + relativeOffset.getZ()*Math.cos(target.getYaw() * toRadian),
                target.getY() + relativeOffset.getY(),
                target.getZ() + relativeOffset.getX()*Math.sin((target.getYaw()+90) * toRadian) + relativeOffset.getZ()*Math.sin(target.getYaw() * toRadian));
        armorStandLocation.setYaw((float) (target.getYaw()+headRotation.getY()));

        playSpellEffects(EffectPosition.TARGET, armorStandLocation);
        ArmorStand armorStand = (ArmorStand) armorStandLocation.getWorld().spawnEntity(armorStandLocation, EntityType.ARMOR_STAND);
        armorStand.setHeadPose(new EulerAngle(armorStand.getHeadPose().getX() + headRotation.getX()*(toRadian),
                                                 armorStand.getHeadPose().getY(),
                                              armorStand.getHeadPose().getZ() + headRotation.getZ()*(toRadian)));
        armorStand.setSilent(true);
        armorStand.setVisible(false);
        armorStand.setGravity(false);
        armorStand.setCollidable(false);
        armorStand.setInvulnerable(true);
        armorStand.addDisabledSlots(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.FEET, EquipmentSlot.HAND, EquipmentSlot.LEGS, EquipmentSlot.OFF_HAND);
        armorStand.addScoreboardTag("MS_ARMOR_STAND");

        //PotionMeta headItemPM = (PotionMeta) new ItemStack(Material.POTION).getItemMeta();

        final int[] duration = {0};
        int orientWhenTeleportToCasterInt = orientWhenTeleportToCaster ? 1 : 0;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (itemListIS != null && duration[0] < itemListIS.size()) {
                    armorStand.setItem(EquipmentSlot.HEAD, itemListIS.get(duration[0]));
                }
                else {
                    if (duration[0] >= maxDuration){
                        armorStand.remove();
                        this.cancel();
                    }
                }
                if (teleportToCaster) {
                    armorStand.teleport(player.getLocation().add(
                            relativeOffset.getX()*Math.cos(((target.getYaw()-target.getYaw()*orientWhenTeleportToCasterInt)+90 + orientWhenTeleportToCasterInt*player.getLocation().getYaw()) * toRadian) + relativeOffset.getZ()*Math.cos(((target.getYaw()-target.getYaw()*orientWhenTeleportToCasterInt) + orientWhenTeleportToCasterInt*player.getLocation().getYaw()) * toRadian),
                            relativeOffset.getY(),
                            relativeOffset.getX()*Math.sin(((target.getYaw()-target.getYaw()*orientWhenTeleportToCasterInt)+90 + orientWhenTeleportToCasterInt*player.getLocation().getYaw()) * toRadian) + relativeOffset.getZ()*Math.sin(((target.getYaw()-target.getYaw()*orientWhenTeleportToCasterInt) + orientWhenTeleportToCasterInt*player.getLocation().getYaw()) * toRadian)));
                }
                duration[0]++;
            }

            }.runTaskTimer(MagicSpells.getInstance(), 2, 1);
        return true;
    }
}
