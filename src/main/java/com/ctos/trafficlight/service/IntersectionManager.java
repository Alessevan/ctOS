package com.ctos.trafficlight.service;

import com.ctos.trafficlight.model.BlockPosition;
import com.ctos.trafficlight.model.Intersection;

import java.util.*;
import java.util.logging.Logger;

/**
 * Central registry and management of all intersections
 * Provides fast lookups by ID or block position
 */
public class IntersectionManager {
    private static final Logger LOGGER = Logger.getLogger("ctOS");

    private final Map<UUID, Intersection> intersections;
    private final Map<BlockPosition, UUID> blockToIntersection;

    public IntersectionManager() {
        this.intersections = new HashMap<>();
        this.blockToIntersection = new HashMap<>();
    }

    /**
     * Registers an intersection
     */
    public void registerIntersection(Intersection intersection) {
        if (intersection == null) {
            throw new IllegalArgumentException("Intersection cannot be null");
        }

        intersections.put(intersection.getId(), intersection);
        updateBlockIndex(intersection);

        LOGGER.info("Registered intersection: " + intersection.getName() + " (" + intersection.getId() + ")");
    }

    /**
     * Removes an intersection by ID
     */
    public void removeIntersection(UUID id) {
        Intersection intersection = intersections.remove(id);

        if (intersection != null) {
            // Remove block mappings
            for (BlockPosition pos : intersection.getAllBlocks()) {
                blockToIntersection.remove(pos);
            }

            LOGGER.info("Removed intersection: " + intersection.getName() + " (" + id + ")");
        }
    }

    /**
     * Gets an intersection by ID
     */
    public Optional<Intersection> getIntersection(UUID id) {
        return Optional.ofNullable(intersections.get(id));
    }

    /**
     * Gets an intersection by a block position it contains
     */
    public Optional<Intersection> getIntersectionByBlock(BlockPosition position) {
        UUID id = blockToIntersection.get(position);
        if (id == null) {
            return Optional.empty();
        }
        return getIntersection(id);
    }

    /**
     * Gets all registered intersections
     */
    public Collection<Intersection> getAllIntersections() {
        return new ArrayList<>(intersections.values());
    }

    /**
     * Gets the number of registered intersections
     */
    public int getIntersectionCount() {
        return intersections.size();
    }

    /**
     * Checks if an intersection with the given ID exists
     */
    public boolean hasIntersection(UUID id) {
        return intersections.containsKey(id);
    }

    /**
     * Checks if a block position is part of any intersection
     */
    public boolean isBlockManaged(BlockPosition position) {
        return blockToIntersection.containsKey(position);
    }

    /**
     * Updates the block position index for an intersection
     * This should be called whenever an intersection's blocks change
     */
    public void updateBlockIndex(Intersection intersection) {
        // Remove old mappings for this intersection
        blockToIntersection.entrySet().removeIf(entry -> entry.getValue().equals(intersection.getId()));

        // Add new mappings
        for (BlockPosition pos : intersection.getAllBlocks()) {
            blockToIntersection.put(pos, intersection.getId());
        }
    }

    /**
     * Clears all intersections from memory
     */
    public void clear() {
        intersections.clear();
        blockToIntersection.clear();
        LOGGER.info("Cleared all intersections from memory");
    }

    /**
     * Gets intersections by name (case-insensitive partial match)
     */
    public List<Intersection> findIntersectionsByName(String name) {
        List<Intersection> results = new ArrayList<>();
        String searchTerm = name.toLowerCase();

        for (Intersection intersection : intersections.values()) {
            if (intersection.getName().toLowerCase().contains(searchTerm)) {
                results.add(intersection);
            }
        }

        return results;
    }
}
