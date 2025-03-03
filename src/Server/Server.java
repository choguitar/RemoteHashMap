package Server;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

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
	
	private static void TTLCheck() {	// TTL check
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
				
				int operation = dis.readInt();
				String key, value;
				long TTL;

				if (operation == 1) {	// put
					key = dis.readUTF();
					value = dis.readUTF();
					TTL = dis.readLong();
					
					hashMap.put(key, value);
					TTLs.put(key, TTL);					
					saveLogs();
					
					dos.writeInt(1);
				} else if (operation == 2) {	// get
					key = dis.readUTF();
					value = hashMap.get(key);
					
					if (value != null) {
						dos.writeInt(1);
						dos.writeUTF(value);
					} else {
						dos.writeInt(0);
					}
				} else if (operation == 3) {	// remove
					key = dis.readUTF();
					
					if (hashMap.remove(key) != null) {
						TTLs.remove(key);
						saveLogs();
						dos.writeInt(1);
					} else {
						dos.writeInt(0);
					}
				} else {
					dos.writeInt(0);
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
		TTLCheck();
		
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
