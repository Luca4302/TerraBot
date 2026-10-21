package main;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;

abstract class Soil extends Entity {

    @Getter
    double nitrogen;
    @Getter
    double waterRetention;
    @Getter
    double soilpH;
    @Getter
    double organicMatter;
    double soilScoreLevelWord;
    public Soil(String name, String type, double mass, double nitrogen, double waterRetention, double soilpH, double organicMatter) {
        super(name, type, mass);
        this.nitrogen = nitrogen;
        this.waterRetention = waterRetention;
        this.soilpH = soilpH;
        this.organicMatter = organicMatter;

    }

    public double getSoilQuality() { return this.quality(); }

    public abstract double quality();

    public abstract double terraBotStuck();

    public void soilScore(double finalScore) {
        soilScoreLevelWord = (finalScore);
    }

    public void absorbWater() {
        this.waterRetention += 0.1;
        this.quality();  // Obligatoriu! Ca sa recalculeze finalScore cu noul +0.1 la apa
    }

//mai tarziu interactiunea cu mediul
//poate muti normalise quality si rotunjirea sa cuprinda si air si soil cumva.. in entity POATE
//la fel ca la air e basic
//vezi cum interactioneaza cu plantele

    public void increaseWaterRetention(double amount) {
        this.waterRetention += amount;
    }

    public void addOrganicMatter(double amount) {
        this.organicMatter += amount;
    }

    public static class ForestSoil extends Soil {
        double leafLitter;

        public ForestSoil(String name, String type, double mass, double nitrogen, double waterRetention, double soilpH, double organicMatter, double leafLitter) {
            super(name, type, mass, nitrogen, waterRetention, soilpH, organicMatter);
            this.leafLitter = leafLitter;
        }

        public double getLeafLitter() { return leafLitter; }
        @Override @JsonIgnore
        public double quality() {
            double score = (nitrogen * 1.2) + (organicMatter * 2) + (waterRetention * 1.5) + (leafLitter * 0.3);
            double finalScore = finalScore(score);
            double finalScoreVal = finalScore(score);
            soilScore(finalScore);
            return finalScoreVal;
        }

        @Override
        public double terraBotStuck() {
            return (waterRetention * 0.6 + leafLitter * 0.4) / 80 * 100;
        }
    }

    public static class SwampSoil extends Soil {
        double waterLogging;

        public SwampSoil(String name, String type, double mass, double nitrogen, double waterRetention, double soilpH, double organicMatter, double waterLogging) {
            super(name, type, mass, nitrogen, waterRetention, soilpH, organicMatter);
            this.waterLogging = waterLogging;
        }
        public double getWaterLogging() { return waterLogging; }
        @Override @JsonIgnore
        public double quality() {
            double score = (nitrogen * 1.1) + (organicMatter * 2.2) - (waterLogging * 5);
            double finalScore = finalScore(score);
            double finalScoreVal = finalScore(score);
            soilScore(finalScore);
            return finalScoreVal;
        }

        @Override
        public double terraBotStuck() {
            return waterLogging * 10;
        }
    }

    public static class DesertSoil extends Soil {
        double salinity;

        public DesertSoil(String name, String type, double mass, double nitrogen, double waterRetention, double soilpH, double organicMatter, double salinity) {
            super(name, type, mass, nitrogen, waterRetention, soilpH, organicMatter);
            this.salinity=salinity;
        }
        public double getSalinity() { return salinity; }
        @Override @JsonIgnore
        public double quality() {
            double score = (nitrogen * 0.5) + (waterRetention * 0.3) - (salinity * 2);
            double finalScore = finalScore(score);
            double finalScoreVal = finalScore(score);
            soilScore(finalScore);
            return finalScoreVal;
        }

        @Override
        public double terraBotStuck() {
            return (100 - waterRetention + salinity) / 100 * 100;
        }
    }

    public static class GrasslandSoil extends Soil {
        double rootDensity;

        public GrasslandSoil(String name, String type, double mass, double nitrogen, double waterRetention, double soilpH, double organicMatter, double rootDensity) {
            super(name, type, mass, nitrogen, waterRetention, soilpH, organicMatter);
            this.rootDensity = rootDensity;
        }
        public double getRootDensity() { return rootDensity; }
        @Override @JsonIgnore
        public double quality() {
            double score = (nitrogen * 1.3) + (organicMatter * 1.5) + (rootDensity * 0.8);
            double finalScore = finalScore(score);
            double finalScoreVal = finalScore(score);
            soilScore(finalScore);
            return finalScoreVal;
        }

        @Override
        public double terraBotStuck() {
            return ((50 - rootDensity) + waterRetention * 0.5) / 75 * 100;
        }
    }

    public static class TundraSoil extends Soil {
        double permafrostDepth;

        public TundraSoil(String name, String type, double mass, double nitrogen, double waterRetention, double soilpH, double organicMatter, double permafrostDepth) {
            super(name, type, mass, nitrogen, waterRetention, soilpH, organicMatter);
            this.permafrostDepth = permafrostDepth;
        }
        public double getPermafrostDepth() { return permafrostDepth; }
        @Override @JsonIgnore
        public double quality() {
            double score = (nitrogen * 0.7) + (organicMatter * 0.5) - (permafrostDepth * 1.5);
            double finalScore = finalScore(score);
            double finalScoreVal = finalScore(score);
            soilScore(finalScore);
            return finalScoreVal;
        }

        @Override
        public double terraBotStuck() {
            return (50 - permafrostDepth) / 50 * 100;
        }
    }
}

/*Ce am facut azi:
toata clasa soil
o trimitere de date din soil catre planetMap care prelucreaza niste date despre interactiunea claselor
am trimis aceasta metoda acolo si nu am facut o in soil pentru ca aveam nevoie de trecerea timpului care apartine de time
De facut:
partea cu interactiunea cu plantele si metoda de miscare a robotului si (un getter in planetmaps si un getter in time ca acolo se face conexiunea cu terrabot si inca un getter in terrabot pentru stuckChance)
 */
