package main;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.CommandInput;
import fileio.TerritorySectionParamsInput;

class PlanetMap {
    Cell[][] planetMaps;
    int rows, cols;

    public PlanetMap(int rows, int cols, TerritorySectionParamsInput territorySectionParams) {
        this.rows = rows;
        this.cols = cols;
        this.planetMaps = new Cell[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                this.planetMaps[i][j] = new Cell(null, null, null, null, null);
            }
        }
        if (territorySectionParams.getSoil() != null) {
            for (var input : territorySectionParams.getSoil()) {
                String name = input.getName();
                String type = input.getType();
                double mass = input.getMass();
                double nitrogen = input.getNitrogen();
                double waterRetention = input.getWaterRetention();
                double soilpH = input.getSoilpH();
                double organicMatter = input.getOrganicMatter();
                for (var coords : input.getSections()) {
                    int x = coords.getX();
                    int y = coords.getY();

                    switch (type) {
                        case "ForestSoil":
                            double leafLitter = input.getLeafLitter();
                            this.planetMaps[x][y].soil = new Soil.ForestSoil(name, type, mass, nitrogen, waterRetention, soilpH, organicMatter, leafLitter);
                            break;
                        case "SwampSoil":
                            double waterLogging = input.getWaterLogging();
                            this.planetMaps[x][y].soil = new Soil.SwampSoil(name, type, mass, nitrogen, waterRetention, soilpH, organicMatter, waterLogging);
                            break;
                        case "DesertSoil":
                            double salinity = input.getSalinity();
                            this.planetMaps[x][y].soil = new Soil.DesertSoil(name, type, mass, nitrogen, waterRetention, soilpH, organicMatter, salinity);
                            break;
                        case "GrasslandSoil":
                            double rootDensity = input.getRootDensity();
                            this.planetMaps[x][y].soil = new Soil.GrasslandSoil(name, type, mass, nitrogen, waterRetention, soilpH, organicMatter, rootDensity);
                            break;
                        case "TundraSoil":
                            double permafrostDepth = input.getPermafrostDepth();
                            this.planetMaps[x][y].soil = new Soil.TundraSoil(name, type, mass, nitrogen, waterRetention, soilpH, organicMatter, permafrostDepth);
                            break;
                    }
                }
            }
        }

