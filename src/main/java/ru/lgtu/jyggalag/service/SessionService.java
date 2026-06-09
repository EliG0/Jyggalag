package ru.lgtu.jyggalag.service;

import ru.lgtu.jyggalag.config.AppContext;
import ru.lgtu.jyggalag.core.LogService;
import ru.lgtu.jyggalag.model.SessionConfig;
import ru.lgtu.jyggalag.model.User;
import java.util.Optional;

/**
 * Хранит состояние текущей сессии авторизованного пользователя и управляет авто-входом.
 */
public class SessionService {
    private User currentUser;
    private final String SESSION_FILE_PATH = "config/session.toml";

    /**
     * Открыть сессию при успешном входе.
     * @param user Пользователь
     * @param rememberMe Флаг из чекбокса на форме авторизации
     */
    public void startSession(User user, boolean rememberMe) {
        this.currentUser = user;
        LogService.info("SessionService | Сессия запущена для пользователя: " + user.getUsername());
        SessionConfig config = new SessionConfig(rememberMe, rememberMe ? user.getId() : -1);
        AppContext.getInstance().getFileRepository().saveTomlConfig(SESSION_FILE_PATH, config);
    }

    /**
     * Закрыть сессию
     */
    public void closeSession() {
        this.currentUser = null;
        SessionConfig resetConfig = new SessionConfig(false, -1);
        AppContext.getInstance().getFileRepository().saveTomlConfig(SESSION_FILE_PATH, resetConfig);
        LogService.info("SessionService | Сессия закрыта, данные авто-входа стерты.");
    }

    /**
     * Метод авто-входа при старте приложения.
     * Проверяет файл конфигурации, и если там стоит rememberMe, восстанавливает сессию.
     * @return true, если авто-вход удался; false, если не удался.
     */
    public boolean tryAutoLogin() {
        var result = AppContext.getInstance().getFileRepository().loadTomlConfig(SESSION_FILE_PATH, SessionConfig.class);

        if (result.isSuccess() && result.getData() != null) {
            SessionConfig config = result.getData();

            if (config.isRememberMe() && config.getSavedUserId() != -1) {
                LogService.info("SessionService | tryAutoLogin | Попытка восстановления сессии для: " + config.getSavedUserId());

                var userResult = AppContext.getInstance().getUserService().getUser(config.getSavedUserId());

                if (userResult.isSuccess() && userResult.getData() != null) {
                    this.currentUser = userResult.getData();
                    LogService.info("SessionService | tryAutoLogin | Авто-вход для: " + currentUser.getUsername());
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isLoggedIn() { return currentUser != null; }
    public Optional<User> getCurrentUser() { return Optional.ofNullable(currentUser); }
    public int getCurrentUserId() { return currentUser != null ? currentUser.getId() : -1; }
}