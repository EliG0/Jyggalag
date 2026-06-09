package ru.lgtu.jyggalag.ui.controllers;

import javafx.fxml.FXML;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

import ru.lgtu.jyggalag.config.AppContext;
import ru.lgtu.jyggalag.core.LogService;
import ru.lgtu.jyggalag.core.OperationResult;
import ru.lgtu.jyggalag.model.User;
import ru.lgtu.jyggalag.service.AuthService;
import ru.lgtu.jyggalag.service.NavigationService;
import ru.lgtu.jyggalag.service.SessionService;
import ru.lgtu.jyggalag.ui.Screen;

import java.awt.*;

public class AuthController {


    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;
    @FXML
    private CheckBox rememberMe;

    @FXML
    private VBox loginCard;
    @FXML
    private VBox registerCard;

    @FXML
    private TextField regLoginField;
    @FXML
    private PasswordField regPasswordField;
    @FXML
    private PasswordField regRepeatPasswordField;

    @FXML
    private void handleSubmit() {
        String login = loginField.getText();
        String password = passwordField.getText();

        if (errorLabel != null) {
            errorLabel.setText("");
        }

        if (login == null || login.trim().isEmpty() || password == null || password.isEmpty()) {
            showError("Пожалуйста, заполните все поля.");
            return;
        }

        AuthService authService = AppContext.getInstance().getAuthService();
        SessionService sessionService = AppContext.getInstance().getSessionService();
        NavigationService navigationService = AppContext.getInstance().getNavigationService();

        OperationResult<User> result = authService.login(login.trim(), password);

        if (result.isSuccess()) {
            sessionService.startSession(result.getData(), rememberMe.isSelected());
            navigationService.loadMainLayout(Screen.DASHBOARD);
        } else {
            showError(result.getErrorMessage());
        }
    }

    @FXML
    private void handleRegistration() {
        String login = regLoginField.getText();
        String password = regPasswordField.getText();
        String password2 = regRepeatPasswordField.getText();

        if (errorLabel != null) {
            errorLabel.setText("");
        }

        if (login == null || login.trim().isEmpty() || password == null || password.isEmpty() || password2 == null || password2.isEmpty()) {
            showError("Пожалуйста, заполните все поля.");
            return;
        }

        if (!password.equals(password2)) {
            showError("Пароли не совпадают.");
            return;
        }

        AuthService authService = AppContext.getInstance().getAuthService();
        OperationResult<Void> result = authService.registration(login, password);

        if (result.isSuccess()) {
            if (errorLabel != null) {
                errorLabel.setStyle("-fx-text-fill: #00ff00;");
                errorLabel.setText("Регистрация успешна! Теперь вы можете войти.");
                regLoginField.clear();
                regPasswordField.clear();
                regRepeatPasswordField.clear();
            }
        } else {
            showError(result.getErrorMessage());
        }
    }

    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setStyle("-fx-text-fill: #ff3333;");
            errorLabel.setText(message);
        } else {
            LogService.error("AuthController | showError | {}", message);
        }
    }




    /**
     * Переключить экран на форму РЕГИСТРАЦИИ
     */
    @FXML
    private void showRegisterForm() {
        switchCard(loginCard, registerCard);
    }

    /**
     * Переключить экран на форму ЛОГИНА
     */
    @FXML
    private void showLoginForm() {

        switchCard(registerCard, loginCard);
    }

    private void switchCard(VBox fromCard, VBox toCard) {
        if (errorLabel != null) errorLabel.setText("");

        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), fromCard);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        fadeOut.setOnFinished(e -> {
            fromCard.setVisible(false);
            fromCard.setManaged(false);

            toCard.setVisible(true);
            toCard.setManaged(true);
            toCard.setOpacity(0.0);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), toCard);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });

        fadeOut.play();
    }

    // Хрень для перемещения окна
    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    private HBox mainHeader;

    @FXML
    private void handleMousePressed(javafx.scene.input.MouseEvent event) {
        xOffset = event.getSceneX();
        yOffset = event.getSceneY();

    }

    @FXML
    private void handleMouseDragged(javafx.scene.input.MouseEvent event) {
        Stage stage = (Stage) mainHeader.getScene().getWindow();
        stage.setX(event.getScreenX() - xOffset);
        stage.setY(event.getScreenY() - yOffset);
    }

    @FXML
    private Button minimizeButton;

    @FXML
    private void handleClose() {
        javafx.application.Platform.exit();
    }

    @FXML
    private void handleMinimize() {
        Stage stage = (Stage) minimizeButton.getScene().getWindow();
        stage.setIconified(true);
    }

}
