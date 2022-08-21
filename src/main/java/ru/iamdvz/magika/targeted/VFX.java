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
    private EquipmentSlot equipmentSlot;
    private Vector headRotation;
    private int maxDuration;
    private List<String> itemList;
    private Vector relativeOffset;

    public VFX(MagicConfig config, String spellName) {
        super(config, spellName);
        itemList = getConfigStringList("items-list", null);
        headRotation = getConfigVector("head-rotation", "0,0,0");
        relativeOffset = getConfigVector("relative-offset", "0,0,0");
        maxDuration = getConfigInt("max-duration", itemList.size());
        equipmentSlot = EquipmentSlot.valueOf(getConfigString("equipment-slot", "HEAD").toUpperCase());

        for (String item : itemList){
            ItemStack itemTemp = new ItemStack(Material.valueOf(item.split(":")[0].toUpperCase()));
            if (itemTemp.getType() == Material.POTION) {
                PotionMeta itemTempM = (PotionMeta) new ItemStack(Material.POTION).getItemMeta();
                itemTempM.setColor(colorUtil.hexToRGBColor(item.split(",")[1].split(":")[1]));
                itemTempM.setCustomModelData(Integer.parseInt(item.split(":")[1].split(",")[0]));
                itemTemp.setItemMeta(itemTempM);
            }
            else {
                ItemMeta itemTempM = itemTemp.getItemMeta();
                itemTempM.setCustomModelData(Integer.valueOf(item.split(":")[1].split(",")[0]));
                itemTemp.setItemMeta(itemTempM);
            }
            itemListIS.add(itemTemp);
        }
        itemList = null;

    }

    @Override
    public PostCastAction castSpell(LivingEntity caster, SpellCastState state, float power, String[] args) {
        playSpellEffects(EffectPosition.CASTER, caster);
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

        final int[] duration = {0};
        new BukkitRunnable() {
            @Override
            public void run() {
                if (itemListIS != null && duration[0] < itemListIS.size()) {
                    armorStand.setItem(equipmentSlot, itemListIS.get(duration[0]));
                }
                else {
                    if (duration[0] >= maxDuration){
                        armorStand.remove();
                        this.cancel();
                    }
                }
                duration[0]++;
            }
        }.runTaskTimer(MagicSpells.getInstance(), 1, 1);
        return true;
    }
}
