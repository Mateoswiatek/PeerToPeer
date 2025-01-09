package pl.agh.model.dto.request;

public record NewTaskRequest(String passwordHash,
                             String alphabet,
                             Long maxLength) {
}
