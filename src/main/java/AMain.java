import java.io.IOException;

/**
 * User: Robin Taylor
 * Date: 16/04/2015
 * Time: 14:22
 */
public class AMain {

    public static void main(String[] args) throws IOException {

        CSVReader csvReader = new CSVReader();

        // Create an empty output directory.
        csvReader.createOutputDir();

        // Now process the CSV.
        csvReader.readFile();

        System.out.println("Finished!");
    }

}
