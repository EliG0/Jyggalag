package ru.lgtu.jyggalag.core;

import ru.lgtu.jyggalag.config.AppContext;

import java.time.LocalTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class GreetingsGenerator {
    static final String greetingsPath = "src/main/resources/ru/lgtu/jyggalag/data/greetings.json";

    public static class GreetingItem {
        private String tag;
        private String text;

        public String getTag() { return tag; }
        public String getText() { return text; }
    }

    public static String generate(String username) {
        var result = AppContext.getInstance().getFileRepository()
                .loadJsonList(greetingsPath, GreetingItem[].class);

        if (result.isSuccess() && !result.getData().isEmpty()) {
            List<GreetingItem> allGreetings = result.getData();

            String currentTag = getRandomTag();

            List<GreetingItem> matchingGreetings = allGreetings.stream()
                    .filter(item -> item.getTag().equalsIgnoreCase(currentTag))
                    .collect(Collectors.toList());

            if (matchingGreetings.isEmpty()) {
                matchingGreetings = allGreetings;
            }

            String rawGreeting = matchingGreetings.get(new Random().nextInt(matchingGreetings.size())).getText();
            return rawGreeting.formatted(username);
        }

        return "Добро пожаловать, " + username + "!";
    }

    private static String getRandomTag(){
        int choice = new Random().nextInt(2);

        return switch (choice) {
            case 0 -> "common";
            case 1 -> getCurrentTimeTag();
            default -> "common";
        };
    }


    /**
     * Метод определения тега в зависимости от часа
     */
    private static String getCurrentTimeTag() {
        int hour = LocalTime.now().getHour();

        if (hour >= 5 && hour < 12)  return "morning"; // с 5 утра до 11:59
        if (hour >= 12 && hour < 17) return "day";     // с 12 дня до 16:59
        if (hour >= 17 && hour < 21) return "evening"; // с 17 вечера до 21:59
        return "night";                                // с 22 вечера до 4:59 утра
    }

}
