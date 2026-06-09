package ru.lgtu.jyggalag.repository;

import ru.lgtu.jyggalag.core.OperationResult;
import ru.lgtu.jyggalag.model.Task;

import java.io.File;
import java.util.List;

public class TaskRepository {
    private final FileRepository fileService;
    private final String baseTasksDir;

    public TaskRepository(FileRepository fileService, String baseTasksDir) {
        this.fileService = fileService;
        this.baseTasksDir = baseTasksDir;
    }

    /**
     * Вспомогательный метод для генерации динамического пути: data/storage/tasks/{user_id}/tasks.json
     */
    private String resolveTasksFilePath(int userId) {
        return baseTasksDir + File.separator + userId + File.separator + "tasks.json";
    }

    /**
     * Загрузить все задачи конкретного пользователя
     */
    public OperationResult<List<Task>> loadTasksList(int userId) {
        String path = resolveTasksFilePath(userId);
        return fileService.loadJsonList(path, Task[].class);
    }

    /**
     * Сохранить (перезаписать) весь список задач конкретного пользователя
     */
    public OperationResult<Void> saveTasksList(int userId, List<Task> tasksList) {
        String path = resolveTasksFilePath(userId);
        return fileService.saveJsonList(path, tasksList);
    }

    /**
     * Найти конкретную задачу по ID у определенного пользователя
     */
    public OperationResult<Task> findById(int userId, int taskId) {
        OperationResult<List<Task>> result = loadTasksList(userId);
        if (!result.isSuccess()) return OperationResult.failure(result.getErrorMessage());

        return result.getData().stream()
                .filter(t -> t.getId() == taskId)
                .findFirst()
                .map(OperationResult::success)
                .orElseGet(() -> OperationResult.failure("Задача с ID " + taskId + " не найдена для пользователя " + userId));
    }

    /**
     * Проверить, существует ли задача у пользователя
     */
    public boolean isTaskExist(int userId, int taskId) {
        return findById(userId, taskId).isSuccess();
    }
}