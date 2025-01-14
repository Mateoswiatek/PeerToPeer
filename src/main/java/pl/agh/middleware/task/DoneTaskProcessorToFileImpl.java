package pl.agh.middleware.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import pl.agh.task.model.Task;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class DoneTaskProcessorToFileImpl implements DoneTaskProcessor {

    private static String DEFAULT_FILE_NAME = "result.json";
    private static DoneTaskProcessorToFileImpl instance;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String fileName;

    private DoneTaskProcessorToFileImpl(String fileName) {
        this.fileName = fileName;
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    /**
     * With default name
     * @return
     */
    public static synchronized DoneTaskProcessorToFileImpl getInstance() {
        return getInstance(DEFAULT_FILE_NAME);
    }


    public static synchronized DoneTaskProcessorToFileImpl getInstance(String fileName) {
        if (instance == null) {
            instance = new DoneTaskProcessorToFileImpl(fileName);
        }
        return instance;
    }

    @Override
    public void processDoneTask(Task task) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {

            writer.write(objectMapper.writeValueAsString(task));
            writer.newLine();
        } catch (IOException e) {
            throw new RuntimeException("Błąd podczas zapisywania do pliku: " + fileName, e);
        }
    }
}
