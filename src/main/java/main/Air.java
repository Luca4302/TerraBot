package main;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fileio.CommandInput;

abstract class Air extends Entity {
    protected double weatherModifier = 0.0;
    protected int weatherDuration = 0;
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

    public abstract boolean changeWeatherConditions(CommandInput comandaVreme);

    public abstract double MaxScore();
    public void tickWeather() {
        if (this.weatherDuration > 0) {
            this.weatherDuration--;
            if (this.weatherDuration == 0) {
                this.weatherModifier = 0.0;
                onWeatherExpired();
                updateAirStatus();
            }
        }
    }
    protected void onWeatherExpired() {}
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
        double rawScore = airQuality() + weatherModifier;
        this.airQualityScore = finalScore(rawScore);
        this.toxicityLevel = toxicityScore();
        this.isAirToxic = airToxic();
        this.airQualityLevel = determineQuality(airQualityScore);
    }

    public double getHumidity() { return humidity; }
    public double getTemperature() { return temperature; }
    public double getOxygenLevel() { return oxygenLevel; }
    public double getAirQuality() { return airQualityScore; }


    public void increaseHumidity(double amount) {
        this.humidity = Math.round((this.humidity + amount) * 100.0) / 100.0;
        updateAirStatus();
    }

    public void addOxygen(double amount) {
        this.oxygenLevel = Math.round((this.oxygenLevel + amount) * 100.0) / 100.0;
        updateAirStatus();
    }

    public static class tropical extends Air {
        double co2Level;

        public tropical(String name, String type, Double mass, double humidity, double temperature, double oxygenLevel, double co2Level) {
            super(name, type, mass, humidity, temperature, oxygenLevel);
            this.co2Level = co2Level;
            updateAirStatus();
        }
        public double getCo2Level() { return Math.round(co2Level * 100.0) / 100.0; }
        public double airQuality() {
            double score = (oxygenLevel * 2) + (humidity * 0.5) - (co2Level * 0.01);
            return finalScore(score);
        }

        @Override
        public boolean changeWeatherConditions(CommandInput comandaVreme) {
            if (comandaVreme != null && "rainfall".equals(comandaVreme.getType())){
                this.weatherModifier = comandaVreme.getRainfall() * 0.3;
                this.weatherDuration = 2;
                updateAirStatus();
                return true;
            }
            return false;
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
        public boolean changeWeatherConditions(CommandInput comandaVreme) {
            if (comandaVreme != null && "polarStorm".equals(comandaVreme.getType())){
                this.weatherModifier = -(comandaVreme.getWindSpeed() * 0.2);
                this.weatherDuration = 2;
                updateAirStatus();
                return true;
            }
            return false;
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
        public boolean changeWeatherConditions(CommandInput comandaVreme) {
            if (comandaVreme != null && "newSeason".equals(comandaVreme.getType())) {
                String season = comandaVreme.getSeason();
                this.weatherModifier = (season != null && season.equalsIgnoreCase("Spring")) ? -15.0 : 0.0;
                this.weatherDuration = 2;
                updateAirStatus();
                return true;
            }
            return false;
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
        @JsonIgnore
        public double getDustParticles() { return dustParticles; }

        @Override
        public double airQuality() {
            double score = (oxygenLevel * 2) - (dustParticles * 0.2) - (temperature * 0.3);
            return finalScore(score);
        }

        @Override
        public boolean changeWeatherConditions(CommandInput comandaVreme) {
            if (comandaVreme != null && "desertStorm".equals(comandaVreme.getType())) {
                this.desertStorm = comandaVreme.isDesertStorm();
                airQualityScore = this.airQuality() - (this.desertStorm ? 30 : 0);
                this.weatherModifier = this.desertStorm ? -30.0 : 0.0;
                this.weatherDuration = 2;
                updateAirStatus();
                return true;
            }
            return false;
        }
        public boolean isDesertStorm() { return desertStorm; }
        @Override
        protected void onWeatherExpired() {
            this.desertStorm = false;
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
        public boolean changeWeatherConditions(CommandInput comandaVreme) {
            if (comandaVreme != null && "peopleHiking".equals(comandaVreme.getType())){
                this.weatherModifier = -(comandaVreme.getNumberOfHikers() * 0.1);
                this.weatherDuration = 2;
                updateAirStatus();
                return true;
            }
            return false;
        }

        @Override
        public double MaxScore() {
            return 78;
        }
    }
}
