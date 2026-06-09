package ru.lgtu.jyggalag.service;

import ru.lgtu.jyggalag.core.LogService;
import ru.lgtu.jyggalag.core.OperationResult;
import ru.lgtu.jyggalag.model.Note;
import ru.lgtu.jyggalag.repository.NoteRepository;

import java.util.List;
import java.util.Optional;

public class NoteService {
    private final NoteRepository noteRepository;

    public NoteService(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    /**
     * Получить все заметки конкретного пользователя
     */
    public OperationResult<List<Note>> getAllNotes(int userId) {
        return noteRepository.loadNotesList(userId);
    }

    /**
     * Добавить новую заметку пользователю с автогенерацией ID
     */
    public OperationResult<Void> addNote(int userId, Note note) {
        OperationResult<List<Note>> notesResult = noteRepository.loadNotesList(userId);
        if (!notesResult.isSuccess()) {
            LogService.error("NoteService | addNote | Не удалось загрузить заметки для пользователя {}: {}",
                    String.valueOf(userId), notesResult.getErrorMessage());
            return OperationResult.failure(notesResult.getErrorMessage());
        }

        List<Note> notes = notesResult.getData();

        int nextId = notes.stream()
                .mapToInt(Note::getId)
                .max()
                .orElse(0) + 1;
        note.setId(nextId);

        notes.add(note);
        OperationResult<Void> saveResult = noteRepository.saveNotesList(userId, notes);
        if (!saveResult.isSuccess()) {
            LogService.error("NoteService | addNote | Ошибка сохранения заметок для пользователя {}: {}",
                    String.valueOf(userId), saveResult.getErrorMessage());
            return OperationResult.failure(saveResult.getErrorMessage());
        }

        LogService.userAction("ID_" + userId, "Создал заметку: " + note.getTitle());
        return OperationResult.success(null);
    }

    /**
     * Отредактировать существующую заметку
     */
    public OperationResult<Void> editNote(int userId, int noteId, Note updatedNote) {
        OperationResult<List<Note>> notesResult = noteRepository.loadNotesList(userId);
        if (!notesResult.isSuccess()) {
            LogService.error("NoteService | editNote | Не удалось загрузить заметки для пользователя {}: {}",
                    String.valueOf(userId), notesResult.getErrorMessage());
            return OperationResult.failure(notesResult.getErrorMessage());
        }

        List<Note> notes = notesResult.getData();

        Optional<Note> noteOpt = notes.stream().filter(n -> n.getId() == noteId).findFirst();
        if (noteOpt.isEmpty()) {
            return OperationResult.failure("Заметка с ID " + noteId + " не найдена для редактирования.");
        }

        Note note = noteOpt.get();
        String oldTitle = note.getTitle();

        if (updatedNote.getTitle() != null) {
            note.setTitle(updatedNote.getTitle());
        }
        if (updatedNote.getContent() != null) {
            note.setContent(updatedNote.getContent());
        }
        if (updatedNote.getUpdatedAt() != null) {
            note.setUpdatedAt(updatedNote.getUpdatedAt());
        }

        OperationResult<Void> saveResult = noteRepository.saveNotesList(userId, notes);
        if (!saveResult.isSuccess()) {
            LogService.error("NoteService | editNote | Ошибка сохранения заметок для пользователя {}: {}",
                    String.valueOf(userId), saveResult.getErrorMessage());
            return OperationResult.failure(saveResult.getErrorMessage());
        }

        LogService.userAction("ID_" + userId, "Отредактировал заметку: " + oldTitle + " -> " + note.getTitle());
        return OperationResult.success(null);
    }

    /**
     * Удалить заметку пользователя
     */
    public OperationResult<Void> deleteNote(int userId, int noteId) {
        OperationResult<List<Note>> notesResult = noteRepository.loadNotesList(userId);
        if (!notesResult.isSuccess()) {
            return OperationResult.failure(notesResult.getErrorMessage());
        }

        List<Note> notes = notesResult.getData();

        boolean removed = notes.removeIf(n -> n.getId() == noteId);
        if (!removed) {
            return OperationResult.failure("Заметка с ID " + noteId + " не найдена для удаления.");
        }

        OperationResult<Void> saveResult = noteRepository.saveNotesList(userId, notes);
        if (!saveResult.isSuccess()) {
            return OperationResult.failure(saveResult.getErrorMessage());
        }

        LogService.userAction("ID_" + userId, "Удалил заметку с ID " + noteId);
        return OperationResult.success(null);
    }
}
