package ru.araclecore.battlecore.abilities.utilities;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.jetbrains.annotations.Nullable;
import ru.araclecore.battlecore.abilities.Abilities;
import ru.araclecore.battlecore.heroes.heroes.enums.Trigger;

public class Triggers implements Listener {
    @EventHandler
    public void swap(PlayerSwapHandItemsEvent event) {
        event.setCancelled(true);
        Player player = event.getPlayer();
        action(player, Trigger.F, target(player));
    }

    @EventHandler
    public void damage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        if (event.getDamager() instanceof Player player)
            action(player, Trigger.TARGET, target);
        if (event.getDamager() instanceof Arrow arrow)
            if (arrow.getShooter() instanceof Player player)
                action(player, Trigger.TARGET, target);


    }

    @EventHandler
    public void shoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        action(player, Trigger.ARROW, event.getProjectile());
    }


    @EventHandler
    public void interact(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        Trigger trigger;
        switch (action) {
            case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> trigger = Trigger.L;
            default -> trigger = Trigger.R;
        }
        action(player, trigger, target(player));
    }

    public LivingEntity target(Player player) {
        LivingEntity target = null;
        Entity entity = player.getTargetEntity(120);
        if (entity instanceof LivingEntity) target = (LivingEntity) entity;
        return target;
    }

    public void action(Player player, Trigger trigger, @Nullable Entity target) {
        if (!Abilities.manager.abilities.containsKey(player)) return;
        Abilities.manager.abilities.get(player).forEach(ability -> {
            if (ability.triggers().contains(trigger)) ability.ability().action(trigger, target);
        });
    }
}
