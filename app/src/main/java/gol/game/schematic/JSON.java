package gol.game.schematic;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JSON {
    public static void JSONWrite(HashSet<String> linkedData, String filePath) {
        JSONObject json = new JSONObject();

        int index = 0;
        for (String i : linkedData)
            json.put(index++, i);
        
        try {
            FileWriter file = new FileWriter(filePath);
            file.write(json.toJSONString());
            file.close();
        } catch (IOException e) {
            System.out.println("Invalid path given!");
        }
    }

    public static HashSet<String> JSONRead(String filePath) {
        JSONParser parser = new JSONParser();

        try (FileReader reader = new FileReader(filePath)) {
            Object obj = parser.parse(reader);
            JSONObject jsonOut = (JSONObject) obj;
            return new HashSet<String>(jsonOut.values());

        } catch (FileNotFoundException e) { System.out.println("File not found"); }
          catch (IOException e) { System.out.println("IOException"); }
          catch (ParseException e) { System.out.println("ParseException: "+e.getUnexpectedObject()); }
        
        return null;
    }

    public static Schematic loadSchem() {
        JFileChooser chooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        chooser.setDialogTitle("Load");
        int r = chooser.showDialog(chooser, "Open");

        if (r == JFileChooser.APPROVE_OPTION) {
            String path = chooser.getSelectedFile().getAbsolutePath();
            if (Schematic.filePathLUT.containsKey(path))
                return Schematic.filePathLUT.get(path);

            Schematic output = Schematic.parseFile(JSONRead(path));
            Schematic.filePathLUT.put(path, output);
            return output;
        }
        else {
            System.out.println("The user cancelled the task");
            return null;
        }
    }

    public static void saveSchem(Schematic schem) {
        JFileChooser chooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        chooser.setDialogTitle("Save");
        int r = chooser.showOpenDialog(null);

        if (r == JFileChooser.APPROVE_OPTION) {
            String path = chooser.getSelectedFile().getAbsolutePath();
            String extension = ".json";

            // adds .json extension
            if (!path.endsWith(extension))
                path = path.split("\\.")[0]+extension;

            JSONWrite(schem.getLinkedData(), path);
        } else
            System.out.println("The user cancelled the task");
    }
}
