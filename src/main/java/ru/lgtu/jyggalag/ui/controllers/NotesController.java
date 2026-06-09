package ru.lgtu.jyggalag.ui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import ru.lgtu.jyggalag.config.AppContext;
import ru.lgtu.jyggalag.core.LogService;
import ru.lgtu.jyggalag.core.OperationResult;
import ru.lgtu.jyggalag.model.Note;
import ru.lgtu.jyggalag.utils.DateUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NotesController {

    @FXML
    private Label notesCountLabel;
    @FXML
    private ListView<Note> notesList;
    @FXML
    private HBox editorToolbar;
    @FXML
    private Label noteTitle;
    @FXML
    private Label noteMetaLabel;

    @FXML
    private TextField noteTitleField;

    @FXML
    private TextArea noteBodyField;
    @FXML
    private Label wordCountLabel;
    @FXML
    private Label charCountLabel;
    @FXML
    private Button saveButton;

    @FXML
    private VBox noteEditor;

    @FXML private TextField searchField;

    private Note currentNote;

    private List<Note> allLoadedNotes = new ArrayList<>();

    @FXML
    public void initialize() {
        LogService.info("NotesController | Инициализация интерфейса заметок...");

        notesList.setCellFactory(param -> new ListCell<Note>() {
            private Parent row;
            private Label titleLabel;
            private Label dateLabel;
            private Label pinIndicator;
            private Label previewLabel;

            {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/lgtu/jyggalag/fxml/notes/note_row.fxml"));
                    row = loader.load();

                    titleLabel = (Label) row.lookup("#titleLabel");
                    dateLabel = (Label) row.lookup("#dateLabel");
                    pinIndicator = (Label) row.lookup("#pinIndicator");
                    previewLabel = (Label) row.lookup("#previewLabel");
                } catch (IOException e) {
                    LogService.error("TasksController | Не удалось загрузить кастомный task_row.fxml: ", e);
                    setText("Ошибка загрузки строки");
                    setGraphic(null);
                }
            }

            @Override
            protected void updateItem(Note note, boolean empty) {
                super.updateItem(note, empty);

                if (empty || note == null || row == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String displayTitle = note.getTitle().trim().isEmpty() ? "Без названия" : note.getTitle();
                    if (titleLabel != null) {
                        titleLabel.setText(displayTitle);
                    }

                    if (dateLabel != null) {
                        dateLabel.setText(DateUtils.formatDateTime(note.getCreatedAt()));
                    }

                    if (previewLabel != null) {
                        String content = note.getContent();
                        if (content.length() > 69) {
                            content = content.substring(0, 69) + "...";
                        }
                        previewLabel.setText(content);
                    }

                    if (pinIndicator != null) {
                        pinIndicator.setText(note.isPinned() ? "◈" : "");
                    }

                    if (note.isPinned()) {
                        getStyleClass().add("jy-notes-cell-pinned");
                    } else {
                        getStyleClass().remove("jy-notes-cell-pinned");
                    }

                    getStyleClass().add("jy-note-row");

                    setGraphic(row);
                }
            }

            @Override
            public void updateSelected(boolean selected) {
                super.updateSelected(selected);
                if (row != null) {
                    if (selected) {
                        if (!row.getStyleClass().contains("jy-note-row-active")) {
                            row.getStyleClass().add("jy-note-row-active");
                        }
                    } else {
                        row.getStyleClass().remove("jy-note-row-active");
                    }
                }
            }
        });

        refreshNotesList();
        hideEditor();
    }

    /**
     * Загружает данные из NoteService и обновляет UI-список
     */
    private void refreshNotesList() {
        int currentUserId = AppContext.getInstance().getSessionService().getCurrentUserId();
        OperationResult<List<Note>> result = AppContext.getInstance().getNoteService().getAllNotes(currentUserId);

        if (result.isSuccess() && result.getData() != null) {
            allLoadedNotes = result.getData();

            notesList.getItems().clear();
            notesList.getItems().addAll(allLoadedNotes);

            notesCountLabel.setText(String.valueOf(allLoadedNotes.size()));
        } else {
            LogService.error("NotesController | Не удалось загрузить заметки: " + result.getErrorMessage());
        }
    }

    /**
     * Срабатывает при клике по заметке в списке (onMouseClicked)
     */
    @FXML
    private void handleNoteSelect() {
        Note selectedNote = notesList.getSelectionModel().getSelectedItem();

        if (selectedNote == null) {
            return;
        }

        this.currentNote = selectedNote;

        noteTitle.setText(selectedNote.getTitle().isEmpty() ? "Без названия" : selectedNote.getTitle());
        noteMetaLabel.setText("Создано: %s / Изменено: %s".formatted(DateUtils.formatDateTime(selectedNote.getCreatedAt()), DateUtils.formatDateTime(selectedNote.getUpdatedAt())));


        noteTitleField.setText(selectedNote.getTitle());
        noteBodyField.setText(selectedNote.getContent());


        updateWordAndCharCount(selectedNote.getContent());

        editorToolbar.getChildren().forEach(node -> {
            if (node instanceof Button) {
                setVisibleNode(node, true);
            }
        });

        setVisibleNode(saveButton, false);
        viewEditor();

    }

    /**
     * Срабатывает при каждом вводе текста в TextArea (onKeyReleased)
     */
    @FXML
    private void handleBodyEdit() {
        if (currentNote == null) return;

        String currentText = noteBodyField.getText();
        updateWordAndCharCount(currentText);

        boolean textChanged = !currentText.equals(currentNote.getContent());
        saveButton.setDisable(!textChanged);
        saveButton.setVisible(textChanged);
    }

    /**
     * Срабатывает при изменении инпута заголовка (onKeyReleased)
     */
    @FXML
    private void handleTitleEdit() {
        if (currentNote == null) return;

        String currentTitle = noteTitleField.getText();
        noteTitle.setText(currentTitle.isEmpty() ? "Без названия" : currentTitle);

        boolean titleChanged = !currentTitle.equals(currentNote.getTitle());
        saveButton.setDisable(!titleChanged);
        saveButton.setVisible(titleChanged);
    }

    /**
     * Метод сохранения изменений (onAction для кнопки "Сохранить")
     */
    @FXML
    private void handleSaveNote() {
        if (currentNote == null) return;

        int currentUserId = AppContext.getInstance().getSessionService().getCurrentUserId();

        currentNote.setTitle(noteTitleField.getText());
        currentNote.setContent(noteBodyField.getText());
        currentNote.setUpdatedAt(java.time.LocalDateTime.now());

        OperationResult<Void> result = AppContext.getInstance().getNoteService().editNote(currentUserId, currentNote.getId(), currentNote);

        if (!result.isSuccess()) {
            LogService.error("NotesController | Ошибка сохранения: " + result.getErrorMessage());

        } else {
            LogService.info("NotesController | Заметка успешно сохранена.");
            saveButton.setDisable(true);
            saveButton.setVisible(false);

            refreshNotesList();
            notesList.getSelectionModel().select(currentNote);
        }
        setVisibleNode(saveButton, !result.isSuccess());
        noteMetaLabel.setText("Создано: %s / Изменено: %s".formatted(DateUtils.formatDateTime(currentNote.getCreatedAt()), DateUtils.formatDateTime(currentNote.getUpdatedAt())));
    }

    /**
     * Вспомогательный метод для подсчета слов и символов
     */
    private void updateWordAndCharCount(String text) {
        if (text == null || text.trim().isEmpty()) {
            wordCountLabel.setText("0 слов");
            charCountLabel.setText("0 символов");
            return;
        }

        int charCount = text.length();
        String[] words = text.trim().split("\\s+");
        int wordCount = words.length;

        wordCountLabel.setText(wordCount + " " + getWordDeclension(wordCount));
        charCountLabel.setText(charCount + " " + getCharDeclension(charCount));
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().toLowerCase().trim();

        List<Note> filtered = allLoadedNotes.stream()
                .filter(note -> note.getTitle().toLowerCase().contains(query) ||
                        note.getContent().toLowerCase().contains(query))
                .toList();

        notesList.getItems().clear();
        notesList.getItems().addAll(filtered);
        notesCountLabel.setText(String.valueOf(allLoadedNotes.size()));
    }

    @FXML
    private void handleNewNote() {
        Note note = new Note();
        note.setTitle("Новая заметка");
        note.setCreatedAt(LocalDateTime.now());


        AppContext.getInstance().getNoteService().addNote(AppContext.getInstance().getSessionService().getCurrentUserId(), note);
        refreshNotesList();
        notesList.getSelectionModel().selectLast();
        handleNoteSelect();
    }

    @FXML
    private void handleDeleteNote() {
        if (currentNote == null) return;
        AppContext.getInstance().getNoteService().deleteNote(AppContext.getInstance().getSessionService().getCurrentUserId(), currentNote.getId());
        currentNote = null;
        refreshNotesList();
        notesList.getSelectionModel().clearSelection();
        editorToolbar.getChildren().forEach(node -> {
            setVisibleNode(node, false);
        });
        initialize();
    }

    void viewEditor() {
        noteEditor.getChildren().forEach(node -> {
            if (!node.equals(editorToolbar)) {
                setVisibleNode(node, true);
            }
        });
        noteEditor.getStyleClass().remove("transparent");
        noteEditor.getStyleClass().add("jy-notes-editor");
    }

    void hideEditor() {
        noteEditor.getChildren().forEach(node -> {
            if (!node.equals(editorToolbar)) {
                setVisibleNode(node, false);
            }
        });
        noteEditor.getStyleClass().remove("jy-notes-editor");
        noteEditor.getStyleClass().add("transparent");

    }

    void setVisibleNode(Node node, boolean bool) {
        node.setVisible(bool);
        node.setDisable(!bool);
    }

    private String getWordDeclension(int n) {
        if (n % 10 == 1 && n % 100 != 11) return "слово";
        if (n % 10 >= 2 && n % 10 <= 4 && (n % 100 < 10 || n % 100 >= 20)) return "слова";
        return "слов";
    }

    private String getCharDeclension(int n) {
        if (n % 10 == 1 && n % 100 != 11) return "символ";
        if (n % 10 >= 2 && n % 10 <= 4 && (n % 100 < 10 || n % 100 >= 20)) return "символа";
        return "символов";
    }
}