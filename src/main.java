
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;

class Config {
    static final String CLIENT_DIR = "client";
    static final String SERVER_DIR = "server";
    static final int SERVER_PORT = 8000;
}

class FileWatcher {
    private final Path dir;
    private final Map<String, String> fileHashes = new HashMap<>();
    private final FileTransferHandler transferHandler;

    public FileWatcher(String directory, FileTransferHandler handler) {
        this.dir = Paths.get(directory);
        this.transferHandler = handler;
        loadInitialHashes();
    }

    private void loadInitialHashes() {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path file : stream) {
                if (Files.isRegularFile(file)) {
                    fileHashes.put(file.toString(), getFileChecksum(file));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void watch() {
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            dir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                         StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
            
            while (true) {
                WatchKey key = watchService.take();
                for (WatchEvent<?> event : key.pollEvents()) {
                    Path changedFile = dir.resolve((Path) event.context());
                    handleFileChange(event.kind(), changedFile);
                }
                key.reset();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleFileChange(WatchEvent.Kind<?> kind, Path file) {
        if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
            fileHashes.remove(file.toString());
        } else {
            try {
                String newHash = getFileChecksum(file);
                if (!newHash.equals(fileHashes.get(file.toString()))) {
                    fileHashes.put(file.toString(), newHash);
                    transferHandler.sendFile(file.toFile());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getFileChecksum(Path file) throws IOException {
        try (InputStream fis = Files.newInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] byteArray = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesRead);
            }
            return Base64.getEncoder().encodeToString(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}

class FileTransferHandler {
    private final String serverAddress;

    public FileTransferHandler(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public void sendFile(File file) {
        try (Socket socket = new Socket(serverAddress, Config.SERVER_PORT);
             FileInputStream fis = new FileInputStream(file);
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {
            
            dos.writeUTF(file.getName());
            dos.writeLong(file.length());
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class Client {
    public static void main(String[] args) {
        FileTransferHandler handler = new FileTransferHandler("localhost");
        FileWatcher watcher = new FileWatcher(Config.CLIENT_DIR, handler);
        watcher.watch();
    }
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
