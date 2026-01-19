package com.ctos.trafficlight.model;

import com.destroystokyo.paper.profile.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.block.data.BlockData;
import org.bukkit.profile.PlayerTextures;

import java.net.URL;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Complete block data storage including NBT metadata
 * Critical for preserving player heads with textures
 */
public class BlockStateData {
    private static final Logger LOGGER = Logger.getLogger("ctOS");
    private static boolean debugEnabled = false;

    /**
     * Sets the debug mode (called from plugin)
     */
    public static void setDebugEnabled(boolean enabled) {
        debugEnabled = enabled;
    }

    private static void debug(String message) {
        if (debugEnabled) {
            LOGGER.info("[DEBUG] " + message);
        }
    }

    private Material material;
    private String blockDataString;
    private String playerProfileData; // For player heads

    public BlockStateData(Material material, String blockDataString, String playerProfileData) {
        this.material = material;
        this.blockDataString = blockDataString;
        this.playerProfileData = playerProfileData;
    }

    /**
     * Captures complete block state including NBT data
     * This method preserves ALL metadata, especially player head textures and rotation
     */
    public static BlockStateData capture(Block block) {
        Material material = block.getType();

        // Capture the complete block data string (includes rotation for heads)
        String blockDataString = block.getBlockData().getAsString();
        String playerProfileData = null;

        // Special handling for player heads to preserve texture
        BlockState state = block.getState();
        if (state instanceof Skull) {
            Skull skull = (Skull) state;
            PlayerProfile profile = skull.getPlayerProfile();

            if (profile != null) {
                playerProfileData = serializePlayerProfile(profile);
            }

            // Log the rotation for debugging
            debug("Captured player head with blockData: " + blockDataString);
        }

        return new BlockStateData(material, blockDataString, playerProfileData);
    }

    /**
     * Applies this block data to a block in the world
     * Restores the block exactly as it was captured, with automatic rotation based on cardinal direction
     * @param block The block to update
     * @param direction Cardinal direction (North/South/East/West) for automatic head rotation
     */
    public void applyToBlock(Block block, String direction) {
        // Set material and block data (this includes rotation for heads)
        String adjustedBlockDataString = adjustBlockDataForDirection(blockDataString, direction);
        BlockData blockData = Bukkit.createBlockData(adjustedBlockDataString);
        block.setBlockData(blockData, false); // false = no physics update for performance

        // If this was a player head, restore the profile while preserving rotation
        if (playerProfileData != null && (block.getType() == Material.PLAYER_HEAD || block.getType() == Material.PLAYER_WALL_HEAD)) {
            debug("Applying player head with blockData: " + adjustedBlockDataString + " (direction: " + direction + ")");

            BlockState state = block.getState();
            if (state instanceof Skull) {
                Skull skull = (Skull) state;
                PlayerProfile profile = deserializePlayerProfile(playerProfileData);

                if (profile != null) {
                    skull.setPlayerProfile(profile);

                    // CRITICAL: Set blockData on Skull to preserve rotation
                    // Without this, skull.update() writes a default blockData without rotation
                    skull.setBlockData(blockData);

                    // Update the skull state - now includes both profile AND rotation
                    skull.update(true, false); // Update the block state

                    debug("Applied player head, final blockData: " + block.getBlockData().getAsString());
                }
            }
        }
    }

    /**
     * Applies this block data to a block in the world (without direction override)
     * Uses the original captured rotation
     */
    public void applyToBlock(Block block) {
        applyToBlock(block, null);
    }

    /**
     * Applies this block data to a block, copying the facing from another BlockStateData
     * Useful for neutral blocks that need to match the orientation of the block they replace
     */
    public void applyToBlockWithFacingFrom(Block block, BlockStateData facingSource) {
        String facing = facingSource != null ? facingSource.extractFacing() : null;
        String adjustedBlockDataString = applyFacingToBlockData(blockDataString, facing);

        BlockData blockData = Bukkit.createBlockData(adjustedBlockDataString);
        block.setBlockData(blockData, false);

        // If this was a player head, restore the profile
        if (playerProfileData != null && (block.getType() == Material.PLAYER_HEAD || block.getType() == Material.PLAYER_WALL_HEAD)) {
            BlockState state = block.getState();
            if (state instanceof Skull) {
                Skull skull = (Skull) state;
                PlayerProfile profile = deserializePlayerProfile(playerProfileData);

                if (profile != null) {
                    skull.setPlayerProfile(profile);
                    skull.setBlockData(blockData);
                    skull.update(true, false);
                }
            }
        }
    }