        if (territorySectionParams.getAnimals() != null) {
            for (var input : territorySectionParams.getAnimals()) {
                String name = input.getName();
                String type = input.getType();
                double mass = input.getMass();
                for (var coords : input.getSections()) {
                    int x = coords.getX();
                    int y = coords.getY();
                    this.planetMaps[x][y].animal = new Animal(name, type, mass);
                }
            }
        }
        if (territorySectionParams.getWater() != null) {
            for (var input : territorySectionParams.getWater()) {
                String name = input.getName();
                String type = input.getType();
                double mass = input.getMass();
                double purity = input.getPurity();
                double salinity = input.getSalinity();
                double turbidity = input.getTurbidity();
                double contaminantIndex = input.getContaminantIndex();
                double pH = input.getPH();
                boolean isFrozen = input.isFrozen();
                for (var coords : input.getSections()) {
                    int x = coords.getX();
                    int y = coords.getY();
                    this.planetMaps[x][y].water = new Water(name, type, mass, purity, salinity, turbidity, contaminantIndex, pH, isFrozen);
                }
            }
        }
        if (territorySectionParams.getPlants() != null) {
            for (var input : territorySectionParams.getPlants()) {
                String name = input.getName();
                String type = input.getType();
                double mass = input.getMass();
                for (var coords : input.getSections()) {
                    int x = coords.getX();
                    int y = coords.getY();
                    this.planetMaps[x][y].plant = new Plants(name, type, mass);
                }
            }
        }
        if (territorySectionParams.getAir() != null) {
            for (var input : territorySectionParams.getAir()) {
                String name = input.getName();
                String type = input.getType();
                double mass = input.getMass();
                double humidity = input.getHumidity();
                double temperature = input.getTemperature();
                double oxygenLevel = input.getOxygenLevel();


                for (var coords : input.getSections()) {
                    int x = coords.getX();
                    int y = coords.getY();
                    switch (type) {
                        case "TropicalAir":
                            double co2Level = input.getCo2Level();
                            this.planetMaps[x][y].air = new Air.tropical(name, type, mass, humidity, temperature, oxygenLevel, co2Level);
                            break;
                        case "PolarAir":
                            double iceCrystalConcentration = input.getIceCrystalConcentration();
                            this.planetMaps[x][y].air = new Air.polar(name, type, mass, humidity, temperature, oxygenLevel, iceCrystalConcentration);
                            break;
                        case "TemperateAir":
                            double pollenLevel = input.getPollenLevel();
                            this.planetMaps[x][y].air = new Air.temperat(name, type, mass, humidity, temperature, oxygenLevel, pollenLevel);
                            break;
                        case "DesertAir":
                            double dustParticles = input.getDustParticles();
                            this.planetMaps[x][y].air = new Air.desert(name, type, mass, humidity, temperature, oxygenLevel, dustParticles);
                            break;
                        case "MountainAir":
                            double altitude = input.getAltitude();
                            this.planetMaps[x][y].air = new Air.mountain(name, type, mass, humidity, temperature, oxygenLevel, altitude);
                            break;
                    }
                }
            }
        }
    }

    public void applyWeather(CommandInput command) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (this.planetMaps[i][j].air != null) {
                    this.planetMaps[i][j].air.changeWeatherConditions(command);
                }
            }
        }
    }


    public JsonNode printMap(ObjectMapper mapper) {
        ArrayNode jsonMap = mapper.createArrayNode();

        for (int j = 0; j < cols; j++) {
            for (int i = 0; i < rows; i++) {
                Cell cell = planetMaps[i][j];
                ObjectNode sectionData = mapper.createObjectNode();

                ArrayNode coords = mapper.createArrayNode();
                coords.add(i);
                coords.add(j);
                sectionData.set("section", coords);

                int totalObjects = 0;
                if (cell.animal != null) totalObjects++;
                if (cell.plant != null) totalObjects++;
                if (cell.water != null) totalObjects++;
                sectionData.put("totalNrOfObjects", totalObjects);

                String airQ = (cell.air != null) ? cell.air.determineQuality(cell.air.airQuality()) : "N/A";
                String soilQ = (cell.soil != null) ? cell.soil.determineQuality(cell.soil.quality()) : "N/A";

                sectionData.put("airQuality", airQ);
                sectionData.put("soilQuality", soilQ);

                jsonMap.add(sectionData);
            }
        }

        return jsonMap;
    }

    public void applyTimeInteractions(int currentTimestamp) {
        int rows = this.planetMaps.length;
        int cols = this.planetMaps[0].length;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Cell cell = this.planetMaps[i][j];

                if (cell.soil != null && cell.plant != null && cell.plant.isScanned()) {
                    cell.plant.grow(0.2); // Soil -> Plant (la fiecare iteratie)
                }

                if (cell.water != null && cell.water.isScanned()) {
                    if (cell.plant != null && cell.plant.isScanned()) {
                        cell.plant.grow(0.2);
                    }

                    if (currentTimestamp % 2 == 0) {
                        if (cell.air != null) {
                            cell.air.increaseHumidity(0.1); // Water -> Air
                        }
                        if (cell.soil != null) {
                            cell.soil.increaseWaterRetention(0.1); // Water -> Soil
                        }
                    }
                }

                if (cell.plant != null && cell.plant.isScanned()) {
                    if (cell.air != null) {
                        cell.air.addOxygen(cell.plant.oxigenGenerated());
                    }

                    if (cell.plant.isDead()) {
                        cell.plant = null;
                    }
                }

                if (cell.animal != null && cell.animal.isScanned()) {
                    if (cell.soil != null && cell.animal.getPendingSoilBonus() > 0) {
                        cell.soil.addOrganicMatter(cell.animal.getPendingSoilBonus());
                        cell.animal.resetPendingSoilBonus();
                    }

                    cell.animal.feed(this, i, j);

                    if (currentTimestamp % 2 == 0) {
                        cell.animal.animalMovement(this, i, j);
                    }
                }
            }
        }
    }
}


