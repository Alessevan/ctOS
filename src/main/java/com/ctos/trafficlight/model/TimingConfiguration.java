package com.ctos.trafficlight.model;

/**
 * Configuration for traffic light timing
 * All durations are in ticks (20 ticks = 1 second)
 */
public class TimingConfiguration {
    private int greenDurationTicks;
    private int orangeDurationTicks;
    private int pedestrianGreenDurationTicks;
    private int allRedGapTicks;

    public TimingConfiguration(int greenDurationTicks, int orangeDurationTicks,
                               int pedestrianGreenDurationTicks, int allRedGapTicks) {
        this.greenDurationTicks = greenDurationTicks;
        this.orangeDurationTicks = orangeDurationTicks;
        this.pedestrianGreenDurationTicks = pedestrianGreenDurationTicks;
        this.allRedGapTicks = allRedGapTicks;
    }

    /**
     * Creates default timing configuration
     * Green: 10 seconds, Orange: 3 seconds, Pedestrian: 7.5 seconds, Gap: 1 second
     */
    public static TimingConfiguration getDefault() {
        return new TimingConfiguration(200, 60, 150, 20);
    }

    public int getGreenDurationTicks() {
        return greenDurationTicks;
    }

    public void setGreenDurationTicks(int greenDurationTicks) {
        this.greenDurationTicks = greenDurationTicks;
    }

    public int getOrangeDurationTicks() {
        return orangeDurationTicks;
    }

    public void setOrangeDurationTicks(int orangeDurationTicks) {
        this.orangeDurationTicks = orangeDurationTicks;
    }

    public int getPedestrianGreenDurationTicks() {
        return pedestrianGreenDurationTicks;
    }

    public void setPedestrianGreenDurationTicks(int pedestrianGreenDurationTicks) {
        this.pedestrianGreenDurationTicks = pedestrianGreenDurationTicks;
    }

    public int getAllRedGapTicks() {
        return allRedGapTicks;
    }

    public void setAllRedGapTicks(int allRedGapTicks) {
        this.allRedGapTicks = allRedGapTicks;
    }

    /**
     * Calculates the total duration for one full cycle
     * This includes both north-south and east-west phases
     */
    public int getTotalCycleDuration() {
        // One complete cycle has two main phases (NS and EW)
        // Each phase has: green + orange + all-red gap
        int singlePhaseDuration = greenDurationTicks + orangeDurationTicks + allRedGapTicks;
        return singlePhaseDuration * 2;
    }
}
