package com.LukeHackett.server;

import java.io.DataInputStream;;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.PublicKey;

public class KeyDistributor implements Runnable{

    Socket clientServerSocket;

    KeyDistributor(Socket clientServerSocket){
        this.clientServerSocket = clientServerSocket;
    }

    public void run(){
        boolean done = false;
        while(!done) {
            try {
                sendKey();
            } catch (IOException e) {
                done = true;
                e.printStackTrace();
            }
        }
    }

    private void sendKey() throws IOException {
            DataInputStream dIn = new DataInputStream(clientServerSocket.getInputStream());
            long recipientID = dIn.readLong();

            PublicKey recipientKey = Main.clientsKeys.get(recipientID);
            ByteBuffer bb = ByteBuffer.allocate(4);
            bb.putInt(recipientKey.getEncoded().length);
            clientServerSocket.getOutputStream().write(bb.array());
            clientServerSocket.getOutputStream().write(recipientKey.getEncoded());
            clientServerSocket.getOutputStream().flush();

    }

}
