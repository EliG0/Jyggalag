package ru.lgtu.jyggalag.config;


import ru.lgtu.jyggalag.core.LogService;
import ru.lgtu.jyggalag.core.OperationResult;
import ru.lgtu.jyggalag.model.User;
import ru.lgtu.jyggalag.repository.FileRepository;
import ru.lgtu.jyggalag.service.UserService;
import ru.lgtu.jyggalag.utils.PasswordUtils;

import java.io.File;
import java.io.IOException;

/**
 * Инициализация системы при запуске (создание файлов, дефолтных пользователей и игр)
 */

public class BootService {
    private final String usersFilePath;
    private final UserService userService;

    public BootService(ConfigService configService, UserService userService) {
        this.usersFilePath = configService.getUsersJsonPath();
        this.userService = userService;
    }

    public void initSystem() {
        LogService.info("Запуск инициализации системы... - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");

        String[] filesToCheck = {
                usersFilePath,
        };

        for (String path : filesToCheck) {
            checkAndCreateFile(path);
        }

        if (checkUsers().isSuccess()) {
            LogService.info("Проверка пользователей завершена успешно.");
        } else {
            LogService.error(checkUsers().getErrorMessage());
        }

        LogService.info("Инициализация завершена.");
    }

    private void checkAndCreateFile(String filePath) {
        File file = new File(filePath);

        try {
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }

            if (!file.exists()) {
                if (file.createNewFile()) {
                    LogService.info("Файл создан: {}", filePath);
                }
            }

        } catch (IOException e) {
            LogService.error("Ошибка при создании файла {}: {}", filePath, e);
        }
    }

    private OperationResult<Void> checkUsers() {
        if (!userService.isUserExist(0)) {
            return userService.addUser(new User(0, "test", PasswordUtils.hashPassword("test")));
        }
        return OperationResult.success(null);
    }


}