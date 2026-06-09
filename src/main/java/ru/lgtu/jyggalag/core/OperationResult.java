package ru.lgtu.jyggalag.core;

public class OperationResult<T> {
    private final boolean success;
    private final T data;
    private final String errorMessage;

    private OperationResult(boolean success, T data, String errorMessage) {
        this.success = success;
        this.data = data;
        this.errorMessage = errorMessage;
    }

    public static <T> OperationResult<T> success(T data) {
        return new OperationResult<>(true, data, null);
    }

    public static <T> OperationResult<T> failure(String message) {
        return new OperationResult<>(false, null, message);
    }

    public boolean isSuccess() { return success; }
    public T getData() { return data; }
    public String getErrorMessage() { return errorMessage; }
}