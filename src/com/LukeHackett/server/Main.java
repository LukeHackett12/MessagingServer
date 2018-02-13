package com.LukeHackett.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Hashtable;

public class Main {

    static Socket client = null;
    static ServerSocket server = null;

    public static long id = 0;
    public static Hashtable<Long, PublicKey> clients = new Hashtable<Long, PublicKey>();

    public static void main(String[] args) throws IOException {
        System.out.println("Server is starting...");
        System.out.println("Server is listening...");

        startServer();

        boolean done = false;
        while (!done) {
            client = server.accept();
            keyExchange();
            System.out.print("Client Connected");


            DataInputStream dIn = new DataInputStream(client.getInputStream());

            int length = dIn.readInt();
            byte[] message = new byte[length];// read length of incoming message
            if (length > 0) {
                dIn.readFully(message, 0, message.length); // read the message
            }
        }
    }

    public static void startServer(){
        try {
            server = new ServerSocket(5050);
            System.out.println(server.toString());
        } catch (IOException ex) {
            System.out.println("Could not listen on port 5050");
            System.exit(-1);
        }
    }

    public static void keyExchange(){
        try {
            byte[] lenb = new byte[4];
            client.getInputStream().read(lenb, 0, 4);
            ByteBuffer bb = ByteBuffer.wrap(lenb);
            int len = bb.getInt();

            byte[] servPubKeyBytes = new byte[len];
            client.getInputStream().read(servPubKeyBytes);
            X509EncodedKeySpec ks = new X509EncodedKeySpec(servPubKeyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PublicKey pubKey = kf.generatePublic(ks);

            clients.put(id, pubKey);
        } catch (IOException e) {
            System.out.println("Error obtaining server public key 1.");
            System.exit(0);
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Error obtaining server public key 2.");
            System.exit(0);
        } catch (InvalidKeySpecException e) {
            System.out.println("Error obtaining server public key 3.");
            System.exit(0);
        }
    }
}
