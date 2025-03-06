package Client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    private final String serverAddress;
    private final int port;

    public Client(String address, int port) {
        this.serverAddress = address;
        this.port = port;
    }

    public boolean put(String key, String value, int ttl) throws Exception {
        try (Socket socket = new Socket(serverAddress, port);
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
             DataInputStream dis = new DataInputStream(socket.getInputStream())) {

        	dos.writeUTF("PUT");

        	byte[] keyBytes = key.getBytes();
            dos.writeInt(keyBytes.length);
            dos.write(keyBytes);

            byte[] valueBytes = value.getBytes();
            dos.writeInt(valueBytes.length);
            dos.write(valueBytes);
            
            dos.writeInt(ttl);

            return dis.readInt() == 0;
        }
    }

    public String get(String key) throws Exception {
        try (Socket socket = new Socket(serverAddress, port);
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
             DataInputStream dis = new DataInputStream(socket.getInputStream())) {
        	
        	dos.writeUTF("GET");

        	byte[] keyBytes = key.getBytes();
            dos.writeInt(keyBytes.length);
            dos.write(keyBytes);

            if (dis.readInt() == 0) {
                int valueLen = dis.readInt();
                
                byte[] valueBytes = new byte[valueLen];
                dis.readFully(valueBytes);
                
                return new String(valueBytes);
            }
            return null;
        }
    }

    public boolean remove(String key) throws Exception {
        try (Socket socket = new Socket(serverAddress, port);
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
             DataInputStream dis = new DataInputStream(socket.getInputStream())) {

        	dos.writeUTF("REMOVE");

            byte[] keyBytes = key.getBytes();
            dos.writeInt(keyBytes.length);
            dos.write(keyBytes);
        	
            return dis.readInt() == 0;
        }
    }

    public static void main(String[] args) {
        String serverAddress = "localhost";
        int serverPort = 8000;

        Client client = new Client(serverAddress, serverPort);
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("1. PUT");
            System.out.println("2. GET");
            System.out.println("3. REMOVE");
            System.out.println("4. FINISH");
            System.out.print("SELECT: ");
            
            int command = scanner.nextInt();
            scanner.nextLine();
            System.out.println();
            
            String key, value;
            int ttl;
            
            try {
                switch (command) {
                    case 1:
                        System.out.print("KEY: ");
                        key = scanner.nextLine();
                        
                        System.out.print("VALUE: ");
                        value = scanner.nextLine();
                        
                        System.out.print("TTL: ");
                        ttl = scanner.nextInt();
                        scanner.nextLine();
                        
                        if (client.put(key, value, ttl)) {
                            System.out.println("SUCCESS");
                        } else {
                            System.out.println("FAIL");
                        }
                        break;

                    case 2:
                        System.out.print("KEY: ");
                        key = scanner.nextLine();
                        
                        value = client.get(key);
                        
                        if (value != null) {
                            System.out.println("VALUE: " + value);
                        } else {
                            System.out.println("NOT FOUND");
                        }
                        break;

                    case 3:
                        System.out.print("KEY: ");
                        key = scanner.nextLine();
                        
                        if (client.remove(key)) {
                            System.out.println("SUCCESS");
                        } else {
                            System.out.println("FAIL");
                        }
                        break;

                    case 4:
                        System.out.println("FINISHED...");
                        scanner.close();
                        return;

                    default:
                        System.out.println("PLEASE INPUT THE CORRECT COMMAND");
                }
                System.out.println();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
