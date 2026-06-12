package ru.lgtu.jyggalag.service;

import ru.lgtu.jyggalag.core.LogService;
import ru.lgtu.jyggalag.core.OperationResult;
import ru.lgtu.jyggalag.model.Task;
import ru.lgtu.jyggalag.model.TaskStatus;
import ru.lgtu.jyggalag.repository.TaskRepository;

import java.util.List;
import java.util.Optional;


/**
 * Сервис управления задачами (Бизнес-логика).
 */
public class TaskService {
    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /**
     * Получить все задачи конкретного пользователя
     */
    public OperationResult<List<Task>> getAllTasks(int userId) {
        return taskRepository.loadTasksList(userId);
    }

    /**
     * Добавить новую задачу пользователю с автогенерацией ID
     */
    public OperationResult<Void> addTask(int userId, Task task) {
        OperationResult<List<Task>> tasksResult = taskRepository.loadTasksList(userId);
        if (!tasksResult.isSuccess()) {
            LogService.error("TaskService | addTask | Не удалось загрузить задачи для пользователя {}: {}",
                    String.valueOf(userId), tasksResult.getErrorMessage());
            return OperationResult.failure(tasksResult.getErrorMessage());
        }

        List<Task> tasks = tasksResult.getData();

        int nextId = tasks.stream()
                .mapToInt(Task::getId)
                .max()
                .orElse(0) + 1;
        task.setId(nextId);

        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.TODO);
        }

        tasks.add(task);
        OperationResult<Void> saveResult = taskRepository.saveTasksList(userId, tasks);
        if (!saveResult.isSuccess()) {
            LogService.error("TaskService | addTask | Ошибка сохранения задач для пользователя {}: {}",
                    String.valueOf(userId), saveResult.getErrorMessage());
            return OperationResult.failure(saveResult.getErrorMessage());
        }

        LogService.userAction("ID_" + userId, "Создал задачу: " + task.getTitle());
        return OperationResult.success(null);
    }

    /**
     * Отредактировать существующую задачу
     */
    public OperationResult<Void> editTask(int userId, int taskId, Task updatedTask) {
        OperationResult<List<Task>> tasksResult = taskRepository.loadTasksList(userId);
        if (!tasksResult.isSuccess()) {
            LogService.error("TaskService | editTask | Не удалось загрузить задачи для пользователя {}: {}",
                    String.valueOf(userId), tasksResult.getErrorMessage());
            return OperationResult.failure(tasksResult.getErrorMessage());
        }

        List<Task> tasks = tasksResult.getData();

        Optional<Task> taskOpt = tasks.stream().filter(t -> t.getId() == taskId).findFirst();
        if (taskOpt.isEmpty()) {
            return OperationResult.failure("Задача с ID " + taskId + " не найдена для редактирования.");
        }

        Task task = taskOpt.get();
        Task oldTask = task;

        if (updatedTask.getTitle() != null) {
            task.setTitle(updatedTask.getTitle());
        }
        if (updatedTask.getDescription() != null) {
            task.setDescription(updatedTask.getDescription());
        }

        if (updatedTask.getTags() != null) {
            task.setTags(updatedTask.getTags());
        }

        if (updatedTask.getPriority() != null) {
            task.setPriority(updatedTask.getPriority());
        }
        if (updatedTask.getStatus() != null) {
            task.setStatus(updatedTask.getStatus());
        }
        if (updatedTask.getUpdatedAt() != null) {
            task.setUpdatedAt(updatedTask.getUpdatedAt());
        }

        if (updatedTask.getColor() != null) {
            task.setColor(updatedTask.getColor());
        }

        OperationResult<Void> saveResult = taskRepository.saveTasksList(userId, tasks);
        if (!saveResult.isSuccess()) {
            LogService.error("TaskService | editTask | Ошибка сохранения задач для пользователя {}: {}",
                    String.valueOf(userId), saveResult.getErrorMessage());
            return OperationResult.failure(saveResult.getErrorMessage());
        }

        LogService.userAction("ID_" + userId, "Отредактировал задачу: " + oldTask + " -> " + task);
        return OperationResult.success(null);
    }

    /**
     * Переключить статус задачи
     */
    public OperationResult<Void> toggleTaskStatus(int userId, int taskId, boolean isDone) {
        OperationResult<List<Task>> tasksResult = taskRepository.loadTasksList(userId);
        if (!tasksResult.isSuccess()) {
            return OperationResult.failure(tasksResult.getErrorMessage());
        }

        List<Task> tasks = tasksResult.getData();

        Optional<Task> taskOpt = tasks.stream().filter(t -> t.getId() == taskId).findFirst();
        if (taskOpt.isEmpty()) {
            return OperationResult.failure("Задача с ID " + taskId + " не найдена.");
        }

        Task task = taskOpt.get();
        TaskStatus oldStatus = task.getStatus();
        TaskStatus newStatus = isDone ? TaskStatus.DONE : TaskStatus.TODO;

        task.setStatus(newStatus);

        OperationResult<Void> saveResult = taskRepository.saveTasksList(userId, tasks);
        if (!saveResult.isSuccess()) {
            return OperationResult.failure(saveResult.getErrorMessage());
        }

        LogService.userAction("ID_" + userId,
                String.format("Изменил статус задачи '%s' с %s на %s", task.getTitle(), oldStatus, newStatus));
        return OperationResult.success(null);
    }

    /**
     * Удалить задачу пользователя
     */
    public OperationResult<Void> deleteTask(int userId, int taskId) {
        OperationResult<List<Task>> tasksResult = taskRepository.loadTasksList(userId);
        if (!tasksResult.isSuccess()) {
            return OperationResult.failure(tasksResult.getErrorMessage());
        }

        List<Task> tasks = tasksResult.getData();

        boolean removed = tasks.removeIf(t -> t.getId() == taskId);
        if (!removed) {
            return OperationResult.failure("Задача с ID " + taskId + " не найдена для удаления.");
        }

        OperationResult<Void> saveResult = taskRepository.saveTasksList(userId, tasks);
        if (!saveResult.isSuccess()) {
            return OperationResult.failure(saveResult.getErrorMessage());
        }

        LogService.userAction("ID_" + userId, "Удалил задачу с ID " + taskId);
        return OperationResult.success(null);
    }
}