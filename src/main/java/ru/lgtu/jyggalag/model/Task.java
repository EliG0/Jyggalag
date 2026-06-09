package ru.lgtu.jyggalag.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * Модель задачи.
 */
public class Task {
    private int id; // Уникальный ID задачи
    private String title = ""; // Краткое название задачи
    private String description = ""; // Полное описание задачи
    private List<String> tags = new ArrayList<>(); // Теги задачи
    private TaskPriority priority = TaskPriority.NULL; // Приоритет задачи
    private TaskStatus status = TaskStatus.NULL; // Текущее состояние задачи
    private LocalDateTime createdAt = LocalDateTime.now(); // Дата и время создания задачи
    private LocalDateTime updatedAt = LocalDateTime.now(); // Дата и время обновления задачи
    private LocalDateTime deadline; // Дедлайн
    private LocalDateTime completedAt; // Фактическая дата и время выполнения (null, если не выполнена)
    private String color = "#ABCDEF"; // Цвет задачи

    public Task() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title == null ? "" : title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? "" : description;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags == null ? new ArrayList<>() : new ArrayList<>(tags);
    }

    public void addTag(String tag) {
        if (tag != null && !tag.isEmpty()) this.tags.add(tag);
    }

    public void removeTag(String tag) {
        this.tags.remove(tag);
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority == null ? TaskPriority.NULL : priority;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status == null ? TaskStatus.NULL : status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color == null ? "#ABCDEF" : color;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Task{id=%d, title='%s', status=%s, priority=%s}".formatted(id, title, status, priority);
    }
}
