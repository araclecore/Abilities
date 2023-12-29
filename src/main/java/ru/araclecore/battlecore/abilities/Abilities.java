package ru.araclecore.battlecore.abilities;

import org.bukkit.plugin.java.JavaPlugin;
import ru.araclecore.battlecore.abilities.utilities.Manager;
import ru.araclecore.battlecore.abilities.utilities.Timer;
import ru.araclecore.battlecore.abilities.utilities.Triggers;
import ru.araclecore.battlecore.connection.utilities.Configuration;


public final class Abilities extends JavaPlugin {

    public static Abilities instance;

    public static Configuration abilities;
    public static Manager manager;

    public static Timer timer;

    @Override
    public void onEnable() {
        instance = this;
        manager = new Manager();
        abilities = new Configuration(instance, "abilities.yml");
        timer = new Timer();
        getServer().getPluginManager().registerEvents(manager, instance);
        getServer().getPluginManager().registerEvents(new Triggers(), instance);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
