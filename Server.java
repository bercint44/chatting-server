// Server.java

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class Server {

    // Menentukan port yang akan digunakan oleh server
    private static final int PORT = 7890;

    // Membuat Set untuk menyimpan PrintWriter dari setiap client yang terhubung
    private static Set<PrintWriter> clientWriters = new HashSet<>();

    public static void main(String[] args) {
        System.out.println("Server Aktif");
        
        try (ServerSocket listener = new ServerSocket(PORT)) {
            while (true) {
                // Menerima koneksi dari client baru dan menjalankan thread ClientHandler untuk menghandle koneksi tersebut
                new ClientHandler(listener.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Kelas ClientHandler yang merupakan turunan dari Thread dan digunakan untuk menghandle koneksi dengan setiap client
    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private String nickname;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                // Membuka input stream dan output stream untuk berkomunikasi dengan client
                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                OutputStream output = socket.getOutputStream();
                out = new PrintWriter(output, true);

                synchronized (clientWriters) {
                    // Menambahkan PrintWriter ke dalam Set clientWriters agar dapat digunakan untuk broadcast pesan
                    clientWriters.add(out);
                }

                // Membaca nickname dari client
                nickname = reader.readLine();
                
                String message;
                while ((message = reader.readLine()) != null) {
                    // Menampilkan pesan yang diterima dari client dan melakukan broadcast ke semua client yang terhubung
                    System.out.println(nickname + "==> " + message);
                    broadcastMessage(nickname + ": " + message);
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
                    // Menghapus PrintWriter dari Set clientWriters setelah client terputus
                    clientWriters.remove(out);
                }
            }
        }

        private void broadcastMessage(String message) {
            synchronized (clientWriters) {
                // Mengirimkan pesan ke semua client yang terhubung, kecuali client pengirim pesan tersebut
                for (PrintWriter writer : clientWriters) {
                    if (writer != out) {
                        writer.println(message);
                    }
                }
            }
        }
    }
}
