package Chat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientHandler {
    private final MyServer myServer;
    private final Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String name;
    //Пул потоков для обмена данными с сервером. Статический, т.к. для каждого клиент хэндлера поток свой,
    //а пул один на всех. Количество клиентов заранее мы не знаем, поэтому CachedThreadPool.
    private static final ExecutorService executorService = Executors.newCachedThreadPool();
    private static final Logger LOGGER = LogManager.getLogger(ClientHandler.class);

    public String getName() {
        return name;
    }

    /**
     * Остановка пула потоков обмена данными с сервером
     */
    public static void shutdownExecutor() { executorService.shutdown(); }

    public ClientHandler(MyServer myServer, Socket socket) {
        this.myServer = myServer;
        this.socket = socket;
        this.name = "";

        LOGGER.debug("Создание нового клиентского интерфейса...");
        try {
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            //Вместо вызова потока теперь передаём задачу на выполнение в пул потоков
            executorService.execute(()-> {
                try {
                    authenticate();
                    readMessages();
                } catch (IOException ex) {
                    LOGGER.error("Ошибка исполнения клиентского интерфейса: " + ex.getMessage());
                } finally {
                    closeConnection();
                    LOGGER.debug("Клиентский интерфейс " + socket + " закрыт.");
                }
            });
        } catch (IOException ex) {
            LOGGER.error("Ошибка создания клиентского интерфейса: " + ex.getMessage());
        }
    }

    private void alterNick(String newNick) {
        // Если такого ника в БД пользователей нет, то производим смену ника
        if (!myServer.getAuthService().isNickExist(newNick)) {
            if (myServer.getAuthService().changeNick(name, newNick)) {
                LOGGER.debug("Клиент " + name + ": успешная смена ника на " + newNick);
                // Рассылаем всем в чате сообщение о том, что у пользователя теперь новый ник
                myServer.broadcast(name + " now is " + newNick, true);
                sendMsg("Your new nick is " + newNick);
                // И непосредственно самому объекту пользователь меняем ник
                name = newNick;
            } else {
                LOGGER.error("Клиент " + name + ": ошибка смены ника на " + newNick);
                sendMsg("Error of change nick. Try again");
            }
            // Если ник уже занят, сообщаем пользователю об этом
        } else {
            LOGGER.error("Клиент " + name + ": запрос на смену ника отклонён - ник " + newNick + " уже используется");
            sendMsg("Nick " + newNick + " is already in use. Enter another nick.");
        }
    }

    /**
     * Обработка служебных сообщений - тех, которые начинаются с символа "/"
     * @param message Входящее сообщение
     */
    private boolean serviceMessageProcessing(String message) {
        String[] parts = message.split("\\s");
        String command = parts[0].substring(1);
        switch (command){
            case "w": { //Отправка приватного сообщения
                String realMessage = message.substring(message.indexOf(" ", message.indexOf(" ") + 1));
                LOGGER.debug("Клиент " + name + ": запрос на отправку приватного сообщения пользователю " +
                        parts[1] + ": " + realMessage);
                myServer.sendDirect(parts[1],name + ": " + realMessage);
                return false;
            } case "alternick": { //Смена ника
                LOGGER.debug("Клиент " + name + ": запрос на смену ника на " + parts[1]);
                alterNick(parts[1]);
                return false;
            } case "end": { //Выход из чата
                LOGGER.debug("Клиент " + name + ": команда на выход из чата");
                sendMsg("/end");
                closeConnection();
                return true;
            } default: {
                LOGGER.warn("Клиент " + name + ": введена некорректная команда: " + parts[0]);
                sendMsg("Incorrect command. Enter correct command or write message not beginning from \"/\"");
                return false;
            }
        }
    }

    private void closeConnection() {
        myServer.unsubscribe(this);
        myServer.broadcast("User " + name + " left", true);
        try {
            in.close();
            LOGGER.debug("  Клиент " + name + ": входной поток закрыт.");
            out.close();
            LOGGER.debug("  Клиент " + name + ": выходной поток закрыт.");
            socket.close();
            LOGGER.debug("  Клиент " + name + ": соединение закрыто.");
        } catch (IOException ex) {
            LOGGER.error("Клиент " + name + ": ошибка при закрытии соединения: " + ex.getMessage());
        }
    }

    private void readMessages() throws IOException {
        while (true) {
            if (in.available()>0) {
                String message = in.readUTF();
                LOGGER.info("Клиент " + name + ": " + message);
                if (message.startsWith("/")) {
                    if (serviceMessageProcessing(message)) { break; }
                } else {
                    myServer.broadcast(name + ": " + message, true);
                }
            }
        }
    }

    private void authenticate() throws IOException {
        while(true) {
            if (in.available()>0){
                String str = in.readUTF();
                if (str.startsWith("/auth")) {
                    String[] parts = str.split("\\s");
                    LOGGER.debug("Клиент " + socket + ": запрос авторизации с ником " + parts[1] +
                            " и паролем " + parts[2]);
                    String nick = myServer.getAuthService().getNickByLoginAndPwd(parts[1], parts[2]);
                    if (nick != null) {
                        if (!myServer.isNickLogged(nick)) {
                            LOGGER.info("Клиент " + socket + " успешно прошёл авторизацию как "+ nick);
                            name = nick;
                            sendMsg("/authOk " + nick);
                            myServer.broadcast(nick + " is in chat", true);
                            myServer.subscribe(this);
                            return;
                        } else {
                            sendMsg("User " + parts[1] + " already logged in");
                        }
                    } else {
                        LOGGER.warn("Клиент " + socket + ": неверный логин или пароль.");
                        sendMsg("Incorrect login attempted: wrong login/password");
                    }
                }
            }

        }
    }

    public void sendMsg(String s) {
        try {
            out.writeUTF(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
