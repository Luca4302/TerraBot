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
    public void moveRobot(PlanetMap theMap) {
        double stuckChance = theMap.getSoilStuckChance(x,y);
    }

    public void learnFact() {
        this.battery -=2;
    }

    public void rechargeBattery(int timeToCharge) {
        this.battery= this.battery+timeToCharge;
    }

    public int energyStatus() {
        return this.battery;
    }

    enum TerraBotInventory{
        Plants,
        Animals,
        Water
        //Actionam terrBot.collect
        //Bagam in inventar (arrayList)si eliminam din celula
    }

    ArrayList<TerraBotInventory> inventory = new ArrayList<>();

    @Override
    public void move(PlanetMap theMap) {
        int[] dx = {-1, 0, 1, 0};
        int[] dy = {0, 1, 0, -1};
        int rows = theMap.planetMaps.length;
        int cols = theMap.planetMaps[0].length;

        int bestX = this.x;
        int bestY = this.y;
        int minDangerScore = Integer.MAX_VALUE;
        for (int i = 0; i < 4; i++) {
            int nx = this.x + dx[i];
            int ny = this.y + dy[i];

            if (nx >= 0 && nx < rows && ny >= 0 && ny < cols) {
                Cell neighbor = theMap.planetMaps[nx][ny];

                // Tragem daunele din entitati (daca nu exista, dauna e 0)
                double soilDanger = (neighbor.soil != null) ? neighbor.soil.terraBotStuck() : 0.0;
                int currentScore = getCurrentScore(neighbor, soilDanger);

                // Alegem celula cu cel mai mic pericol
                if (currentScore < minDangerScore) {
                    minDangerScore = currentScore;
                    bestX = nx;
                    bestY = ny;
                }
            }
        }
        this.x = bestX;
        this.y = bestY;
        this.battery -= minDangerScore;
    }

    private static int getCurrentScore(Cell neighbor, double soilDanger) {
        double animalDanger = (neighbor.animal != null) ? neighbor.animal.getAttackProbability() : 0.0;

        double soilDanger = (neighbor.soil != null) ? neighbor.soil.terraBotStuck() : 0.0;
        double animalDanger = (neighbor.animal != null) ? neighbor.animal.getAttackProbability() : 0.0;
        double plantDanger = (neighbor.plant != null) ? neighbor.plant.getPlantStuckChance() : 0.0;
        double airDanger = (neighbor.air != null) ? neighbor.air.getAirDamage() : 0.0;

        // Formula fixa din PDF
        double sum = soilDanger + airDanger + animalDanger + plantDanger;
        double mean = Math.abs(sum / 4.0);
        int currentScore = (int) Math.round(mean);
    }

    @Override
    public void scanner(String color, String sound, String smell, PlanetMap theMap) {
        this.battery -= 7;
        Cell currentCell = theMap.planetMaps[this.x][this.y];
        if (currentCell.water != null) currentCell.water.setScanned();
        if (currentCell.plant != null) currentCell.plant.setScanned();
        if (currentCell.animal != null) currentCell.animal.setScanned();
        if (currentCell.air != null) currentCell.air.setScanned();
        if (currentCell.soil != null) currentCell.soil.setScanned();
    }

    public TerraBot(int energieStart){
        this.battery= energieStart;
    }

    public void accidentFinal(Plants.PlantType plantAccidentChance)
    {
        accidentTerraBotChance = plantAccidentChance.getterAccident();
    }
}
