package main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.CommandInput;
import fileio.InputLoader;
import fileio.SimulationInput;
import java.io.IOException;
import java.util.ArrayList;

public class Time {
    int standByRobot = 0;
    boolean isSimulationStarted = false;
    ArrayList<String> learnedFacts = new ArrayList<>();
    InputLoader loader;
    ArrayList<CommandInput> inputsList;
    ArrayList<SimulationInput>  simulationInputsList;
    ArrayNode outputFinal;
    ObjectMapper mapper = new ObjectMapper();

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

            // Sincronizarea timpului (Game Loop)
            if (theMap != null && lastTimestamp != -1 && isSimulationStarted) {
                for (int t = lastTimestamp + 1; t <= currentTimestamp; t++) {
                    theMap.applyTimeInteractions(t);
                }
            }
            lastTimestamp = currentTimestamp;

            String actiune = contor.getCommand();

            // 2. Formatul JSON obligatoriu pentru fiecare comanda (Command + Timestamp)
            ObjectNode commandOutput = mapper.createObjectNode();
            commandOutput.put("command", actiune);
            commandOutput.put("timestamp", currentTimestamp);

            // 3. EROARE: Simulare nepornita
            if (!isSimulationStarted && !actiune.equals("startSimulation")) {
                commandOutput.put("output", "ERROR: Simulation not started. Cannot perform action");
                this.outputFinal.add(commandOutput);
                continue; // Sare la urmatoarea comanda
            }

            // 4. EROARE: Robot la incarcat (Doar pt comenzile robotului)
            boolean isCharging = currentTimestamp < standByRobot;
            if (isCharging && (actiune.equals("moveRobot") || actiune.equals("scanObject") ||
                    actiune.equals("learnFact") || actiune.equals("improveEnvironment"))) {
                commandOutput.put("output", "ERROR: Robot still charging. Cannot perform action");
                this.outputFinal.add(commandOutput);
                continue;
            }
            switch (actiune) {
                case "startSimulation":
                    if (isSimulationStarted) {
                        commandOutput.put("output", "ERROR: Simulation already started. Cannot perform action");
                    } else {
                        isSimulationStarted = true;
                        String coords = simulationInputsList.getFirst().getTerritoryDim();
                        String[] bucati = coords.split("x");
                        int rows = Integer.parseInt(bucati[0]);
                        int cols = Integer.parseInt(bucati[1]);
                        theMap = new PlanetMap(rows, cols, simulationInputsList.getFirst().getTerritorySectionParams());
                        commandOutput.put("output", "Simulation has started.");
                    }
                break;
            case "endSimulation":
                    isSimulationStarted = false;
                    theMap = null;
                    learnedFacts.clear();
                    commandOutput.put("output", "Simulation has ended.");
                    break;
            case "printEnvConditions":
                ObjectNode currentEvent = mapper.createObjectNode();
                commandOutput.put("output", "Environment conditions printed.");
                Environment.printConditions();
                break;
            case "printMap":
                if(theMap!=null) {
                    ObjectNode currentMap = mapper.createObjectNode();
                    commandOutput.set("output", theMap.printMap(mapper));
                }
                break;
            case "scanObject":
                if (robot.energyStatus() < 7) {
                    commandOutput.put("output", "ERROR: Not enough energy to perform action");
                } else {
                    robot.scanner(contor.getColor(), contor.getSound(), contor.getSmell(), theMap);
                    commandOutput.put("output", "The scanned object was processed.");
                }
                break;
            case "changeWeatherConditions":
                theMap.applyWeather(contor);
                commandOutput.put("output", "The weather has changed.");
                break;
            case "getEnergyStatus":
                ObjectNode currentEnergy = mapper.createObjectNode();
                if(contor.getTimestamp()>=standByRobot) {
                    commandOutput.put("output", "TerraBot has " + robot.energyStatus() + " energy points left.");
                    this.outputFinal.add(currentEnergy);
                    //SA VERIFICI IN PDF EXACT CUM TREBUIE AFISAT OUTPUT UL!! MAI FACI .PUT()..
                }
                break;
            case "moveRobot":
                if (robot.energyStatus() <= 0) {
                    commandOutput.put("output", "ERROR: Not enough battery left. Cannot perform action");
                } else {
                    robot.moveRobot(theMap);
                    commandOutput.put("output", "The robot has successfully moved to position (" + robot.getX() + ", " + robot.getY() + ").");
                }
                break;
            case "learnFact":
                if (robot.energyStatus() < 2) {
                    commandOutput.put("output", "ERROR: Not enough battery left. Cannot perform action");
                } else {
                    robot.learnFact();
                    learnedFacts.add(contor.getSubject());
                    commandOutput.put("output", "The fact has been successfully saved in the database.");
                }
                break;
            case "improveEnvironment":
                if (robot.energyStatus() < 10) {
                    commandOutput.put("output", "ERROR: Not enough battery left. Cannot perform action");
                } else {
                    Environment.improveEnvironment(contor.getImprovementType(), contor.getType(), contor.getName());
                    commandOutput.put("output", "The environment was improved successfully.");
                }
                break;
            case "rechargeBattery":
                robot.rechargeBattery(contor.getTimeToCharge());
                this.standByRobot = contor.getTimeToCharge() + contor.getTimestamp();
                commandOutput.put("output", "Robot battery is charging.");
                break;
                case "printKnowledgeBase":
                    ArrayNode currentKnowledgeBase = mapper.createArrayNode();
                    for(String fact : learnedFacts)
                        currentKnowledgeBase.add(fact);
                    commandOutput.set("output", currentKnowledgeBase);
                    break;
        }
            this.outputFinal.add(commandOutput);
    }
    }
}
