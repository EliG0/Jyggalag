package ru.lgtu.jyggalag.repository;

import com.google.gson.*;
import ru.lgtu.jyggalag.core.LogService;
import ru.lgtu.jyggalag.core.OperationResult;
import ru.lgtu.jyggalag.utils.JsonUtils;
import ru.lgtu.jyggalag.utils.TomlUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class FileRepository {
    private final Gson gson = JsonUtils.getGson();

    private String loadRawString(String filePath) throws IOException {
        File file = ensureFileExists(filePath);
        if (file.length() == 0) return "";
        return Files.readString(file.toPath());
    }

    private void saveRawString(String filePath, String content) throws IOException {
        File file = ensureFileExists(filePath);

        Files.writeString(file.toPath(), content);
    }

    private File ensureFileExists(String filePath) throws IOException {
        File file = new File(filePath);

        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }

        if (!file.exists()) {
            LogService.warn("FileRepository | ensureFileExist | File not found: " + filePath);
            Files.createFile(file.toPath());
            Files.writeString(file.toPath(), "[]");
            LogService.info("FileRepository | ensureFileExist | Файл данных успешно создан: " + file.toPath());
        }

        return file;
    }

    /**
     * Загрузка JSON Списков теперь возвращает OperationResult
     */
    public <T> OperationResult<List<T>> loadJsonList(String filePath, Class<T[]> arrayClass) {
        try {
            String json = loadRawString(filePath);
            if (json.isEmpty()) {
                return OperationResult.success(new ArrayList<>());
            }

            T[] array = gson.fromJson(json, arrayClass);
            List<T> list = (array != null) ? new ArrayList<>(Arrays.asList(array)) : new ArrayList<>();
            return OperationResult.success(list);

        } catch (IOException | JsonSyntaxException e) {
            return OperationResult.failure("Ошибка загрузки JSON [" + filePath + "]: " + e.getMessage());
        }
    }

    /**
     * Сохранение JSON Списков
     */
    public <T> OperationResult<Void> saveJsonList(String filePath, List<T> data) {
        try {
            String json = gson.toJson(data);
            saveRawString(filePath, json);
            return OperationResult.success(null);
        } catch (IOException e) {
            return OperationResult.failure("Не удалось сохранить JSON в " + filePath + ": " + e.getMessage());
        }
    }

    /**
     * Загрузка TOML конфигурации
     */
    public <T> OperationResult<T> loadTomlConfig(String filePath, Class<T> clazz) {
        try {
            String tomlContent = loadRawString(filePath);
            T config = TomlUtils.parse(tomlContent, clazz);
            return OperationResult.success(config);
        } catch (Exception e) {
            return OperationResult.failure("Ошибка парсинга TOML [" + filePath + "]: " + e.getMessage());
        }
    }

    /**
     * Сохранение TOML конфигурации
     */
    public <T> OperationResult<Void> saveTomlConfig(String filePath, T data) {
        try {
            String tomlContent = TomlUtils.toTomlString(data);
            saveRawString(filePath, tomlContent);
            return OperationResult.success(null);
        } catch (Exception e) {
            return OperationResult.failure("Не удалось сохранить TOML в " + filePath + ": " + e.getMessage());
        }
    }
}