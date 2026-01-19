package com.ctos.trafficlight.cycle;

import com.ctos.trafficlight.model.Intersection;
import com.ctos.trafficlight.model.TimingConfiguration;

/**
 * Manages the traffic light cycle for an intersection
 */
public class TrafficCycle {
    private final Intersection intersection;
    private CyclePhase currentPhase;
    private long phaseStartTime;

    public TrafficCycle(Intersection intersection) {
        this.intersection = intersection;
        this.currentPhase = CyclePhase.NS_GREEN;
        this.phaseStartTime = System.currentTimeMillis();
    }

    /**
     * Advances to the next phase if enough time has elapsed
     * @return true if the phase was advanced
     */
    public boolean tick() {
        if (shouldAdvance()) {
            advance();
            return true;
        }
        return false;
    }

    /**
     * Checks if enough time has passed to advance to the next phase
     */
    private boolean shouldAdvance() {
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - phaseStartTime;
        long required = getRequiredDurationForCurrentPhase();

        return elapsed >= required;
    }

    /**
     * Advances to the next phase
     */
    private void advance() {
        currentPhase = currentPhase.getNext();
        phaseStartTime = System.currentTimeMillis();
    }

    /**
     * Gets the required duration for the current phase in milliseconds
     */
    private long getRequiredDurationForCurrentPhase() {
        TimingConfiguration timing = intersection.getTiming();
        int ticks;

        switch (currentPhase) {
            case NS_GREEN:
            case EW_GREEN:
                ticks = timing.getGreenDurationTicks();
                break;

            case NS_ORANGE:
            case EW_ORANGE:
                ticks = timing.getOrangeDurationTicks();
                break;

            case NS_TO_EW_TRANSITION:
            case EW_TO_NS_TRANSITION:
                ticks = timing.getAllRedGapTicks();
                break;

            default:
                ticks = 20; // 1 second default
        }

        // Convert ticks to milliseconds (1 tick = 50ms)
        return ticks * 50;
    }

    /**
     * Forces advancement to a specific phase
     */
    public void setPhase(CyclePhase phase) {
        this.currentPhase = phase;
        this.phaseStartTime = System.currentTimeMillis();
    }

    /**
     * Resets the cycle to the beginning
     */
    public void reset() {
        this.currentPhase = CyclePhase.NS_GREEN;
        this.phaseStartTime = System.currentTimeMillis();
    }

    public CyclePhase getCurrentPhase() {
        return currentPhase;
    }

    public Intersection getIntersection() {
        return intersection;
    }

    public long getTimeInCurrentPhase() {
        return System.currentTimeMillis() - phaseStartTime;
    }

    public long getTimeRemainingInPhase() {
        long required = getRequiredDurationForCurrentPhase();
        long elapsed = getTimeInCurrentPhase();
        return Math.max(0, required - elapsed);
    }
}
