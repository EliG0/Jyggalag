package ru.lgtu.jyggalag.core;

import ru.lgtu.jyggalag.config.AppContext;
import java.util.List;
import java.util.Random;

public class ComplementGenerator {
    static final String complimentsPath = "src/main/resources/ru/lgtu/jyggalag/data/compliments.json";


    public static String generate() {
        var result = AppContext.getInstance().getFileRepository().loadJsonList(complimentsPath, String[].class);

        if (result.isSuccess() && !result.getData().isEmpty()) {
            List<String> list = result.getData();
            return list.get(new Random().nextInt(list.size()));
        }

        return "Ты справляешься лучше, чем думаешь)";
    }

}