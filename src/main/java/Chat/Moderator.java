package Chat;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Сервис модерации
 */
public class Moderator {
    private Map<String, String> dictionary; // Словарь "плохих слов" и их заменителей

    public Moderator() {
        this.dictionary = new HashMap<>();
        // Словарь заполняем значениями из базы данных
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:chat.s3db")) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT badword, goodword FROM moderation");
            while (resultSet.next()) {
                dictionary.put(resultSet.getString("badword"), resultSet.getString("goodword"));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    /**
     * Функция модерации
     * @param inputString входное не отредактированное сообщение
     * @return отредактированное сообщение: недопустимые слова заменяются каноничными согласно словарю
     */
    public String moderate(String inputString) {
        StringBuilder result = new StringBuilder();
        String[] parts = inputString.split("\\s");
        for (String s:parts) {
            String redactPart = dictionary.get(s);
            if (redactPart == null) {
                result.append(s).append(" ");
            } else { result.append(redactPart).append(" "); }
        }
        result.deleteCharAt(result.length()-1); // Удаление последнего пробела в строке
        return result.toString();
    }
}
