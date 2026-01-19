package com.ctos.trafficlight.cycle;

import com.ctos.trafficlight.model.LightPhase;

/**
 * Represents the different phases in a traffic light cycle
 * For a 4-way intersection with North-South and East-West axes
 *
 * Pedestrian logic: Pedestrians on a side cross PERPENDICULAR to traffic.
 * - NS pedestrians (on North/South sides) cross the EW road
 * - EW pedestrians (on East/West sides) cross the NS road
 *
 * So pedestrians can cross when the PERPENDICULAR traffic is stopped:
 * - NS pedestrians GREEN when EW traffic is GREEN (NS traffic stopped)
 * - EW pedestrians GREEN when NS traffic is GREEN (EW traffic stopped)
 */
public enum CyclePhase {
    // North-South green phase - NS cars moving, EW cars stopped
    // NS pedestrians RED (would walk into NS traffic), EW pedestrians GREEN (NS road is clear)
    NS_GREEN(LightPhase.GREEN, LightPhase.RED, false, true),

    // North-South orange phase - transition, all pedestrians RED
    NS_ORANGE(LightPhase.ORANGE, LightPhase.RED, false, false),

    // All red transition phase - all pedestrians RED for safety
    NS_TO_EW_TRANSITION(LightPhase.RED, LightPhase.RED, false, false),

    // East-West green phase - EW cars moving, NS cars stopped
    // EW pedestrians RED (would walk into EW traffic), NS pedestrians GREEN (EW road is clear)
    EW_GREEN(LightPhase.RED, LightPhase.GREEN, true, false),

    // East-West orange phase - transition, all pedestrians RED
    EW_ORANGE(LightPhase.RED, LightPhase.ORANGE, false, false),

    // All red transition phase - all pedestrians RED for safety
    EW_TO_NS_TRANSITION(LightPhase.RED, LightPhase.RED, false, false);

    private final LightPhase nsPhase;  // North-South phase
    private final LightPhase ewPhase;  // East-West phase
    private final boolean nsPedestrianGreen;  // Pedestrian lights for NS direction
    private final boolean ewPedestrianGreen;  // Pedestrian lights for EW direction

    CyclePhase(LightPhase nsPhase, LightPhase ewPhase, boolean nsPedestrianGreen, boolean ewPedestrianGreen) {
        this.nsPhase = nsPhase;
        this.ewPhase = ewPhase;
        this.nsPedestrianGreen = nsPedestrianGreen;
        this.ewPedestrianGreen = ewPedestrianGreen;
    }

    /**
     * Gets the light phase for a specific side index
     * @param sideIndex Index of the side (0-based)
     * @param totalSides Total number of sides in the intersection
     * @return The light phase for this side
     */
    public LightPhase getPhaseForSide(int sideIndex, int totalSides) {
        // For 4-way intersections: sides 0,1 are NS, sides 2,3 are EW
        // For 3-way intersections: sides 0,1 are NS, side 2 is EW
        int sidesPerGroup = (int) Math.ceil(totalSides / 2.0);

        if (sideIndex < sidesPerGroup) {
            return nsPhase;  // North-South group
        } else {
            return ewPhase;  // East-West group
        }
    }

    /**
     * Checks if pedestrian lights should be green for a specific side
     */
    public boolean isPedestrianGreen(int sideIndex, int totalSides) {
        int sidesPerGroup = (int) Math.ceil(totalSides / 2.0);

        if (sideIndex < sidesPerGroup) {
            return nsPedestrianGreen;
        } else {
            return ewPedestrianGreen;
        }
    }

    /**
     * Gets the next phase in the cycle
     */
    public CyclePhase getNext() {
        CyclePhase[] phases = CyclePhase.values();
        int currentIndex = this.ordinal();
        int nextIndex = (currentIndex + 1) % phases.length;
        return phases[nextIndex];
    }

    public LightPhase getNsPhase() {
        return nsPhase;
    }

    public LightPhase getEwPhase() {
        return ewPhase;
    }

    public boolean isNsPedestrianGreen() {
        return nsPedestrianGreen;
    }

    public boolean isEwPedestrianGreen() {
        return ewPedestrianGreen;
    }
}
