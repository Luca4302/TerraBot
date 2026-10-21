package main;

import com.fasterxml.jackson.annotation.JsonIgnore;

class Plants extends Entity {
    private final PlantType plantDetails;
    double plantMaturity = 0.0;
    double oxygenLevel = 0.0;
    PlantMaturity plantAge;
    private int stageIndex = 1;
    public Plants(String name, String type, Double mass) {
        super(name, type, mass);
        this.plantDetails = PlantType.valueOf(type);
        this.plantAge = PlantMaturity.Young;
    }



    public PlantMaturity setMaturity() {
        return switch (this.stageIndex) {
            case 1 -> PlantMaturity.Young;
            case 2 -> PlantMaturity.Mature;
            case 3 -> PlantMaturity.Old;
            case 4 -> PlantMaturity.Dead;
            default -> null;
        };
    }

    public void grow(double value) {
        plantMaturity += value;
        if (plantMaturity >= 1) {
            plantMaturity = 0;
            stageIndex++;
        }
        this.plantAge = setMaturity();
    }

    public double oxigenGenerated() {
        oxygenLevel = this.plantAge.getter() + this.plantDetails.getter();
        return oxygenLevel;
    }
    @JsonIgnore
    public double getPlantStuckChance() {
        return this.plantDetails.getterAccident() / 100.0;
    }
    @JsonIgnore
    public boolean isDead() {
        return this.plantAge == PlantMaturity.Dead;
    }

    enum PlantType {
        FloweringPlants(6.0, 90),
        GymnospermsPlants(0.0, 60),
        Ferns(0.0, 30),
        Mosses(0.8, 40),
        Algae(0.5, 20);

        private final double o2;
        private final Integer accidentChance;

        PlantType(double o2, int accidentChance) {
            this.o2 = o2;
            this.accidentChance = accidentChance;
        }

        public double getter() {
            return this.o2;
        }

        public Integer getterAccident() {
            return this.accidentChance;
        }
    }

    enum PlantMaturity {
        Young(0.2),
        Mature(0.7),
        Old(0.4),
        Dead(0.0);

        private final double o2;

        PlantMaturity(double o2) {
            this.o2 = o2;
        }

        public double getter() {
            return this.o2;
        }
    }

}
