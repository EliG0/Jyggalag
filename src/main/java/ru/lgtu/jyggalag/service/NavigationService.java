package ru.lgtu.jyggalag.service;

import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import ru.lgtu.jyggalag.core.LogService;
import ru.lgtu.jyggalag.ui.Screen;

import java.awt.*;
import java.io.IOException;
import java.util.Objects;

public class NavigationService {
    private Stage primaryStage;
    private Pane contentArea;

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
        try {
            this.primaryStage.initStyle(StageStyle.TRANSPARENT);
        } catch (IllegalStateException e) {
            // Игнорируем, если стиль уже задан
        }

        // Установка иконки приложения для панели задач
        try {
            Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/ru/lgtu/jyggalag/assets/logo.png")));
            this.primaryStage.getIcons().add(icon);
        } catch (Exception e) {
            LogService.warn("NavigationService | Не удалось загрузить иконку приложения: " + e.getMessage());
        }
    }

    /**
     * Открыть окно авторизации (с анимацией проявления)
     */
    public void navigateToAuth() {
        if (primaryStage == null) return;

        if (primaryStage.getScene() != null && primaryStage.getScene().getRoot() != null) {
            Parent currentRoot = primaryStage.getScene().getRoot();
            FadeTransition fadeOut = new FadeTransition(Duration.millis(500), currentRoot);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(event -> executeNavigateToAuth());
            fadeOut.play();
        } else {
            executeNavigateToAuth();
        }
    }

    private void executeNavigateToAuth() {
        try {
            Screen authScreen = Screen.AUTH;
            FXMLLoader loader = new FXMLLoader(getClass().getResource(authScreen.getFxmlPath()));
            Parent root = loader.load();

            root.setOpacity(0.0);

            Scene scene = new Scene(root, 900, 600);
            scene.setFill(Color.TRANSPARENT);

            applyGlobalStyles(scene);
            applyRandomWallpaper(root);

            primaryStage.setTitle(authScreen.getTitle());
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
            primaryStage.show();

            FadeTransition fadeIn = new FadeTransition(Duration.millis(500), root);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();

            this.contentArea = null;
            LogService.info("NavigationService | Открыто окно авторизации");
        } catch (IOException e) {
            LogService.error("NavigationService | Ошибка загрузки окна авторизации: ", e);
        }
    }

    /**
     * Загрузить главный каркас Main.fxml
     */
    public void loadMainLayout(Screen targetScreen) {
        if (primaryStage == null) return;

        if (primaryStage.getScene() != null && primaryStage.getScene().getRoot() != null) {
            Parent currentRoot = primaryStage.getScene().getRoot();
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), currentRoot);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(event -> executeLoadMainLayout(targetScreen));
            fadeOut.play();
        } else {
            executeLoadMainLayout(targetScreen);
        }
    }

    private void executeLoadMainLayout(Screen targetScreen) {
        try {
            Screen mainScreen = Screen.MAIN_LAYOUT;
            FXMLLoader loader = new FXMLLoader(getClass().getResource(mainScreen.getFxmlPath()));
            Parent root = loader.load();

            root.setOpacity(0.0);

            Scene scene = new Scene(root, 1600, 900);
            scene.setFill(Color.TRANSPARENT);

            applyGlobalStyles(scene);
            applyRandomWallpaper(root);


            this.contentArea = (Pane) root.lookup("#contentArea");

            primaryStage.setTitle(mainScreen.getTitle());
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
            primaryStage.show();


            FadeTransition fadeIn = new FadeTransition(Duration.millis(1200), root);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();

            LogService.info("NavigationService | Главная оболочка программы загружена");

            if (targetScreen != null) {
                navigateTo(targetScreen);
            }

        } catch (IOException e) {
            LogService.error("NavigationService | Ошибка загрузки каркаса Main.fxml: ", e);
        }
    }

    /**
     * УНИВЕРСАЛЬНЫЙ метод для переключения внутренних страниц (Dashboard, Tasks и т.д.)
     */
    public void navigateTo(Screen screen) {
        if (this.contentArea == null) {
            LogService.error("NavigationService | Ошибка навигации: область #contentArea недоступна.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(screen.getFxmlPath()));
            Parent subPage = loader.load();

            this.contentArea.getChildren().setAll(subPage);

            if (subPage instanceof javafx.scene.layout.Region) {
                javafx.scene.layout.Region region = (javafx.scene.layout.Region) subPage;
                region.prefWidthProperty().bind(this.contentArea.widthProperty());
                region.prefHeightProperty().bind(this.contentArea.heightProperty());
            }

            primaryStage.setTitle(screen.getTitle());
            LogService.info("NavigationService | Контент переключен на: " + screen.name());
        } catch (IOException e) {
            LogService.error("NavigationService | Ошибка загрузки страницы " + screen.name() + ": ", e);
        }
    }

    private void applyGlobalStyles(Scene scene) {
        String cssPath = "/ru/lgtu/jyggalag/styles/jyggalag.css";
        if (getClass().getResource(cssPath) != null) {
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(cssPath)).toExternalForm());
        }

    }

    private void applyRandomWallpaper(Parent root) {
        BorderPane borderPane = (BorderPane) root.lookup("#mainWallpapers");
        if (borderPane != null) {

            int randomBg = (int) (Math.random() * 6) + 1;
            borderPane.getStyleClass().add("bg-" + randomBg);

            javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle();

            clip.setArcWidth(40);
            clip.setArcHeight(40);

            clip.widthProperty().bind(borderPane.widthProperty());
            clip.heightProperty().bind(borderPane.heightProperty());

            borderPane.setClip(clip);
        }
    }

}