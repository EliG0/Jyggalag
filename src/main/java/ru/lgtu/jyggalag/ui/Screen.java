package ru.lgtu.jyggalag.ui;

/**
 * Перечисление всех доступных экранов приложения.
 */
public enum Screen {
    AUTH("/ru/lgtu/jyggalag/fxml/auth.fxml", "Джиггалаг — Авторизация"),
    MAIN_LAYOUT("/ru/lgtu/jyggalag/fxml/main.fxml", "Джиггалаг — Главная панель"),
    DASHBOARD("/ru/lgtu/jyggalag/fxml/dashboard/dashboard.fxml", "Джиггалаг — Панель управления"),
    TASKS("/ru/lgtu/jyggalag/fxml/tasks/tasks.fxml", "Джиггалаг — Список задач"),
    PROJECTS("/ru/lgtu/jyggalag/fxml/projects/projects.fxml", "Джиггалаг — Список проектов"),
    CALENDAR("/ru/lgtu/jyggalag/fxml/calendar/calendar.fxml", "Джиггалаг — Календарь"),
    REMINDERS("/ru/lgtu/jyggalag/fxml/reminders/reminders.fxml", "Джиггалаг — Напоминания"),
    NOTES("/ru/lgtu/jyggalag/fxml/notes/notes.fxml", "Джиггалаг — Заметки"),
    POMODORO("/ru/lgtu/jyggalag/fxml/notes/pomodoro.fxml", "Джиггалаг — Помидор"),
    ANALYTICS("/ru/lgtu/jyggalag/fxml/analytics/analytics.fxml", "Джиггалаг — Аналитика"),
    SETTINGS("/ru/lgtu/jyggalag/fxml/settings/settings.fxml", "Джиггалаг — Настройки");


    private final String fxmlPath;
    private final String title;

    Screen(String fxmlPath, String title) {
        this.fxmlPath = fxmlPath;
        this.title = title;
    }

    public String getFxmlPath() {
        return fxmlPath;
    }

    public String getTitle() {
        return title;
    }
}