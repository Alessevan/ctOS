package com.ctos.commands;

import com.ctos.CtOSPlugin;
import com.ctos.trafficlight.model.Intersection;
import com.ctos.trafficlight.service.IntersectionManager;
import com.ctos.trafficlight.state.SetupSession;
import com.ctos.trafficlight.state.WandState;
import com.ctos.trafficlight.state.WandStateManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.ctos.trafficlight.model.BlockPosition;
import com.ctos.trafficlight.model.TrafficLightSide;
import com.ctos.trafficlight.model.LightPhase;

/**
 * Handles all ctOS commands
 */
public class WandCommand implements CommandExecutor, TabCompleter {
    private final CtOSPlugin plugin;
    private final WandStateManager wandStateManager;
    private final IntersectionManager intersectionManager;

    public WandCommand(CtOSPlugin plugin, WandStateManager wandStateManager, IntersectionManager intersectionManager) {
        this.plugin = plugin;
        this.wandStateManager = wandStateManager;
        this.intersectionManager = intersectionManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subcommand = args[0].toLowerCase();

        switch (subcommand) {
            case "wand":
                return handleWand(sender);
            case "list":
                return handleList(sender);
            case "remove":
                return handleRemove(sender, args);
            case "info":
                return handleInfo(sender, args);
            case "edit":
                return handleEdit(sender, args);
            case "cancel":
                return handleCancel(sender);
            case "reload":
                return handleReload(sender);
            default:
                sendHelp(sender);
                return true;
        }
    }

