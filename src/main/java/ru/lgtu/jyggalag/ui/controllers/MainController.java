package ru.lgtu.jyggalag.ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import ru.lgtu.jyggalag.config.AppContext;
import ru.lgtu.jyggalag.core.LogService;
import ru.lgtu.jyggalag.service.NavigationService;
import ru.lgtu.jyggalag.service.ReportService;
import ru.lgtu.jyggalag.ui.Screen;

import java.util.List;

import static java.lang.System.exit;

public class MainController {

    private final NavigationService navigationService = AppContext.getInstance().getNavigationService();

    /* =================================
        Переключение внутренних экранов
       ================================= */


    @FXML private Button todayButton;
    @FXML private Button tasksButton;
    @FXML private Button notesButton;

    private List<Button> navButtons;
    private static final String ACTIVE_CLASS = "jy-nav-item-active";

    @FXML
    public void initialize() {
        navButtons = List.of(todayButton, tasksButton, notesButton);
        setActiveNavigationStyle(todayButton);
    }

    /**
     * Централизованный метод переключения стилей навигации.
     * Очищает активный стиль у всех кнопок и добавляет его только выбранной.
     */
    private void setActiveNavigationStyle(Button clickedButton) {
        if (navButtons == null) return;

        for (Button btn : navButtons) {
            if (btn != null) {
                btn.getStyleClass().remove(ACTIVE_CLASS);
            }
        }

        if (clickedButton != null && !clickedButton.getStyleClass().contains(ACTIVE_CLASS)) {
            clickedButton.getStyleClass().add(ACTIVE_CLASS);
        }
    }


    @FXML
    private void handleNavToday() {
        setActiveNavigationStyle(todayButton);
        navigationService.navigateTo(Screen.DASHBOARD);
    }

    @FXML
    private void handleNavTasks() {
        setActiveNavigationStyle(tasksButton);
        navigationService.navigateTo(Screen.TASKS);
    }

    @FXML
    private void handleNavNotes() {
        setActiveNavigationStyle(notesButton);
        navigationService.navigateTo(Screen.NOTES);
    }

    @FXML
    private void handleLogout() {
        AppContext.getInstance().getSessionService().closeSession();
        exit(0);
    }

    @FXML
    private void handleReport() {
        LogService.info("MainController | Запуск выгрузки аналитических отчетов...");
        try {
            int userId = AppContext.getInstance().getSessionService().getCurrentUserId();

            var tasksResult = AppContext.getInstance().getTaskService().getAllTasks(userId);
            var notesResult = AppContext.getInstance().getNoteService().getAllNotes(userId);

            if (tasksResult.isSuccess() && notesResult.isSuccess() && tasksResult.getData() != null && notesResult.getData() != null) {

                ReportService reportService = new ReportService();

                reportService.generateTasksReport(tasksResult.getData());
                reportService.generateNotesReport(notesResult.getData());

                LogService.info("MainController | Сводные PDF-отчеты успешно сгенерированы.");

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Генерация отчетов");
                alert.setHeaderText("Отчеты успешно сформированы!");
                alert.setContentText("Файлы 'tasks_report.pdf' и 'notes_report.pdf' сохранены в корневую директорию приложения.");
                alert.showAndWait();

            } else {
                String errorMsg = !tasksResult.isSuccess() ? tasksResult.getErrorMessage() : notesResult.getErrorMessage();
                LogService.error("MainController | Не удалось собрать данные для отчета: " + errorMsg);

                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Ошибка системы");
                alert.setHeaderText("Сбой при формировании отчета");
                alert.setContentText("Причина: " + errorMsg + "\nРекомендация: Проверьте подключение к базе данных или перезапустите сессию.");
                alert.showAndWait();
            }
        } catch (Exception e) {
            LogService.error("MainController | Критическая ошибка генерации PDF: ", e);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Критическая ошибка");
            alert.setHeaderText("Внутренний сбой iText ядра");
            alert.setContentText("Не удалось записать файлы на диск. Убедитесь, что старые PDF не открыты в сторонних программах просмотра.");
            alert.showAndWait();
        }
    }


    /* ==================
        УПРАВЛЕНИЕ ОКНОМ
       ================== */

    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    private HBox mainHeader;

    @FXML
    private void handleMousePressed(javafx.scene.input.MouseEvent event) {
        xOffset = event.getSceneX();
        yOffset = event.getSceneY();

        if (event.getButton() == javafx.scene.input.MouseButton.PRIMARY && event.getClickCount() == 2) {
            handleMaximize();
        }
    }

    @FXML
    private void handleMouseDragged(javafx.scene.input.MouseEvent event) {
        Stage stage = (Stage) mainHeader.getScene().getWindow();
        stage.setX(event.getScreenX() - xOffset);
        stage.setY(event.getScreenY() - yOffset);
    }

    @FXML private Button minimizeButton;
    @FXML private Button maximizeButton;

    @FXML
    private void handleClose() {
        javafx.application.Platform.exit();
    }

    @FXML
    private void handleMinimize() {
        Stage stage = (Stage) minimizeButton.getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML
    private void handleMaximize() {
        Stage stage = (Stage) maximizeButton.getScene().getWindow();
        stage.setMaximized(!stage.isMaximized());
    }
}