package Client;

import java.io.*;
import java.net.*;
import java.util.*;

public class Client {
	private static final int serverPort = 8000;
	private String serverAddr;
	
	public Client(String serverAddr) {
		this.serverAddr = serverAddr;
	}
	
	public int put(String key, String value, Long TTL) throws Exception {
		DataOutputStream dos = null;
		DataInputStream dis = null;
		Socket socket = null;
		
		try {
			socket = new Socket(serverAddr, serverPort);
			dos = new DataOutputStream(socket.getOutputStream());
			dis = new DataInputStream(socket.getInputStream());
			
			dos.writeInt(1);
			dos.writeUTF(key);
			dos.writeUTF(value);
			dos.writeLong(TTL);
						
			return dis.readInt();
		} catch (Exception e) {
			throw new Exception(e);
		} finally {
			try {
				if (socket != null) socket.close();
				if (dos != null) dos.close();
				if (dis != null) dis.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public String get(String key) throws Exception {
		DataOutputStream dos = null;
		DataInputStream dis = null;
		Socket socket = null;
		
		try {
			socket = new Socket(serverAddr, serverPort);			
			dos = new DataOutputStream(socket.getOutputStream());
			dis = new DataInputStream(socket.getInputStream());
						
			dos.writeInt(2);
			dos.writeUTF(key);
			
			int res = dis.readInt();
			if (res == 1) {
				return dis.readUTF();
			} else {
				return null;
			}			
		} catch (Exception e) {
			throw new Exception(e);
		} finally {
			try {
				if (socket != null) socket.close();
				if (dos != null) dos.close();
				if (dis != null) dis.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public int remove(String key) throws Exception {
		DataOutputStream dos = null;
		DataInputStream dis = null;
		Socket socket = null;
		
		try {
			socket = new Socket(serverAddr, serverPort);
			dos = new DataOutputStream(socket.getOutputStream());
			dis = new DataInputStream(socket.getInputStream());
						
			dos.writeInt(3);
			dos.writeUTF(key);
			
			return dis.readInt();
		} catch (Exception e) {
			throw new Exception(e);
		} finally {
			try {
				if (socket != null) socket.close();
				if (dos != null) dos.close();
				if (dis != null) dis.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		Client client = new Client("localhost");
		Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Input (put / get / remove / exit)");
            String command = scanner.nextLine().trim();

            try {
                if (command.equals("put")) {
                    System.out.print("Key: ");
                    String key = scanner.nextLine().trim();
                    
                    System.out.print("Value: ");
                    String value = scanner.nextLine().trim();
                    
                    System.out.print("TTL: ");
                    long ttl = Long.parseLong(scanner.nextLine().trim());
                    
                    try {
                    	int res = client.put(key, value, ttl);
                    	if (res == 1) {
                    		System.out.println("SUCCESS");
                    	} else {
                    		System.out.println("FAIL");
                    	}
                    } catch (Exception e) {
                    	e.printStackTrace();
                    }
                } else if (command.equals("get")) {
                    System.out.print("Key: ");
                    String key = scanner.nextLine().trim();
                    
                    try {
                    	String res = client.get(key);
                    	if (res != null) {
                    		System.out.println("Value: " + res);
                    	} else {
                    		System.out.println("Not found");
                    	}
                    } catch (Exception e) {
                    	e.printStackTrace();
                    }
                } else if (command.equals("remove")) {
                    System.out.print("Key: ");
                    String key = scanner.nextLine().trim();
                    
                    try {
                    	int res = client.remove(key);
                    	if (res == 1) {
                    		System.out.println("SUCCESS");
                    	} else {
                    		System.out.println("FAIL");
                    	}
                    } catch (Exception e) {
                    	e.printStackTrace();
                    }                    
                } else if (command.equals("exit")) {
                    break;
                } else {
                    System.out.println("Wrong command!");
                }
            } catch (Exception e) {
            	e.printStackTrace();
            }
        }
        scanner.close();
	}
}
