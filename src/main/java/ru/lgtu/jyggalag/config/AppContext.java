package ru.lgtu.jyggalag.config;

import ru.lgtu.jyggalag.repository.FileRepository;
import ru.lgtu.jyggalag.repository.NoteRepository;
import ru.lgtu.jyggalag.repository.TaskRepository;
import ru.lgtu.jyggalag.repository.UserRepository;
import ru.lgtu.jyggalag.service.*;

/**
 * Глобальный контекст приложения (Ручной Dependency Injection контейнер).
 * Инициализирует и хранит синглтоны всех сервисов.
 */
public class AppContext {
    private static AppContext instance;

    private ConfigService configService;
    private FileRepository fileRepository;

    private UserRepository userRepository;
    private TaskRepository taskRepository;
    private NoteRepository noteRepository;

    private UserService userService;
    private TaskService taskService;
    private AuthService authService;
    private NoteService noteService;

    private SessionService sessionService;
    private NavigationService navigationService;



    private AppContext() {
    }

    public static synchronized AppContext getInstance() {
        if (instance == null) {
            instance = new AppContext();
        }
        return instance;
    }


    public void initialize() {
        // 1. Базовая инфраструктура
        this.configService = new ConfigService();
        this.fileRepository = new FileRepository();

        // 2. Репозитории
        this.userRepository = new UserRepository(fileRepository, configService.getUsersJsonPath());
        this.taskRepository = new TaskRepository(fileRepository, configService.getBasePath());
        this.noteRepository = new NoteRepository(fileRepository, configService.getBasePath());

        // 3. Бизнес-сервисы
        this.userService = new UserService(userRepository);
        this.taskService = new TaskService(taskRepository);
        this.noteService = new NoteService(noteRepository);
        this.authService = new AuthService(userService);

        // 4. Системные UI сервисы
        this.sessionService = new SessionService();
        this.navigationService = new NavigationService();

        // 5. Сервис инициализации файлов (Boot)
        BootService bootService = new BootService(configService, userService);
        bootService.initSystem();
        
        // 6. Дебаг
        DebugService.initialize(taskService, noteService);
    }

    public ConfigService getConfigService() { return configService; }
    public FileRepository getFileRepository() { return fileRepository; }
    public UserRepository getUserRepository() { return userRepository; }
    public TaskRepository getTaskRepository() { return taskRepository; }
    public NoteRepository getNoteRepository() { return noteRepository;}
    public UserService getUserService() { return userService; }
    public TaskService getTaskService() { return taskService; }
    public NoteService getNoteService() { return noteService; }
    public AuthService getAuthService() { return authService; }
    public SessionService getSessionService() { return sessionService; }
    public NavigationService getNavigationService() { return navigationService; }
}