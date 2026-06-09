package ru.lgtu.jyggalag.repository;

import ru.lgtu.jyggalag.core.OperationResult;
import ru.lgtu.jyggalag.model.User;

import java.util.List;

public class UserRepository {
    private final FileRepository fileService;
    private final String usersFilePath;

    public UserRepository(FileRepository fileService, String usersFilePath) {
        this.fileService = fileService;
        this.usersFilePath = usersFilePath;
    }

    /**
     * Получить всех
     */
    public OperationResult<List<User>> loadUsersList() {
        return fileService.loadJsonList(usersFilePath, User[].class);
    }

    /**
     * Сохранить всех
     */
    public OperationResult<Void> saveUsersList(List<User> usersList) {
        return fileService.saveJsonList(usersFilePath, usersList);
    }

    /**
     * поиск по ID
     */

    public OperationResult<User> findById(int id) {
        OperationResult<List<User>> result = loadUsersList();
        if (!result.isSuccess()) return OperationResult.failure(result.getErrorMessage());

        return result.getData().stream()
                .filter(u -> u.getId() == id)
                .findFirst()
                .map(OperationResult::success)
                .orElseGet(() -> OperationResult.failure("Пользователь с ID " + id + " не найден"));
    }

    /**
     * поиск по имени
     */
    public OperationResult<User> findByUsername(String username) {
        OperationResult<List<User>> result = loadUsersList();
        if (!result.isSuccess()) return OperationResult.failure(result.getErrorMessage());

        return result.getData().stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username))
                .findFirst()
                .map(OperationResult::success)
                .orElseGet(() -> OperationResult.failure("Пользователь " + username + " не найден"));
    }

}