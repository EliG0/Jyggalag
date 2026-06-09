package ru.lgtu.jyggalag.config;

import ru.lgtu.jyggalag.core.LogService;
import ru.lgtu.jyggalag.core.OperationResult;
import ru.lgtu.jyggalag.service.NoteService;
import ru.lgtu.jyggalag.service.TaskService;
import ru.lgtu.jyggalag.model.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Личная утилита для отладки и генерации демонстрационных данных
 */
public class DebugService {

    public static void initialize(TaskService taskService, NoteService noteService) {
        LogService.info("DebugService | initialize | Начало дебагинга и загрузки моков");

        loadMockTasks(taskService);
        loadMockNotes(noteService);

        LogService.info("DebugService | initialize | Тестовые данные успешно загружены!");
    }

    private static void loadMockTasks(TaskService taskService) {
        OperationResult<List<Task>> tasks = taskService.getAllTasks(0);
        if (!tasks.isSuccess()) {
            LogService.error("DebugService | loadMockTasks | Не удалось загрузить задачи для тестового пользователя: {}",
                    tasks.getErrorMessage());
            return;
        }

        List<Task> taskList = tasks.getData();
        if (taskList != null && taskList.isEmpty()) {
            Task t1 = new Task();
            t1.setId(1);
            t1.setTitle("Подготовка к защите курсача");
            t1.setStatus(TaskStatus.IN_PROGRESS);
            t1.setTags(Collections.singletonList("Учеба"));
            t1.setColor("ORANGE");
            t1.setDeadline(LocalDateTime.now().plusDays(2));

            Task t2 = new Task();
            t2.setId(2);
            t2.setTitle("Рефакторинг");
            t2.setStatus(TaskStatus.TODO);
            t2.setTags(Collections.singletonList("Разработка"));
            t2.setColor("BLUE");
            t2.setDeadline(LocalDateTime.now().plusHours(12));

            Task t3 = new Task();
            t3.setId(3);
            t3.setTitle("Спроектировать логическую схему базы данных приложения");
            t3.setStatus(TaskStatus.DONE);
            t3.setTags(Collections.singletonList("Проектирование"));
            t3.setColor("GREEN");
            t3.setDeadline(LocalDateTime.now().minusDays(1));

            Task t4 = new Task();
            t4.setId(4);
            t4.setTitle("Обновить документацию и файл README.md в репозитории на GitHub");
            t4.setStatus(TaskStatus.TODO);
            t4.setTags(Collections.singletonList("Курсовая"));
            t4.setColor("PURPLE");
            t4.setDeadline(LocalDateTime.now().plusDays(4));

            taskService.addTask(0, t1);
            taskService.addTask(0, t2);
            taskService.addTask(0, t3);
            taskService.addTask(0, t4);
        }
    }

    private static void loadMockNotes(NoteService noteService) {
        OperationResult<List<Note>> notesRes = noteService.getAllNotes(0);
        if (!notesRes.isSuccess()) {
            LogService.error("DebugService | loadMockNotes | Не удалось загрузить заметки для тестового пользователя: {}",
                    notesRes.getErrorMessage());
            return;
        }

        List<Note> noteList = notesRes.getData();
        if (noteList != null && noteList.isEmpty()) {
            Note n1 = new Note();
            n1.setId(1);
            n1.setTitle("Идеи для дипломного проекта");
            n1.setContent("Рассмотреть тему использования искусственного интеллекта и нейросетей для прогнозирования запасов медикаментов в больнице. Название проекта: Peryite. Стек: Python, TensorFlow, FastAPI.");
            n1.setTags(Collections.singletonList("Диплом"));

            Note n2 = new Note();
            n2.setId(2);
            n2.setTitle("Полезные ссылки JavaFX");
            n2.setTags(Collections.singletonList("Разработка"));

            Note n3 = new Note();
            n3.setId(3);
            n3.setTitle("План подготовки к сессии");
            n3.setContent("1. Сдать лабораторные.\n2. Отправить ссылку на репозиторий преподавателю до выходных.\n3. Повторить теоретические вопросы к экзамену.");
            n3.setTags(Collections.singletonList("Учеба"));

            noteService.addNote(0, n1);
            noteService.addNote(0, n2);
            noteService.addNote(0, n3);
        }
    }
}