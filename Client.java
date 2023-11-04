// Client.java

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {

    // Alamat server yang akan dikoneksi
    private static final String SERVER_ADDRESS = "localhost";
    // Port server yang akan dikoneksi
    private static final int SERVER_PORT = 7890;

    public static void main(String[] args) {
        System.out.println("untuk keluar ketik q");
        
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT)) {
            BufferedReader keyboardReader = new BufferedReader(new InputStreamReader(System.in));
            
            System.out.print("Nickname: ");
            String nickname = keyboardReader.readLine();
			

            // Membuka input stream dan output stream untuk berkomunikasi dengan server
            InputStream serverInput = socket.getInputStream();
            BufferedReader serverReader = new BufferedReader(new InputStreamReader(serverInput));
            OutputStream serverOutput = socket.getOutputStream();
            PrintWriter serverWriter = new PrintWriter(serverOutput, true);

            // Mengirimkan nickname ke server
            serverWriter.println(nickname);

            // Thread untuk membaca pesan dari server
            Thread readThread = new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = serverReader.readLine()) != null) {
                        // Menampilkan pesan yang diterima dari server
                        System.out.println(serverMessage);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            readThread.start();

            String message;
            while (true) {
                // Membaca pesan dari keyboard
                message = keyboardReader.readLine();
				
                if (message == null || message.toLowerCase().equals("q")) {
                    break;
                }
					serverWriter.println(message);
            }
            
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
