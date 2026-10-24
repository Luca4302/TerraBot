package main;

import static java.lang.Math.abs;

class Water extends Entity {

    double pH;
    boolean isFrozen;
    double purity;
    double salinity;
    double turbidity;
    double contaminantIndex;
    double purity_score;
    double pH_score;
    double salinity_score;
    double turbidity_score;
    double contaminant_score;
    double frozen_score;
    double water_quality;
    String waterQualityString;
    public Water(String name, String type, Double mass, double purity, double salinity, double turbidity, double contaminantIndex, double pH, boolean isFrozen) {
        super(name, type, mass);
        this.purity = purity;
        this.salinity = salinity;
        this.turbidity = turbidity;
        this.contaminantIndex = contaminantIndex;
        this.isFrozen = isFrozen;
        this.pH = pH;
    }

    public double quality() {
        purity_score = purity / 100;
        pH_score = 1 - abs(pH - 7.5) / 7.5;
        salinity_score = 1 - (salinity / 350);
        turbidity_score = 1 - (turbidity / 100);
        contaminant_score = 1 - (contaminantIndex / 100);
        frozen_score = isFrozen ? 0 : 1;

        water_quality =
                (0.3 * purity_score
                        + 0.2 * pH_score
                        + 0.15 * salinity_score
                        + 0.1 * turbidity_score
                        + 0.15 * contaminant_score
                        + 0.2 * frozen_score) * 100;
        waterQualityString = determineQuality(water_quality);
        return water_quality;
    }

    public void consume(double amount) {
        this.mass = Math.round((this.mass - amount) * 100.0) / 100.0;
        if (this.mass < 0) {
            this.mass = 0.0;
        }
    }
}
