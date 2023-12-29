package ru.araclecore.battlecore.abilities.utilities;

import com.google.common.collect.Maps;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import ru.araclecore.battlecore.abilities.Abilities;
import ru.araclecore.battlecore.heroes.Heroes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Timer {

    public Map<Player, List<Time>> timers;

    public record Time(Ability ability, BukkitTask task) {
    }

    public Timer() {
        timers = Maps.newHashMap();
    }

    public void start(Ability ability) {

        if (!timers.containsKey(ability.player())) {
            List<Time> timers = new ArrayList<>();
            this.timers.put(ability.player(), timers);
        }

        timers.get(ability.player()).add(new Time(ability, new BukkitRunnable() {
            final int count = ability.count();

            @Override
            public void run() {
                if (!ability.player().isOnline()) return;
                if (ability.active()) return;
                if (ability.countdown() > count) {
                    ability.countdown(0);
                    cancel();
                    return;
                }
                charge(ability);
                ability.countdown(ability.countdown() + 1);
            }
        }.runTaskTimer(Abilities.instance, 0, 20)));
    }

    public void stop(Player player) {
        if (!timers.containsKey(player)) return;
        timers.get(player).forEach(time -> time.task.cancel());
        timers.remove(player);
    }

    private void charge(Ability ability) {
        int countdown = ability.countdown();
        int count = ability.count();
        Player player = ability.player();
        if (countdown == count * 0.25)
            Utilities.charge(player, Heroes.manager.get(player).hero.suit().weapon().charge25(), 0);
        if (countdown == count * 0.5)
            Utilities.charge(player, Heroes.manager.get(player).hero.suit().weapon().charge50(), 0);
        if (countdown == count * 0.75)
            Utilities.charge(player, Heroes.manager.get(player).hero.suit().weapon().charge75(), 0);
        if (countdown == count) {
            player.playSound(player.getLocation(), "warrior-secret-hit-charged", 1, 1);
            Utilities.charge(player, Heroes.manager.get(player).hero.suit().weapon().charged(), 0);
        }
    }
}
