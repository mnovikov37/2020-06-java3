package Chat;

import Chat.auth.AuthenticationService;
import Chat.auth.DatabaseAuthService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MyServer {
    private final int PORT = 8189;
    private List<ClientHandler> clients;
    private AuthenticationService authService;
    private Moderator moderator;
    private static final Logger LOGGER = LogManager.getLogger(MyServer.class);

    public AuthenticationService getAuthService() {
        return authService;
    }

    public MyServer() {
        LOGGER.debug("Запуск сервера...");
        try (ServerSocket server = new ServerSocket(PORT)) {
            LOGGER.debug("Запуск сервиса авторизации...");
            authService = new DatabaseAuthService();
            authService.start();
            LOGGER.debug("Запуск сервиса модерации...");
            moderator = new Moderator();
            LOGGER.debug("Сервис модерации запущен.");
            clients = new ArrayList<>();
            LOGGER.debug("Сервер запущен.");
            while (true) {
                LOGGER.debug("Ожидание подключения клиента...");
                Socket socket = server.accept();
                new ClientHandler(this, socket);
                LOGGER.debug("Клиентское подключение создано.");
            }
        } catch (IOException ex) {
            LOGGER.error("Ошибка сервера: " + ex.getMessage());
        } finally {
            if(authService!=null) {
                authService.stop();
                LOGGER.debug("Cервис авторизации остановлен.");
                //При завершении работы сервера отключаем пул потоков для клиентских интерфейсов
                ClientHandler.shutdownExecutor();
                LOGGER.debug("Клиентские интерфейсы закрыты.");
            }
        }
    }


    public synchronized void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        LOGGER.info("Клиент " + clientHandler.getName() + " отключен.");
        broadcastClientsList();
    }

    public synchronized void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
        LOGGER.info(clientHandler.getName() + " внесён в список клиентов.");
        broadcastClientsList();
    }

    public synchronized void broadcast(String s, boolean addTime) {
        if (addTime) s += " @" + LocalTime.now().format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT));
        s = moderator.moderate(s); // Пропускаем сообщение перед рассылкой через модератор
        LOGGER.info("Отправка всем: " + s);
        for(ClientHandler client: clients) {
            client.sendMsg(s);
            LOGGER.debug("  Клиенту " + client.getName() + ": " + s);
        }
        LOGGER.debug("Завершена отправка всем: " + s);
    }

    public synchronized void broadcastClientsList() {
        StringBuilder sb = new StringBuilder("/clients ");
        for (ClientHandler o : clients) {
            sb.append(o.getName()).append(" ");
        }
        broadcast(sb.toString(), false);
        LOGGER.debug("Актуальный список клиентов разослан.");
    }

    public synchronized boolean isNickLogged(String nick) {
        for(ClientHandler client: clients) {
            if (client.getName().equals(nick)) {
                LOGGER.warn("Клиент " + nick + " пытается залогиниться повторно.");
                return true;
            }
        }
        return false;
    }

    public void sendDirect(String nick, String message) {
        for (ClientHandler client: clients) {
            if (client.getName().equals(nick)) {
                message = moderator.moderate(message);
                client.sendMsg("(direct) " + message + "@" +
                        LocalTime.now().format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)));
                LOGGER.info("Отправка приватно пользователю " + nick + ": " + message);
                return;
            }
        }
        LOGGER.warn("Попытка отправки приватного сообщения несуществующему пользователю " + nick);
    }
}