package ru.lgtu.jyggalag.service;

import ru.lgtu.jyggalag.core.LogService;
import ru.lgtu.jyggalag.core.OperationResult;
import ru.lgtu.jyggalag.model.User;
import ru.lgtu.jyggalag.utils.PasswordUtils;
import ru.lgtu.jyggalag.utils.ValidatorUtils;

/**
 * Логика авторизации и регистрации пользователей.
 */
public class AuthService {
    private final UserService userService;

    public AuthService(UserService userService) {
        this.userService = userService;
    }

    public OperationResult<User> login(String username, String password) {
        OperationResult<User> userResult = userService.getUser(username);
        if (!userResult.isSuccess()) {
            return OperationResult.failure("Неверное имя пользователя или пароль.");
        }

        User user = userResult.getData();

        if (!PasswordUtils.checkPassword(password, user.getPasswordHash())) {
            return OperationResult.failure("Неверное имя пользователя или пароль.");
        }

        LogService.userAction(user.getUsername(), "Успешно вошел в систему");
        return OperationResult.success(user);
    }

    public OperationResult<Void> registration(String username, String password) {
        if (!ValidatorUtils.isValidLogin(username)) {
            return OperationResult.failure("Логин должен содержать от 4 до 20 символов (латиница и цифры).");
        }

        if (!ValidatorUtils.isValidPassword(password)) {
            return OperationResult.failure("Слишком простой пароль. Требуется минимум 8 символов, одна цифра и одна заглавная буква.");
        }

        OperationResult<Integer> idResult = userService.generateNextId();
        if (!idResult.isSuccess()) {
            return OperationResult.failure("Ошибка при создании аккаунта: " + idResult.getErrorMessage());
        }

        int newId = idResult.getData();
        String hashedPassword = PasswordUtils.hashPassword(password);

        User newUser = new User(newId, username.trim(), hashedPassword);
        OperationResult<Void> createResult = userService.addUser(newUser);

        if (!createResult.isSuccess()) {
            return OperationResult.failure(createResult.getErrorMessage());
        }

        LogService.userAction(newUser.getUsername(), "Зарегистрировал новый аккаунт");
        return OperationResult.success(null);
    }
}