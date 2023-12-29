package ru.araclecore.battlecore.abilities.abilities;

import com.google.common.collect.Maps;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import ru.araclecore.battlecore.abilities.Abilities;
import ru.araclecore.battlecore.abilities.utilities.Ability;
import ru.araclecore.battlecore.abilities.utilities.Utilities;
import ru.araclecore.battlecore.heroes.Heroes;
import ru.araclecore.battlecore.heroes.heroes.enums.Trigger;

import java.util.Map;

public class Hunter implements Ability {
    public Hunter(Player player) {
        this.player = player;
        count(20);
    }

    private final Player player;
    private boolean active = false;
    private int count;
    private int countdown = 0;

    @Override
    public Player player() {
        return player;
    }

    @Override
    public boolean active() {
        return active;
    }

    @Override
    public int count() {
        return count;
    }

    @Override
    public int countdown() {
        return countdown;
    }

    @Override
    public void count(int count) {
        this.count = count;
    }

    @Override
    public void active(boolean active) {
        this.active = active;
    }

    @Override
    public void countdown(int countdown) {
        this.countdown = countdown;
    }

    @Override
    public void action(Trigger trigger, @Nullable Entity target) {
        switch (trigger) {
            case L -> ability();
            case TARGET -> impact((LivingEntity) target);
            case ARROW -> shoot((Arrow) target);
        }
    }

    public void ability() {
        if (!active && countdown == 0) activate();
        if (active && countdown == 0) activated();
        if (!active && countdown != 0) charging();
    }

    public void impact(LivingEntity target) {
        if (!active && launched) {
            launched = false;
            target.damage(7, player);
            target.setVelocity(vector.multiply(1.5));
        }
    }

    private boolean launched;
    private Vector vector;

    public void shoot(@Nullable Arrow arrow) {
        if (!active) return;
        if (arrow == null) return;
        arrow.setGlowing(true);
        launched = true;
        active = false;
        Abilities.timer.start(this);
        player.getWorld().playSound(player.getLocation(), "hunter-powerfulshot-action", 1, 1);

        Utilities.charge(player, Heroes.manager.get(player).hero.suit().weapon().inactive(), 0);

        Location location = player.getLocation();
        location = location.set(location.getX(), location.getY() + 1.7, location.getZ());
        ItemStack effect = new ItemStack(Material.STICK);
        ItemMeta meta = effect.getItemMeta();

        meta.setCustomModelData(2);
        effect.setItemMeta(meta);

        Map<Integer, ItemDisplay> particles = Maps.newHashMap();
        for (int number = 0; number < 3; number++) {
            ItemDisplay display = player.getWorld().spawn(location, ItemDisplay.class);
            display.setItemStack(effect);
            display.setTransformation(
                    new Transformation(
                            new Vector3f(0F, 0F, 5F),
                            new Quaternionf(), new Vector3f(0.1F, 0.1F, 1F),
                            new Quaternionf().rotationAxis(1.57F, 1, 0, 0)));
            display.setInterpolationDuration(0);
            display.setInterpolationDelay(0);
            display.setBrightness(new Display.Brightness(15, 15));
            particles.put((number + 1), display);

        }

        new BukkitRunnable() {
            int rate = 0;

            @Override
            public void run() {

                rate++;

                if (rate == 1) {
                    for (int number : particles.keySet()) {
                        ItemDisplay display = particles.get(number);
                        display.setTransformation(
                                new Transformation(
                                        new Vector3f(0F, 0F, ((float) number / 8 * -1)),
                                        new Quaternionf(), new Vector3f(3, 3, 1),
                                        new Quaternionf().rotationAxis(1.57F, 1, 0, 0)));
                        display.setInterpolationDuration(5 * (number + 1));
                    }
                }
                ItemMeta meta = effect.getItemMeta();
                switch (rate) {

                    case (2) -> {
                        meta.setCustomModelData(3);
                        effect.setItemMeta(meta);
                        particles.get(1).setItemStack(effect);
                    }
                    case (3) -> {
                        meta.setCustomModelData(4);
                        effect.setItemMeta(meta);
                        particles.get(1).setItemStack(effect);
                    }
                    case (4) -> {
                        meta.setCustomModelData(3);
                        effect.setItemMeta(meta);
                        particles.get(2).setItemStack(effect);
                    }
                    case (5) -> {
                        particles.get(1).remove();
                        meta.setCustomModelData(3);
                        effect.setItemMeta(meta);
                        particles.get(3).setItemStack(effect);
                    }
                    case (7) -> {
                        meta.setCustomModelData(4);
                        effect.setItemMeta(meta);
                        particles.get(2).setItemStack(effect);
                    }
                    case (10) -> {
                        particles.get(2).remove();
                        meta.setCustomModelData(4);

                    }
                    case (15) -> {
                        particles.get(3).remove();
                        cancel();
                    }
                }
            }

        }.runTaskTimer(Abilities.instance, 3, 1);

        new BukkitRunnable() {

            @Override
            public void run() {
                if (arrow.isDead() || arrow.isOnGround()) {
                    launched = false;
                    if (!arrow.isDead()) arrow.remove();
                    cancel();
                    return;
                }
                if (target == null) return;
                vector = target.getEyeLocation().toVector().subtract(arrow.getLocation().toVector()).normalize();
                arrow.setVelocity(vector.multiply(2));
            }
        }.runTaskTimer(Abilities.instance, 0, 1);
    }

