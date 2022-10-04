package ru.iamdvz.magika.targeted;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.spells.TargetedLocationSpell;
import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.util.MagicConfig;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import ru.iamdvz.magika.utils.ItemsUtil;

import java.util.ArrayList;
import java.util.List;

public class VFXrefactor extends TargetedSpell implements TargetedLocationSpell {
    private List<ItemStack> itemListIS = new ArrayList<>();
    private final EquipmentSlot equipmentSlot;
    private final Vector headRotation;
    private final Vector headRandomRotation;
    private final Vector headRotationSpeed;
    private final int maxDuration;
    private int spawnDelay;
    private List<String> itemList;
    private final Vector relativeOffset;
    private final String equationX;
    private final String equationY;
    private final String equationZ;

    public VFXrefactor(MagicConfig config, String spellName) {
        super(config, spellName);
        itemList = getConfigStringList("items-list", null);
        headRotation = getConfigVector("head-rotation", "0,0,0");
        headRandomRotation = getConfigVector("head-random-rotation", "0,0,0");
        relativeOffset = getConfigVector("relative-offset", "0,0,0");
        headRotationSpeed = getConfigVector("head-rotation-speed", "0,0,0");
        maxDuration = getConfigInt("max-duration", itemList.size());
        spawnDelay = getConfigInt("spawn-delay", 1);
        if (spawnDelay < 1) { spawnDelay = 1; }
        equationX = getConfigString("equation-x", null);
        equationY = getConfigString("equation-y", null);
        equationZ = getConfigString("equation-z", null);
        equipmentSlot = EquipmentSlot.valueOf(getConfigString("equipment-slot", "HEAD").toUpperCase());

        itemListIS = ItemsUtil.stringListToItemStack(itemList);
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

    private boolean armorstandSpawn(Location target, Player player) {
        Location armorStandLocation = new Location(target.getWorld(),
                target.getX() + relativeOffset.getX()*Math.cos(Math.toRadians(target.getYaw()+90)) + relativeOffset.getZ()*Math.cos(Math.toRadians(target.getYaw())),
                target.getY() + relativeOffset.getY(),
                target.getZ() + relativeOffset.getX()*Math.sin(Math.toRadians(target.getYaw()+90)) + relativeOffset.getZ()*Math.sin(Math.toRadians(target.getYaw())));
        armorStandLocation.setYaw((float) (target.getYaw()+headRotation.getY()));

        playSpellEffects(EffectPosition.TARGET, armorStandLocation);
        ArmorStand armorStand = (ArmorStand) armorStandLocation.getWorld().spawnEntity(armorStandLocation, EntityType.ARMOR_STAND);
        armorStand.setHeadPose(new EulerAngle(armorStand.getHeadPose().getX() + Math.toRadians(headRotation.getX() + Math.random()*headRandomRotation.getX()),
                armorStand.getHeadPose().getY() + Math.toRadians(Math.random()*headRandomRotation.getY()),
                armorStand.getHeadPose().getZ() + Math.toRadians(headRotation.getZ()) + Math.random()*headRandomRotation.getZ()));
        armorStand.setSilent(true);
        armorStand.setVisible(false);
        armorStand.setGravity(false);
        armorStand.setCollidable(false);
        armorStand.setInvulnerable(true);
        armorStand.addScoreboardTag("MS_ARMOR_STAND");
        armorStand.addDisabledSlots(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.FEET, EquipmentSlot.HAND, EquipmentSlot.LEGS, EquipmentSlot.OFF_HAND);

        final int[] duration = {0};
        Expression exprX = null;
        Expression exprY = null;
        Expression exprZ = null;
        if (equationX != null) {
            exprX = new ExpressionBuilder(equationX).variable("t").build();
            exprX.setVariable("t", 0);
        }
        if (equationY != null) {
            exprY = new ExpressionBuilder(equationY).variable("t").build();
            exprY.setVariable("t", 0);
        }
        if (equationZ != null) {
            exprZ = new ExpressionBuilder(equationZ).variable("t").build();
            exprZ.setVariable("t", 0);
        }
        final double[] xCoord = new double[1];
        final double[] yCoord = new double[1];
        final double[] zCoord = new double[1];
        Expression finalExprX = exprX;
        Expression finalExprY = exprY;
        Expression finalExprZ = exprZ;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (duration[0] < itemListIS.size() && itemListIS.get(duration[0]).getType() != Material.AIR) {
                    armorStand.setItem(equipmentSlot, itemListIS.get(duration[0]));
                }
                else {
                    if (duration[0] >= maxDuration) {
                        armorStand.remove();
                        this.cancel();
                    }
                }
                if (equationX != null) {
                    xCoord[0] = finalExprX.evaluate();
                    finalExprX.setVariable("t", duration[0]);
                }
                if (equationY != null) {
                    yCoord[0] = finalExprY.evaluate();
                    finalExprY.setVariable("t", duration[0]);
                }
                if (equationZ != null) {
                    zCoord[0] = finalExprZ.evaluate();
                    finalExprZ.setVariable("t", duration[0]);
                }
                if (equationX != null && equationY != null && equationZ != null) {
                    armorStand.teleport(new Location(armorStandLocation.getWorld(), armorStandLocation.getX() + xCoord[0], armorStandLocation.getY() + yCoord[0], armorStandLocation.getZ() + zCoord[0])); //armorStandLocation.add(xCoord[0],yCoord[0],zCoord[0])));
                }
                if (headRotationSpeed.getX() != 0 || headRotationSpeed.getY() != 0 || headRotationSpeed.getZ() != 0) {
                    armorStand.setHeadPose(new EulerAngle(armorStand.getHeadPose().getX() + Math.toRadians(headRotationSpeed.getX()*duration[0]),
                            armorStand.getHeadPose().getY() + Math.toRadians(headRotationSpeed.getY()*duration[0]),
                            armorStand.getHeadPose().getZ() + Math.toRadians(headRotationSpeed.getZ()*duration[0])));
                }
                duration[0]++;
            }
        }.runTaskTimer(MagicSpells.getInstance(), spawnDelay, 1);
        return true;
    }
}
