package ru.araclecore.battlecore.abilities.abilities;

import com.google.common.collect.Maps;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import ru.araclecore.battlecore.abilities.Abilities;
import ru.araclecore.battlecore.abilities.utilities.Ability;
import ru.araclecore.battlecore.abilities.utilities.Utilities;
import ru.araclecore.battlecore.heroes.Heroes;
import ru.araclecore.battlecore.heroes.heroes.enums.Trigger;

import java.util.Map;

public class Warrior implements Ability {
    public Warrior(Player player) {
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
            case R -> ability();
            case TARGET -> {
                if (target instanceof LivingEntity) action((LivingEntity) target);
            }
        }
    }

    public void ability() {
        if (!active && countdown == 0) activate();
        if (active && countdown == 0) activated();
        if (!active && countdown != 0) charging();
    }

    public void action(LivingEntity target) {
        if (!active) return;
        active = false;
        Abilities.timer.start(this);
        Utilities.charge(player, Heroes.manager.get(player).hero.suit().weapon().inactive(), 0);
        new BukkitRunnable() {
            int rate = 0;

            @Override
            public void run() {
                if (target.isDead() || player.isDead() || !player.isOnline() || rate == 4) {
                    cancel();
                    return;
                }
                rate++;
                target.damage(2, player);
            }

        }.runTaskTimer(Abilities.instance, 0, 10);

        Location location = target.getLocation();
        ItemStack effect = new ItemStack(Material.STICK);
        ItemMeta meta = effect.getItemMeta();
        meta.setCustomModelData(8);
        effect.setItemMeta(meta);
        ItemDisplay display = target.getWorld().spawn(location, ItemDisplay.class);
        display.setTransformation(
                new Transformation(
                        new Vector3f(0F, -1.2F, 0F),
                        new Quaternionf(), new Vector3f(2F, 1F, 2F),
                        new Quaternionf()));
        display.setBrightness(new Display.Brightness(15, 15));
        target.addPassenger(display);
        new BukkitRunnable() {
            int rate = 0;

            @Override
            public void run() {
                rate++;
                if (target.isDead() || target.isEmpty()) {
                    display.remove();
                    cancel();
                    return;
                }
                ItemMeta meta = effect.getItemMeta();
                switch (rate) {
                    case (14), (24) -> meta.setCustomModelData(8);
                    case (11) -> meta.setCustomModelData(9);
                    case (12) -> meta.setCustomModelData(10);
                    case (13) -> meta.setCustomModelData(11);
                    case (21) -> meta.setCustomModelData(12);
                    case (22) -> meta.setCustomModelData(13);
                    case (23) -> meta.setCustomModelData(14);
                    case (31) -> meta.setCustomModelData(15);
                    case (32) -> meta.setCustomModelData(16);
                    case (33) -> meta.setCustomModelData(17);
                }
                effect.setItemMeta(meta);
                display.setItemStack(effect);
                if (rate == 34) {
                    display.remove();
                    cancel();
                }
            }
        }.runTaskTimer(Abilities.instance, 0, 1);
    }

    public void activate() {
        active = true;
        this.countdown(0);
        player.getWorld().playSound(player.getLocation(), "warrior-secret-hit-activate", 1, 1);
        Location location = new Location(player.getWorld(), player.getX(), player.getY(), player.getZ());
        ItemStack effect = new ItemStack(Material.STICK);
        ItemMeta meta = effect.getItemMeta();
        meta.setCustomModelData(1);
        effect.setItemMeta(meta);
        Map<Integer, ItemDisplay> particles = Maps.newHashMap();
        for (int number = 0; number < 3; number++) {
            ItemDisplay display = player.getWorld().spawn(location, ItemDisplay.class);
            display.setItemStack(effect);
            player.addPassenger(display);
            display.setTransformation(
                    new Transformation(
                            new Vector3f(0F, 0F, 0F),
                            new Quaternionf(), new Vector3f(0, 2, 0),
                            new Quaternionf()));
            display.setInterpolationDuration(2);
            display.setInterpolationDelay(0);
            display.setBrightness(new Display.Brightness(15, 15));
            display.setShadowRadius(-1);
            display.setShadowStrength(-1);
            particles.put((number + 1), display);
        }
        new BukkitRunnable() {
            int rate = 0;

            @Override
            public void run() {
                rate++;
                if (rate == 0) {
                    for (int number : particles.keySet()) {
                        ItemDisplay display = particles.get(number);
                        display.setTransformation(
                                new Transformation(
                                        new Vector3f(0F, -0.8F, 0F),
                                        new Quaternionf(0F, 1F, 0F, 1.57F), new Vector3f(3F, 2F, 3F),
                                        new Quaternionf()));
                        display.setInterpolationDuration(5 * (number + 1));
                    }
                }
                if (rate == 1) {
                    for (int number : particles.keySet()) {
                        ItemDisplay display = particles.get(number);
                        display.setTransformation(
                                new Transformation(
                                        new Vector3f(0F, -0.8F, 0F),
                                        new Quaternionf(0F, 1F, 0F, 1.57F), new Vector3f(3F, 2F, 3F),
                                        new Quaternionf()));
                        display.setInterpolationDuration(5 * (number + 1));
                    }
                }
                if (rate == 3) particles.get(1).remove();
                if (rate == 5) particles.get(2).remove();
                if (rate == 7) {
                    particles.get(3).remove();
                    Utilities.charge(player, Heroes.manager.get(player).hero.suit().weapon().active(), 0);
                    cancel();
                }
            }

        }.runTaskTimer(Abilities.instance, 3, 2);
    }

    public void activated() {
        player.playSound(player.getLocation(), "warrior-secret-hit-active", 1, 1);
    }

    public void charging() {
        player.playSound(player.getLocation(), "warrior-secret-hit-charging", 1, 1);
    }


}
