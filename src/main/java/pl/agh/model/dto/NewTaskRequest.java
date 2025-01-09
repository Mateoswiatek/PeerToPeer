package pl.agh.model.dto;

public record NewTaskRequest(String passwordHash,
                             String alphabet,
                             Long maxLength) {
}
