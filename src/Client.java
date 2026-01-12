import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        client("localhost", 5556);
    }

    public static void client(String host, int port) {
        try (Socket socket = new Socket(host, port);
            final BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            Scanner scanner = new Scanner(System.in)) {
            Thread listener = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String serverMessage;
                        while ((serverMessage = in.readLine()) != null) {
                            System.out.println(serverMessage);
                        }
                    } catch (IOException e) {}
                }
            });
            
            listener.start();

            boolean quitter = false;

            while (!quitter && scanner.hasNextLine()) {
                String line = scanner.nextLine();
                out.println(line);

                if (line.trim().equals("quit")) {
                    quitter = true;
                }
            }
            
        } catch (IOException e) {
            System.err.println("Erreur client : " + e.getMessage());
        }
    }
}