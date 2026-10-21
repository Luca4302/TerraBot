package main;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fileio.CommandInput;

abstract class Air extends Entity {
    protected double humidity;
    protected double temperature;
    protected double oxygenLevel;
    protected double airQualityScore;
    protected double toxicityLevel;
    protected boolean isAirToxic;
    protected String airQualityLevel;
    public Air(String name, String type, Double mass, double humidity, double temperature, double oxygenLevel) {
        super(name, type, mass);
        this.humidity = humidity;
        this.temperature = temperature;
        this.oxygenLevel = oxygenLevel;
        this.airQualityScore = airQuality();
        updateAirStatus();
    }

    public abstract double airQuality();

    public abstract void changeWeatherConditions(CommandInput comandaVreme);

    public abstract double MaxScore();
    @JsonIgnore
    public double getAirDamage() {
        return this.toxicityScore();
    }

    public boolean airToxic() {
        return toxicityLevel > 0.8 * MaxScore();
    }

    public double toxicityScore() {
        double score = 100 * (1 - airQualityScore / MaxScore());
        double normalisedScore;
        normalisedScore = Math.max(0, Math.min(100, score));
        return Math.round(normalisedScore * 100.0) / 100.0;
    }

    public void updateAirStatus() {
        this.airQualityScore = Math.round(Math.max(0, Math.min(100, airQuality())) * 100.0) / 100.0;
        this.toxicityLevel = toxicityScore();
        this.isAirToxic = airToxic();
        this.airQualityLevel = determineQuality(airQualityScore);
    }

    public double getHumidity() { return humidity; }
    public double getTemperature() { return temperature; }
    public double getOxygenLevel() { return oxygenLevel; }
    public double getAirQuality() { return airQualityScore; }


    public void increaseHumidity(double amount) {
        this.humidity += amount;
        updateAirStatus();
    }

    public void addOxygen(double amount) {
        this.oxygenLevel += amount;
        updateAirStatus();
    }

    public static class tropical extends Air {
        double co2Level;

        public tropical(String name, String type, Double mass, double humidity, double temperature, double oxygenLevel, double co2Level) {
            super(name, type, mass, humidity, temperature, oxygenLevel);
            this.co2Level = co2Level;
            updateAirStatus();
        }
        public double getCo2Level() { return co2Level; }
        public double airQuality() {
            double score = (oxygenLevel * 2) + (humidity * 0.5) - (co2Level * 0.01);
            return finalScore(score);
        }

        @Override
        public void changeWeatherConditions(CommandInput comandaVreme) {
            if (comandaVreme != null && comandaVreme.getType() != null && comandaVreme.getType().equals("rainfall")) {
                double rainfall = comandaVreme.getRainfall();
                this.airQualityScore += (rainfall * 0.3);
                updateAirStatus();
            }
        }

        @Override
        public double MaxScore() {
            return 82;
        }
    }

    public static class polar extends Air {
        double iceCrystalConcentration;

        public polar(String name, String type, Double mass, double humidity, double temperature, double oxygenLevel, double iceCrystalConcentration) {
            super(name, type, mass, humidity, temperature, oxygenLevel);
            this.iceCrystalConcentration = iceCrystalConcentration;
            updateAirStatus();
        }
        public double getIceCrystalConcentration() { return iceCrystalConcentration; }
        @Override
        public double airQuality() {
            double score = (oxygenLevel * 2) + (100 - Math.abs(temperature)) - (iceCrystalConcentration * 0.05);
            return finalScore(score);
        }

        @Override
        public void changeWeatherConditions(CommandInput comandaVreme) {
            if (comandaVreme != null && comandaVreme.getType() != null && comandaVreme.getType().equals("polarStorm")) {
                double vant = comandaVreme.getWindSpeed();
                this.airQualityScore = this.airQualityScore - (vant * 0.2);
                updateAirStatus();
            }
        }

        @Override
        public double MaxScore() {
            return 142;
        }

    }

    public static class temperat extends Air {
        double pollenLevel;
        public temperat(String name, String type, Double mass, double humidity, double temperature, double oxygenLevel, double pollenLevel) {
            super(name, type, mass, humidity, temperature, oxygenLevel);
            this.pollenLevel = pollenLevel;
            updateAirStatus();
        }
        public double getPollenLevel() { return pollenLevel; }
        @Override
        public double airQuality() {
            double score = (oxygenLevel * 2) + (humidity * 0.7) - (pollenLevel * 0.1);
            return finalScore(score);
        }

        @Override
        public void changeWeatherConditions(CommandInput comandaVreme) {
            if (comandaVreme != null && comandaVreme.getType() != null && comandaVreme.getType().equals("newSeason")) {
                String season = comandaVreme.getSeason();
                double seasonPenalty = 0.0;
                if (season.equalsIgnoreCase("Spring")) {
                    seasonPenalty = 15.0;
                }
                airQualityScore -= seasonPenalty;
            }
            updateAirStatus();
        }

        @Override
        public double MaxScore() {
            return 84;
        }
    }

    public static class desert extends Air {
        public boolean desertStorm;
        double dustParticles;
        public desert(String name, String type, Double mass, double humidity, double temperature, double oxygenLevel, double dustParticles) {
            super(name, type, mass, humidity, temperature, oxygenLevel);
            this.dustParticles = dustParticles;
            this.desertStorm = false;
            updateAirStatus();
        }
        public double getDustParticles() { return dustParticles; }
        public boolean isDesertStorm() { return desertStorm; }
        @Override
        public double airQuality() {
            double score = (oxygenLevel * 2) - (dustParticles * 0.2) - (temperature * 0.3);
            return finalScore(score);
        }

        @Override
        public void changeWeatherConditions(CommandInput comandaVreme) {
            if (comandaVreme != null && comandaVreme.getType() != null && comandaVreme.getType().equals("desertStorm")) {
                this.desertStorm = comandaVreme.isDesertStorm();
                airQualityScore = this.airQuality() - (this.desertStorm ? 30 : 0);
            }
            updateAirStatus();
        }

        @Override @JsonIgnore
        public double MaxScore() {
            return 65;
        }
    }

    public static class mountain extends Air {
        double altitude;

        public mountain(String name, String type, Double mass, double humidity, double temperature, double oxygenLevel, double altitude) {
            super(name, type, mass, humidity, temperature, oxygenLevel);
            this.altitude = altitude;
            updateAirStatus();
        }
        public double getAltitude() { return altitude; }
        @Override
        public double airQuality() {
            double oxygenFactor = this.oxygenLevel - (this.altitude / 1000.0 * 0.5);
            double score = (oxygenFactor * 2) + (this.humidity * 0.6);
            return finalScore(score);
        }

        @Override
        public void changeWeatherConditions(CommandInput comandaVreme) {
            if (comandaVreme != null && comandaVreme.getType() != null && comandaVreme.getType().equals("peopleHiking")) {
                double hikerNumber = comandaVreme.getNumberOfHikers();
                airQualityScore -= (hikerNumber * 0.1);
            }
            updateAirStatus();
        }

        @Override
        public double MaxScore() {
            return 78;
        }
    }
}
