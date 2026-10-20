package main;

import fileio.CommandInput;

abstract class Air extends Entity {
    public Air(String name, String type, Double mass, double humidity, double temperature, double oxygenLevel) {
        super(name, type, mass);
        this.humidity = humidity;
        this.temperature = temperature;
        this.oxygenLevel = oxygenLevel;
        this.airQualityScore = airQuality();
    }

    double humidity;
    double temperature;
    double oxygenLevel;
    double airQualityScore;
    double toxicityLevel;
    boolean isAirToxic;
    String airQualityLevel;

    public abstract double airQuality();

    public abstract void changeWeatherConditions(CommandInput comandaVreme);

    public abstract double getMaxScore();

    public double getAirDamage() {
        return this.toxicityScore();
    }

    public boolean airToxic() {
        if (toxicityLevel > 0.8 * getMaxScore()) {
            return true;
        } else
            return false;
    }

    public double toxicityScore() {
        double score = 100 * (1 - airQualityScore / getMaxScore());
        double normalisedScore;
        normalisedScore = Math.max(0, Math.min(100, score));
        return Math.round(normalisedScore * 100.0) / 100.0;
    }

    public void updateAirStatus() {
        toxicityLevel = toxicityScore();
        isAirToxic = airToxic();
        airQualityLevel = determineQuality(airQualityScore);
        airQualityScore = airQuality();
    }

    public void oxygenFinal(Plants.PlantType plant, Plants.PlantMaturity maturity) {
        oxygenLevel = oxygenLevel + plant.getter() + maturity.getter();
    }

    public static class tropical extends Air {
        public tropical(String name, String type, Double mass, double humidity, double temperature, double oxygenLevel, double co2Level) {
            super(name, type, mass, humidity, temperature, oxygenLevel);
            this.co2Level = co2Level;
        }

        double co2Level;

        public double airQuality() {
            double score = (oxygenLevel * 2) + (humidity * 0.5) - (co2Level * 0.01);
            return finalScore(score);
        }

        @Override
        public void changeWeatherConditions(CommandInput comandaVreme) {
            if (comandaVreme.getType().equals("rainfall")) {
                double rainfall = comandaVreme.getRainfall();
                this.airQualityScore += (rainfall * 0.3);
                updateAirStatus();
            }
        }
        @Override
        public double getMaxScore() {
            return 82;
        }
    }
    public static class polar extends Air{
        public polar(String name, String type, Double mass, double humidity, double temperature, double oxygenLevel, double iceCrystalConcentration) {
            super(name, type, mass, humidity, temperature, oxygenLevel);
            this.iceCrystalConcentration = iceCrystalConcentration;
        }
        double iceCrystalConcentration;

        @Override
        public double airQuality() {
            double score = (oxygenLevel*2) + (100-Math.abs(temperature)) - (iceCrystalConcentration*0.05);
            return finalScore(score);
        }

        @Override
        public void changeWeatherConditions(CommandInput comandaVreme) {
            if (comandaVreme.getType().equals("polarStorm")) {
                double vant = comandaVreme.getWindSpeed();
                this.airQualityScore = this.airQualityScore - (vant * 0.2);
                updateAirStatus();
            }
        }

        @Override
        public double getMaxScore() {
            return 142;
        }

    }
    public static class temperat extends Air{
        public temperat(String name, String type, Double mass, double humidity, double temperature, double oxygenLevel, double pollenLevel) {
            super(name, type, mass, humidity, temperature, oxygenLevel);
            this.pollenLevel=pollenLevel;
        }
        double pollenLevel;
        @Override
        public double airQuality() {
            double score = (oxygenLevel*2) + (humidity*0.7) - (pollenLevel*0.1);
            return finalScore(score);
        }

        @Override
        public void changeWeatherConditions(CommandInput comandaVreme) {
            if(comandaVreme.getType().equals("newSeason")){
                String season = comandaVreme.getSeason();
                double seasonPenalty = 0.0;
                if(season.equalsIgnoreCase("Spring")){
                    seasonPenalty = 15.0;
                }
                airQualityScore-=seasonPenalty;
            }
            updateAirStatus();
        }

        @Override
        public double getMaxScore() {
            return 84;
        }
    }
    public static class desert extends Air{
        public desert(String name, String type, Double mass, double humidity, double temperature, double oxygenLevel,double dustParticles) {
            super(name, type, mass, humidity, temperature, oxygenLevel);
            this.dustParticles=dustParticles;
        }
        double dustParticles;
        @Override
        public double airQuality() {
            double score = (oxygenLevel*2) - (dustParticles*0.2) - (temperature*0.3);
            return finalScore(score);
        }

        @Override
        public void changeWeatherConditions(CommandInput comandaVreme) {
            if(comandaVreme.getType().equals("desertStorm")){
                boolean isStorm = comandaVreme.isDesertStorm();
                airQualityScore = this.airQuality() - (isStorm ? 30 : 0);
            }
            updateAirStatus();
        }

        @Override
        public double getMaxScore() {
            return 65;
        }
    }
    public static class mountain extends Air{
        public mountain(String name, String type, Double mass, double humidity, double temperature, double oxygenLevel,double altitude) {
            super(name, type, mass, humidity, temperature, oxygenLevel);
            this.altitude=altitude;
        }
        double altitude;
        @Override
        public double airQuality() {
            double oxygenFactor = this.oxygenLevel - (this.altitude / 1000.0 * 0.5);
            double score = (oxygenFactor * 2) + (this.humidity * 0.6);
            return finalScore(score);
        }

        @Override
        public void changeWeatherConditions(CommandInput comandaVreme) {
            if(comandaVreme.getType().equals("peopleHiking")){
                double hikerNumber = comandaVreme.getNumberOfHikers();
                airQualityScore -= (hikerNumber*0.1);
            }
            updateAirStatus();
        }

        @Override
        public double getMaxScore() {
            return 78;
        }
    }
    public void increaseHumidity(double amount) {
        this.humidity += amount;
    }

    public void addOxygen(double amount) {
        this.oxygenLevel += amount;
    }
}//mai tarziu
