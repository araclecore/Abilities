package ru.araclecore.battlecore.abilities.utilities;

import com.google.common.collect.Maps;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.araclecore.battlecore.abilities.Abilities;
import ru.araclecore.battlecore.abilities.abilities.Hunter;
import ru.araclecore.battlecore.abilities.abilities.Nothing;
import ru.araclecore.battlecore.abilities.abilities.Warrior;
import ru.araclecore.battlecore.abilities.abilities.Wizard;
import ru.araclecore.battlecore.connection.utilities.Configuration;
import ru.araclecore.battlecore.connection.utilities.Utilities;
import ru.araclecore.battlecore.heroes.Heroes;
import ru.araclecore.battlecore.heroes.heroes.enums.Trigger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Manager implements Listener {


    public Map<Player, List<ability>> abilities;

    public record ability(List<Trigger> triggers, Ability ability) {
    }

    public Manager() {
        abilities = Maps.newHashMap();
    }

    @EventHandler
    private void join(PlayerJoinEvent event) {
        if (!Utilities.connection()) return;
        Player player = event.getPlayer();
        create(player);
    }

    @EventHandler
    public void quit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Abilities.timer.stop(player);
        if (!Utilities.connection()) return;
        abilities.remove(player);
    }


    private void create(Player player) {
        if (!Utilities.connection()) return;
        String hero = Heroes.manager.get(player).hero.name();
        String ability = Heroes.manager.get(player).hero.ability().name();
        List<Trigger> triggers = Heroes.manager.get(player).hero.ability().triggers();
        List<ability> abilities = new ArrayList<>();
        abilities.add(new ability(triggers, ability(player, ability)));
        this.abilities.put(player, abilities);
        abilities(player, hero);
    }

    private void abilities(Player player, String hero) {
        if (Abilities.abilities.configuration().isConfigurationSection("Abilities." + hero)) {
            ConfigurationSection section = Abilities.abilities.configuration().getConfigurationSection("Abilities." + hero);
            if (section == null) return;
            section.getKeys(false).forEach(ability -> {
                List<Trigger> triggers = Triggers(Abilities.abilities, "Abilities." + hero + "." + ability);
                this.abilities.get(player).add(new ability(triggers, ability(player, ability)));
            });
        }
    }

    private List<Trigger> Triggers(Configuration configuration, String path) {
        if (configuration.String(path) == null) return new ArrayList<>();
        List<String> strings = configuration.Strings(path);
        List<Trigger> triggers = new ArrayList<>();
        strings.forEach(trigger -> {
            switch (trigger) {
                case "R" -> triggers.add(Trigger.R);
                case "L" -> triggers.add(Trigger.L);
                case "SHIFT" -> triggers.add(Trigger.SHIFT);
                case "TARGET" -> triggers.add(Trigger.TARGET);
                case "ARROW" -> triggers.add(Trigger.ARROW);
                case "DAMAGE" -> triggers.add(Trigger.DAMAGE);
                default -> triggers.add(Trigger.F);
            }
        });
        return triggers;
    }

    private Ability ability(Player player, String name) {
        Ability ability;
        switch (name) {
            case "warrior-ability" -> ability = new Warrior(player);
            case "hunter-ability" -> ability = new Hunter(player);
            case "wizard-ability" -> ability = new Wizard(player);
            default -> ability = new Nothing(player);
        }
        return ability;
    }

}
