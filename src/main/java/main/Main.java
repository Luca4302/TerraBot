package main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import fileio.InputLoader;

import java.io.File;
import java.io.IOException;

interface TerraBotActions {
    boolean move(PlanetMap theMap); // de pus terraBot.pozitie

    String scanner(String color, String sound, String smell, PlanetMap theMap); // de pus terraBot.inventory (Enum plante animale apa) si animal.EnumItem.quantity
}


interface AnimalActions {
    void animalMovement(PlanetMap theMap, int currentX, int currentY);//de pus pozitia animal.pozitie
}//mai tarziu

/**
 * The entry point to this homework. It runs the checker that tests your implementation.
 */
public final class Main {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    public static final ObjectWriter WRITER = MAPPER.writer().withDefaultPrettyPrinter();

    private Main() {
    }

    /**
     * @param inputPath  input file path
     * @param outputPath output file path
     * @throws IOException when files cannot be loaded.
     */
    public static void action(final String inputPath,
                              final String outputPath) throws IOException {

        InputLoader inputLoader = new InputLoader(inputPath);
        ArrayNode output = MAPPER.createArrayNode();

        Time simulation = new Time(inputPath, output);

        File outputFile = new File(outputPath);
        outputFile.getParentFile().mkdirs();
        WRITER.writeValue(outputFile, output);
    }
}
