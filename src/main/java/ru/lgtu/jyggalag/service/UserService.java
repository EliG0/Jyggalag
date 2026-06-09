package ru.lgtu.jyggalag.service;

import ru.lgtu.jyggalag.core.LogService;
import ru.lgtu.jyggalag.core.OperationResult;
import ru.lgtu.jyggalag.model.User;
import ru.lgtu.jyggalag.repository.UserRepository;

import java.util.List;


/**
 * Логика по работе с пользователями.
 */
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Добавление нового пользователя с бизнес-проверкой на дубликат логина
     */
    public OperationResult<Void> addUser(User user) {
        OperationResult<List<User>> usersResult = userRepository.loadUsersList();

        if (!usersResult.isSuccess()) {
            LogService.error("UserService | addUser | Не удалось загрузить пользователей: {}", usersResult.getErrorMessage());
            return OperationResult.failure(usersResult.getErrorMessage());
        }

        List<User> users = usersResult.getData();

        if (isUserExist(user.getUsername())) {
            return OperationResult.failure("Пользователь с именем '" + user.getUsername() + "' уже существует!");
        }

        users.add(user);
        OperationResult<Void> saveResult = userRepository.saveUsersList(users);

        if (!saveResult.isSuccess()) {
            LogService.error("UserService | addUser | Ошибка сохранения: {}", saveResult.getErrorMessage());
            return OperationResult.failure(saveResult.getErrorMessage());
        }

        LogService.info("Добавлен новый пользователь {}!", user.getUsername());
        return OperationResult.success(null);
    }

    /**
     * Генерирует следующий свободный ID
     */
    public OperationResult<Integer> generateNextId() {
        OperationResult<List<User>> usersResult = userRepository.loadUsersList();
        if (!usersResult.isSuccess()) {
            return OperationResult.failure("Не удалось рассчитать ID: " + usersResult.getErrorMessage());
        }

        int nextId = usersResult.getData().stream()
                .mapToInt(User::getId)
                .max()
                .orElse(0) + 1;

        return OperationResult.success(nextId);
    }

    /**
     * Получить пользователя по ID
     */
    public OperationResult<User> getUser(int userId) {
        OperationResult<List<User>> usersResult = userRepository.loadUsersList();
        if (!usersResult.isSuccess()) {
            return OperationResult.failure(usersResult.getErrorMessage());
        }

        return usersResult.getData().stream()
                .filter(user -> user.getId() == userId)
                .findFirst()
                .map(OperationResult::success)
                .orElseGet(() -> OperationResult.failure("Пользователь с ID " + userId + " не найден"));
    }

    /**
     * Получить пользователя по имени
     */
    public OperationResult<User> getUser(String username) {
        OperationResult<User> usersResult = userRepository.findByUsername(username);

        if (!usersResult.isSuccess()) {
            return OperationResult.failure(usersResult.getErrorMessage());
        }

        return OperationResult.success(usersResult.getData());
    }

    /**
     * Проверка существования пользователя
     */
    public boolean isUserExist(String username) {
        return userRepository.findByUsername(username).isSuccess();
    }

    public boolean isUserExist(int id) {
        return userRepository.findById(id).isSuccess();
    }

}