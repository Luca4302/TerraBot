package main;

import lombok.Getter;

import java.util.ArrayList;

class TerraBot implements TerraBotActions {
    Integer accidentTerraBotChance;
    int battery;
    @Getter
    int x;
    @Getter
    int y;
    ArrayList<String> scannedEntities = new ArrayList<>();

    public TerraBot(int energieStart) {
        this.battery = energieStart;
    }

    public void learnFact() {
        this.battery -= 2;
    }

    public void rechargeBattery(int timeToCharge) {
        this.battery = this.battery + timeToCharge;
    }

    public int energyStatus() {
        return this.battery;
    }

    @Override
    public boolean move(PlanetMap theMap) {
        int[] dx = {0, 1, 0, -1};
        int[] dy = {1, 0, -1, 0};
        int rows = theMap.planetMaps.length;
        int cols = theMap.planetMaps[0].length;

        int bestX = -1;
        int bestY = -1;
        int minDangerScore = Integer.MAX_VALUE;
        for (int i = 0; i < 4; i++) {
            int nx = this.x + dx[i];
            int ny = this.y + dy[i];

            if (nx >= 0 && nx < rows && ny >= 0 && ny < cols) {
                Cell neighbor = theMap.planetMaps[nx][ny];
                int currentScore = getCurrentScore(neighbor);

                if (currentScore < minDangerScore) {
                    minDangerScore = currentScore;
                    bestX = nx;
                    bestY = ny;
                }
            }
        }
        if (bestX == -1 || bestY == -1 || this.battery < minDangerScore) {
            return false;
        }
        this.x = bestX;
        this.y = bestY;
        this.battery -= minDangerScore;
        return true;
    }

    private int getCurrentScore(Cell neighbor) {
        double count = 0.0;
        double soilDanger = 0.0;
        double animalDanger = 0.0;
        double plantDanger = 0.0;
        double airDanger = 0.0;
        if (neighbor.soil != null) {
            soilDanger = Math.round(neighbor.soil.terraBotStuck() * 100.0) / 100.0;
            count++;
        }
        if (neighbor.animal != null) {
            animalDanger = neighbor.animal.getAttackProbability();
            count++;
        }
        if (neighbor.plant != null) {
            plantDanger = neighbor.plant.getPlantStuckChance();
            count++;
        }
        if (neighbor.air != null) {
            airDanger = neighbor.air.getAirDamage();
            count++;
        }
        if (count == 0) return 0;
        double sum = soilDanger + airDanger + animalDanger + plantDanger;
        double mean = Math.abs(sum / count);
        return (int) Math.round(mean);
    }

    @Override
    public String scanner(String color, String sound, String smell, PlanetMap theMap) {
        return scanner(color, sound, smell, theMap, 0);
    }

    public String scanner(String color, String sound, String smell, PlanetMap theMap, int currentTimestamp) {
        this.battery -= 7;
        Cell currentCell = theMap.planetMaps[this.x][this.y];

        String entityType = "unknown";

        if (color.equals("none") && smell.equals("none") && sound.equals("none")) {
            if (currentCell.water != null) {
                currentCell.water.setScanned(currentTimestamp);
                scannedEntities.add(currentCell.water.name);
                entityType = "water";
            }
        } else if (!sound.equals("none")) {
            if (currentCell.animal != null) {
                currentCell.animal.setScanned(currentTimestamp);
                scannedEntities.add(currentCell.animal.name);
                entityType = "animal";
            }
        } else {
            if (currentCell.plant != null) {
                currentCell.plant.setScanned(currentTimestamp);
                scannedEntities.add(currentCell.plant.name);
                entityType = "plant";
            }
        }

        if (currentCell.air != null) currentCell.air.setScanned(currentTimestamp);
        if (currentCell.soil != null) currentCell.soil.setScanned(currentTimestamp);
        return entityType;
    }
}