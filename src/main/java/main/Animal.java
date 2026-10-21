package main;

import com.fasterxml.jackson.annotation.JsonIgnore;

class Animal extends Entity implements AnimalActions {

    private final AnimalType animalType;
    int x;
    int y;
    private String state;
    private double pendingSoilBonus;


    public Animal(String name, String type, double mass) {
        super(name, type, mass);
        this.animalType = AnimalType.valueOf(type);
        this.state = "hungry";
        this.pendingSoilBonus = 0.0;
    }

    @Override
    public void animalMovement(PlanetMap theMap, int currentX, int currentY) {
        int[] dx = {-1, 0, 1, 0};
        int[] dy = {0, 1, 0, -1};

        int rows = theMap.planetMaps.length;
        int cols = theMap.planetMaps[0].length;

        int bestX = -1, bestY = -1;
        double bestWaterQuality = -1.0;

        for (int i = 0; i < 4; i++) {
            int nx = currentX + dx[i];
            int ny = currentY + dy[i];

            if (nx >= 0 && nx < rows && ny >= 0 && ny < cols) {
                var neighborCell = theMap.planetMaps[nx][ny];

                if (neighborCell.plant != null && neighborCell.water != null) {

                    if (neighborCell.water.quality() > bestWaterQuality) {
                        bestWaterQuality = neighborCell.water.quality();
                        bestX = nx;
                        bestY = ny;
                    }
                }
            }
        }
        if (bestX != -1) {
            moveToNewCell(theMap, currentX, currentY, bestX, bestY);
            return;
        }
        for (int i = 0; i < 4; i++) {
            int nx = currentX + dx[i];
            int ny = currentY + dy[i];
            if (nx >= 0 && nx < rows && ny >= 0 && ny < cols) {
                if (theMap.planetMaps[nx][ny].plant != null) {
                    moveToNewCell(theMap, currentX, currentY, nx, ny);
                    return;
                }
            }
        }
        bestWaterQuality = -1.0;
        for (int i = 0; i < 4; i++) {
            int nx = currentX + dx[i];
            int ny = currentY + dy[i];
            if (nx >= 0 && nx < rows && ny >= 0 && ny < cols) {
                var neighborCell = theMap.planetMaps[nx][ny];
                if (neighborCell.water != null) {
                    if (neighborCell.water.quality() > bestWaterQuality) {
                        bestWaterQuality = neighborCell.water.quality();
                        bestX = nx;
                        bestY = ny;
                    }
                }
            }
        }

        if (bestX != -1) {
            moveToNewCell(theMap, currentX, currentY, bestX, bestY);
            return;
        }

        for (int i = 0; i < 4; i++) {
            int nx = currentX + dx[i];
            int ny = currentY + dy[i];
            if (nx >= 0 && nx < rows && ny >= 0 && ny < cols) {
                moveToNewCell(theMap, currentX, currentY, nx, ny);
                return;
            }
        }
    }

    private void moveToNewCell(PlanetMap theMap, int oldX, int oldY, int newX, int newY) {
        theMap.planetMaps[newX][newY].animal = this;
        theMap.planetMaps[oldX][oldY].animal = null;
        this.x = newX;
        this.y = newY;
    }
    @JsonIgnore
    public double getAttackProbability() {
        return (100.0 - this.animalType.getAttackPossibility()) / 10.0;
    }

    public void feed(PlanetMap theMap, int currentX, int currentY) {

        var currentCell = theMap.planetMaps[currentX][currentY];

        if (currentCell.air != null && currentCell.air.airQuality() < 20.0) {
            this.state = "sick";
            this.pendingSoilBonus = 0.0;
            return;
        }
        boolean isCarnivoreOrParasite = (this.animalType == AnimalType.Carnivores || this.animalType == AnimalType.Parasites);
        boolean ateSomething = false;

        if (isCarnivoreOrParasite && currentCell.animal != null && currentCell.animal != this) {
            this.mass += currentCell.animal.getMass();
            currentCell.animal = null;
            this.state = "well-fed";
            this.pendingSoilBonus = 0.5;
            ateSomething = true;
        }

        if (!ateSomething) {
            boolean plantScanned = currentCell.plant != null && currentCell.plant.isScanned();
            boolean waterScanned = currentCell.water != null && currentCell.water.isScanned();

            if (plantScanned && waterScanned) {
                double intakeRate = 0.08;
                double waterToDrink = Math.min(this.mass * intakeRate, currentCell.water.getMass());
                currentCell.water.consume(waterToDrink);
                this.mass += (waterToDrink + currentCell.plant.getMass());
                currentCell.plant = null;
                this.state = "well-fed";
                this.pendingSoilBonus = 0.8;
            } else if (plantScanned) {
                this.mass += currentCell.plant.getMass();
                currentCell.plant = null;
                this.state = "well-fed";
                this.pendingSoilBonus = 0.5;
            } else if (waterScanned) {
                double intakeRate = 0.08;
                double waterToDrink = Math.min(this.mass * intakeRate, currentCell.water.getMass());
                currentCell.water.consume(waterToDrink);
                this.mass += waterToDrink;
                this.state = "well-fed";
                this.pendingSoilBonus = 0.5;
            } else {
                this.state = "hungry";
                this.pendingSoilBonus = 0.0;
            }
        }
    }
    @JsonIgnore
    public double getPendingSoilBonus() {
        return this.pendingSoilBonus;
    }
    @JsonIgnore
    public void resetPendingSoilBonus() {
        this.pendingSoilBonus = 0.0;
    }

    enum AnimalType {
        Herbivores(85),
        Carnivores(30),
        Omnivores(60),
        Detritivores(90),
        Parasites(10);

        private final int attackPossibility;

        AnimalType(int attackPossibility) {
            this.attackPossibility = attackPossibility;
        }

        public int getAttackPossibility() {
            return this.attackPossibility;
        }
    }
}
