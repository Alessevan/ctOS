package com.ctos.trafficlight.model;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;

import java.util.Objects;

/**
 * Immutable representation of a block position in the world
 */
public class BlockPosition {
    private final String worldName;
    private final int x;
    private final int y;
    private final int z;

    public BlockPosition(String worldName, int x, int y, int z) {
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static BlockPosition fromLocation(Location location) {
        return new BlockPosition(
                location.getWorld().getName(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
        );
    }

    public Location toLocation(Server server) {
        World world = server.getWorld(worldName);
        if (world == null) {
            throw new IllegalStateException("World " + worldName + " is not loaded");
        }
        return new Location(world, x, y, z);
    }

    public Location toLocation() {
        return toLocation(Bukkit.getServer());
    }

    public String getWorldName() {
        return worldName;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockPosition that = (BlockPosition) o;
        return x == that.x &&
                y == that.y &&
                z == that.z &&
                Objects.equals(worldName, that.worldName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(worldName, x, y, z);
    }

    @Override
    public String toString() {
        return worldName + "_" + x + "_" + y + "_" + z;
    }

    /**
     * Creates a BlockPosition from a string representation
     * Format: "worldName_x_y_z"
     */
    public static BlockPosition fromString(String str) {
        String[] parts = str.split("_", 4);
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid BlockPosition string: " + str);
        }
        return new BlockPosition(
                parts[0],
                Integer.parseInt(parts[1]),
                Integer.parseInt(parts[2]),
                Integer.parseInt(parts[3])
        );
    }
}
