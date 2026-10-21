package main;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;

abstract class Entity {
    @Getter
    String name;
    Double mass;
    @Getter
    String type;
    private boolean scanned = false;

    public Entity(String name, String type, Double mass) {
        this.name = name;
        this.type = type;
        this.mass = mass;
    }

    public String determineQuality(double QualityScore) {
        if (QualityScore < 40) {
            return "poor";
        } else if (QualityScore >= 70) {
            return "good";
        } else {
            return "moderate";
        }
    }

    protected double finalScore(double score) {
        double normalisedScore = Math.max(0, Math.min(100, score));
        return Math.round(normalisedScore * 100.0) / 100.0;
    }
    @JsonIgnore
    public boolean isScanned() {
        return this.scanned;
    }

    public void setScanned() {
        this.scanned = true;
    }

    public double getMass() {
        return this.mass;
    }

}
