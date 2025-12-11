import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {
        client("localhost", 5557);
    }

    public static void client(String host, int port) {
        try (Socket socket = new Socket(host, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {

            boolean quitter = false;

            while (!quitter && scanner.hasNextLine()) {
                String line = scanner.nextLine();
                out.println(line);
            }
            
        } catch (IOException e) {
            System.err.println("Erreur client : " + e.getMessage());
        }
    }
}