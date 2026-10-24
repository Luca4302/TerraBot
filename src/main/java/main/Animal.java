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

    private Animal preyToEat = null;

    @Override
    public void animalMovement(PlanetMap theMap, int currentX, int currentY) {
        int[] dx = {0, 1, 0, -1};
        int[] dy = {1, 0, -1, 0};
        boolean isCarnivoreOrParasite = (this.animalType == AnimalType.Carnivores || this.animalType == AnimalType.Parasites);

        int rows = theMap.planetMaps.length;
        int cols = theMap.planetMaps[0].length;

        int bestX = -1, bestY = -1;
        double bestWaterQuality = -1.0;

        for (int i = 0; i < 4; i++) {
            int nx = currentX + dx[i];
            int ny = currentY + dy[i];
            if (nx >= 0 && nx < rows && ny >= 0 && ny < cols) {
                if (theMap.planetMaps[nx][ny].animal != null && !isCarnivoreOrParasite) continue;
                if (theMap.planetMaps[nx][ny].plant != null && theMap.planetMaps[nx][ny].plant.isScanned() &&
                        theMap.planetMaps[nx][ny].water != null && theMap.planetMaps[nx][ny].water.isScanned()) {
                    if (theMap.planetMaps[nx][ny].water.quality() > bestWaterQuality) {
                        bestWaterQuality = theMap.planetMaps[nx][ny].water.quality();
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
                if (theMap.planetMaps[nx][ny].animal != null && !isCarnivoreOrParasite) continue;
                if (theMap.planetMaps[nx][ny].plant != null && theMap.planetMaps[nx][ny].plant.isScanned()) {
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
                if (theMap.planetMaps[nx][ny].animal != null && !isCarnivoreOrParasite) continue;
                if (theMap.planetMaps[nx][ny].water != null && theMap.planetMaps[nx][ny].water.isScanned()) {
                    if (theMap.planetMaps[nx][ny].water.quality() > bestWaterQuality) {
                        bestWaterQuality = theMap.planetMaps[nx][ny].water.quality();
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
                if (theMap.planetMaps[nx][ny].animal != null && !isCarnivoreOrParasite) continue;
                moveToNewCell(theMap, currentX, currentY, nx, ny);
                return;
            }
        }
    }

    private void moveToNewCell(PlanetMap theMap, int oldX, int oldY, int newX, int newY) {
        Cell targetCell = theMap.planetMaps[newX][newY];
        boolean isCarnivoreOrParasite = (this.animalType == AnimalType.Carnivores || this.animalType == AnimalType.Parasites);
        if (isCarnivoreOrParasite && targetCell.animal != null && targetCell.animal != this) {
            this.mass = Math.round((this.mass + targetCell.animal.getMass()) * 100.0) / 100.0;
            this.state = "well-fed";
            this.pendingSoilBonus = 0.5;
            if (targetCell.soil != null) {
                targetCell.soil.addOrganicMatter(0.5);
                this.pendingSoilBonus = 0.0;
            }
        }
        targetCell.animal = this;
        theMap.planetMaps[oldX][oldY].animal = null;
        this.x = newX;
        this.y = newY;
    }

    public void feed(PlanetMap theMap, int currentX, int currentY) {
        var currentCell = theMap.planetMaps[currentX][currentY];

        if (currentCell.air != null && currentCell.air.airToxic()) {
            this.state = "sick";
            this.pendingSoilBonus = 0.0;
            return;
        }
        boolean isCarnivoreOrParasite = (this.animalType == AnimalType.Carnivores || this.animalType == AnimalType.Parasites);
        boolean ateSomething = false;

        if (isCarnivoreOrParasite && currentCell.animal != null && currentCell.animal != this) {
            this.mass = Math.round((this.mass + currentCell.animal.getMass()) * 100.0) / 100.0;
            currentCell.animal = this;
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
                if (currentCell.water.getMass() <= 0) {
                    currentCell.water = null;
                }
                this.mass = Math.round((this.mass + waterToDrink + currentCell.plant.getMass()) * 100.0) / 100.0;
                currentCell.plant = null;
                this.state = "well-fed";
                this.pendingSoilBonus = 0.8;
            } else if (plantScanned) {
                this.mass = Math.round((this.mass + currentCell.plant.getMass()) * 100.0) / 100.0;
                currentCell.plant = null;
                this.state = "well-fed";
                this.pendingSoilBonus = 0.5;
            } else if (waterScanned) {
                double intakeRate = 0.08;
                double waterToDrink = Math.min(this.mass * intakeRate, currentCell.water.getMass());
                currentCell.water.consume(waterToDrink);
                if (currentCell.water.getMass() <= 0) {
                    currentCell.water = null;
                }
                this.mass = Math.round((this.mass + waterToDrink) * 100.0) / 100.0;
                this.state = "well-fed";
                this.pendingSoilBonus = 0.5;
            } else {
                this.state = "hungry";
                this.pendingSoilBonus = 0.0;
            }
        }
    }

    @JsonIgnore
    public double getAttackProbability() {
        return (100.0 - this.animalType.getAttackPossibility()) / 10.0;
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
