package com.ctos.trafficlight.model;

import java.util.*;

/**
 * Represents one side/direction of a traffic light intersection
 * Each side has blocks for red, orange, and green lights, plus optional pedestrian lights
 */
public class TrafficLightSide {
    /**
     * Groups sides by cardinal direction for NS/EW synchronization
     */
    public enum DirectionGroup {
        NORTH_SOUTH,
        EAST_WEST,
        UNKNOWN
    }

    private String direction; // e.g., "North", "South", "East", "West"
    private Map<LightPhase, List<BlockPosition>> lightBlocks; // Blocks for each phase
    private Map<BlockPosition, BlockStateData> blockStates; // Original block data for each position
    private List<BlockPosition> pedestrianGreenBlocks; // Pedestrian green light blocks
    private Map<BlockPosition, BlockStateData> pedestrianGreenBlockStates; // Pedestrian green block states
    private List<BlockPosition> pedestrianRedBlocks; // Pedestrian red light blocks
    private Map<BlockPosition, BlockStateData> pedestrianRedBlockStates; // Pedestrian red block states

    public TrafficLightSide(String direction) {
        this.direction = direction;
        this.lightBlocks = new HashMap<>();
        this.blockStates = new HashMap<>();
        this.pedestrianGreenBlocks = new ArrayList<>();
        this.pedestrianGreenBlockStates = new HashMap<>();
        this.pedestrianRedBlocks = new ArrayList<>();
        this.pedestrianRedBlockStates = new HashMap<>();

        // Initialize empty lists for each phase
        for (LightPhase phase : LightPhase.values()) {
            lightBlocks.put(phase, new ArrayList<>());
        }
    }

    /**
     * Sets the blocks for a specific light phase
     */
    public void setLightBlocks(LightPhase phase, List<BlockPosition> blocks, List<BlockStateData> states) {
        if (blocks.size() != states.size()) {
            throw new IllegalArgumentException("Blocks and states lists must have same size");
        }

        lightBlocks.put(phase, new ArrayList<>(blocks));

        // Store block states
        for (int i = 0; i < blocks.size(); i++) {
            blockStates.put(blocks.get(i), states.get(i));
        }
    }

    /**
     * Adds a single block for a specific light phase
     */
    public void addLightBlock(LightPhase phase, BlockPosition position, BlockStateData state) {
        lightBlocks.get(phase).add(position);
        blockStates.put(position, state);
    }

    /**
     * Sets the pedestrian green light blocks
     */
    public void setPedestrianGreenBlocks(List<BlockPosition> blocks, List<BlockStateData> states) {
        if (blocks.size() != states.size()) {
            throw new IllegalArgumentException("Blocks and states lists must have same size");
        }

        this.pedestrianGreenBlocks = new ArrayList<>(blocks);

        // Store pedestrian green block states
        for (int i = 0; i < blocks.size(); i++) {
            pedestrianGreenBlockStates.put(blocks.get(i), states.get(i));
        }
    }

    /**
     * Adds a single pedestrian green light block
     */
    public void addPedestrianGreenBlock(BlockPosition position, BlockStateData state) {
        pedestrianGreenBlocks.add(position);
        pedestrianGreenBlockStates.put(position, state);
    }

    /**
     * Sets the pedestrian red light blocks
     */
    public void setPedestrianRedBlocks(List<BlockPosition> blocks, List<BlockStateData> states) {
        if (blocks.size() != states.size()) {
            throw new IllegalArgumentException("Blocks and states lists must have same size");
        }

        this.pedestrianRedBlocks = new ArrayList<>(blocks);

        // Store pedestrian red block states
        for (int i = 0; i < blocks.size(); i++) {
            pedestrianRedBlockStates.put(blocks.get(i), states.get(i));
        }
    }

    /**
     * Adds a single pedestrian red light block
     */
    public void addPedestrianRedBlock(BlockPosition position, BlockStateData state) {
        pedestrianRedBlocks.add(position);
        pedestrianRedBlockStates.put(position, state);
    }

    /**
     * Gets all blocks managed by this side (for any phase)
     */
    public List<BlockPosition> getAllBlocks() {
        Set<BlockPosition> allBlocks = new HashSet<>();

        for (List<BlockPosition> blocks : lightBlocks.values()) {
            allBlocks.addAll(blocks);
        }

        allBlocks.addAll(pedestrianGreenBlocks);
        allBlocks.addAll(pedestrianRedBlocks);

        return new ArrayList<>(allBlocks);
    }

    /**
     * Gets the blocks for a specific phase
     */
    public List<BlockPosition> getLightBlocks(LightPhase phase) {
        return new ArrayList<>(lightBlocks.get(phase));
    }

    /**
     * Gets the original block state for a position
     */
    public BlockStateData getBlockState(BlockPosition position) {
        BlockStateData state = blockStates.get(position);
        if (state == null) {
            state = pedestrianGreenBlockStates.get(position);
        }
        if (state == null) {
            state = pedestrianRedBlockStates.get(position);
        }
        return state;
    }

    /**
     * Checks if this side is fully configured
     */
    public boolean isComplete() {
        // Must have at least one block for each light phase
        for (LightPhase phase : LightPhase.values()) {
            if (lightBlocks.get(phase).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if this side has pedestrian lights configured
     */
    public boolean hasPedestrianLights() {
        return !pedestrianGreenBlocks.isEmpty() && !pedestrianRedBlocks.isEmpty();
    }

    /**
     * Determines the direction group (NS/EW) based on the actual direction name
     * This is used for intelligent NS/EW synchronization regardless of configuration order
     */
    public DirectionGroup getDirectionGroup() {
        if (direction == null) {
            return DirectionGroup.UNKNOWN;
        }

        String dir = direction.toLowerCase();
        if (dir.contains("north") || dir.contains("south") || dir.equals("ns")) {
            return DirectionGroup.NORTH_SOUTH;
        } else if (dir.contains("east") || dir.contains("west") || dir.equals("ew")) {
            return DirectionGroup.EAST_WEST;
        }
        return DirectionGroup.UNKNOWN;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public List<BlockPosition> getPedestrianGreenBlocks() {
        return new ArrayList<>(pedestrianGreenBlocks);
    }

    public List<BlockPosition> getPedestrianRedBlocks() {
        return new ArrayList<>(pedestrianRedBlocks);
    }

    public Map<BlockPosition, BlockStateData> getBlockStates() {
        return new HashMap<>(blockStates);
    }

    public Map<BlockPosition, BlockStateData> getPedestrianGreenBlockStates() {
        return new HashMap<>(pedestrianGreenBlockStates);
    }

    public Map<BlockPosition, BlockStateData> getPedestrianRedBlockStates() {
        return new HashMap<>(pedestrianRedBlockStates);
    }

    public Map<LightPhase, List<BlockPosition>> getLightBlocksMap() {
        Map<LightPhase, List<BlockPosition>> copy = new HashMap<>();
        for (Map.Entry<LightPhase, List<BlockPosition>> entry : lightBlocks.entrySet()) {
            copy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return copy;
    }
}
