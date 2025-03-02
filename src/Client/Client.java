package Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {
	private static final int serverPort = 8000;
	private String serverAddr;
	
	public Client(String serverAddr) {
		this.serverAddr = serverAddr;
	}
	
	public String put(String key, String value, Long TTL) throws Exception {
		Socket socket = null;
		
		try {
			socket = new Socket(serverAddr, serverPort);
			
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			DataInputStream in = new DataInputStream(socket.getInputStream());
						
			out.writeUTF("PUT");
			out.writeUTF(key);
			out.writeUTF(value);
			out.writeLong(TTL);
			
			return in.readUTF();
		} catch (Exception e) {
			throw new Exception(e);
		} finally {
			try {
				if (socket != null) socket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public String get(String key) throws Exception {
		Socket socket = null;
		
		try {
			socket = new Socket(serverAddr, serverPort);
			
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			DataInputStream in = new DataInputStream(socket.getInputStream());
						
			out.writeUTF("GET");
			out.writeUTF(key);
			
			String res = in.readUTF();
			if (res.equals("SUCCESS")) {
				return in.readUTF();
			} else {
				return null;
			}			
		} catch (Exception e) {
			throw new Exception(e);
		} finally {
			try {
				if (socket != null) socket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public String remove(String key) throws Exception {
		Socket socket = null;
		
		try {
			socket = new Socket(serverAddr, serverPort);
			
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			DataInputStream in = new DataInputStream(socket.getInputStream());
						
			out.writeUTF("REMOVE");
			out.writeUTF(key);
			
			return in.readUTF();
		} catch (Exception e) {
			throw new Exception(e);
		} finally {
			try {
				if (socket != null) socket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public static void main(String[] args) {
		Client client = new Client("localhost");
		Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("명령을 입력하세요: put / get / remove / exit");
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
	                    System.out.println("Put Response: " + client.put(key, value, ttl));
                    } catch (Exception e) {
                    	e.printStackTrace();
                    }
                } else if (command.equals("get")) {
                    System.out.print("Key: ");
                    String key = scanner.nextLine().trim();
                    try {
	                    System.out.println("Get Response: " + client.get(key));
                    } catch (Exception e) {
                    	e.printStackTrace();
                    }
                } else if (command.equals("remove")) {
                    System.out.print("Key: ");
                    String key = scanner.nextLine().trim();
                    try {
                    	System.out.println("Remove Response: " + client.remove(key));
                    } catch (Exception e) {
                    	e.printStackTrace();
                    }                    
                } else if (command.equals("exit")) {
                    break;
                } else {
                    System.out.println("잘못된 명령입니다. put / get / remove / exit 중 하나를 입력하세요.");
                }
            } catch (Exception e) {
            	e.printStackTrace();
            }
        }
        scanner.close();
	}
}
