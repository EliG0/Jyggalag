package ru.lgtu.jyggalag.utils;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;

/**
 * Утилитарный класс для сериализации и десериализации данных в формате TOML.
 * Использует библиотеку toml4j.
 */
public final class TomlUtils {
    private static final TomlWriter TOML_WRITER = new TomlWriter();
    private TomlUtils() {
    }

    /**
     * Превращает сырую строку TOML в съедобный Java-объект.
     *
     * @param tomlContent строка с содержимым файла meta.toml
     * @param clazz       класс, в который нужно распарсить данные (например, InstalledMod.class)
     * @return готовый объект с заполненными полями
     */

    public static <T> T parse(String tomlContent, Class<T> clazz) {
        if (tomlContent == null || tomlContent.isBlank()) {
            try {
                // Если файл пустой, возвращаем дефолтный пустой объект класса
                return clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Не удалось создать пустой экземпляр класса " + clazz.getName(), e);
            }
        }

        return new Toml().read(tomlContent).to(clazz);
    }

    /**
     * Превращает любой Java-объект в красивую строку формата TOML для записи на диск.
     *
     * @param data объект с данными (например, экземпляр InstalledMod)
     * @return строка, готовая к сохранению в файл
     */

    public static <T> String toTomlString(T data) {
        if (data == null) {
            return "";
        }
        return TOML_WRITER.write(data);
    }
}