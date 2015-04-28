import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.*;
import static java.nio.file.StandardCopyOption.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * User: Robin Taylor
 * Date: 16/04/2015
 * Time: 14:16
 *
 * Read a CSV file and matching bistreams, and output them in a format suitable for the DSpace batch importer.
 *
 */
public class CSVReader {

    // The input CSV file, duh!
    private String inputCSV = "/Users/rtaylor3/Documents/NC-theses/NC-theses-metadata.csv";
    // The directory where the input files referenced in the CSV live.
    private String inputFilesDir = "/Volumes/sg/lib/groups/lac-store/newcollegetheses/Crops/0074000-0074999/";
    // The directory where the output formatted SAF files will live.
    private String outputDirString = "/Users/rtaylor3/Documents/NC-theses/archive-directory";



    public void createOutputDir() throws IOException {

        // Delete the old parent directory
        FileUtils.deleteDirectory(new File(outputDirString));

        // Create a new parent directory
        Path outputDir = Paths.get(outputDirString);
        Files.createDirectory(outputDir);

    }


    public void readFile() throws IOException {

        int dirNum = 0;

        Reader in = new FileReader(inputCSV);

        // Note - this line will require edited to reflect the the column headings
        Iterable<CSVRecord> records = CSVFormat.EXCEL.withHeader("TITLE", "AUTHOR", "YEAR", "FILE NAME").parse(in);
        for (CSVRecord record : records) {

            // For some crappy reason it doesn't skip the header row, so force it to do so.
            if (record.get("FILE NAME").equals("FILE NAME")) {
                continue;
            }

            String itemDirString = outputDirString + "/item_" + dirNum++;
            Path itemDir = Paths.get(itemDirString);
            Files.createDirectory(itemDir);

            writeDCXml(record, itemDirString);
            writeFileStuff(record, itemDirString);

        }

    }

    private void writeDCXml(CSVRecord record, String itemDirString) {

        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // root element
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("dublin_core");
            doc.appendChild(rootElement);

            // Set up some DC values based on the contents of the csv

            Element title = doc.createElement("dcvalue");
            rootElement.appendChild(title);
            title.setAttribute("element", "title");
            title.setAttribute("qualifier", "none");
            title.appendChild(doc.createTextNode(record.get("TITLE")));

            Element author = doc.createElement("dcvalue");
            rootElement.appendChild(author);
            author.setAttribute("element", "contributor");
            author.setAttribute("qualifier", "author");
            author.appendChild(doc.createTextNode(record.get("AUTHOR")));

            Element year = doc.createElement("dcvalue");
            rootElement.appendChild(year);
            year.setAttribute("element", "date");
            year.setAttribute("qualifier", "issued");
            year.appendChild(doc.createTextNode(record.get("YEAR")));

            // Now set up a bunch of default fields as supplied by Theo.

            Element lang = doc.createElement("dcvalue");
            rootElement.appendChild(lang);
            lang.setAttribute("element", "language");
            lang.setAttribute("qualifier", "iso");
            lang.appendChild(doc.createTextNode("en"));

            Element publisher = doc.createElement("dcvalue");
            rootElement.appendChild(publisher);
            publisher.setAttribute("element", "publisher");
            publisher.setAttribute("qualifier", "none");
            publisher.appendChild(doc.createTextNode("The University of Edinburgh"));

            Element type = doc.createElement("dcvalue");
            rootElement.appendChild(type);
            type.setAttribute("element", "type");
            type.setAttribute("qualifier", "none");
            type.appendChild(doc.createTextNode("Thesis or Dissertation"));

            Element qualificationlevel = doc.createElement("dcvalue");
            rootElement.appendChild(qualificationlevel);
            qualificationlevel.setAttribute("element", "type");
            qualificationlevel.setAttribute("qualifier", "qualificationlevel");
            qualificationlevel.appendChild(doc.createTextNode("Doctoral"));

            Element qualificationname = doc.createElement("dcvalue");
            rootElement.appendChild(qualificationname);
            qualificationname.setAttribute("element", "type");
            qualificationname.setAttribute("qualifier", "qualificationname");
            qualificationname.appendChild(doc.createTextNode("PhD Doctor of Philosophy"));

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(itemDirString + "/" + "dublin_core.xml"));

            transformer.transform(source, result);

        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (TransformerException tfe) {
            tfe.printStackTrace();
        }
    }

    private void writeFileStuff(CSVRecord record, String itemDirString) throws IOException {

        File fout = new File(itemDirString + "/" + "contents");
        FileOutputStream fos = new FileOutputStream(fout);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

        String[] parts = record.get("FILE NAME").split("\\|");

        for (String part : parts) {
            bw.write(part.trim() + ".pdf");
            bw.newLine();

            System.out.println("part is " + part);

            Path sourceFile = Paths.get(inputFilesDir + part.trim() + ".pdf");
            Path targetFile = Paths.get(itemDirString + "/" + part.trim() + ".pdf");

            Files.copy(sourceFile, targetFile, REPLACE_EXISTING);

        }

        bw.close();

    }

}