    /**
     * Gives the player a wand and starts a setup session
     */
    private boolean handleWand(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("Only players can use this command").color(NamedTextColor.RED));
            return true;
        }

        Player player = (Player) sender;

        // Check permission
        if (!player.hasPermission("ctos.wand")) {
            player.sendMessage(Component.text("You don't have permission to use the wand").color(NamedTextColor.RED));
            return true;
        }

        // Give wand item
        ItemStack wand = WandState.createWand();
        player.getInventory().addItem(wand);

        // Start setup session
        SetupSession session = wandStateManager.startSession(player);
        session.sendPrompt(player);

        player.sendMessage(Component.text("You received the ctOS Wand!").color(NamedTextColor.GREEN));

        return true;
    }

    /**
     * Lists all intersections with clickable actions
     */
    private boolean handleList(CommandSender sender) {
        if (!sender.hasPermission("ctos.use")) {
            sender.sendMessage(Component.text("You don't have permission to list intersections").color(NamedTextColor.RED));
            return true;
        }

        List<Intersection> intersections = new ArrayList<>(intersectionManager.getAllIntersections());

        if (intersections.isEmpty()) {
            sender.sendMessage(Component.text("No intersections configured").color(NamedTextColor.YELLOW));
            return true;
        }

        sender.sendMessage(Component.text("=== Intersections ===").color(NamedTextColor.GOLD));
        for (Intersection intersection : intersections) {
            String id = intersection.getId().toString();

            // Info button
            Component infoButton = Component.text("[Info]")
                    .color(NamedTextColor.AQUA)
                    .decorate(TextDecoration.BOLD)
                    .clickEvent(ClickEvent.runCommand("/ctos info " + id))
                    .hoverEvent(HoverEvent.showText(Component.text("Click to view details")));

            // Edit button
            Component editButton = Component.text("[Edit]")
                    .color(NamedTextColor.GREEN)
                    .decorate(TextDecoration.BOLD)
                    .clickEvent(ClickEvent.runCommand("/ctos edit " + id))
                    .hoverEvent(HoverEvent.showText(Component.text("Click to edit this intersection")));

            // Remove button
            Component removeButton = Component.text("[Remove]")
                    .color(NamedTextColor.RED)
                    .decorate(TextDecoration.BOLD)
                    .clickEvent(ClickEvent.suggestCommand("/ctos remove " + id))
                    .hoverEvent(HoverEvent.showText(Component.text("Click to remove this intersection")));

            Component message = Component.text("- ")
                    .append(Component.text(intersection.getName()).color(NamedTextColor.YELLOW))
                    .append(Component.text(" (" + intersection.getSides().size() + " sides) ").color(NamedTextColor.GRAY))
                    .append(infoButton)
                    .append(Component.text(" "))
                    .append(editButton)
                    .append(Component.text(" "))
                    .append(removeButton);

            sender.sendMessage(message);
        }

        return true;
    }

    /**
     * Removes an intersection
     */
    private boolean handleRemove(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ctos.admin")) {
            sender.sendMessage(Component.text("You don't have permission to remove intersections").color(NamedTextColor.RED));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /ctos remove <id|name>").color(NamedTextColor.RED));
            return true;
        }

        String identifier = args[1];

        // Try to parse as UUID
        try {
            UUID id = UUID.fromString(identifier);
            if (intersectionManager.hasIntersection(id)) {
                intersectionManager.removeIntersection(id);
                sender.sendMessage(Component.text("Removed intersection").color(NamedTextColor.GREEN));
                return true;
            }
        } catch (IllegalArgumentException e) {
            // Not a UUID, try by name
            List<Intersection> matches = intersectionManager.findIntersectionsByName(identifier);

            if (matches.isEmpty()) {
                sender.sendMessage(Component.text("No intersection found with that ID or name").color(NamedTextColor.RED));
                return true;
            }

            if (matches.size() > 1) {
                sender.sendMessage(Component.text("Multiple intersections match that name. Use the ID instead:").color(NamedTextColor.YELLOW));
                for (Intersection match : matches) {
                    sender.sendMessage(Component.text("- " + match.getName() + " (" + match.getId() + ")").color(NamedTextColor.GRAY));
                }
                return true;
            }

            intersectionManager.removeIntersection(matches.get(0).getId());
            sender.sendMessage(Component.text("Removed intersection: " + matches.get(0).getName()).color(NamedTextColor.GREEN));
            return true;
        }

        sender.sendMessage(Component.text("Intersection not found").color(NamedTextColor.RED));
        return true;
    }

    /**
     * Shows information about an intersection
     * If no argument given and sender is a player, finds the nearest intersection
     */
    private boolean handleInfo(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ctos.use")) {
            sender.sendMessage(Component.text("You don't have permission to view intersection info").color(NamedTextColor.RED));
            return true;
        }

        Intersection intersection = null;

        if (args.length < 2) {
            // No argument - try to find nearest intersection if player
            if (!(sender instanceof Player)) {
                sender.sendMessage(Component.text("Usage: /ctos info <id|name>").color(NamedTextColor.RED));
                return true;
            }

            Player player = (Player) sender;
            intersection = findNearestIntersection(player, 50); // 50 blocks max distance

            if (intersection == null) {
                sender.sendMessage(Component.text("No intersection found nearby. Usage: /ctos info <id|name>").color(NamedTextColor.RED));
                return true;
            }

            sender.sendMessage(Component.text("Found nearby intersection: " + intersection.getName()).color(NamedTextColor.GRAY));
        } else {
            // Argument given - find by ID or name
            String identifier = args[1];

            // Try to parse as UUID
            try {
                UUID id = UUID.fromString(identifier);
                intersection = intersectionManager.getIntersection(id).orElse(null);
            } catch (IllegalArgumentException e) {
                // Not a UUID, try by name
                List<Intersection> matches = intersectionManager.findIntersectionsByName(identifier);
                if (matches.size() == 1) {
                    intersection = matches.get(0);
                } else if (matches.size() > 1) {
                    sender.sendMessage(Component.text("Multiple intersections match that name:").color(NamedTextColor.YELLOW));
                    for (Intersection match : matches) {
                        sender.sendMessage(Component.text("- " + match.getName() + " (" + match.getId() + ")").color(NamedTextColor.GRAY));
                    }
                    return true;
                }
            }

            if (intersection == null) {
                sender.sendMessage(Component.text("Intersection not found").color(NamedTextColor.RED));
                return true;
            }
        }

        // Display intersection info
        displayIntersectionInfo(sender, intersection);
        return true;
    }

    /**
     * Finds the nearest intersection to a player within maxDistance blocks
     */
    private Intersection findNearestIntersection(Player player, double maxDistance) {
        Location playerLoc = player.getLocation();
        Intersection nearest = null;
        double nearestDistance = maxDistance;

        for (Intersection intersection : intersectionManager.getAllIntersections()) {
            Set<BlockPosition> blocks = intersection.getAllBlocks();

            for (BlockPosition blockPos : blocks) {
                // Check if same world
                if (!blockPos.getWorldName().equals(playerLoc.getWorld().getName())) {
                    continue;
                }

                // Calculate distance
                double dx = blockPos.getX() - playerLoc.getX();
                double dy = blockPos.getY() - playerLoc.getY();
                double dz = blockPos.getZ() - playerLoc.getZ();
                double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearest = intersection;
                }
            }
        }

        return nearest;
    }

    /**
     * Displays detailed information about an intersection
     */
    private void displayIntersectionInfo(CommandSender sender, Intersection intersection) {
        sender.sendMessage(Component.text("=== Intersection: " + intersection.getName() + " ===").color(NamedTextColor.GOLD));
        sender.sendMessage(Component.text("ID: ").color(NamedTextColor.GRAY)
                .append(Component.text(intersection.getId().toString()).color(NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("Status: ").color(NamedTextColor.GRAY)
                .append(Component.text(intersection.isComplete() ? "Complete" : "Incomplete")
                        .color(intersection.isComplete() ? NamedTextColor.GREEN : NamedTextColor.RED)));
        sender.sendMessage(Component.text("Sides: ").color(NamedTextColor.GRAY)
                .append(Component.text(String.valueOf(intersection.getSides().size())).color(NamedTextColor.WHITE)));

        // Display each side
        int sideIndex = 0;
        for (TrafficLightSide side : intersection.getSides()) {
            sideIndex++;
            int redBlocks = side.getLightBlocks(LightPhase.RED).size();
            int orangeBlocks = side.getLightBlocks(LightPhase.ORANGE).size();
            int greenBlocks = side.getLightBlocks(LightPhase.GREEN).size();

            sender.sendMessage(Component.text("  Side " + sideIndex + " (" + side.getDirection() + "): ").color(NamedTextColor.YELLOW)
                    .append(Component.text(redBlocks + "R ").color(NamedTextColor.RED))
                    .append(Component.text(orangeBlocks + "O ").color(NamedTextColor.GOLD))
                    .append(Component.text(greenBlocks + "G").color(NamedTextColor.GREEN)));
        }

        // Display neutral state
        if (intersection.getNeutralState() != null) {
            sender.sendMessage(Component.text("Neutral block: ").color(NamedTextColor.GRAY)
                    .append(Component.text(intersection.getNeutralState().getMaterial().toString()).color(NamedTextColor.WHITE)));
        } else {
            sender.sendMessage(Component.text("Neutral block: ").color(NamedTextColor.GRAY)
                    .append(Component.text("Not set").color(NamedTextColor.RED)));
        }

        // Display timing info
        if (intersection.getTiming() != null) {
            sender.sendMessage(Component.text("Timing: ").color(NamedTextColor.GRAY)
                    .append(Component.text("Green=" + intersection.getTiming().getGreenDurationTicks() + "t, ")
                            .color(NamedTextColor.GREEN))
                    .append(Component.text("Orange=" + intersection.getTiming().getOrangeDurationTicks() + "t, ")
                            .color(NamedTextColor.GOLD))
                    .append(Component.text("Gap=" + intersection.getTiming().getAllRedGapTicks() + "t")
                            .color(NamedTextColor.RED)));
        }
    }

    /**
     * Starts editing an existing intersection
     * If no argument given and sender is a player, finds the nearest intersection
     */
    private boolean handleEdit(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("Only players can use this command").color(NamedTextColor.RED));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("ctos.admin")) {
            player.sendMessage(Component.text("You don't have permission to edit intersections").color(NamedTextColor.RED));
            return true;
        }

        Intersection intersection = null;

        if (args.length < 2) {
            // No argument - try to find nearest intersection
            intersection = findNearestIntersection(player, 50);

            if (intersection == null) {
                sender.sendMessage(Component.text("No intersection found nearby. Usage: /ctos edit <id|name>").color(NamedTextColor.RED));
                return true;
            }
        } else {
            // Argument given - find by ID or name
            String identifier = args[1];

            try {
                UUID id = UUID.fromString(identifier);
                intersection = intersectionManager.getIntersection(id).orElse(null);
            } catch (IllegalArgumentException e) {
                List<Intersection> matches = intersectionManager.findIntersectionsByName(identifier);
                if (matches.size() == 1) {
                    intersection = matches.get(0);
                } else if (matches.size() > 1) {
                    sender.sendMessage(Component.text("Multiple intersections match that name:").color(NamedTextColor.YELLOW));
                    for (Intersection match : matches) {
                        sender.sendMessage(Component.text("- " + match.getName() + " (" + match.getId() + ")").color(NamedTextColor.GRAY));
                    }
                    return true;
                }
            }

            if (intersection == null) {
                sender.sendMessage(Component.text("Intersection not found").color(NamedTextColor.RED));
                return true;
            }
        }

        // Give wand if player doesn't have one
        if (!hasWandInInventory(player)) {
            ItemStack wand = WandState.createWand();
            player.getInventory().addItem(wand);
            player.sendMessage(Component.text("You received the ctOS Wand!").color(NamedTextColor.GREEN));
        }

        // Start edit session
        SetupSession session = wandStateManager.startEditSession(player, intersection);
        player.sendMessage(Component.text("Editing intersection: " + intersection.getName()).color(NamedTextColor.GOLD));
        displayEditMenu(player, intersection);
        session.sendPrompt(player);

        return true;
    }

    /**
     * Displays the edit menu with current intersection state
     */
    private void displayEditMenu(Player player, Intersection intersection) {
        player.sendMessage(Component.text("=== Edit Mode ===").color(NamedTextColor.GOLD));
        player.sendMessage(Component.text("Current sides:").color(NamedTextColor.GRAY));

        int index = 1;
        for (TrafficLightSide side : intersection.getSides()) {
            int r = side.getLightBlocks(LightPhase.RED).size();
            int o = side.getLightBlocks(LightPhase.ORANGE).size();
            int g = side.getLightBlocks(LightPhase.GREEN).size();
            player.sendMessage(Component.text("  " + index + ". " + side.getDirection() + " ")
                    .color(NamedTextColor.YELLOW)
                    .append(Component.text("(" + r + "R/" + o + "O/" + g + "G)").color(NamedTextColor.GRAY)));
            index++;
        }

        player.sendMessage(Component.text("Commands:").color(NamedTextColor.GRAY));
        player.sendMessage(Component.text("  'add' - Add a new side").color(NamedTextColor.WHITE));
        player.sendMessage(Component.text("  'edit <n>' - Re-configure side n").color(NamedTextColor.WHITE));
        player.sendMessage(Component.text("  'remove <n>' - Remove side n").color(NamedTextColor.WHITE));
        player.sendMessage(Component.text("  'neutral' - Change neutral block").color(NamedTextColor.WHITE));
        player.sendMessage(Component.text("  'timing' - Change timing").color(NamedTextColor.WHITE));
        player.sendMessage(Component.text("  'done' - Save and exit").color(NamedTextColor.WHITE));
    }

    /**
     * Checks if the player has a wand in their inventory
     */
    private boolean hasWandInInventory(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && WandState.isWand(item)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Cancels the player's current setup session
     */
    private boolean handleCancel(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("Only players can use this command").color(NamedTextColor.RED));
            return true;
        }

        Player player = (Player) sender;

        if (!wandStateManager.hasSession(player)) {
            player.sendMessage(Component.text("You don't have an active setup session").color(NamedTextColor.RED));
            return true;
        }

        wandStateManager.cancelSession(player);
        return true;
    }

    /**
     * Reloads the plugin configuration
     */
    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("ctos.admin")) {
            sender.sendMessage(Component.text("You don't have permission to reload the plugin").color(NamedTextColor.RED));
            return true;
        }

        plugin.reloadConfig();
        sender.sendMessage(Component.text("Configuration reloaded").color(NamedTextColor.GREEN));
        return true;
    }

    /**
     * Sends help message
     */
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Component.text("=== ctOS Traffic Lights ===").color(NamedTextColor.GOLD));
        sender.sendMessage(Component.text("/ctos wand").color(NamedTextColor.YELLOW)
                .append(Component.text(" - Get the setup wand").color(NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/ctos list").color(NamedTextColor.YELLOW)
                .append(Component.text(" - List all intersections").color(NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/ctos remove <id>").color(NamedTextColor.YELLOW)
                .append(Component.text(" - Remove an intersection").color(NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/ctos info [id]").color(NamedTextColor.YELLOW)
                .append(Component.text(" - Show intersection info (auto-detect if nearby)").color(NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/ctos edit [id]").color(NamedTextColor.YELLOW)
                .append(Component.text(" - Edit an intersection (auto-detect if nearby)").color(NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/ctos cancel").color(NamedTextColor.YELLOW)
                .append(Component.text(" - Cancel current setup").color(NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/ctos reload").color(NamedTextColor.YELLOW)
                .append(Component.text(" - Reload configuration").color(NamedTextColor.GRAY)));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subcommands = Arrays.asList("wand", "list", "remove", "info", "edit", "cancel", "reload");
            String partial = args[0].toLowerCase();

            for (String sub : subcommands) {
                if (sub.startsWith(partial)) {
                    completions.add(sub);
                }
            }
        }

        return completions;
    }
}
