package com.ctos.trafficlight.state;

import com.ctos.CtOSPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates and identifies the magic wand item
 */
public class WandState {
    private static NamespacedKey WAND_KEY;

    /**
     * Initializes the wand key
     */
    public static void initialize(CtOSPlugin plugin) {
        WAND_KEY = new NamespacedKey(plugin, "traffic_wand");
    }

    /**
     * Creates a magic wand ItemStack
     */
    public static ItemStack createWand() {
        if (WAND_KEY == null) {
            throw new IllegalStateException("WandState not initialized");
        }

        ItemStack wand = new ItemStack(Material.STICK);
        ItemMeta meta = wand.getItemMeta();

        // Set display name using Adventure API
        meta.displayName(Component.text("ctOS Traffic Light Wand")
                .color(NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));

        // Set lore
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Right-click: Select block")
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Left-click: Confirm selection")
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("Configure traffic lights")
                .color(NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));

        meta.lore(lore);

        // Add PDC marker
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(WAND_KEY, PersistentDataType.BYTE, (byte) 1);

        wand.setItemMeta(meta);
        return wand;
    }

    /**
     * Checks if an ItemStack is a traffic wand
     */
    public static boolean isWand(ItemStack item) {
        if (WAND_KEY == null) {
            throw new IllegalStateException("WandState not initialized");
        }

        if (item == null || !item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        return pdc.has(WAND_KEY, PersistentDataType.BYTE);
    }

    /**
     * Removes all traffic wands from a player's inventory
     */
    public static void removeWandFromInventory(Player player) {
        if (WAND_KEY == null) {
            throw new IllegalStateException("WandState not initialized");
        }

        PlayerInventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (isWand(item)) {
                inventory.setItem(i, null);
            }
        }
    }
}
