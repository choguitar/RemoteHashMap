package client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class client {
	private String serverAddr;
	private int serverPort;
	
	private Socket socket;
	
	private OutputStream out;
	
	private void connect() {
		try {
			socket = new Socket(serverAddr, serverPort);
			out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));			
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
		
	}
	
	private void disconnect() {
		try {
			socket.close();
			out.close();
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(-1);			
		}
	}
	
	public void sendFile(String fn) {
		File file = new File(fn);
		if (!file.exists()) return;
		
		BufferedInputStream bis = null;
		try {
			((DataOutputStream)out).writeUTF(file.getName());
			bis = new BufferedInputStream(new FileInputStream(file));
			
			byte[] buffer = new byte[1024];
			int nRead;
			while ((nRead = bis.read(buffer)) != -1) {
				out.write(buffer, 0, nRead);
			}
		} catch (FileNotFoundException e) {
			//
			return;
		} catch (IOException e) {
			//
			return;
		} finally {
			try {
				if (bis != null) bis.close();
			} catch (IOException e) {
				//
				return;
			}
		}
	}
	
	public static void main(String[] args) {
		
		

	}

}