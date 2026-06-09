package ru.lgtu.jyggalag.utils;

import org.mindrot.jbcrypt.BCrypt;
/**
 * Работа с паролями
 */
public class PasswordUtils {

    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public static boolean checkPassword(String password, String hashed) {
        return BCrypt.checkpw(password, hashed);
    }

}
