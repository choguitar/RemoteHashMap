package Server;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class Server {
    private static final int PORT = 8000;
    private static final String DATA_FILE = "data.txt";

    private final ConcurrentHashMap<String, VALUE> hashMap = new ConcurrentHashMap<String, VALUE>();
    private final ScheduledExecutorService cleaner = Executors.newScheduledThreadPool(1);
    private final ScheduledExecutorService logger = Executors.newScheduledThreadPool(1);

    public static void main(String[] args) {
        new Server().start();
    }

    public void start() {
    	loadLogs();
        
        cleaner.scheduleAtFixedRate(this::removeExpired, 1, 1, TimeUnit.SECONDS);
        logger.scheduleAtFixedRate(this::saveLogs, 10, 10, TimeUnit.SECONDS);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started...");
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        saveLogs();
    }

    private class ClientHandler implements Runnable {
        private final Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (DataInputStream dis = new DataInputStream(socket.getInputStream());
                 DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {
            	
                String operation = dis.readUTF();
                int keyLen, valueLen;
                byte[] keyBytes, valueBytes;
                String key, value;
                int ttl;
                
                if (operation.equals("PUT")) {
                	keyLen = dis.readInt();
                    keyBytes = new byte[keyLen];
                    dis.readFully(keyBytes);
                    key = new String(keyBytes);

                    valueLen = dis.readInt();
                    valueBytes = new byte[valueLen];
                    dis.readFully(valueBytes);
                    value = new String(valueBytes);
                    
                    ttl = dis.readInt();

                    dos.writeInt(put(key, value, ttl) ? 0 : -1);
                } else if (operation.equals("GET")) {
                    keyLen = dis.readInt();
                    keyBytes = new byte[keyLen];
                    dis.readFully(keyBytes);
                    key = new String(keyBytes);
                    
                    value = get(key);
                    if (value != null) {
                    	dos.writeInt(0);
                    	
                        byte[] valueByte = value.getBytes();
                        dos.writeInt(valueByte.length);
                        dos.write(valueByte);
                    } else {
                    	dos.writeInt(1);
                    }
                } else if (operation.equals("REMOVE")) {
                	keyLen = dis.readInt();
                    keyBytes = new byte[keyLen];
                    dis.readFully(keyBytes);
                    key = new String(keyBytes);
                    
                    dos.writeInt(remove(key) ? 0 : 1);
                } else {
                	dos.writeInt(-1);                	
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized boolean put(String key, String value, int ttl) {
    	if (hashMap.get(key) != null) return false;
    	if (ttl <= 0) return true;
    	
        long expire = System.currentTimeMillis() + (ttl * 1000);
        hashMap.put(key, new VALUE(value, expire));
        saveLogs();
        
        return true;
    }

    private synchronized String get(String key) {
    	VALUE entry = hashMap.get(key);
    	if (entry == null) return null;
    	
    	if (System.currentTimeMillis() > entry.ttl) {
        	hashMap.remove(key);
        	saveLogs();
            return null;
        }        
        return entry.value;
    }

    private synchronized boolean remove(String key) {
    	if (hashMap.remove(key) == null) return false;
    	saveLogs();
        return true;
    }

    private synchronized void removeExpired() {
        long current = System.currentTimeMillis();
        for (String key : hashMap.keySet()) {
        	VALUE entry = hashMap.get(key);
            if (entry != null && current > entry.ttl) {
            	hashMap.remove(key);
            }
        }
    }

    private synchronized void saveLogs() {
    	File file = new File(DATA_FILE);
    	
        try (FileOutputStream fos = new FileOutputStream(file);
        	 BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos))) {
            for (var e : hashMap.entrySet()) {
				bw.write(e.getKey() + ":" + e.getValue().value + ":" + e.getValue().ttl);
				bw.newLine();
			}
			bw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized void loadLogs() {
        File file = new File(DATA_FILE);
        if (!file.exists()) return;

        try (FileInputStream fis = new FileInputStream(file);
        	 BufferedReader br = new BufferedReader(new InputStreamReader(fis))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 3) {
                    String key = parts[0];
                    String value = parts[1];
                    long ttl = Long.parseLong(parts[2]);
                    hashMap.put(key, new VALUE(value, ttl));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class VALUE {
        String value;
        long ttl;

        VALUE(String value, long ttl) {
            this.value = value;
            this.ttl = ttl;
        }
    }
}