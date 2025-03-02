package Server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Server {
	private static final int serverPort = 8000;
	private static final String logFile = "data.log";
	private static ConcurrentHashMap<String, String> hashMap = new ConcurrentHashMap();
	private static ConcurrentHashMap<String, Long> TTLs = new ConcurrentHashMap();
	private static final long TTLInterval = 1000;
	
	private static synchronized void saveLogs() {	// hash map to log file
		File file = new File(logFile);
		
		FileOutputStream fos = null;
		BufferedWriter bw = null;
		
		try {			
			fos = new FileOutputStream(file);
			bw = new BufferedWriter(new OutputStreamWriter(fos));
				
			for (var e : hashMap.entrySet()) {
				bw.write(e.getKey() + ":" + e.getValue());
				bw.newLine();
			}
			bw.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (fos != null) fos.close();
				if (bw != null) bw.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private static synchronized void loadLogs() {	// log file to hash map
		File file = new File(logFile);
		if (!file.exists()) return;

		FileInputStream fis = null;
		BufferedReader br = null;
		
		try {
			fis = new FileInputStream(file);
			br = new BufferedReader(new InputStreamReader(fis));
				
			String line;
			while ((line = br.readLine()) != null) {
				String[] parts = line.split(":", 2);
				if (parts.length == 2) {
					hashMap.put(parts[0], parts[1]);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (fis != null) fis.close();
				if (br != null) br.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void TTLChecker() {	// TTL check
		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            long currentTime = System.currentTimeMillis();
            
            for (var entry : TTLs.entrySet()) {
                if (entry.getValue() <= currentTime) {
                    hashMap.remove(entry.getKey());
                    TTLs.remove(entry.getKey());
                    saveLogs();
                }
            }
        }, TTLInterval, TTLInterval, TimeUnit.MILLISECONDS);
	}
	
	private static class Handler extends Thread {
		private Socket socket;
		
		public Handler(Socket socket) {
			this.socket = socket;
		}
		
		@Override
		public void run() {
			DataInputStream dis = null;
			DataOutputStream dos = null;
			
			try {
				dis = new DataInputStream(socket.getInputStream());
				dos = new DataOutputStream(socket.getOutputStream());
				
				String operation = dis.readUTF();
				String key, value;
				long TTL;
				
				if (operation.equals("PUT")) {
					key = dis.readUTF();
					value = dis.readUTF();
					TTL = dis.readLong();
					
					hashMap.put(key, value);
					TTLs.put(key, TTL);					
					saveLogs();
					
					dos.writeUTF("SUCCESS");
				} else if (operation.equals("GET")) {
					key = dis.readUTF();
					value = hashMap.get(key);
					
					if (value != null) {
						dos.writeUTF("SUCCESS");
						dos.writeUTF(value);
					} else {
						dos.writeUTF("FAIL");
					}
				} else if (operation.equals("REMOVE")) {
					key = dis.readUTF();
					
					if (hashMap.remove(key) != null) {
						TTLs.remove(key);
						saveLogs();
						dos.writeUTF("SUCCESS");
					} else {
						dos.writeUTF("FAIL");
					}
				} else {
					dos.writeUTF("FAIL");
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (dis != null) dis.close();
					if (dos != null) dos.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}		
	}
	
	public static void main(String[] args) {
		loadLogs();
		TTLChecker();
		
		ServerSocket serverSocket = null;
		
		try {
			serverSocket = new ServerSocket(serverPort);
			
            System.out.println("Server started...");
			
            while (true) {
            	Socket socket = null;
            	
            	try {
            		socket = serverSocket.accept();
                    new Handler(socket).start();            		
            	} catch (Exception e) {
            		e.printStackTrace();
            	} finally {
            		try {
            			if (socket != null) socket.close();
            		} catch (Exception e) {
            			e.printStackTrace();
            		}
            	}
            }			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (serverSocket != null) serverSocket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
