package ru.lgtu.jyggalag.ui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import ru.lgtu.jyggalag.config.AppContext;
import ru.lgtu.jyggalag.core.ComplementGenerator;
import ru.lgtu.jyggalag.core.GreetingsGenerator;
import ru.lgtu.jyggalag.core.LogService;
import ru.lgtu.jyggalag.core.OperationResult;
import ru.lgtu.jyggalag.model.Task;
import ru.lgtu.jyggalag.model.TaskStatus;
import ru.lgtu.jyggalag.utils.DateUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class DashboardController {

    @FXML
    private ListView<Task> taskList;

    @FXML
    private Label dateLabel;
    @FXML
    private Label welcomeTitle;
    @FXML
    private Label tasksTotalLabel;
    @FXML
    private Label tasksDoneLabel;
    @FXML
    private Label progressLabel;
    @FXML
    private Label complimentLabel;
    @FXML
    private Label taskCountBadge;

    @FXML
    public void initialize() {
        taskList.setCellFactory(param -> new ListCell<Task>() {
            private Parent row;
            private CheckBox checkBox;
            private Label titleLabel;
            private Label statusLabel;
            private Label tagLabel;
            private Label timeLabel;

            {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/lgtu/jyggalag/fxml/dashboard/task_row.fxml"));
                    row = loader.load();

                    checkBox = (CheckBox) row.lookup("#taskCheckBox");
                    titleLabel = (Label) row.lookup("#taskTitle");
                    statusLabel = (Label) row.lookup("#taskStatus");
                    tagLabel = (Label) row.lookup("#taskTag");
                    timeLabel = (Label) row.lookup("#taskTime");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected void updateItem(Task task, boolean empty) {
                super.updateItem(task, empty);

                if (empty || task == null || row == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    titleLabel.setText(task.getTitle());

                    statusLabel.setText(task.getStatus().name());
                    tagLabel.setText(task.getTags().isEmpty() ? "" : task.getTags().getFirst());
                    tagLabel.setStyle("-fx-background-color: " + task.getColor() + ";");
                    timeLabel.setText(DateUtils.formatTime(task.getDeadline()));

                    checkBox.setOnAction(null);
                    checkBox.setSelected(task.getStatus() == TaskStatus.DONE);

                    checkBox.setOnAction(event -> {
                        if (checkBox.isSelected()) {
                            task.setStatus(TaskStatus.DONE);
                            statusLabel.setText("DONE");
                        } else {
                            task.setStatus(TaskStatus.TODO);
                            statusLabel.setText("TODO");
                        }

                        AppContext.getInstance().getTaskService().editTask(AppContext.getInstance().getSessionService().getCurrentUserId(), task.getId(), task);
                    });
                    setGraphic(row);
                }
            }
        });
        OperationResult<List<Task>> tasksResult = loadUserTasks();
        if (tasksResult.isSuccess() && tasksResult.getData() != null) {
            taskList.getItems().addAll(tasksResult.getData());
        } else {
            LogService.error("DashboardController | Не удалось загрузить задачи: " + tasksResult.getErrorMessage());
        }
        String userName = AppContext.getInstance().getUserService().getUser(AppContext.getInstance().getSessionService().getCurrentUserId()).getData().getUsername();
        welcomeTitle.setText(GreetingsGenerator.generate(userName));
        complimentLabel.setText(ComplementGenerator.generate());
        fillLabels();
    }


    private void fillLabels() {
        int total = taskList.getItems().size();
        long doneCount = taskList.getItems().stream().filter(t -> t.getStatus() == TaskStatus.DONE).count();
        tasksTotalLabel.setText("" + total);
        taskCountBadge.setText("" + total);
        tasksDoneLabel.setText("" + doneCount);
        progressLabel.setText(total == 0 ? "0%" : (int) ((doneCount * 100) / total) + "%");
        dateLabel.setText(DateUtils.formatPrettyWeekDate(LocalDate.now()));
    }


    private OperationResult<List<Task>> loadUserTasks() {
        return AppContext.getInstance().getTaskService().getAllTasks(AppContext.getInstance().getSessionService().getCurrentUserId());
    }


    @FXML
    private void handleListEdit() {
        fillLabels();
    }
}