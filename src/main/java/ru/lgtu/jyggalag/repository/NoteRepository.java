package ru.lgtu.jyggalag.repository;

import ru.lgtu.jyggalag.core.OperationResult;
import ru.lgtu.jyggalag.model.Note;

import java.io.File;
import java.util.List;

public class NoteRepository {
    private final FileRepository fileService;
    private final String baseNotesDir;

    public NoteRepository(FileRepository fileService, String baseNotesDir) {
        this.fileService = fileService;
        this.baseNotesDir = baseNotesDir;
    }

    /**
     * Вспомогательный метод для генерации динамического пути: data/storage/{user_id}/notes.json
     */
    private String resolveNotesFilePath(int userId) {
        return baseNotesDir + File.separator + userId + File.separator + "notes.json";
    }

    /**
     * Загрузить все заметки конкретного пользователя
     */
    public OperationResult<List<Note>> loadNotesList(int userId) {
        String path = resolveNotesFilePath(userId);
        return fileService.loadJsonList(path, Note[].class);
    }

    /**
     * Сохранить (перезаписать) весь список заметок конкретного пользователя
     */
    public OperationResult<Void> saveNotesList(int userId, List<Note> notesList) {
        String path = resolveNotesFilePath(userId);
        return fileService.saveJsonList(path, notesList);
    }

    /**
     * Найти конкретную заметку по ID у определенного пользователя
     */
    public OperationResult<Note> findById(int userId, int noteId) {
        OperationResult<List<Note>> result = loadNotesList(userId);
        if (!result.isSuccess()) return OperationResult.failure(result.getErrorMessage());

        return result.getData().stream()
                .filter(n -> n.getId() == noteId)
                .findFirst()
                .map(OperationResult::success)
                .orElseGet(() -> OperationResult.failure("Заметка с ID " + noteId + " не найдена для пользователя " + userId));
    }

    /**
     * Проверить, существует ли заметка у пользователя
     */
    public boolean isNoteExist(int userId, int noteId) {
        return findById(userId, noteId).isSuccess();
    }
}
