package Chat.auth;

public interface AuthenticationService {
    void start();
    void stop();
    String getNickByLoginAndPwd(String login, String password);
    boolean isNickExist(String nick);
    boolean changeNick (String oldNick, String newNick);
}
