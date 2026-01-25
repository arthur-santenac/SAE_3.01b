import java.io.*;
import java.net.*;
import java.security.*;
import java.security.spec.*;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Scanner;
import java.util.Arrays;

public class Client {
    private static SecretKeySpec secretKey;

    public static void main(String[] args) {
        client("localhost", 5555);
    }

    public static void client(String host, int port) {
        try (Socket socket = new Socket(host, port);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            Scanner scanner = new Scanner(System.in)) {
                
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
            kpg.initialize(new ECGenParameterSpec("secp256r1"));
            KeyPair keyPair = kpg.generateKeyPair();

            byte[] publicKeyBytes = keyPair.getPublic().getEncoded();
            String publicKeyBase64 = Base64.getEncoder().encodeToString(publicKeyBytes);
            
            out.println("sync " + publicKeyBase64); 
            
            in.readLine();
    
            String serverCle = in.readLine(); 
            
            String serverPublicKeyBase64 = serverCle.substring(5);
            
            byte[] serverPublicKeyBytes = Base64.getDecoder().decode(serverPublicKeyBase64);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(serverPublicKeyBytes);
            PublicKey serverPublicKey = keyFactory.generatePublic(x509KeySpec);

            KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH");
            keyAgreement.init(keyPair.getPrivate());
            keyAgreement.doPhase(serverPublicKey, true);
            byte[] sharedSecret = keyAgreement.generateSecret();

            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = Arrays.copyOf(sha256.digest(sharedSecret), 16);
            secretKey = new SecretKeySpec(keyBytes, "AES");

            Thread listener = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String message;
                        while ((message = in.readLine()) != null) {
                            System.out.println(dechiffrer(message));
                        }
                    } catch (Exception e) {
                        System.err.println("Connexion interrompue.");
                    }
                }
            });
            listener.start();
            boolean quitter = false;
            while (!quitter && scanner.hasNextLine()) {
                String line = scanner.nextLine();
                try {
                    out.println(chiffrer(line));
                    if (line.trim().equals("quit")) {
                        quitter = true;
                    }
                } catch (Exception e) {
                    System.err.println("Erreur de chiffrement : " + e.getMessage());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur client : " + e.getMessage());
        }
    }

    private static String chiffrer(String data) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    private static String dechiffrer(String encryptedDataBase64) throws Exception {
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedDataBase64);
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes);
    }
}