package main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.CommandInput;
import fileio.InputLoader;
import fileio.SimulationInput;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class Time {
    private int standByRobot = 0;
    private boolean isSimulationStarted = false;
    private final InputLoader loader;
    private final ArrayList<CommandInput> inputsList;
    private final ArrayList<SimulationInput> simulationInputsList;
    private final ArrayNode outputFinal;
    private final ObjectMapper mapper = new ObjectMapper();
    private final LinkedHashMap<String, ArrayList<String>> knowledgeBase = new LinkedHashMap<>();

    public Time(String wayToFile, ArrayNode output) throws IOException {
        loader = new InputLoader(wayToFile);
        simulationInputsList = loader.getSimulations();
        inputsList = loader.getCommands();
        TerraBot robot = new TerraBot(simulationInputsList.getFirst().getEnergyPoints());
        PlanetMap theMap = null;
        this.outputFinal = output;

        int lastTimestamp = -1;

        for (CommandInput contor : inputsList) {
            int currentTimestamp = contor.getTimestamp();
            if (theMap != null && lastTimestamp != -1 && isSimulationStarted) {
                for (int t = lastTimestamp + 1; t <= currentTimestamp; t++) {
                    theMap.applyTimeInteractions(t);
                }
            }
            lastTimestamp = currentTimestamp;
            String actiune = contor.getCommand();
            ObjectNode commandOutput = mapper.createObjectNode();
            commandOutput.put("command", actiune);
            commandOutput.put("timestamp", currentTimestamp);

            if (!isSimulationStarted && !actiune.equals("startSimulation")) {
                commandOutput.put("message", "ERROR: Simulation not started. Cannot perform action");
                this.outputFinal.add(commandOutput);
                continue;
            }

            boolean isCharging = currentTimestamp < standByRobot;
            if (isCharging && (actiune.equals("moveRobot") || actiune.equals("scanObject") || actiune.equals("learnFact") || actiune.equals("improveEnvironment"))) {
                commandOutput.put("message", "ERROR: Robot still charging. Cannot perform action");
                this.outputFinal.add(commandOutput);
                continue;
            }
            switch (actiune) {
                case "startSimulation":
                    if (isSimulationStarted) {
                        commandOutput.put("message", "ERROR: Simulation already started. Cannot perform action");
                    } else {
                        isSimulationStarted = true;
                        String coords = simulationInputsList.getFirst().getTerritoryDim();
                        String[] bucati = coords.split("x");
                        int rows = Integer.parseInt(bucati[0]);
                        int cols = Integer.parseInt(bucati[1]);
                        theMap = new PlanetMap(rows, cols, simulationInputsList.getFirst().getTerritorySectionParams());
                        commandOutput.put("message", "Simulation has started.");
                    }
                    break;
                case "endSimulation":
                    isSimulationStarted = false;
                    theMap = null;
                    knowledgeBase.clear();
                    commandOutput.put("message", "Simulation has ended.");
                    break;
                case "printEnvConditions":
                    if (theMap == null) {
                        commandOutput.put("message", "ERROR: Simulation not started. Cannot perform action");
                    } else {
                        Cell currentCell = theMap.planetMaps[robot.getX()][robot.getY()];
                        ObjectNode currentEvent = mapper.createObjectNode();

                        if (currentCell.plant != null)
                            currentEvent.set("plants", mapper.valueToTree(currentCell.plant));
                        if (currentCell.animal != null)
                            currentEvent.set("animals", mapper.valueToTree(currentCell.animal));
                        if (currentCell.water != null)
                            currentEvent.set("water", mapper.valueToTree(currentCell.water));
                        if (currentCell.soil != null) {
                            currentEvent.set("soil", mapper.valueToTree(currentCell.soil));
                        }
                        if (currentCell.air != null) {
                            currentEvent.set("air", mapper.valueToTree(currentCell.air));
                        }
                        commandOutput.set("output", currentEvent);
                    }
                    break;
                case "printMap":
                    if (theMap != null) {
                        commandOutput.set("output", theMap.printMap(mapper));
                    }
                    break;
                case "scanObject":
                    if (robot.energyStatus() < 7) {
                        commandOutput.put("message", "ERROR: Not enough battery left. Cannot perform action");
                    } else {
                        String detectedEntity = robot.scanner(contor.getColor(), contor.getSound(), contor.getSmell(), theMap);
                        if (detectedEntity.equals("animal")) {
                            commandOutput.put("message", "The scanned object is an animal.");
                        } else if (detectedEntity.equals("plant")) {
                            commandOutput.put("message", "The scanned object is a plant.");
                        } else if (detectedEntity.equals("water")) {
                            commandOutput.put("message", "The scanned object is water.");
                        } else {
                            commandOutput.put("message", "ERROR: Object not found. Cannot perform action");
                        }
                    }
                    break;
                case "changeWeatherConditions":
                    theMap.applyWeather(contor);
                    commandOutput.put("message", "The weather has changed.");
                    break;
                case "getEnergyStatus":
                    if (contor.getTimestamp() >= standByRobot) {
                        commandOutput.put("message", "TerraBot has " + robot.energyStatus() + " energy points left.");
                        //SA VERIFICI IN PDF EXACT CUM TREBUIE AFISAT OUTPUT UL!! MAI FACI .PUT()..
                    }
                    break;
                case "moveRobot":
                    if (robot.energyStatus() <= 0) {
                        commandOutput.put("message", "ERROR: Not enough battery left. Cannot perform action");
                    } else {
                        boolean successfullMovement = robot.move(theMap);
                        if (successfullMovement) {
                            commandOutput.put("message", "The robot has successfully moved to position (" + robot.getX() + ", " + robot.getY() + ").");
                        } else {
                            commandOutput.put("message", "ERROR: Not enough battery left. Cannot perform action");
                        }

                    }
                    break;
                case "learnFact":
                    String subject = contor.getSubject();
                    if (!robot.scannedEntities.contains(subject)) {
                        commandOutput.put("message", "ERROR: Subject not yet saved. Cannot perform action");
                    } else if (robot.energyStatus() < 2) {
                        commandOutput.put("message", "ERROR: Not enough battery left. Cannot perform action");
                    } else {
                        robot.learnFact();
                        String entityComponent = contor.getComponents();
                        String subjectComponent = contor.getSubject();
                        knowledgeBase.putIfAbsent(entityComponent, new ArrayList<>());
                        knowledgeBase.get(entityComponent).add(subjectComponent);
                        commandOutput.put("message", "The fact has been successfully saved in the database.");
                    }
                    break;
                case "improveEnvironment":
                    if (robot.energyStatus() < 10) {
                        commandOutput.put("message", "ERROR: Not enough battery left. Cannot perform action");
                    } else {
                        robot.battery -= 10;
                        Cell currentCell = theMap.planetMaps[robot.getX()][robot.getY()];
                        String improvementType = contor.getImprovementType();
                        String componentName = contor.getName();

                        if (improvementType != null) {
                            if (improvementType.equals("plantVegetation")) {
                                if (currentCell.air != null) {
                                    currentCell.air.addOxygen(0.3);
                                    currentCell.air.updateAirStatus();
                                }
                                commandOutput.put("message", "The " + componentName + " was planted successfully.");
                            } else if (improvementType.equals("fertilizeSoil")) {
                                if (currentCell.soil != null) {
                                    currentCell.soil.addOrganicMatter(0.3);
                                    currentCell.soil.quality();
                                    commandOutput.put("message", "The " + currentCell.soil.type + " was successfully fertilized using " + componentName);
                                }
                            } else if (improvementType.equals("increaseHumidity")) {
                                if (currentCell.air != null) {
                                    currentCell.air.increaseHumidity(0.2);
                                    currentCell.air.updateAirStatus();
                                    commandOutput.put("message", "The " + currentCell.air.type + " was successfully increased using " + componentName);
                                }
                            } else if (improvementType.equals("increaseMoisture")) {
                                if (currentCell.soil != null) {
                                    currentCell.soil.increaseWaterRetention(0.2);
                                    currentCell.soil.quality();
                                    commandOutput.put("message", "The " + currentCell.soil.type + " was successfully increased using " + componentName);
                                }
                            } else {
                                commandOutput.put("message", "The environment was improved successfully.");
                            }
                        }
                    }
                    break;
                case "rechargeBattery":
                    robot.rechargeBattery(contor.getTimeToCharge());
                    this.standByRobot = contor.getTimeToCharge() + contor.getTimestamp();
                    commandOutput.put("message", "Robot battery is charging.");
                    break;
                case "printKnowledgeBase":
                    if (!isSimulationStarted) {
                        commandOutput.put("message", "ERROR: Simulation not started. Cannot perform action");
                    } else {
                        ArrayNode currentKnowledgeBase = mapper.createArrayNode();
                        for (var entry : knowledgeBase.entrySet()) {
                            ObjectNode topicNode = mapper.createObjectNode();
                            topicNode.put("topic", entry.getKey());

                            ArrayNode factsArray = mapper.createArrayNode();
                            for (String fact : entry.getValue()) {
                                factsArray.add(fact);
                            }
                            topicNode.set("facts", factsArray);
                            currentKnowledgeBase.add(topicNode);
                        }
                        commandOutput.set("output", currentKnowledgeBase);
                    }
                    break;
            }
            this.outputFinal.add(commandOutput);
        }
    }
}
