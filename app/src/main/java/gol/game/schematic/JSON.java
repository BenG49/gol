package gol.game.schematic;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import org.json.simple.JSONObject;

import gol.util.Vector2Int;

public class JSON {
    public static void JSONWrite(HashSet<Vector2Int> data, String filePath) {
        JSONObject json = new JSONObject();

        int index = 0;
        for (Vector2Int i : data)
            json.put(index++, i);
        
        try {
            FileWriter file = new FileWriter(filePath);
            file.write(json.toJSONString());
            file.close();
        } catch (IOException e) {
            System.out.println("Invalid path given!");
        }
    }
}