    public void activate() {
        active = true;
        countdown = 0;
        tracking();
        player.getWorld().playSound(player.getLocation(), "hunter-powerfulshot-activate", 1, 1);

        Utilities.charge(player, Heroes.manager.get(player).hero.suit().weapon().active(), 0);

        Location location = new Location(player.getWorld(), player.getX(), player.getY(), player.getZ());
        ItemStack effect = new ItemStack(Material.STICK);
        ItemMeta meta = effect.getItemMeta();
        meta.setCustomModelData(7);
        effect.setItemMeta(meta);

        ItemDisplay display = player.getWorld().spawn(location, ItemDisplay.class);
        display.setItemStack(effect);
        player.addPassenger(display);
        display.setTransformation(
                new Transformation(
                        new Vector3f(0F, -0.79F, 0F),
                        new Quaternionf(), new Vector3f(4, 2, 4),
                        new Quaternionf()));
        display.setBrightness(new Display.Brightness(15, 15));


        new BukkitRunnable() {
            int rate = 0;

            @Override
            public void run() {
                rate++;
                ItemMeta meta = effect.getItemMeta();
                switch (rate) {
                    case (5) -> meta.setCustomModelData(6);
                    case (10) -> meta.setCustomModelData(5);
                    case (15) -> {
                        display.remove();
                        cancel();
                        return;
                    }
                }
                effect.setItemMeta(meta);
                display.setItemStack(effect);
            }
        }.runTaskTimer(Abilities.instance, 0, 1);
    }

    public void activated() {
        player.playSound(player.getLocation(), "warrior-secret-hit-active", 1, 1);
    }

    public void charging() {
        player.playSound(player.getLocation(), "warrior-secret-hit-charging", 1, 1);
    }

    private LivingEntity target;

    private void tracking() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    if (target != null) target.setGlowing(false);
                    cancel();
                    return;
                }
                if (active) {
                    target();
                    if (target != null) {
                        Vector playerVector = player.getLocation().getDirection().normalize();

                        Vector targetVector = new Vector(
                                target.getX() - player.getX(),
                                target.getY() - player.getY(),
                                target.getZ() - player.getZ());

                        Vector vector = new Vector(playerVector.getX() * targetVector.getX(),
                                playerVector.getY() * targetVector.getY(),
                                playerVector.getZ() * targetVector.getZ());
                        double quantum = vector.getX() + vector.getY() + vector.getZ();

                        double arc = quantum / (
                                Math.sqrt(playerVector.getX() * playerVector.getX() +
                                        playerVector.getY() * playerVector.getY() +
                                        playerVector.getZ() * playerVector.getZ()) *

                                        (Math.sqrt(targetVector.getX() * targetVector.getX() +
                                                targetVector.getY() * targetVector.getY() +
                                                targetVector.getZ() * targetVector.getZ())));

                        target.setGlowing(arc > 0.95);
                        if (arc < 0.95) target = null;
                    }
                } else {
                    if (target != null) target.setGlowing(false);
                    cancel();
                }
            }
        }.runTaskTimer(Abilities.instance, 0, 1);
    }

    private void target() {
        Entity entity = player.getTargetEntity(120);
        if (entity == null) return;
        if (!(entity instanceof LivingEntity target)) return;
        if (target == this.target) return;
        if (this.target != null) this.target.setGlowing(false);
        this.target = target;
    }

}
