package com.LukeHackett.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class WorkerRunnable implements Runnable{
    public static Socket clientServerSocket;
    private Socket clientListenerSocket;

    WorkerRunnable(Socket clientServerSocket) throws IOException {
        WorkerRunnable.clientServerSocket = clientServerSocket;
        clientListenerSocket = Main.server.accept();
    }

    public void run(){
        System.out.println("Client " + Main.id + " Connecting...");
        keyExchange();

        KeyDistributor k = new KeyDistributor(clientServerSocket);
        Thread t = new Thread(k);
        t.start();

        boolean done = false;
        while(!done){
            try {
                rerouteMessage(clientListenerSocket);
            } catch (IOException e) {
                e.printStackTrace();
                done = true;
            }
        }
    }

    private void keyExchange(){
        try {
            //read public key from client
            byte[] lenb = new byte[4];
            clientListenerSocket.getInputStream().read(lenb, 0, 4);
            ByteBuffer bb = ByteBuffer.wrap(lenb);
            int len = bb.getInt();

            byte[] servPubKeyBytes = new byte[len];
            clientListenerSocket.getInputStream().read(servPubKeyBytes);
            X509EncodedKeySpec ks = new X509EncodedKeySpec(servPubKeyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PublicKey pubKey = kf.generatePublic(ks);

            //Assign client unique id
            DataOutputStream dOut = new DataOutputStream(clientListenerSocket.getOutputStream());
            dOut.writeLong(Main.id);

            //Add client to server data
            System.out.println("Successfully obtained key of Client " + Main.id);
            Main.clientsKeys.put(Main.id, pubKey);
            Main.clientListenerSockets.put(Main.id++, clientListenerSocket);

        } catch (IOException e) {
            System.out.println("Error obtaining public key 1.");
            System.exit(0);
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Error obtaining public key 2.");
            System.exit(0);
        } catch (InvalidKeySpecException e) {
            System.out.println("Error obtaining public key 3.");
            System.exit(0);
        }
    }

    private void rerouteMessage(Socket clientListenerSocket) throws IOException {
        DataInputStream dIn = new DataInputStream(clientListenerSocket.getInputStream());
        long end = dIn.readLong();
        long initial = dIn.readLong();
        Message message = new Message(initial, end, new byte[128]);

        //Read in the encrypted message
        int length = dIn.readInt();
        if (length > 0) {
            dIn.readFully(message.message, 0, message.message.length);
        }

        DataOutputStream dOut = new DataOutputStream(Main.clientListenerSockets.get(end).getOutputStream());
        dOut.writeInt(length);
        dOut.write(message.message);
        System.out.println("Message sent to Client" + end + " from Client" + initial);
    }
}
