package ru.lgtu.jyggalag.ui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ru.lgtu.jyggalag.config.AppContext;
import ru.lgtu.jyggalag.core.LogService;
import ru.lgtu.jyggalag.core.OperationResult;
import ru.lgtu.jyggalag.model.Task;
import ru.lgtu.jyggalag.model.TaskStatus;
import ru.lgtu.jyggalag.model.TaskPriority;
import ru.lgtu.jyggalag.utils.DateUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TasksController {


    @FXML
    private Label tasksTotalLabel;
    @FXML
    private TextField searchField;
    @FXML
    private Button filterAll;
    @FXML
    private Button filterActive;
    @FXML
    private Button filterDone;
    @FXML
    private ListView<Task> tasksList;

    @FXML
    private VBox taskEditor;
    @FXML
    private HBox editorToolbar;
    @FXML
    private Label editorTaskTitle;
    @FXML
    private TextField taskTitleField;

    @FXML
    private ComboBox<TaskStatus> statusCombo;
    @FXML
    private ComboBox<TaskPriority> priorityCombo;

    @FXML
    private HBox tagsRow;
    @FXML
    private TextArea taskDescField;

    @FXML
    private Label createdLabel;
    @FXML
    private Label modifiedLabel;
    @FXML
    private Button saveButton;

    private Task currentTask;
    private String currentFilter = "ALL";
    private List<Task> allLoadedTasks = new ArrayList<>();

    @FXML
    public void initialize() {

        statusCombo.getItems().setAll(TaskStatus.values());
        priorityCombo.getItems().setAll(TaskPriority.values());

        tasksList.setCellFactory(param -> new ListCell<>() {
            private javafx.scene.Parent row;
            private CheckBox checkBox;
            private Label titleLabel;
            private Label statusLabel;
            private Label tagLabel;
            private Label timeLabel;

            @Override
            protected void updateItem(Task task, boolean empty) {
                super.updateItem(task, empty);

                if (empty || task == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    if (row == null) {
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/lgtu/jyggalag/fxml/tasks/task_row.fxml"));
                            row = loader.load();

                            checkBox = (CheckBox) row.lookup("#taskCheckBox");
                            titleLabel = (Label) row.lookup("#taskTitle");
                            statusLabel = (Label) row.lookup("#taskStatus");
                            tagLabel = (Label) row.lookup("#taskTag");
                            timeLabel = (Label) row.lookup("#taskTime");

                        } catch (IOException e) {
                            LogService.error("TasksController | Не удалось загрузить кастомный task_row.fxml: ", e);
                            setText("Ошибка загрузки строки");
                            setGraphic(null);
                            return;
                        }
                    }

                    String title = task.getTitle();
                    if (title.length() > 15) {
                        title = title.substring(0, 15) + "...";
                    }

                    titleLabel.setText(title);


                    if (timeLabel != null && task.getDeadline() != null) {
                        timeLabel.setText(DateUtils.formatTime(task.getDeadline()));
                    } else if (timeLabel != null) {
                        timeLabel.setText("");
                    }

                    if (statusLabel != null && task.getStatus() != null) {
                        statusLabel.setText(task.getStatus().name());
                    }

                    if (tagLabel != null) {
                        if (task.getTags() != null && !task.getTags().isEmpty()) {
                            tagLabel.setText(task.getTags().getFirst());
                            tagLabel.setVisible(true);
                            tagLabel.setManaged(true);
                            tagLabel.setStyle("-fx-background-color: " + task.getColor() + ";");
                        } else {
                            tagLabel.setVisible(false);
                            tagLabel.setManaged(false);
                        }
                    }

                    if (checkBox != null) {
                        checkBox.setOnAction(null);
                        checkBox.setSelected(task.getStatus() == TaskStatus.DONE);

                        checkBox.setOnAction(event -> {
                            boolean done = checkBox.isSelected();
                            task.setStatus(done ? TaskStatus.DONE : TaskStatus.TODO);
                            if (statusLabel != null) {
                                statusLabel.setText(task.getStatus().name());
                            }
                            applyDoneStyles(task);
                        });
                    }

                    applyDoneStyles(task);
                    setGraphic(row);
                }
            }

            /**
             * Вспомогательный метод для динамического переключения стилей выполненной задачи
             */
            private void applyDoneStyles(Task task) {
                if (row == null || titleLabel == null) return;

                if (task.getStatus() == TaskStatus.DONE) {
                    if (!row.getStyleClass().contains("jy-task-row-done")) {
                        row.getStyleClass().add("jy-task-row-done");
                    }
                    if (!titleLabel.getStyleClass().contains("jy-task-title-done")) {
                        titleLabel.getStyleClass().add("jy-task-title-done");
                    }
                } else {
                    row.getStyleClass().remove("jy-task-row-done");
                    titleLabel.getStyleClass().remove("jy-task-title-done");
                }
            }

            @Override
            public void updateSelected(boolean selected) {
                super.updateSelected(selected);
                if (row != null) {
                    if (selected) {
                        if (!row.getStyleClass().contains("jy-task-row-active")) {
                            row.getStyleClass().add("jy-task-row-active");
                        }
                    } else {
                        row.getStyleClass().remove("jy-task-row-active");
                    }
                }
            }
        });

        refreshTasksList();
        hideEditor();
    }

    /**
     * Загружает данные из TaskService и распределяет по фильтрам
     */
    private void refreshTasksList() {
        int currentUserId = AppContext.getInstance().getSessionService().getCurrentUserId();
        OperationResult<List<Task>> result = AppContext.getInstance().getTaskService().getAllTasks(currentUserId);

        if (result.isSuccess() && result.getData() != null) {
            allLoadedTasks = result.getData();
            applyFiltersAndSearch();
        } else {
            LogService.error("TasksController | Не удалось загрузить задачи: " + result.getErrorMessage());
        }
    }

    /**
     * Логика фильтрации и поиска по списку
     */
    private void applyFiltersAndSearch() {
        String query = searchField.getText().toLowerCase().trim();

        List<Task> filtered = allLoadedTasks.stream()
                .filter(task -> {
                    return switch (currentFilter) {
                        case "ACTIVE" ->
                                (task.getStatus() != TaskStatus.DONE && task.getStatus() != TaskStatus.CANCELLED);
                        case "DONE" ->
                                (task.getStatus() == TaskStatus.DONE || task.getStatus() == TaskStatus.CANCELLED);
                        default -> true;
                    };
                })
                .filter(task -> task.getTitle().toLowerCase().contains(query) ||
                        task.getDescription().toLowerCase().contains(query))
                .toList();

        tasksList.getItems().clear();
        tasksList.getItems().addAll(filtered);
        tasksTotalLabel.setText(String.valueOf(allLoadedTasks.size()));
    }

    /**
     * Выбор задачи и наполнение редактора данными
     */
    @FXML
    private void handleTaskSelect() {
        Task selected = tasksList.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        this.currentTask = selected;


        editorTaskTitle.setText(selected.getTitle().isEmpty() ? "Без названия" : selected.getTitle());

        taskTitleField.setText(selected.getTitle());
        statusCombo.setValue(selected.getStatus());
        priorityCombo.setValue(selected.getPriority());
        taskDescField.setText(selected.getDescription());
        Button addTagBtn = new Button("+ тег");
        addTagBtn.getStyleClass().add("jy-notes-add-tag");
        addTagBtn.setOnAction(e -> handleAddTag());
        tagsRow.getChildren().add(addTagBtn);

        createdLabel.setText("Создано: " + (selected.getCreatedAt() != null ? DateUtils.formatDateTime(selected.getCreatedAt()) : "—"));
        modifiedLabel.setText("Изменено: " + (selected.getUpdatedAt() != null ? DateUtils.formatDateTime(selected.getUpdatedAt()) : "—"));


        editorToolbar.getChildren().forEach(node -> {
            if (node instanceof Button) {
                setVisibleNode(node, true);
            }
        });

        setVisibleNode(saveButton, false);
        refreshTags();
        viewEditor();

    }

    @FXML
    private void handleSaveTask() {
        if (currentTask == null) return;

        currentTask.setTitle(taskTitleField.getText());
        currentTask.setDescription(taskDescField.getText());

        List<String> tagList = new ArrayList<>();
        if (tagsRow != null && !tagsRow.getChildren().isEmpty()) {
            Node first = tagsRow.getChildren().getFirst();
            if (first instanceof HBox) {
                HBox badge = (HBox) first;
                if (!badge.getChildren().isEmpty() && badge.getChildren().getFirst() instanceof Label) {
                    String text = ((Label) badge.getChildren().getFirst()).getText();
                    if (text != null && !text.trim().isEmpty()) tagList.add(text.trim());
                }
            } else if (first instanceof TextField) {
                String text = ((TextField) first).getText();
                if (text != null && !text.trim().isEmpty()) tagList.add(text.trim());
            } else if (first instanceof Label) {
                String text = ((Label) first).getText();
                if (text != null && !text.trim().isEmpty()) tagList.add(text.trim());
            }
        }
        currentTask.setTags(tagList);

        currentTask.setPriority(priorityCombo.getValue());
        currentTask.setStatus(statusCombo.getValue());
        currentTask.setUpdatedAt(LocalDateTime.now());
        currentTask.setColor(currentTask.getColor());

        OperationResult<Void> result = AppContext.getInstance().getTaskService().editTask(AppContext.getInstance().getSessionService().getCurrentUserId(), currentTask.getId(), currentTask);

        if (result.isSuccess()) {
            LogService.info("TasksController | handleSaveTask | Задача {} сохранена", currentTask.getId());
            refreshTasksList();
            tasksList.getSelectionModel().select(currentTask);

        } else {
            LogService.error("TasksController | handleSaveTask | Ошибка сохранения: " + result.getErrorMessage());
        }
        setVisibleNode(saveButton, !result.isSuccess());
        modifiedLabel.setText("Изменено: " + DateUtils.formatDateTime(currentTask.getUpdatedAt()));
    }

    @FXML
    private void handleMarkDone() {
        if (currentTask == null) return;
        statusCombo.setValue(TaskStatus.DONE);
        handleSaveTask();
    }

    @FXML
    private void handleNewTask() {
        Task task = new Task();
        task.setTitle("Новая задача");
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.LOW);
        task.setCreatedAt(LocalDateTime.now());

        AppContext.getInstance().getTaskService().addTask(AppContext.getInstance().getSessionService().getCurrentUserId(), task);
        refreshTasksList();
        tasksList.getSelectionModel().selectLast();
        handleTaskSelect();
    }

    @FXML
    private void handleSearch() {
        applyFiltersAndSearch();
    }

    @FXML
    private void handleFilterAll() {
        toggleFilterButtons(filterAll);
        currentFilter = "ALL";
        applyFiltersAndSearch();
    }

    @FXML
    private void handleFilterActive() {
        toggleFilterButtons(filterActive);
        currentFilter = "ACTIVE";
        applyFiltersAndSearch();
    }

    @FXML
    private void handleFilterDone() {
        toggleFilterButtons(filterDone);
        currentFilter = "DONE";
        applyFiltersAndSearch();
    }

    private void toggleFilterButtons(Button activeBtn) {
        List.of(filterAll, filterActive, filterDone).forEach(btn -> btn.getStyleClass().remove("jy-tasks-filter-active"));
        activeBtn.getStyleClass().add("jy-tasks-filter-active");
    }

    @FXML
    private void handleDeleteTask() {
        if (currentTask == null) return;
        AppContext.getInstance().getTaskService().deleteTask(AppContext.getInstance().getSessionService().getCurrentUserId(), currentTask.getId());
        currentTask = null;
        refreshTasksList();
        tasksList.getSelectionModel().clearSelection();
        editorToolbar.getChildren().forEach(node -> {
            setVisibleNode(node, false);
        });
        initialize();
    }

    @FXML
    private void handleTitleEdit() {
        if (currentTask == null) return;

        String currentTitle = taskTitleField.getText();
        editorTaskTitle.setText(currentTitle.isEmpty() ? "Без названия" : currentTitle);

        boolean titleChanged = !currentTitle.equals(currentTask.getTitle());

        setVisibleNode(saveButton, titleChanged);

    }

    @FXML
    private void handleStatusChange() {
        if (currentTask == null) return;

        boolean statusChanged = statusCombo.getValue() != currentTask.getStatus();

        setVisibleNode(saveButton, statusChanged || !saveButton.isDisable());
    }

    @FXML
    private void handlePriorityChange() {
        if (currentTask == null) return;

        boolean priorityChanged = priorityCombo.getValue() != currentTask.getPriority();

        setVisibleNode(saveButton, priorityChanged || !saveButton.isDisable());
    }


    @FXML
    private void handleAddTag() {
        if (currentTask == null || tagsRow == null) return;

        if (currentTask.getTags() != null && !currentTask.getTags().isEmpty()) {
            return;
        }

        Button addButton = null;
        for (Node child : tagsRow.getChildren()) {
            if (child instanceof Button && !((Button) child).getText().equals("×")) {
                addButton = (Button) child;
                break;
            }
        }

        String taskColor = currentTask.getColor();
        if (taskColor == null || taskColor.trim().isEmpty()) {
            taskColor = "#555555";
        }

        TextField tagInput = new TextField();
        tagInput.setPromptText("Новый тег...");
        tagInput.getStyleClass().add("jy-field");
        tagInput.setStyle(
                "-fx-pref-width: 90px; -fx-max-width: 140px; -fx-padding: 2 4 2 4; -fx-background-radius: 4; " +
                        "-fx-background-color: " + taskColor + "; -fx-text-fill: white; -fx-font-size: 12px; -fx-prompt-text-fill: #dddddd;"
        );

        if (addButton != null) {
            addButton.setDisable(true);
            int buttonIndex = tagsRow.getChildren().indexOf(addButton);
            tagsRow.getChildren().add(buttonIndex, tagInput);
        } else {
            tagsRow.getChildren().add(tagInput);
        }

        tagInput.requestFocus();

        Runnable commitTag = () -> {
            if (!tagsRow.getChildren().contains(tagInput)) return;

            String typedTag = tagInput.getText().trim();
            if (!typedTag.isEmpty()) {
                List<String> singleTagList = new ArrayList<>();
                singleTagList.add(typedTag);
                currentTask.setTags(singleTagList);
                setVisibleNode(saveButton, true);
            }
            refreshTags();
        };

        tagInput.setOnAction(e -> commitTag.run());
        tagInput.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                commitTag.run();
            }
        });
    }

    private void refreshTags() {
        if (tagsRow == null) return;

        Node addButtonNode = null;
        for (Node child : tagsRow.getChildren()) {
            if (child instanceof Button && !((Button) child).getText().equals("×")) {
                addButtonNode = child;
                break;
            }
        }
        tagsRow.getChildren().clear();

        boolean hasTag = false;

        if (currentTask != null && currentTask.getTags() != null && !currentTask.getTags().isEmpty()) {
            String tag = currentTask.getTags().getFirst();
            hasTag = true;

            String taskColor = currentTask.getColor();
            if (taskColor == null || taskColor.trim().isEmpty()) {
                taskColor = "#555555";
            }

            HBox tagBadge = new HBox(6);
            tagBadge.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            tagBadge.getStyleClass().addAll("task-tag", "jy-tag-default");
            tagBadge.setStyle("-fx-padding: 2 6 2 8; -fx-background-radius: 4; -fx-background-color: " + taskColor + ";");


            Label tagLabel = new Label(tag);
            tagLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;");


            ColorPicker colorPicker = new ColorPicker();
            colorPicker.setStyle(
                    "-fx-color-label-visible: false; -fx-background-color: transparent; -fx-padding: 0; " +
                            "-fx-min-width: 18px; -fx-pref-width: 18px; -fx-max-width: 18px; " +
                            "-fx-min-height: 18px; -fx-pref-height: 18px; -fx-max-height: 18px; -fx-cursor: hand;"
            );

            try {
                colorPicker.setValue(javafx.scene.paint.Color.valueOf(taskColor.toUpperCase()));
            } catch (Exception e) {
                colorPicker.setValue(javafx.scene.paint.Color.GRAY);
            }

            colorPicker.setOnAction(colorEvent -> {
                javafx.scene.paint.Color chosenColor = colorPicker.getValue();
                String hexColor = String.format("#%02X%02X%02X",
                        (int) (chosenColor.getRed() * 255),
                        (int) (chosenColor.getGreen() * 255),
                        (int) (chosenColor.getBlue() * 255));

                currentTask.setColor(hexColor);
                refreshTags();
                setVisibleNode(saveButton, true);
            });

            Button removeButton = new Button("×");
            removeButton.getStyleClass().add("jy-button-ghost");
            removeButton.setStyle("-fx-padding: 0 0 0 2; -fx-cursor: hand; -fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 14px;");

            removeButton.setOnAction(event -> {
                currentTask.setTags(new ArrayList<>());
                refreshTags();
                setVisibleNode(saveButton, true);
            });

            tagBadge.getChildren().addAll(tagLabel, colorPicker, removeButton);
            tagsRow.getChildren().add(tagBadge);
        }

        if (!hasTag) {
            if (addButtonNode != null) {
                addButtonNode.setDisable(false);
                tagsRow.getChildren().add(addButtonNode);
            } else {
                Button newAddButton = new Button("+");
                newAddButton.getStyleClass().add("jy-button-ghost");
                newAddButton.setStyle("-fx-cursor: hand;");
                newAddButton.setOnAction(event -> handleAddTag());
                tagsRow.getChildren().add(newAddButton);
            }
        }
    }

    @FXML
    private void handleDescEdit() {
        if (currentTask == null) return;

        String currentText = taskDescField.getText();

        boolean textChanged = !currentText.equals(currentTask.getDescription());

        setVisibleNode(saveButton, textChanged);
    }

    @FXML
    void viewEditor() {
        taskEditor.getChildren().forEach(node -> {
            if (!node.equals(editorToolbar)) {
                setVisibleNode(node, true);
            }
        });
        taskEditor.getStyleClass().remove("transparent");
        taskEditor.getStyleClass().add("jy-task-editor");
    }

    @FXML
    void hideEditor() {
        taskEditor.getChildren().forEach(node -> {
            if (!node.equals(editorToolbar)) {
                setVisibleNode(node, false);
            }
        });
        taskEditor.getStyleClass().remove("jy-task-editor");
        taskEditor.getStyleClass().add("transparent");

    }

    void setVisibleNode(Node node, boolean bool) {
        node.setVisible(bool);
        node.setDisable(!bool);
    }

}