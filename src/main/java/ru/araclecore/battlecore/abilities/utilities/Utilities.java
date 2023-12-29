package ru.araclecore.battlecore.abilities.utilities;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Utilities {
    public static Component convert(String string) {
        return LegacyComponentSerializer.legacySection().deserialize(string);
    }

    public static Location point(double distance, Location location) {
        return location.set(location.getX() + location.getDirection().getX() * distance,
                location.getY() + location.getDirection().getY() * distance,
                location.getZ() + location.getDirection().getZ() * distance);
    }

    public static void charge(Player player, int model, int slot) {
        ItemStack item = player.getInventory().getItem(0);
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        meta.setCustomModelData(model);
        item.setItemMeta(meta);
        player.getInventory().setItem(slot, item);

    }

}
