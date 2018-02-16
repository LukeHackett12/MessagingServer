package com.LukeHackett.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.util.Hashtable;

public class Main {

    public static ServerSocket server = null;

    public static long id = 0;
    public static Hashtable<Long, PublicKey> clientsKeys = new Hashtable<>();
    public static Hashtable<Long, Socket> clientListenerSockets = new Hashtable<>();

    public static void main(String[] args) throws IOException {
        System.out.println("Server is starting...");
        System.out.println("Server is listening...");

        startServer();

        while (true) {
            WorkerRunnable c = new WorkerRunnable(server.accept());
            Thread t = new Thread(c);
            t.start();
        }
    }

    private static void startServer(){
        try {
            server = new ServerSocket(5050);
            System.out.println(server.toString());
        } catch (IOException ex) {
            System.out.println("Could not listen on port 5050");
            System.exit(-1);
        }
    }
}
