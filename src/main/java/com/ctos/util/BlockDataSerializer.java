package com.ctos.util;

import com.ctos.trafficlight.model.BlockStateData;
import org.bukkit.Material;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Helper class for block data serialization
 */
public class BlockDataSerializer {
    private static final Logger LOGGER = Logger.getLogger("ctOS");

    /**
     * Validates that a block data string is valid
     */
    public static boolean isValidBlockData(String blockDataString) {
        if (blockDataString == null || blockDataString.isEmpty()) {
            return false;
        }

        try {
            org.bukkit.Bukkit.createBlockData(blockDataString);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Invalid block data string: " + blockDataString, e);
            return false;
        }
    }

    /**
     * Validates that a material string is valid
     */
    public static boolean isValidMaterial(String materialName) {
        if (materialName == null || materialName.isEmpty()) {
            return false;
        }

        try {
            Material.valueOf(materialName);
            return true;
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Invalid material name: " + materialName);
            return false;
        }
    }
}
