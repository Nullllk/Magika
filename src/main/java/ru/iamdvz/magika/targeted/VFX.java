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

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class VFX extends TargetedSpell implements TargetedLocationSpell {
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
        armorStand.setArms(false);
        armorStand.setSilent(true);
        armorStand.setVisible(true);
        armorStand.setGravity(false);
        armorStand.setBasePlate(false);
        armorStand.setCollidable(false);
        armorStand.setInvulnerable(true);
        armorStand.addDisabledSlots(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.FEET, EquipmentSlot.HAND, EquipmentSlot.LEGS, EquipmentSlot.OFF_HAND);
        armorStand.addScoreboardTag("MS_ARMOR_STAND");

        ItemMeta headItemM = new ItemStack(Material.FEATHER).getItemMeta();
        headItemM.setCustomModelData(1);

        PotionMeta headItemPM = (PotionMeta) new ItemStack(Material.POTION).getItemMeta();

        ItemStack blankItemIS = new ItemStack(Material.FEATHER);
        blankItemIS.setItemMeta(headItemM);
        armorStand.setItem(EquipmentSlot.HEAD, blankItemIS);

        AtomicInteger duration = new AtomicInteger(0);
        int orientWhenTeleportToCasterInt = orientWhenTeleportToCaster ? 1 : 0;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (itemList != null && duration.get() < itemList.size()) {
                    armorStand.getItem(EquipmentSlot.HEAD).setType(Material.valueOf(itemList.get(duration.get()).split(":")[0].toUpperCase()));
                    if (armorStand.getItem(EquipmentSlot.HEAD).getType() == Material.POTION) {
                        headItemPM.setColor(colorUtil.hexToRGBColor(itemList.get(duration.get()).split(",")[1].split(":")[1]));
                        headItemPM.setCustomModelData(Integer.parseInt(itemList.get(duration.get()).split(":")[1].split(",")[0]));
                        armorStand.getItem(EquipmentSlot.HEAD).setItemMeta(headItemPM);
                    }
                    else {
                        headItemM.setCustomModelData(Integer.valueOf(itemList.get(duration.get()).split(":")[1].split(",")[0]));
                        armorStand.getItem(EquipmentSlot.HEAD).setItemMeta(headItemM);
                    }
                }
                else {
                    if (duration.get() >= maxDuration){
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
                duration.addAndGet(1);
            }

            }.runTaskTimer(MagicSpells.getInstance(), 2, 1);
        return true;
    }
}
