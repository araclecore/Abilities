package ru.araclecore.battlecore.abilities.abilities;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import ru.araclecore.battlecore.abilities.Abilities;
import ru.araclecore.battlecore.abilities.utilities.Ability;
import ru.araclecore.battlecore.connection.utilities.Logger;
import ru.araclecore.battlecore.heroes.heroes.enums.Trigger;

public class Nothing implements Ability {
    public Nothing(Player player) {
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
        Logger.info(Abilities.instance, "Plug ability trigger was used");
    }
}
