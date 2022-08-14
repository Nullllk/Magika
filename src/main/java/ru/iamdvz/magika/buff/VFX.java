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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class VFX extends BuffSpell {
    private final Set<UUID> players;
    private Vector headRotation;
    private List<String> itemList;
    private Vector relativeOffset;
    private boolean orientYaw;

    public VFX(MagicConfig config, String spellName) {
        super(config, spellName);
        itemList = getConfigStringList("items-list", null);
        headRotation = getConfigVector("head-rotation", "0,0,0");
        relativeOffset = getConfigVector("relative-offset", "0,0,0");
        orientYaw = getConfigBoolean("orient-yaw", false);

        players = new HashSet<>();
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
        int orientYawInt = orientYaw ? 1 : 0;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!players.contains(player.getUniqueId())){
                    armorStand.remove();
                    this.cancel();
                }
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
                armorStand.teleport(player.getLocation().add(
                        relativeOffset.getX()*Math.cos(((playerLocation.getYaw()-playerLocation.getYaw()*orientYawInt)+90 + orientYawInt*player.getLocation().getYaw()) * toRadian) + relativeOffset.getZ()*Math.cos(((playerLocation.getYaw()-playerLocation.getYaw()*orientYawInt) + orientYawInt*player.getLocation().getYaw()) * toRadian),
                        relativeOffset.getY(),
                        relativeOffset.getX()*Math.sin(((playerLocation.getYaw()-playerLocation.getYaw()*orientYawInt)+90 + orientYawInt*player.getLocation().getYaw()) * toRadian) + relativeOffset.getZ()*Math.sin(((playerLocation.getYaw()-playerLocation.getYaw()*orientYawInt) + orientYawInt*player.getLocation().getYaw()) * toRadian)));
                duration.addAndGet(1);
            }
            }.runTaskTimer(MagicSpells.getInstance(), 2, 1);
        return true;
    }
}
