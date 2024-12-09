package pl.agh;

import java.io.*;

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
