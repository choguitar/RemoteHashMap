package Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class Client {
	private String serverAddr;
	private final int serverPort = 8000;
	
	public Client(String serverAddr) {
		this.serverAddr = serverAddr;
	}
	
	public String put(String key, String value, long TTL) throws Exception {
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
						
			out.writeUTF("GET");
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
		
		
		
	}

}
