package main;

abstract class Entity {
    String name;
    Double mass;
    String type;

    public Entity(String name, String type, Double mass) {
        this.name = name;
        this.type = type;
        this.mass = mass;
    }

    public String determineQuality(double QualityScore) {
        if (QualityScore < 40) {
            return "Poor";
        } else if (QualityScore >= 70) {
            return "Good";
        } else {
            return "Moderate";
        }
    }

    public double finalScore(double score) {
        double normalisedScore;
        normalisedScore = Math.max(0, Math.min(100, score));
        return Math.round(normalisedScore * 100.0) / 100.0;
    }

    private boolean scanned = false;

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
