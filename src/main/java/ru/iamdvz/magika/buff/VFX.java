package ru.iamdvz.magika.buff;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.spells.BuffSpell;
import com.nisovin.magicspells.util.MagicConfig;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import ru.iamdvz.magika.utils.colorUtil;

import java.util.*;

public class VFX extends BuffSpell {
    private List<ItemStack> itemListIS = new ArrayList<>();
    private EquipmentSlot equipmentSlot;
    private final Set<UUID> players;
    private Vector headRotation;
    private Vector headRotationSpeed;
    private List<String> itemList;
    private Vector relativeOffset;
    private boolean orientYaw;

    public VFX(MagicConfig config, String spellName) {
        super(config, spellName);
        itemList = getConfigStringList("items-list", null);
        headRotation = getConfigVector("head-rotation", "0,0,0");
        relativeOffset = getConfigVector("relative-offset", "0,0,0");
        headRotationSpeed = getConfigVector("head-rotation-speed", "0,0,0");
        orientYaw = getConfigBoolean("orient-yaw", false);
        equipmentSlot = EquipmentSlot.valueOf(getConfigString("equipment-slot", "HEAD").toUpperCase());

        players = new HashSet<>();

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
    public boolean castBuff(LivingEntity entity, float power, String[] args) {
        players.add(entity.getUniqueId());
        playSpellEffects(EffectPosition.CASTER, entity);
        armorstandSpawn((Player) entity);
        return true;
    }

    @Override
    public boolean isActive(LivingEntity entity) {
        return players.contains(entity.getUniqueId());
    }

    @Override
    protected void turnOffBuff(LivingEntity entity) {
        players.remove(entity.getUniqueId());
    }

    @Override
    protected void turnOff() {
        players.clear();
    }

    private boolean armorstandSpawn(Player player){
        Location playerLocation = player.getLocation();
        double toRadian = Math.PI/180;
        Location armorStandLocation = new Location(playerLocation.getWorld(),
                playerLocation.getX() + relativeOffset.getX()*Math.cos((playerLocation.getYaw()+90) * toRadian) + relativeOffset.getZ()*Math.cos(playerLocation.getYaw() * toRadian),
                playerLocation.getY() + relativeOffset.getY(),
                playerLocation.getZ() + relativeOffset.getX()*Math.sin((playerLocation.getYaw()+90) * toRadian) + relativeOffset.getZ()*Math.sin(playerLocation.getYaw() * toRadian));
        armorStandLocation.setYaw((float) (playerLocation.getYaw()+headRotation.getY()));

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
        int orientYawInt = orientYaw ? 1 : 0;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!players.contains(player.getUniqueId())){
                    armorStand.remove();
                    this.cancel();
                }
                if (itemListIS != null && duration[0] < itemListIS.size()) {
                    armorStand.setItem(equipmentSlot, itemListIS.get(duration[0]));
                }
                armorStand.teleport(player.getLocation().add(
                        relativeOffset.getX()*Math.cos(((playerLocation.getYaw()-playerLocation.getYaw()*orientYawInt)+90 + orientYawInt*player.getLocation().getYaw()) * toRadian) + relativeOffset.getZ()*Math.cos(((playerLocation.getYaw()-playerLocation.getYaw()*orientYawInt) + orientYawInt*player.getLocation().getYaw()) * toRadian),
                        relativeOffset.getY(),
                        relativeOffset.getX()*Math.sin(((playerLocation.getYaw()-playerLocation.getYaw()*orientYawInt)+90 + orientYawInt*player.getLocation().getYaw()) * toRadian) + relativeOffset.getZ()*Math.sin(((playerLocation.getYaw()-playerLocation.getYaw()*orientYawInt) + orientYawInt*player.getLocation().getYaw()) * toRadian)));
                if (headRotationSpeed.getX() != 0 || headRotationSpeed.getY() != 0 || headRotationSpeed.getZ() != 0){
                    armorStand.setHeadPose(new EulerAngle(
                            armorStand.getHeadPose().getX() + Math.toRadians(headRotationSpeed.getX()*duration[0]),
                            armorStand.getHeadPose().getY() + Math.toRadians(headRotationSpeed.getY()*duration[0]),
                            armorStand.getHeadPose().getZ() + Math.toRadians(headRotationSpeed.getZ()*duration[0])));
                }
                duration[0]++;
                if (duration[0] > itemListIS.size()){
                    duration[0] = 0;
                }
            }
        }.runTaskTimer(MagicSpells.getInstance(), 1, 1);
        return true;
    }
}
