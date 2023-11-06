import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {

    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 7890;

    public static void main(String[] args) {
        System.out.println("Untuk keluar, ketik 'q'");

        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT)) {
            BufferedReader keyboardReader = new BufferedReader(new InputStreamReader(System.in));

            System.out.print("Nickname: ");
            String nickname = keyboardReader.readLine();

            InputStream serverInput = socket.getInputStream();
            BufferedReader serverReader = new BufferedReader(new InputStreamReader(serverInput));
            OutputStream serverOutput = socket.getOutputStream();
            PrintWriter serverWriter = new PrintWriter(serverOutput, true);

            serverWriter.println(nickname);

            Thread readThread = new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = serverReader.readLine()) != null) {
                        if (serverMessage.startsWith("RESPONSE:")) {
                            System.out.println("Server response: " + serverMessage.substring(9));
                        } else {
                            System.out.println(serverMessage);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            readThread.start();

            String message;
            while (true) {
                message = keyboardReader.readLine();

                if (message == null || message.toLowerCase().equals("q")) {
                    break;
                }

                serverWriter.println(message);

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
