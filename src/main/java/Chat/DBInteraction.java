package Chat;

import java.sql.*;

/**
 * Класс для манипуляций с базой данных вручную, к основному заданию отношения не имеет.
 */
public class DBInteraction {
    public static Connection connection;
    public static Statement statement;
    public static ResultSet resultSet;

    public static void main(String[] args) {
        connection = null;
//        try {
//            Class.forName("org.sqlite.JDBC");
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:chat.s3db");
            System.out.println("Подключение к БД выполнено");
            statement = connection.createStatement();
//            statement.execute("DROP TABLE moderation");
//            statement.execute("CREATE TABLE moderation(id INTEGER PRIMARY KEY, badword TEXT, goodword TEXT)");
//            statement.execute("INSERT INTO moderation (badword, goodword) VALUES" +
//                    "('кокс','кабель коаксиальный')," +
//                    "('рыжик','коннектор RJ-45')," +
//                    "('клопы','соединители Scotch-lock')," +
//                    "('витуха','кабель UTP-5E')," +
//                    "('эфка','F-коннектор обжимной')," +
//                    "('тоник','генератор тонового сингала')," +
//                    "('дракон','устройство затяжки кабеля')," +
//                    "('тупарик','неуправляемый коммутатор Ethernet')," +
//                    "('медик','медиаконвертор')");
            resultSet = statement.executeQuery("SELECT * FROM moderation");
//            System.out.println(resultSet.getMetaData());
            int columnCount = resultSet.getMetaData().getColumnCount();
            for (int i = 0; i < columnCount; i++) {
                System.out.print(String.format("%" + 20 + "s", resultSet.getMetaData().getColumnLabel(i+1)));
            }
            System.out.println("");
            while (resultSet.next()) {
                for (int i = 0; i < columnCount; i++) {
                    System.out.print(String.format("%" + 20 + "s", resultSet.getString(i+1)));
                }
                System.out.println("");
            }

            resultSet.close();
            statement.close();
            connection.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
