package ru.araclecore.battlecore.abilities.abilities;

import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
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

import java.util.*;

public class Wizard implements Ability {
    public Wizard(Player player) {
        this.player = player;
        active(false);
        count(20);
        countdown(0);
    }

    private final Player player;
    private boolean active;
    private int count;
    private int countdown;

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
            case R -> shoot();
        }
    }

    public void ability() {
        if (!active && countdown == 0) activate();
        if (active && countdown == 0) activated();
        if (!active && countdown != 0) charging();
    }

    public void activate() {
        active = true;
        countdown = 0;

    }

    public void activated() {
        active = false;
        Abilities.timer.start(this);

        Utilities.charge(player, Heroes.manager.get(player).hero.suit().weapon().inactive(), 0);

        player.getWorld().playSound(player.getLocation(), "wizard-magichit-action-effect", 1, 1);

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
                    cancel();
                }
            }

        }.runTaskTimer(Abilities.instance, 5, 2);


        Location startLocation = player.getLocation();
        Collection<LivingEntity> targets = startLocation.getNearbyLivingEntities(5);
        targets.remove(player);
        new BukkitRunnable() {
            @Override
            public void run() {
                targets.forEach(target -> {
                    Location endLocation = target.getLocation();
                    double x = (endLocation.toVector().getX() - startLocation.toVector().getX());
                    double z = (endLocation.toVector().getZ() - startLocation.toVector().getZ());
                    org.bukkit.util.Vector vector = new Vector(x, 0.1, z);
                    double distance = startLocation.distance(endLocation);
                    if (distance <= 1) target.setVelocity(vector.multiply(2));
                    if (distance <= 2) target.setVelocity(vector.multiply(1.75));
                    if (distance <= 3) target.setVelocity(vector.multiply(1.5));
                    if (distance > 3) target.setVelocity(vector.multiply(1.25));
                    target.damage(5, player);
                });
            }
        }.runTaskLater(Abilities.instance, 5);
    }

    public void charging() {
        player.playSound(player.getLocation(), "warrior-secret-hit-charging", 1, 1);
    }

    private int charge = 3;

    public void charge() {
        Bukkit.getScheduler().runTaskLater(Abilities.instance, bukkitTask -> charge++, 40);
    }

    public void shoot() {
        if (charge <= 0) {
            player.playSound(player.getLocation(), "warrior-secret-hit-charging", 1, 1);
            return;
        }
        charge--;
        charge();
        action(player);
        Location startlocation = player.getLocation().set(player.getX(), player.getY() + 1.8, player.getZ());
        ItemStack effect = new ItemStack(Material.STICK);
        ItemMeta meta = effect.getItemMeta();
        meta.setCustomModelData(18);
        effect.setItemMeta(meta);

        ItemDisplay trigger = player.getWorld().spawn(startlocation, ItemDisplay.class);

        trigger.setItemStack(effect);
        trigger.setTransformation(
                new Transformation(
                        new Vector3f(0F, 0F, 0F),
                        new Quaternionf(), new Vector3f(1, 1, 1),
                        new Quaternionf().rotationAxis(1.57F, 1, 0, 0)));
        trigger.setInterpolationDuration(1);
        trigger.setInterpolationDelay(0);
        trigger.setTeleportDuration(3);
        trigger.setBrightness(new Display.Brightness(15, 15));

        List<Material> blockWhiteList = new ArrayList<>();
        blockWhiteList.add(Material.AIR);
        blockWhiteList.add(Material.WATER);
        blockWhiteList.add(Material.LAVA);
        blockWhiteList.add(Material.SNOW);
        blockWhiteList.add(Material.TALL_GRASS);
        blockWhiteList.add(Material.TALL_SEAGRASS);

        particles(trigger);
        new BukkitRunnable() {
            int rate = 0;
            float angle = 0.785F;

            double speed = 0;

            @Override
            public void run() {
                rate++;

                Location points = Utilities.point(1 + speed, startlocation);
                speed += 0.05;
                trigger.teleport(points);
                Location current = trigger.getLocation();

                angle = angle + 0.785F;

                trigger.setTransformation(
                        new Transformation(
                                new Vector3f(0F, 0F, 0F),
                                new Quaternionf(), new Vector3f(1, 1, 1),
                                new Quaternionf().rotationAxis(angle, 1, 0, 1)));
                trigger.setInterpolationDuration(1);
                trigger.setInterpolationDelay(0);

                Collection<LivingEntity> targets = current.getNearbyLivingEntities(0.5, 0.5, 0.5);
                targets.remove(player);

                if (!targets.isEmpty()) {
                    targets.forEach(target -> {
                        target.damage(4, player);
                        hit(target);
                    });
                    trigger.remove();
                    cancel();
                    return;
                }

                if (!player.isOnline() || player.isDead() || rate == 30 || !blockWhiteList.contains(current.getBlock().getType())) {
                    hit(trigger);
                    trigger.remove();
                    cancel();
                }
            }

        }.runTaskTimer(Abilities.instance, 0, 1);
    }

    public static void particles(ItemDisplay trigger) {
        new BukkitRunnable() {

            float angle = 0.75F;

            @Override
            public void run() {
                if (trigger.isDead()) {
                    cancel();
                    return;
                }

                angle += 0.75F;
                Random random = new Random();
                float x = random.nextFloat(0.2F + 0.2F) - 0.2F;
                float y = random.nextFloat(0.2F + 0.2F) - 0.2F;
                float z = random.nextFloat(0.2F + 0.2F) - 0.2F;
                ItemDisplay display = trigger.getWorld().spawn(Utilities.point(-3, trigger.getLocation()), ItemDisplay.class);
                display.setItemStack(trigger.getItemStack());
                display.setTransformation(
                        new Transformation(
                                new Vector3f(x, y, z),
                                new Quaternionf(), new Vector3f(0.3F, 0.3F, 0.3F),
                                new Quaternionf().rotationAxis(angle, 1, 1, 1)));
                display.setInterpolationDuration(3);
                display.setInterpolationDelay(0);
                display.setBrightness(new Display.Brightness(15, 15));
                Bukkit.getScheduler().runTaskLater(Abilities.instance, bukkitTask -> {
                    angle += 0.75F;
                    display.setTransformation(
                            new Transformation(
                                    new Vector3f(x, y, z),
                                    new Quaternionf(), new Vector3f(0F, 0F, 0F),
                                    new Quaternionf().rotationAxis(angle, 1, 1, 1)));
                    display.setInterpolationDuration(3);
                    display.setInterpolationDelay(0);
                }, 1);
                Bukkit.getScheduler().runTaskLater(Abilities.instance, bukkitTask -> display.remove(), 7);
            }
        }.runTaskTimer(Abilities.instance, 5, 1);

    }

    public static void hit(Entity target) {
        Location location = target.getLocation().set(target.getX(), target.getY(), target.getZ());
        ItemStack effect = new ItemStack(Material.STICK);
        ItemMeta meta = effect.getItemMeta();
        meta.setCustomModelData(19);
        effect.setItemMeta(meta);


        Map<Integer, ItemDisplay> particles = Maps.newHashMap();

        for (int number = 0; number < 5; number++) {
            ItemDisplay display = target.getWorld().spawn(location, ItemDisplay.class);
            display.setItemStack(effect);
            target.addPassenger(display);

            if (target instanceof ItemDisplay) {
                display.setTransformation(new Transformation(new Vector3f(0F, 0F, 0F), new Quaternionf(), new Vector3f(0F, 0F, 0F), new Quaternionf()));
            } else {
                display.setTransformation(new Transformation(new Vector3f(0F, -0.8F, 0F), new Quaternionf(), new Vector3f(0F, 0F, 0F), new Quaternionf()));
            }
            display.setInterpolationDuration(3);
            display.setInterpolationDelay(0);
            display.setBrightness(new Display.Brightness(15, 15));
            particles.put((number + 1), display);
        }

        Random random = new Random();
        int number = random.nextInt(3);
        switch (number) {
            case (0) -> target.getWorld().playSound(target.getLocation(), "wizard-magicshot-hit-effect1", 1, 1);
            case (1) -> target.getWorld().playSound(target.getLocation(), "wizard-magicshot-hit-effect2", 1, 1);
            case (2) -> target.getWorld().playSound(target.getLocation(), "wizard-magicshot-hit-effect3", 1, 1);
        }

        new BukkitRunnable() {
            int rate = 0;
            float angle = 0.19625F;
            float size = 1;

            @Override
            public void run() {

                rate++;

                if (rate == 14) {
                    for (int particle : particles.keySet()) {
                        particles.get(particle).remove();
                    }
                    cancel();
                    return;
                }

                angle = angle + 0.19625F;
                size = size + 0.2F;
                Random random = new Random();

                for (int number : particles.keySet()) {
                    int axisX = random.nextInt(2) == 0 ? -1 : 1;
                    int axisZ = random.nextInt(2) == 0 ? -1 : 1;
                    if (target instanceof ItemDisplay) {
                        particles.get(number).setTransformation(new Transformation(new Vector3f(0F, 0F, 0F), new Quaternionf(), new Vector3f(0F + size, 0F + size, 0F + size), new Quaternionf().rotationAxis(angle + number / 0.5F, axisX, 0, axisZ)));
                    } else {
                        particles.get(number).setTransformation(new Transformation(new Vector3f(0F, -0.8F, 0F), new Quaternionf(), new Vector3f(0F + size, 0F + size, 0F + size), new Quaternionf().rotationAxis(angle + number, axisX, 0, axisZ)));
                    }
                    particles.get(number).setInterpolationDuration(3);
                    particles.get(number).setInterpolationDelay(0);

                }

                ItemMeta meta = effect.getItemMeta();


                switch (rate) {
                    case (4) -> meta.setCustomModelData(20);
                    case (8) -> meta.setCustomModelData(21);
                    case (12) -> meta.setCustomModelData(22);
                }
                effect.setItemMeta(meta);
                particles.get(1).setItemStack(effect);
                particles.get(2).setItemStack(effect);
                particles.get(3).setItemStack(effect);
                particles.get(4).setItemStack(effect);
                particles.get(5).setItemStack(effect);


            }
        }.runTaskTimer(Abilities.instance, 0, 1);

    }

    public static void action(Player player) {
        Random random = new Random();
        int number = random.nextInt(3);
        switch (number) {
            case (0) -> player.getWorld().playSound(player.getLocation(), "wizard-magicshot-action-effect1", 1, 1);
            case (1) -> player.getWorld().playSound(player.getLocation(), "wizard-magicshot-action-effect2", 1, 1);
            case (2) -> player.getWorld().playSound(player.getLocation(), "wizard-magicshot-action-effect3", 1, 1);
        }

        player.getWorld().playSound(player.getLocation(), "hunter-powerfulshot-action", 1, 1);

        Location location = player.getLocation();
        location = location.set(location.getX(), location.getY() + 1.7, location.getZ());
        ItemStack effect = new ItemStack(Material.STICK);
        ItemMeta meta = effect.getItemMeta();

        meta.setCustomModelData(2);
        effect.setItemMeta(meta);

        ItemDisplay display = player.getWorld().spawn(location, ItemDisplay.class);
        display.setItemStack(effect);
        display.setTransformation(new Transformation(new Vector3f(0F, 0F, 2.5F), new Quaternionf(), new Vector3f(3, 3, 1), new Quaternionf().rotationAxis(1.57F, 1, 0, 0)));
        display.setInterpolationDuration(0);
        display.setInterpolationDelay(0);
        display.setBrightness(new Display.Brightness(15, 15));

        new BukkitRunnable() {
            int rate = 0;

            @Override
            public void run() {

                rate++;

                ItemMeta meta = effect.getItemMeta();
                switch (rate) {
                    case (2) -> meta.setCustomModelData(20);
                    case (4) -> meta.setCustomModelData(21);
                    case (6) -> meta.setCustomModelData(22);
                    case (8) -> {
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

}
