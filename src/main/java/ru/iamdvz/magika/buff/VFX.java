package ru.iamdvz.magika.buff;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.spells.BuffSpell;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.SpellData;
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
    private final List<ItemStack> itemListIS = new ArrayList<>();
    private final EquipmentSlot equipmentSlot;
    private final Set<UUID> players;
    private final Vector headRotation;
    private final Vector headRotationSpeed;
    private int spawnDelay;
    private List<String> itemList;
    private final Vector relativeOffset;
    private final String equationX;
    private final String equationY;
    private final String equationZ;
    private final boolean orientYaw;

    public VFX(MagicConfig config, String spellName) {
        super(config, spellName);
        itemList = getConfigStringList("items-list", null);
        headRotation = getConfigVector("head-rotation", "0,0,0");
        relativeOffset = getConfigVector("relative-offset", "0,0,0");
        headRotationSpeed = getConfigVector("head-rotation-speed", "0,0,0");
        spawnDelay = getConfigInt("spawn-delay", 1);
        if (spawnDelay < 1) { spawnDelay = 1; }
        orientYaw = getConfigBoolean("orient-yaw", false);
        equationX = getConfigString("equation-x", null);
        equationY = getConfigString("equation-y", null);
        equationZ = getConfigString("equation-z", null);
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
        playSpellEffects(EffectPosition.CASTER, entity, new SpellData(entity));
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
        Location armorStandLocation = new Location(playerLocation.getWorld(),
                                                playerLocation.getX() + relativeOffset.getX()*Math.cos(Math.toRadians(playerLocation.getYaw()+90)) + relativeOffset.getZ()*Math.cos(Math.toRadians(playerLocation.getYaw())),
                                                playerLocation.getY() + relativeOffset.getY(),
                                                playerLocation.getZ() + relativeOffset.getX()*Math.sin(Math.toRadians(playerLocation.getYaw()+90)) + relativeOffset.getZ()*Math.sin(Math.toRadians(playerLocation.getYaw())));
        armorStandLocation.setYaw((float) (playerLocation.getYaw()+headRotation.getY()));

        playSpellEffects(EffectPosition.TARGET, armorStandLocation, new SpellData(player));
        ArmorStand armorStand = (ArmorStand) armorStandLocation.getWorld().spawnEntity(armorStandLocation, EntityType.ARMOR_STAND);
        armorStand.setHeadPose(new EulerAngle(armorStand.getHeadPose().getX() + Math.toRadians(headRotation.getX()),
                                                 armorStand.getHeadPose().getY(),
                                              armorStand.getHeadPose().getZ() + Math.toRadians(headRotation.getZ())));
        armorStand.setSilent(true);
        armorStand.setVisible(false);
        armorStand.setGravity(false);
        armorStand.setCollidable(false);
        armorStand.setInvulnerable(true);
        armorStand.addScoreboardTag("MS_ARMOR_STAND");
        armorStand.addDisabledSlots(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.FEET, EquipmentSlot.HAND, EquipmentSlot.LEGS, EquipmentSlot.OFF_HAND);

        final int[] duration = {0};
        final int[] ticker = {0};
        int orientYawInt = orientYaw ? 1 : 0;
        Expression expr_x = null;
        Expression expr_y = null;
        Expression expr_z = null;
        if (equationX != null) {
            expr_x = new ExpressionBuilder(equationX).variable("t").build();
            expr_x.setVariable("t", 0);
        }
        if (equationY != null) {
            expr_y = new ExpressionBuilder(equationY).variable("t").build();
            expr_y.setVariable("t", 0);
        }
        if (equationZ != null) {
            expr_z = new ExpressionBuilder(equationZ).variable("t").build();
            expr_z.setVariable("t", 0);
        }
        final double[] xCoord = new double[1];
        final double[] yCoord = new double[1];
        final double[] zCoord = new double[1];
        Expression finalExpr_x = expr_x;
        Expression finalExpr_y = expr_y;
        Expression finalExpr_z = expr_z;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!players.contains(player.getUniqueId())){
                    armorStand.remove();
                    this.cancel();
                }
                if (duration[0] < itemListIS.size()) {
                    armorStand.setItem(equipmentSlot, itemListIS.get(duration[0]));
                }
                xCoord[0] = relativeOffset.getX()*Math.cos(Math.toRadians((playerLocation.getYaw()-playerLocation.getYaw()*orientYawInt)+90 + orientYawInt*player.getLocation().getYaw())) + relativeOffset.getZ()*Math.cos(Math.toRadians((playerLocation.getYaw()-playerLocation.getYaw()*orientYawInt) + orientYawInt*player.getLocation().getYaw()));
                yCoord[0] = relativeOffset.getY();
                zCoord[0] = relativeOffset.getX()*Math.sin(Math.toRadians((playerLocation.getYaw()-playerLocation.getYaw()*orientYawInt)+90 + orientYawInt*player.getLocation().getYaw())) + relativeOffset.getZ()*Math.sin(Math.toRadians((playerLocation.getYaw()-playerLocation.getYaw()*orientYawInt) + orientYawInt*player.getLocation().getYaw()));
                if (equationX != null) {xCoord[0] += finalExpr_x.evaluate();}
                if (equationY != null) {yCoord[0] += finalExpr_y.evaluate();}
                if (equationZ != null) {zCoord[0] += finalExpr_z.evaluate();}

                armorStand.teleport(player.getLocation().add(xCoord[0], yCoord[0], zCoord[0]));

                if (headRotationSpeed.getX() != 0 || headRotationSpeed.getY() != 0 || headRotationSpeed.getZ() != 0){
                    armorStand.setHeadPose(new EulerAngle(armorStand.getHeadPose().getX() + Math.toRadians(headRotationSpeed.getX()*duration[0]),
                                                          armorStand.getHeadPose().getY() + Math.toRadians(headRotationSpeed.getY()*duration[0]),
                                                          armorStand.getHeadPose().getZ() + Math.toRadians(headRotationSpeed.getZ()*duration[0])));
                }
                ticker[0]++;
                duration[0]++;
                if (equationX != null) {
                    finalExpr_x.setVariable("t", ticker[0]);
                }
                if (equationY != null) {
                    finalExpr_y.setVariable("t", ticker[0]);
                }
                if (equationZ != null) {
                    finalExpr_z.setVariable("t", ticker[0]);
                }
                if (duration[0] > itemListIS.size()){
                    duration[0] = 0;
                }
            }
        }.runTaskTimer(MagicSpells.getInstance(), spawnDelay, 1);
        return true;
    }
}
