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
    private int scannedTimestamp = -1;

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
    public void setScanned(int timestamp) {
        if (!this.scanned) {
            this.scanned = true;
            this.scannedTimestamp = timestamp;
        }
    }
    @JsonIgnore
    public int getScannedTimestamp() {
        return this.scannedTimestamp;
    }
    public double getMass() {
        return Math.round(this.mass * 100.0) / 100.0;
    }
}