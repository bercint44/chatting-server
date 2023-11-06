import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

//ini program server
public class Server {
    private static final int PORT = 7890;
    private static Set<PrintWriter> clientWriters = new HashSet<>();

    public static void main(String[] args) {
        System.out.println("Server Aktif");

        try (ServerSocket listener = new ServerSocket(PORT)) {
            while (true) {
                new ClientHandler(listener.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private String nickname;
        private boolean inCMDMode = false;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                OutputStream output = socket.getOutputStream();
                out = new PrintWriter(output, true);

                synchronized (clientWriters) {
                    clientWriters.add(out);
                }

                nickname = reader.readLine();

                String message;
                while ((message = reader.readLine()) != null) {
                    if (inCMDMode) {
                        if (message.equalsIgnoreCase("exit")) {
                            inCMDMode = false;
                            out.println("Keluar dari mode CMD. Silakan kirim pesan biasa lagi.");
                        } else {
                            executeCommand(message);
                        }
                    } else {
                        if (message.equalsIgnoreCase("CMD")) {
                            inCMDMode = true;
                            out.println("Masukkan perintah yang akan dieksekusi:");
                        } else {
                            System.out.println(nickname + " ==> " + message);
                            broadcastMessage(nickname + ": " + message);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                synchronized (clientWriters) {
                    clientWriters.remove(out);
                }
            }
        }

        private void executeCommand(String command) {
            try {
                if (System.getProperty("os.name").startsWith("Windows")) {
                    command = "cmd /c " + command;
                }
                Process process = Runtime.getRuntime().exec(command);
                BufferedReader commandOutput = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = commandOutput.readLine()) != null) {
                    out.println("RESPONSE: " + line);
                }
            } catch (IOException e) {
                out.println("Gagal mengeksekusi perintah.");
                e.printStackTrace();
            }
        }

        private void broadcastMessage(String message) {
            synchronized (clientWriters) {
                for (PrintWriter writer : clientWriters) {
                    if (writer != out) {
                        writer.println(message);
                    }
                }
            }
        }
    }
}
