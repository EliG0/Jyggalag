package ru.lgtu.jyggalag.ui;

import javafx.application.Application;
import javafx.stage.Stage;
import ru.lgtu.jyggalag.config.AppContext;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        AppContext.getInstance().initialize();

        AppContext.getInstance().getNavigationService().setPrimaryStage(stage);

        if (AppContext.getInstance().getSessionService().tryAutoLogin()) {
            AppContext.getInstance().getNavigationService().loadMainLayout(Screen.DASHBOARD);
        } else {
            AppContext.getInstance().getNavigationService().navigateToAuth();
        }
    }
}