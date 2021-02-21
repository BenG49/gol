package gol.game.schematic;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import gol.util.Vector2Int;

public class JSON {
    public static void JSONWrite(HashSet<Vector2Int> data, String filePath) {
        JSONObject json = new JSONObject();

        int index = 0;
        for (Vector2Int i : data)
            json.put(index++, i.JSONtoString());
        
        try {
            FileWriter file = new FileWriter(filePath);
            file.write(json.toJSONString());
            file.close();
        } catch (IOException e) {
            System.out.println("Invalid path given!");
        }
    }

    public static HashSet<Vector2Int> JSONRead(String filePath) {
        JSONParser parser = new JSONParser();

        try (FileReader reader = new FileReader(filePath)) {
            Object obj = parser.parse(reader);
            JSONObject jsonOut = (JSONObject) obj;
            HashSet<String> temp = new HashSet<String>(jsonOut.values());
            HashSet<Vector2Int> output = new HashSet<Vector2Int>();
            for (String i : temp)
                output.add(new Vector2Int(i));

            return output;

        } catch (FileNotFoundException e) { System.out.println("File not found"); }
          catch (IOException e) { System.out.println("IOException"); }
          catch (ParseException e) { System.out.println("ParseException: "+e.getUnexpectedObject()); }
        
        return null;
    }

    public static HashSet<Vector2Int> loadJSON() {
        JFileChooser chooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        chooser.setDialogTitle("Load");
        int r = chooser.showDialog(chooser, "Open");

        if (r == JFileChooser.APPROVE_OPTION)
            return JSONRead(chooser.getSelectedFile().getAbsolutePath());
        else {
            System.out.println("The user cancelled the task");
            return null;
        }
    }

    public static void saveJSON(HashSet<Vector2Int> data) {
        JFileChooser chooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        chooser.setDialogTitle("Save");
        int r = chooser.showOpenDialog(null);

        if (r == JFileChooser.APPROVE_OPTION) {
            String path = chooser.getSelectedFile().getAbsolutePath();
            String extension = ".json";

            // adds .json extension
            if (!path.endsWith(extension))
                path = path.split("\\.")[0]+extension;

            JSONWrite(data, path);
        } else
            System.out.println("The user cancelled the task");
    }
}
