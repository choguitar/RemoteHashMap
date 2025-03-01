package Server;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


class Config {
    static final String CLIENT_DIR = "client";
    static final String SERVER_DIR = "server";
    static final int SERVER_PORT = 8000;
}

class Server {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(Config.SERVER_PORT)) {
            System.out.println("Server started...");
            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     DataInputStream dis = new DataInputStream(clientSocket.getInputStream())) {
                    
                    String fileName = dis.readUTF();
                    long fileSize = dis.readLong();
                    File file = new File(Config.SERVER_DIR, fileName);
                    
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while (fileSize > 0 && (bytesRead = dis.read(buffer, 0, (int)Math.min(buffer.length, fileSize))) != -1) {
                            fos.write(buffer, 0, bytesRead);
                            fileSize -= bytesRead;
                        }
                    }
                    System.out.println("Received: " + fileName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}