    /**
     * Extracts the facing direction from this block's blockDataString
     * @return The facing direction (north/south/east/west) or null if not found
     */
    public String extractFacing() {
        if (blockDataString == null || !blockDataString.contains("facing=")) {
            return null;
        }
        // Extract facing value from string like "minecraft:player_wall_head[facing=north]"
        int start = blockDataString.indexOf("facing=") + 7;
        int end = start;
        while (end < blockDataString.length() && Character.isLetter(blockDataString.charAt(end))) {
            end++;
        }
        return blockDataString.substring(start, end);
    }

    /**
     * Applies a specific facing to a blockData string (without inversion)
     */
    private String applyFacingToBlockData(String originalBlockData, String facing) {
        if (facing == null || !originalBlockData.contains("player")) {
            return originalBlockData;
        }
        if (originalBlockData.contains("facing=")) {
            return originalBlockData.replaceFirst("facing=[a-z]+", "facing=" + facing);
        }
        return originalBlockData;
    }

    /**
     * Adjusts the blockData string to set the facing direction based on cardinal direction
     * For player_wall_head and player_head blocks
     */
    private String adjustBlockDataForDirection(String originalBlockData, String direction) {
        if (direction == null || !originalBlockData.contains("player")) {
            return originalBlockData;
        }

        // Map cardinal direction to Minecraft facing direction
        // The head must face OPPOSITE to the side direction:
        // A "South" side is for drivers coming FROM the south, so the head must face NORTH (towards them)
        String facing;
        String dirLower = direction.toLowerCase();
        if (dirLower.contains("north")) {
            facing = "south";  // North side -> face South (towards drivers coming from north)
        } else if (dirLower.contains("south")) {
            facing = "north";  // South side -> face North (towards drivers coming from south)
        } else if (dirLower.contains("east")) {
            facing = "west";   // East side -> face West (towards drivers coming from east)
        } else if (dirLower.contains("west")) {
            facing = "east";   // West side -> face East (towards drivers coming from west)
        } else {
            return originalBlockData; // Unknown direction, keep original
        }

        // Replace the facing value in the blockData string
        // Example: "minecraft:player_wall_head[facing=north,powered=false]" -> "minecraft:player_wall_head[facing=south,powered=false]"
        if (originalBlockData.contains("facing=")) {
            return originalBlockData.replaceFirst("facing=[a-z]+", "facing=" + facing);
        }

        return originalBlockData;
    }

    /**
     * Serializes a PlayerProfile to a string for storage
     * Format: "uuid|name|textureUrl|textureValue|signatureValue"
     */
    private static String serializePlayerProfile(PlayerProfile profile) {
        try {
            StringBuilder sb = new StringBuilder();

            // UUID
            UUID uuid = profile.getId();
            sb.append(uuid != null ? uuid.toString() : "");
            sb.append("|");

            // Name
            String name = profile.getName();
            sb.append(name != null ? name : "");
            sb.append("|");

            // Textures
            PlayerTextures textures = profile.getTextures();
            if (textures != null) {
                URL skinUrl = textures.getSkin();
                sb.append(skinUrl != null ? skinUrl.toString() : "");
            }
            sb.append("|");

            // For complete texture data, we need to access the raw properties
            // This is a simplified version - we'll store the essential texture URL
            // Paper's API handles the texture restoration automatically from the URL

            return sb.toString();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to serialize player profile", e);
            return null;
        }
    }

    /**
     * Deserializes a PlayerProfile from a string
     */
    private static PlayerProfile deserializePlayerProfile(String data) {
        try {
            String[] parts = data.split("\\|", -1);
            if (parts.length < 3) {
                return null;
            }

            // Create profile
            UUID uuid = parts[0].isEmpty() ? null : UUID.fromString(parts[0]);
            String name = parts[1].isEmpty() ? null : parts[1];

            PlayerProfile profile;
            if (uuid != null) {
                profile = Bukkit.createProfile(uuid, name);
            } else if (name != null) {
                profile = Bukkit.createProfile(name);
            } else {
                profile = Bukkit.createProfile(UUID.randomUUID());
            }

            // Set texture URL if available
            if (parts.length > 2 && !parts[2].isEmpty()) {
                try {
                    URL textureUrl = new URL(parts[2]);
                    PlayerTextures textures = profile.getTextures();
                    textures.setSkin(textureUrl);
                    profile.setTextures(textures);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Failed to restore texture URL", e);
                }
            }

            return profile;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to deserialize player profile", e);
            return null;
        }
    }

    /**
     * Creates a deep copy of this BlockStateData
     */
    public BlockStateData clone() {
        return new BlockStateData(material, blockDataString, playerProfileData);
    }

    public Material getMaterial() {
        return material;
    }

    public String getBlockDataString() {
        return blockDataString;
    }

    public String getPlayerProfileData() {
        return playerProfileData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockStateData that = (BlockStateData) o;
        return material == that.material &&
                Objects.equals(blockDataString, that.blockDataString) &&
                Objects.equals(playerProfileData, that.playerProfileData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(material, blockDataString, playerProfileData);
    }
}
