package pl.agh.kernel;

import lombok.AllArgsConstructor;
import pl.agh.model.dto.request.NewTaskRequest;

import java.util.UUID;

@AllArgsConstructor
public class TaskController {
    private TaskService taskService;

    //TODO (09.01.2025): Można byłoby dod
//    Dodać tutaj ogarnianie aktualizacji przychodzącej z zewnątrz,
//    Dodanie wysyłania powiadomień o skończonej robocie.
//    Dodanie sytuacji, kiedy wszystkie są zajęte

    //TODO (10.01.2025): Zrobić, aby to było asynchronicznie wołane
    public UUID createNewTask(NewTaskRequest newTaskRequest) {
        UUID taksId = UUID.randomUUID();
        taskService.createTask(taksId, newTaskRequest);
        return taksId;
    }


}
