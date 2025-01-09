package pl.agh.old;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

// Różne implementacje, rodzaje, tutaj może strategię zrobimy ???

public class FileSaver {
    public static void saveToFile(String filename, String content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true))) {
            writer.write(content);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
