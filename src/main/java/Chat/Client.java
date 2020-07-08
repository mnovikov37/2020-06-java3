package Chat;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

import static java.lang.System.in;

public class Client {
    private static boolean authorized = false;
    private static Logger logger = new Logger();

    public Logger getLogger() { return logger; }

    public static void main(String[] args) throws IOException {
        Socket socket = null;
        try {
            socket = new Socket("localhost", 8189);
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            BufferedReader consoleIn = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Enter your login:");
            String login = consoleIn.readLine();
            System.out.println("Enter your password:");
            String password = consoleIn.readLine();
            out.writeUTF("/auth " + login + " " + password);
            logger.write("Login attempt...");

            Thread t = new Thread(() -> {
                try {
                    while (true) {
                        if(in.available()>0) {
                            String strFromServer = in.readUTF();
                            if (strFromServer.startsWith("/authOk")) {
                                authorized = true;
                                String[] parts = strFromServer.split("\\s");
                                System.out.println("Authorized on server. Your nick: " + parts[1]);
                                logger.write("Authorization successfull. Nick: " + parts[1]);
                                logger.read(100);
                                Client.runOutputThread(out);
                                break;
                            }
                            System.out.println(strFromServer);
                            logger.write(strFromServer);
                        }
                    }
                    while (true) {
                        if (in.available()>0) {
                            String strFromServer = in.readUTF();
                            if (strFromServer.startsWith("/yournick")) {
                                String[] parts = strFromServer.split("\\s");
                                System.out.println("Nick is changed. Your nick: " + parts[1]);
                                logger.write("NIck is changed. New nick: " + parts[1]);
                            }
                            if (strFromServer.equalsIgnoreCase("/end")) {
                                logger.write("Disconnect from server");
                                logger.stop();
                                break;
                            } else {
                                System.out.println(strFromServer);
                                logger.write(strFromServer);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            t.start();
            t.join();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            socket.close();
        }
    }

    private static Thread runOutputThread(DataOutputStream out) {
        Thread thread = new Thread(()-> {
            Scanner scanner = new Scanner(in);
            while (true) {
                String message = scanner.nextLine();
                try {
                    out.writeUTF(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (message.equals("/end")) {
                    scanner.close();
                    break;
                }
            }
        });
        thread.start();
        return thread;
    }
}
