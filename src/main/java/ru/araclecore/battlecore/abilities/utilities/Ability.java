package ru.araclecore.battlecore.abilities.utilities;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import ru.araclecore.battlecore.heroes.heroes.enums.Trigger;

public interface Ability {

    Player player();

    boolean active();

    int count();

    int countdown();

    void count(int count);

    void active(boolean active);

    void countdown(int countdown);

    void action(Trigger trigger, @Nullable Entity target);

}
